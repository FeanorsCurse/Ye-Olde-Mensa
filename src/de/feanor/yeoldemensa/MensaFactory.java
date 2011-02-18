/**
 *   Ye Olde Mensa is an android application for displaying the current
 *   mensa plans of University Oldenburg on an android mobile phone.
 *   
 *   Copyright (C) 2009/2010 Daniel Süpke
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
package de.feanor.yeoldemensa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.feanor.yeoldemensa.Mensa.Day;

/**
 * Factory Class for single access to Mensas (and their data). Provides a
 * high-level interface, retrieving data either from the sqlite database or from
 * the central server.
 * 
 * @author Daniel Süpke
 */
public class MensaFactory {

	/** Number of seconds to wait before server connection times out */
	public static final int TIMEOUT = 10;

	/** URL of the server providing the mensa data JSON stream */
	private static final String SERVER_URL = "http://suepke.eu:3000/";

	// Singleton pattern. We cannot do it all static (without any instance)
	// because of the inner class
	private static MensaFactory instance = new MensaFactory();

	// Singleton pattern
	private MensaFactory() {
	}

	/**
	 * Returns Mensa for the given ID. Will be fetched from internal sqlite db
	 * if still up-to-date, otherwise will be retrieved from central server.
	 * 
	 * @param id
	 *            ID of the mensa
	 * @param context
	 *            Required for the sqlite db
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
	public static Mensa getMensa(int id, Context context, boolean forceRefetch)
			throws SocketTimeoutException, IOException, JSONException,
			ParseException {
		return instance._getMensa(id, context, forceRefetch);
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
	 */
	public static Map<Integer, String> getMensaList(Context context)
			throws JSONException, IOException {
		return instance._getMensaList(context);
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
		} else {
			return "";
		}
	}

	/**
	 * @see #getMensa(int, Context, boolean)
	 */
	private Mensa _getMensa(int id, Context context, boolean forceRefetch)
			throws MalformedURLException, IOException, JSONException,
			ParseException {
		MensaSQLiteHelper sqlHelper = new MensaSQLiteHelper(context);
		Date date = new Date();

		// If we are up to date and don't need to fetch per user request, just
		// load from db and be done
		if (sqlHelper.isMensaUpToDate(id) && !forceRefetch) {
			Log.i("yom", "Using internal database for mensa data");
			Mensa mensa = sqlHelper.loadMensa(id);
			Log.i("yom", "Fetching the mensa took "
					+ (new Date().getTime() - date.getTime()) + "ms.");
			return mensa;
		}
		Log.i("yom", "Fetching mensa data from server");

		Mensa mensa = new Mensa(id);
		URLConnection conn = new URL(SERVER_URL + "mensas/" + id + ".json")
				.openConnection();

		JSONObject mensaJson = new JSONObject(convertStreamToString(conn
				.getInputStream())).getJSONObject("mensa");

		mensa.setName(mensaJson.getString("name"));
		mensa.setValidTo(new SimpleDateFormat("yyyy-MM-dd").parse(mensaJson
				.getString("validTo").substring(0, 10)));

		JSONArray mitemTypesJson = mensaJson.getJSONArray("menu_item_types");

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

		mensa.setLastActualised(new Date());
		sqlHelper.storeMensa(mensa);

		Log.i("yom", "Fetching, parsing and storing the mensa took "
				+ (new Date().getTime() - date.getTime()) + "ms.");
		return mensa;
	}

	// TODO: See below
	/**
	 * @see #getMensaList(Context)
	 */
	private Map<Integer, String> _getMensaList(Context context)
			throws JSONException, IOException {
		Map<Integer, String> mensas = new HashMap<Integer, String>();

		Date date = new Date();

		URLConnection conn = new URL(SERVER_URL + "/mensas.json")
				.openConnection();

		String json = convertStreamToString(conn.getInputStream());

		JSONArray mensasJson = new JSONArray(json);

		for (int i = 0; i < mensasJson.length(); i++) {
			JSONObject mensaJSON = mensasJson.getJSONObject(i).getJSONObject(
					"mensa");

			mensas.put(mensaJSON.getInt("id"), mensaJSON.getString("name"));
		}

		Log.i("yom", "Fetching mensa list from server took "
				+ (new Date().getTime() - date.getTime()) + "ms.");
		return mensas;
	}

	/**
	 * Helper for the internal sqlite db of the app. Provides fast access for
	 * storing and retrieving Mensas.
	 * 
	 * @author Daniel Süpke
	 */
	private class MensaSQLiteHelper extends SQLiteOpenHelper {

		/**
		 * Used in Android. If higher than stored version, onUpgrade will be
		 * called by android
		 */
		private static final int DATABASE_VERSION = 11;

		private static final String DATABASE_NAME = "yeoldemensa";

		/**
		 * Constructor
		 * 
		 * @param context
		 *            Required for db access in android
		 */
		MensaSQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO: Server and client should have same table col names.
			db
					.execSQL("CREATE TABLE mensen (id INTEGER PRIMARY KEY, name TEXT, validTo TIMESTAMP, actualised TIMESTAMP);");
			db
					.execSQL("CREATE TABLE mtypes (id INTEGER, mensaID INTEGER, name TEXT);");
			db
					.execSQL("CREATE TABLE mitems (mtypeID INTEGER, mensaID INTEGER, day INTEGER, name TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE mensen");
			db.execSQL("DROP TABLE mtypes");
			db.execSQL("DROP TABLE mitems");
			onCreate(db);
		}

		/**
		 * Stores the given Mensa in the db.
		 * 
		 * @apram mensa Mensa to store
		 */
		public void storeMensa(Mensa mensa) {
			// TODO: lastActualised
			Date date = new Date();
			SQLiteDatabase db = getWritableDatabase();
			int i = 0;

			db.beginTransaction();
			db.execSQL("DELETE FROM mensen WHERE id=" + mensa.getID());
			db.execSQL("DELETE FROM mtypes WHERE mensaID=" + mensa.getID());
			db.execSQL("DELETE FROM mitems WHERE mensaID=" + mensa.getID());
			db.execSQL("INSERT INTO mensen VALUES (" + mensa.getID() + ", \""
					+ mensa.getName() + "\"," + mensa.getValidTo().getTime()
					+ "," + mensa.getLastActualised().getTime() + ")");

			for (String type : mensa.getMenuForDay(Day.MONDAY).keySet()) {
				db.execSQL("INSERT INTO mtypes VALUES (" + i + ", "
						+ mensa.getID() + ", \"" + type + "\")");

				for (Day day : Day.values()) {
					if (mensa.getMenuforDayType(day, type) != null) {
						for (String item : mensa.getMenuforDayType(day, type)) {
							db
									.execSQL("INSERT INTO mitems (name, day, mtypeID, mensaID) VALUES (\""
											+ item
											+ "\", "
											+ day.ordinal()
											+ ", "
											+ i
											+ ", "
											+ mensa.getID()
											+ ")");
						}
					}
				}

				i++;
			}

			db.setTransactionSuccessful();
			db.endTransaction();

			isMensaUpToDate(mensa.getID());
		}

		/**
		 * Loads a Mensa from the db.
		 * 
		 * @param id
		 *            ID of the Mensa to load
		 * @return Mensa
		 */
		public Mensa loadMensa(int id) {
			Mensa mensa = new Mensa(id);
			SQLiteDatabase db = getReadableDatabase();
			Map<Integer, String> types = new HashMap<Integer, String>();

			Cursor cursor = db.rawQuery("SELECT * FROM mensen WHERE id="
					+ mensa.getID(), null);

			cursor.moveToFirst();
			mensa.setName(cursor.getString(1));
			mensa.setValidTo(new Date(cursor.getLong(2)));
			mensa.setLastActualised(new Date(cursor.getLong(3)));
			cursor.close();

			cursor = db.rawQuery("SELECT id, name FROM mtypes WHERE mensaID="
					+ mensa.getID(), null);

			while (cursor.moveToNext()) {
				types.put(cursor.getInt(0), cursor.getString(1));
			}
			cursor.close();

			cursor = db.rawQuery("SELECT * FROM mitems WHERE mensaID="
					+ mensa.getID(), null);

			while (cursor.moveToNext()) {
				mensa.addMenuItem(new MenuItem(Day.values()[cursor.getInt(2)],
						types.get(cursor.getInt(0)), cursor.getString(3)));
			}
			cursor.close();

			return mensa;
		}

		/**
		 * Retrieves the date until the Mensa for the given id is valid from the
		 * db. If this date is after now, returns true, otherwise false.
		 * 
		 * @return True, if Mensa data is still up-to-date
		 */
		public boolean isMensaUpToDate(int id) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT * FROM mensen WHERE id=" + id,
					null);

			boolean upToDate = false;

			if (cursor.moveToFirst()) {
				upToDate = new Date(cursor.getLong(2)).after(new Date());
			}
			cursor.close();

			return upToDate;
		}

		/**
		 * Returns all available Mensa names and their IDs from the db
		 * 
		 * @return Map in the format (ID, mensaName)
		 */
		public Map<Integer, String> getMensaList() {
			Map<Integer, String> mensas = new HashMap<Integer, String>();
			SQLiteDatabase db = getReadableDatabase();

			Cursor cursor = db.rawQuery("SELECT * FROM mensen", null);

			while (cursor.moveToNext()) {
				mensas.put(cursor.getInt(0), cursor.getString(1));
			}
			cursor.close();

			return mensas;
		}
	}
}
