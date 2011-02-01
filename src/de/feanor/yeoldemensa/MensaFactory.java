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

import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.feanor.yeoldemensa.Mensa.Day;

/**
 * @author Daniel Süpke
 */
public class MensaFactory {

	// Singleton pattern
	private static MensaFactory instance;

	// Singleton pattern
	private MensaFactory() {
	}

	/**
	 * @param id
	 * @param context
	 * @param forceRefetch
	 * @return
	 */
	public static Mensa getMensa(int id, Context context, boolean forceRefetch) throws SocketTimeoutException {
		if (instance == null) {
			instance = new MensaFactory();
		}

		return instance._getMensa(id, context, forceRefetch);
	}

	/**
	 * @param context
	 * @return
	 */
	public static Map<Integer, String> getMensaList(Context context) {
		if (instance == null) {
			instance = new MensaFactory();
		}

		return instance._getMensaList(context);
	}

	/**
	 * @param id
	 * @param context
	 * @param forceRefetch
	 * @return
	 */
	private Mensa _getMensa(int id, Context context, boolean forceRefetch) throws SocketTimeoutException {
		MensaSQLiteHelper sqlHelper = new MensaSQLiteHelper(context);

		// If we are up to date and don't need to fetch per user request, just
		// load from db and be done
		if (sqlHelper.isMensaUpToDate(id) && !forceRefetch) {
			return sqlHelper.loadMensa(id);
		}

		Mensa mensa = new Mensa(id);
		// TODO: Do JSON

		mensa.setLastActualised(new Date());
		sqlHelper.storeMensa(mensa);

		return mensa;
	}

	/**
	 * @param context
	 * @return
	 */
	private Map<Integer, String> _getMensaList(Context context) {
		return new MensaSQLiteHelper(context).getMensaList();
	}

	/**
	 * @author suepke
	 * 
	 */
	private class MensaSQLiteHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 9;
		private static final String DATABASE_NAME = "yeoldemensa";

		/**
		 * @param mensa
		 * @param context
		 */
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

		/**
		 * 
		 */
		public void storeMensa(Mensa mensa) {
			SQLiteDatabase db = getWritableDatabase();
			int i = 0;

			db.execSQL("INSERT OR REPLACE INTO mensen VALUES (" + mensa.getID()
					+ "," + mensa.getValidTo().getTime() + ","
					+ mensa.getLastActualised().getTime() + ")");
			db.execSQL("DELETE FROM mtypes WHERE mensaID=" + mensa.getID());
			db.execSQL("DELETE FROM mitems WHERE mensaID=" + mensa.getID());

			for (String type : mensa.getMenuForDay(Day.MONDAY).keySet()) {
				db.execSQL("INSERT INTO mtypes VALUES (" + i + ", "
						+ mensa.getID() + ", \"" + type + "\")");

				for (Day day : Day.values()) {
					if (mensa.getMenuforDayType(day, type) != null) {
						for (String item : mensa.getMenuforDayType(day, type)) {
							ContentValues values = new ContentValues();
							values.put("mtypeID", i);
							values.put("mensaID", mensa.getID());
							values.put("day", day.ordinal());
							values.put("name", item);
							db.insert("mitems", null, values);
						}
					}
				}

				i++;
			}
		}

		/**
		 * 
		 */
		public Mensa loadMensa(int id) {
			Mensa mensa = new Mensa(id);
			SQLiteDatabase db = getReadableDatabase();
			Map<Integer, String> types = new HashMap<Integer, String>();

			Cursor cursor = db.rawQuery("SELECT * FROM mensen WHERE id="
					+ mensa.getID(), null);

			cursor.moveToFirst();
			mensa.setValidTo(new Date(cursor.getLong(1)));
			mensa.setLastActualised(new Date(cursor.getLong(2)));
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
		 * @return
		 */
		public boolean isMensaUpToDate(int id) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT * FROM mensen WHERE id="
					+ id, null);

			if (cursor.moveToFirst())
				return new Date(cursor.getLong(1)).after(new Date());

			return false;
		}

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
