/**
 *   Ye Olde Mensa is an android application for displaying the current
 *   mensa plans of University Oldenburg on an android mobile phone.
 *   
 *   Copyright (C) 2009/2010 Daniel SÃ¼pke
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.feanor.yeoldemensa.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import de.feanor.yeoldemensa.data.Mensa.Day;
import de.feanor.yeoldemensa.YeOldeMensa;

/**
 * Factory Class for single access to Mensas (and their data). Provides a
 * high-level interface, retrieving data either from the sqlite database or from
 * the central server.
 * 
 * @author Daniel SŸpke
 */
public class MensaFactory {

	/** Number of seconds to wait before server connection times out */
	public static final int TIMEOUT = 10;

	/** URL of the server providing the mensa data JSON stream */
	private static final String SERVER_URL = "http://suepke.eu:3000/";

	/**
	 * Only provides static methods
	 */
	private MensaFactory() {
		// Singleton pattern
	}

	/**
	 * Returns the application context required for the
	 * {@link MensaSQLiteHelper}. I sbound to YeOldeMensa, but can be easily
	 * extended or changed if necessary.
	 * 
	 * @return context
	 */
	private static Context getContext() {
		return YeOldeMensa.context;
	}

	/**
	 * Checks whether the mensa in the database is up to date or should be
	 * refreshed from the server
	 * 
	 * @param mensaID
	 *            id of the mensa to look for
	 * @return Up to date
	 */
	public static boolean isUpToDate(int mensaID) {
		return new MensaSQLiteHelper(getContext()).isMensaUpToDate(mensaID);
	}

	/**
	 * Returns Mensa for the given ID. Will be fetched from internal sqlite db
	 * if still up-to-date, otherwise will be retrieved from central server.
	 * 
	 * @param id
	 *            ID of the mensa
	 * @param forceRefetch
	 *            If true, will refetch from server even if still valid
	 * @return Mensa for the given id
	 * @throws SocketTimeoutException
	 *             Thrown if server connection times out. See
	 *             MensaFactory.TIMEOUT
	 * @throws IOException
	 *             Thrown if there is a problem with the server connection
	 *             besides TIMEOUT
	 * @throws JSONException
	 *             Thrown if there is a problem with the JSON stream
	 * @throws ParseException
	 *             Thrown if there is a problem with the JSON stream
	 */
	public static Mensa getMensa(int id, boolean forceRefetch)
			throws SocketTimeoutException, IOException, JSONException,
			ParseException {
		MensaSQLiteHelper sqlHelper = new MensaSQLiteHelper(getContext());
		Date date = new Date();

		// If we are up to date and don't need to fetch per user request, just
		// load from db and be done
		if (sqlHelper.isMensaUpToDate(id) && !forceRefetch) {
			Log.d("yom", "Using internal database for mensa data");
			Mensa mensa = sqlHelper.loadMensa(id);
			Log.d("yom", "Fetching the mensa took "
					+ (new Date().getTime() - date.getTime()) + "ms.");
			return mensa;
		}

		// Otherwise fetch from the server
		Log.i("yom", "Fetching mensa data from server (forceRefetch="
				+ forceRefetch + ")");

		Mensa mensa = new Mensa(id);

		JSONObject mensaJSON = new JSONObject(
				convertStreamToString(getInputStream("mensas/" + id + ".json")))
				.getJSONObject("mensa");

		mensa.setName(mensaJSON.getString("name"));
		mensa.setValidTo(new SimpleDateFormat("yyyy-MM-dd").parse(mensaJSON
				.getString("validTo").substring(0, 10)));

		JSONArray mitemTypesJson = mensaJSON.getJSONArray("menu_item_types");

		for (int i = 0; i < mitemTypesJson.length(); i++) {
			JSONObject mitemTypeJson = mitemTypesJson.getJSONObject(i);
			String type = mitemTypeJson.getString("name");
			JSONArray mitemsJson = mitemTypeJson.getJSONArray("menu_items");

			for (int j = 0; j < mitemsJson.length(); j++) {
				JSONObject mitemJson = mitemsJson.getJSONObject(j);
				String item = mitemJson.getString("name");
				Day day = Day.values()[mitemJson.getInt("day")];
				mensa.addMenuItem(new MenuItem(day, type, item));
			}
		}

		// Set dates and store in db
		mensa.setLastActualised(new Date());
		mensa.setValidTo(getNextSaturday());
		sqlHelper.storeMensa(mensa);

		// Also, update list of Mensas while we're at it!
		// TODO: Do this in background
		updateMensaList();

		Log.i("yom", "Fetching, parsing and storing the mensa took "
				+ (new Date().getTime() - date.getTime()) + "ms.");
		return mensa;
	}

	// TODO: Don't do refetch from server here, is way to slow! Instead, refetch
	// the list when getting mensa data or none available
	// TODO: See below
	/**
	 * WARNING: THIS WORKS ONLY WHEN THE MENSAS ARE RETURNED FROM THE SERVERIN
	 * ASCENDING ID, STARTING BY 1!
	 * 
	 * Returns all available Mensa names and their IDs
	 * 
	 * @param context
	 *            Required for the sqlite db
	 * @return Map in the format (ID, mensaName)
	 * @throws IOException
	 * @throws JSONException
	 * @throws ParseException
	 */
	public static Map<Integer, String> getMensaList() throws JSONException,
			IOException, ParseException {
		MensaSQLiteHelper sqlHelper = new MensaSQLiteHelper(getContext());
		Map<Integer, String> mensas = sqlHelper.getMensaList();

		// We always use the mensa list in the database. Only if it is empty
		// (should only happen on first run of the app) refresh it from the web
		// server
		if (mensas.isEmpty()) {
			updateMensaList();
			mensas = sqlHelper.getMensaList();
		}

		return mensas;
	}

	// TODO: Rewrite
	private static String convertStreamToString(InputStream is)
			throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		}
		return "";
	}

	/**
	 * Will update the list of available mensas. Do not call after fetching the
	 * content of a mensa, will reset the actualisation date!
	 * 
	 * @throws IOException
	 * @throws JSONException
	 * @throws ParseException
	 * @throws NoConnectionException
	 */
	private static void updateMensaList() throws IOException, JSONException,
			ParseException {
		Date date = new Date(); // Used for measuring the connection speed
		MensaSQLiteHelper sqlHelper = new MensaSQLiteHelper(getContext());
		List<Mensa> mensas = new ArrayList<Mensa>();

		String json = convertStreamToString(getInputStream("mensas.json"));

		JSONArray mensasJSON = new JSONArray(json);

		for (int i = 0; i < mensasJSON.length(); i++) {
			JSONObject mensaJSON = mensasJSON.getJSONObject(i).getJSONObject(
					"mensa");
			Mensa mensa = new Mensa(mensaJSON.getInt("id"));
			mensa.setName(mensaJSON.getString("name"));
			mensa.setValidTo(new SimpleDateFormat("yyyy-MM-dd").parse(mensaJSON
					.getString("validTo").substring(0, 10)));

			// If the mensa's content is up to date, set the last actualized
			// date so not to refetch it's data
			if (sqlHelper.isMensaUpToDate(mensa.getID())) {
				mensa.setLastActualised(sqlHelper.loadMensa(mensa.getID())
						.getLastActualised());
			}

			mensas.add(mensa);
		}

		// Should not happen...
		if (mensas.isEmpty()) {
			throw new RuntimeException("Keine Mensa vom Server erhalten!");
		}

		sqlHelper.setMensaList(mensas);

		Log.i("yom",
				"Fetching mensa list from server took "
						+ (new Date().getTime() - date.getTime()) + "ms.");
	}

	/**
	 * Returns an input stream for the given path on the server defined by
	 * MensaFactory.SERVER_URL. Checks for a valid internet connection before
	 * conneting.
	 * 
	 * @param path
	 *            Path on the server/URL
	 * @param context
	 *            Application context, required for connection check
	 * @return Inputstream for the given path
	 * @throws IOException
	 *             Thrown if problems with the connection occur
	 */
	private static InputStream getInputStream(String path) throws IOException {
		URLConnection conn = new URL(SERVER_URL + path).openConnection();
		conn.setConnectTimeout(TIMEOUT * 1000);
		conn.setReadTimeout(TIMEOUT * 1000);

		return conn.getInputStream();
	}

	/**
	 * Returns the Date for next Saturday midnight. Used for valiTo in Mensa.
	 * 
	 * @return Date of next Saturday
	 */
	private static Date getNextSaturday() {
		Calendar calendar = Calendar.getInstance();
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);

		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR, 0);

		// TODO: Better solved with locales probably
		if (weekday == Calendar.SATURDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, 7);
			return calendar.getTime();
		} else if (weekday == Calendar.SUNDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, 6);
			return calendar.getTime();
		} else {
			calendar.add(Calendar.DAY_OF_MONTH, Calendar.SATURDAY - weekday);
		}

		return calendar.getTime();
	}
}
