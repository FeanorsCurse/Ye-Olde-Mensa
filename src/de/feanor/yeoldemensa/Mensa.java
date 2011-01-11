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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Daniel Süpke
 * 
 */
public abstract class Mensa {

	public enum Day {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
	}

	// Map<Day, Map<type, List<menuItem>>>
	// So, it's a map of weekdays with the menu-map, consiting of the menu types
	// and menu items
	private Map<Day, Map<String, List<String>>> menu = new LinkedHashMap<Day, Map<String, List<String>>>();

	private Date validTo = new GregorianCalendar(1970, 1, 1).getTime();
	private Date lastActualised;

	private MensaSQLiteHelper sqlHelper;

	/**
	 * TODO: I don't like that with the context. However it is required for the
	 * sqlHelper. Don't see any other way round now, Suggestions?
	 * 
	 * @param context
	 */
	public Mensa(Context context) {
		sqlHelper = new MensaSQLiteHelper(context);
	}

	/**
	 * Adds a menu item to a the food items. Example: addMenuItem("Ausgabe A",
	 * "Spaghetti Napoli"); Usually used within loadMenu(); Duplicate items will
	 * not be added.
	 * 
	 * @param type
	 *            Type of food, will be used as header in the view
	 * @param menuItem
	 *            Menu item (food) to add
	 */
	public void addMenuItem(MenuItem menuItem) {
		Map<String, List<String>> dayMenu = menu.get(menuItem.day);

		if (dayMenu == null) {
			dayMenu = new LinkedHashMap<String, List<String>>();
			menu.put(menuItem.day, dayMenu);
		}

		List<String> menuList = dayMenu.get(menuItem.type);

		if (menuList == null) {
			menuList = new ArrayList<String>();
			dayMenu.put(menuItem.type, menuList);
		}

		// Avoid double items
		if (!menuList.contains(menuItem.item)) {
			menuList.add(menuItem.item);
		}
	}

	public Map<String, List<String>> getMenuForDay(Day day) {
		// Check if data is valid
		if (validTo.before(new Date())) {
			throw new RuntimeException("Warning: Used outdated Mensa data!");
		}

		return menu.get(day);
	}

	public List<String> getMenuforDayType(Day day, String type) {
		// Check if data is valid
		if (validTo.before(new Date())) {
			throw new RuntimeException("Warning: Used outdated Mensa data!");
		}

		return menu.get(day).get(type);
	}

	public void loadMenu(boolean forceRefetch) throws IOException {
		// Set up hashmaps for each week day
		menu.clear();
		for (Day day : Day.values())
			menu.put(day, new LinkedHashMap<String, List<String>>());

		// If we are up to date and don't need to fetch per user requst, just
		// load from db and be done
		if (sqlHelper.isMensaUpToDate(this.getID()) && !forceRefetch) {
			Log.d("yom", "Fetching Mensa " + this.getName() + " from database");
			this.sqlHelper.loadMensa();
			return;
		}
		Log.d("yom", "Fetching Mensa " + this.getName() + " from web site");

		fetchMenu();
		Calendar cal = Calendar.getInstance();
		int currentDoW = cal.get(Calendar.DAY_OF_WEEK);

		// Set validity to next Friday
		if ((currentDoW >= Calendar.MONDAY)
				&& (currentDoW <= Calendar.THURSDAY)) {
			cal.add(Calendar.DAY_OF_MONTH, Calendar.FRIDAY - currentDoW);
		} else {
			// Since the calendar starts with Sunday, we need to add 12 days
			if (currentDoW == Calendar.SUNDAY) {
				cal.add(Calendar.DAY_OF_MONTH, 12);
			}
			// Otherwise just add 6 days
			if (currentDoW == Calendar.SATURDAY) {
				cal.add(Calendar.DAY_OF_MONTH, 6);
			}
		}
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);

		this.validTo = cal.getTime();
		this.lastActualised = new Date();

		this.sqlHelper.storeMensa();
	}

	/**
	 * Return true if there is no menu data for the given day.
	 * 
	 * @param day
	 *            Day to check
	 * @return True if no menu
	 */
	public boolean isEmpty(Day day) {
		return menu.get(day).isEmpty();
	}

	/**
	 * @return
	 */
	public Date lastActualised() {
		return this.lastActualised;
	}

	/**
	 * Return true, if it's currently weekend, so the next week's plans should
	 * be fetched instead of the current week.
	 * 
	 * @return True, if next week should be fetched
	 */
	protected boolean getNextWeek() {
		int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
	}

	/**
	 * Load the menu, usually by parsing a web site. Use addMenuItem(String
	 * type, String menuItem) to add items.
	 * 
	 * Use getNextWeek() to test if the current or next week's plans should be
	 * fetched.
	 * 
	 * @see Mensa.addMenuItem(String type, String menuItem)
	 * @throws IOException
	 */
	protected abstract void fetchMenu() throws IOException;

	/***
	 * @return coordinates of the Mensa
	 */
	public abstract double[] getCoordinates() throws Exception;

	/**
	 * Return the name of the Mensa, e.g. "Mensa Oldenburg"
	 * 
	 * @return Name of the Mensa
	 */
	public abstract String getName();

	public Date getActualisedOn() {
		return this.lastActualised;
	}

	protected abstract int getID();

	/**
	 * Return the name of the town where the Mensa is in. Used to build groups
	 * of Mensas in the same town.
	 * 
	 * @return Name of the Mensa's town
	 */
	// TODO
	// public abstract String getTown();

	private class MensaSQLiteHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 9;
		private static final String DATABASE_NAME = "yeoldemensa";

		MensaSQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db
					.execSQL("CREATE TABLE mensen (id INTEGER PRIMARY KEY, validTo TIMESTAMP, actualised TIMESTAMP);");
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

		public void storeMensa() {
			SQLiteDatabase db = getWritableDatabase();
			int i = 0;

			db.execSQL("INSERT OR REPLACE INTO mensen VALUES (" + getID() + ","
					+ validTo.getTime() + "," + lastActualised.getTime() + ")");
			db.execSQL("DELETE FROM mtypes WHERE mensaID=" + getID());

			for (String type : getMenuForDay(Day.MONDAY).keySet()) {
				db.execSQL("INSERT INTO mtypes VALUES (" + i + ", " + getID()
						+ ", \"" + type + "\")");

				for (Day day : Day.values()) {
					for (String item : getMenuforDayType(day, type)) {
						ContentValues values = new ContentValues();
						values.put("mtypeID", i);
						values.put("mensaID", getID());
						values.put("day", day.ordinal());
						values.put("name", item);
						db.insert("mitems", null, values);
					}
				}

				i++;
			}
		}

		public void loadMensa() {
			SQLiteDatabase db = getReadableDatabase();
			Map<Integer, String> types = new HashMap<Integer, String>();

			Cursor cursor = db.rawQuery("SELECT * FROM mensen WHERE id="
					+ getID(), null);

			cursor.moveToFirst();
			validTo = new Date(cursor.getLong(1));
			lastActualised = new Date(cursor.getLong(2));
			cursor.close();

			cursor = db.rawQuery("SELECT id, name FROM mtypes WHERE mensaID="
					+ getID(), null);

			while (cursor.moveToNext()) {
				types.put(cursor.getInt(0), cursor.getString(1));
				Log.d("yom", "mtype: " + cursor.getString(1));
			}
			cursor.close();

			cursor = db.rawQuery("SELECT * FROM mitems WHERE mensaID="
					+ getID(), null);
			while (cursor.moveToNext()) {
				addMenuItem(new MenuItem(Day.values()[cursor.getInt(2)], types
						.get(cursor.getInt(0)), cursor.getString(3)));
			}
			cursor.close();
		}

		public boolean isMensaUpToDate(int mensaID) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT * FROM mensen WHERE id="
					+ getID(), null);

			if (cursor.moveToFirst())
				return new Date(cursor.getLong(1)).after(new Date());

			return false;
		}
	}
}
