package de.feanor.yeoldemensa.data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.feanor.yeoldemensa.data.Mensa.Day;

/**
 * Helper for the internal sqlite db of the app. Provides fast access for
 * storing and retrieving Mensas.
 * 
 * @author Daniel SŸpke
 */
class MensaSQLiteHelper extends SQLiteOpenHelper {
	// TODO: db.close() necessary? Look up!

	/**
	 * Used in Android. If higher than stored version, onUpgrade will be called
	 * by android
	 */
	private static final int DATABASE_VERSION = 13;

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
		db.execSQL("CREATE TABLE mensen (id INTEGER PRIMARY KEY, name TEXT, validTo TIMESTAMP, actualised TIMESTAMP);");
		db.execSQL("CREATE TABLE mtypes (id INTEGER, mensaID INTEGER, name TEXT);");
		db.execSQL("CREATE TABLE mitems (mtypeID INTEGER, mensaID INTEGER, day INTEGER, name TEXT);");
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
		SQLiteDatabase db = getWritableDatabase();
		int i = 0;
		Long lastActualised = mensa.getLastActualised() != null ? mensa
				.getLastActualised().getTime() : null;
		Map<String, Integer> types = new HashMap<String, Integer>();

		db.beginTransaction();
		db.execSQL("DELETE FROM mensen WHERE id=" + mensa.getID());
		db.execSQL("DELETE FROM mtypes WHERE mensaID=" + mensa.getID());
		db.execSQL("DELETE FROM mitems WHERE mensaID=" + mensa.getID());

		db.execSQL("INSERT INTO mensen VALUES (" + mensa.getID() + ", \""
				+ mensa.getName() + "\"," + mensa.getValidTo().getTime() + ","
				+ lastActualised + ")");
		Log.d("yom", "INSERT INTO mensen VALUES (" + mensa.getID() + ", \""
				+ mensa.getName() + "\"," + mensa.getValidTo().getTime() + ","
				+ lastActualised + ")");

		for (Day day : Day.values()) {
			for (String type : mensa.getMenuForDay(day).keySet()) {
				Integer typeID = types.get(type);
				if (typeID == null) {
					typeID = i++;
					Log.d("yom", "INSERT INTO mtypes VALUES (" + typeID + ", "
							+ mensa.getID() + ", \"" + type + "\")");
					db.execSQL("INSERT INTO mtypes VALUES (" + typeID + ", "
							+ mensa.getID() + ", \"" + type + "\")");
				}

				if (mensa.getMenuforDayType(day, type) != null) {
					Log.d("yom", "mensa " + mensa.getName() + " with Menu: "
							+ mensa.getMenuforDayType(day, type));
					for (String item : mensa.getMenuforDayType(day, type)) {
						Log.d("yom",
								"Inserting "
										+ (item.length() > 10 ? item.substring(
												0, 10) + "..." : item)
										+ " into " + type + " at " + day
										+ " for " + mensa.getName());
						db.execSQL("INSERT INTO mitems (name, day, mtypeID, mensaID) VALUES (\""
								+ item
								+ "\", "
								+ day.ordinal()
								+ ", "
								+ typeID
								+ ", " + mensa.getID() + ")");
					}
				}
			}

			i++;
		}

		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	/**
	 * Loads a Mensa from the db. Returns null if the mensa has not been found.
	 * 
	 * @param id
	 *            ID of the Mensa to load
	 * @return Mensa
	 */
	public Mensa loadMensa(int id) {
		Mensa mensa = new Mensa(id);
		SQLiteDatabase db = getReadableDatabase();
		Map<Integer, String> types = new HashMap<Integer, String>();

		Cursor cursor = db.rawQuery(
				"SELECT * FROM mensen WHERE id=" + mensa.getID(), null);

		if (cursor.getCount() == 0) {
			return null;
		}

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

		cursor = db.rawQuery(
				"SELECT * FROM mitems WHERE mensaID=" + mensa.getID(), null);

		while (cursor.moveToNext()) {
			mensa.addMenuItem(new MenuItem(Day.values()[cursor.getInt(2)],
					types.get(cursor.getInt(0)), cursor.getString(3)));
		}
		cursor.close();
		db.close();

		return mensa;
	}

	/**
	 * Retrieves the date until the Mensa for the given id is valid from the db.
	 * If this date is after now, returns true, otherwise false. If the mensa
	 * has not been found, also returns false.
	 * 
	 * @return True, if Mensa data is still up-to-date
	 */
	public boolean isMensaUpToDate(int id) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db
				.rawQuery("SELECT * FROM mensen WHERE id=" + id, null);

		boolean upToDate = false;

		if (cursor.moveToFirst()) {
			upToDate = new Date(cursor.getLong(2)).after(new Date());
		}
		cursor.close();
		db.close();

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
		db.close();

		return mensas;
	}

	/**
	 * Stores all mensa in the db. Warning: Do NOT call after store mensa for a
	 * specific Mensa, as it will overwrite actualizedAt!
	 * 
	 * @param mensas
	 *            Map of mensas in format (ID, name)
	 */
	public void setMensaList(List<Mensa> mensas) {
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();
		db.execSQL("DELETE FROM mensen");

		for (Mensa mensa : mensas) {
			Long lastActualised = mensa.getLastActualised() != null ? mensa
					.getLastActualised().getTime() : null;
			db.execSQL("INSERT INTO mensen VALUES (" + mensa.getID() + ", \""
					+ mensa.getName() + "\"," + mensa.getValidTo().getTime()
					+ "," + lastActualised + ")");
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
}
