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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;
import de.feanor.yeoldemensa.Mensa.Day;
import de.feanor.yeoldemensa.mensen.MensaMagdbCampus;
import de.feanor.yeoldemensa.mensen.MensaMagdbHerren;
import de.feanor.yeoldemensa.mensen.MensaOldbUhlhornsweg;
import de.feanor.yeoldemensa.mensen.MensaOldbWechloy;
import de.feanor.yeoldemensa.mensen.MensaStendal;
import de.feanor.yeoldemensa.mensen.MensaWerninger;

/**
 * Main class of the application.
 * 
 * @author Daniel Süpke
 */
public class YeOldeMensa extends Activity {

	public static final String VERSION = "0.9";
	// suepke: Keeps crashing my phone
	// public SimpleGSMHelper gsm = new SimpleGSMHelper();

	// ADD YOUR MENSA HERE, THE REST IS DONE THROUGH MAGIC
	private Mensa[] mensa = { new MensaOldbUhlhornsweg(this),
			new MensaOldbWechloy(this), new MensaMagdbCampus(this),
			new MensaMagdbHerren(this), new MensaWerninger(this),
			new MensaStendal(this) };

	// Use this for testing
	// private Mensa[] mensa = { new MensaTest() };

	// currently selected mensa index
	private int selectedMensa;

	// One for each day of the week
	private MenuDayView[] menuDayView = new MenuDayView[5];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("yom", "### Starting application ###");
		setContentView(R.layout.main);

		// Retrieve selected mensa
		SharedPreferences settings = getSharedPreferences("yom_prefs", 0);
		selectedMensa = settings.getInt("selected mensa", 0);

		// Load current Mensa
		loadMenu(false);

		// setCurrentCoordinates;
		// TelephonyManager tm = (TelephonyManager)
		// getSystemService(Context.TELEPHONY_SERVICE);
		// gsm.setMobileLocation(tm);

		// Set up tabs
		TabHost host = (TabHost) findViewById(R.id.tabhost);
		host.setup();

		for (int i = 0; i < 5; i++) {
			menuDayView[i] = new MenuDayView(this, Day.values()[i]);
		}

		host.addTab(createTab(host, "Montag", menuDayView[0]));
		host.addTab(createTab(host, "Dienstag", menuDayView[1]));
		host.addTab(createTab(host, "Mittwoch", menuDayView[2]));
		host.addTab(createTab(host, "Donnerstag", menuDayView[3]));
		host.addTab(createTab(host, "Freitag", menuDayView[4]));

		// Select tab for current day or Monday on weekends
		int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		if (currentDay >= Calendar.MONDAY && currentDay <= Calendar.FRIDAY)
			host.setCurrentTab(currentDay - 2);
		else
			host.setCurrentTab(Calendar.MONDAY - 2);
	}

	private TabSpec createTab(TabHost host, String title,
			final MenuDayView menuDayView2) {
		View view = getLayoutInflater().inflate(R.layout.tabs_bg, null);
		TextView textView = (TextView) view.findViewById(R.id.tabsText);
		textView.setText(title);

		return host.newTabSpec(title).setIndicator(view)
				.setContent(new TabHost.TabContentFactory() {

					public View createTabContent(String tag) {
						return menuDayView2;
					}
				});
	}

	/**
	 * @return
	 */
	public Mensa getCurrentMensa() {
		return mensa[selectedMensa];
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO: This Builder stuff seems all a bit messy, see if this can be
		// improved
		AlertDialog.Builder builder;

		switch (item.getItemId()) {
		case R.id.refresh:
			loadMenu(true);
			// refreshView();
			return true;

		case R.id.settings:
			String[] mensaNames = new String[mensa.length];

			for (int i = 0; i < mensa.length; i++) {
				mensaNames[i] = mensa[i].getName();
			}

			builder = new AlertDialog.Builder(this);
			builder.setTitle("Einstellungen");
			builder.setSingleChoiceItems(mensaNames, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							selectedMensa = item;
						}
					});
			builder.setCancelable(false);
			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							loadMenu(false);

							for (int i = 0; i < 5; i++) {
								menuDayView[i].refreshView();
							}

							// Store selected mensa
							SharedPreferences settings = getSharedPreferences(
									"yom_prefs", 0);
							SharedPreferences.Editor editor = settings.edit();
							editor.putInt("selected mensa", selectedMensa);
							editor.commit();

							dialog.dismiss();
						}
					});

			AlertDialog alert = builder.create();
			alert.show();
			return true;

		case R.id.about:
			builder = new AlertDialog.Builder(this);
			try {

				// Location not working currently, just darkens screen
				// (Exception, probably).
				/*
				 * String distance = String.valueOf(gsm
				 * .getDistance(this.mensa[this.selectedMensa]
				 * .getCoordinates()));
				 */

				builder.setMessage(
						"Ye Olde Mensa v"
								+ VERSION
								+ "\n\nCopyright 2010/2011\nby Daniel Süpke, Frederik Kramer\n\nFür weitere Mensen und FAQ: http://yeoldemensa.de/ ")
						// +
						// "\n Die Entfernung\n zur ausgewählten Mensa\n beträgt zur Zeit: "
						// + distance + "km")
						.setCancelable(false)
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								});
				builder.create();
			} catch (Exception e) {
				Log.i("Location", e.toString());
			}
			builder.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Refreshes the menu data (i.e. parses the web site) and displays any error
	 * messages. TODO: Include in refreshView()? Otherwise always both calls
	 * necessary. Maybe integrate FakeMenu into it and use a DEBUG constant
	 */
	private void loadMenu(boolean forceRefresh) {
		try {
			this.mensa[selectedMensa].loadMenu(forceRefresh);

			// Display last actualisation date
			String date = new SimpleDateFormat("dd.MM.yyyy HH:mm")
					.format(new Date());
			((TextView) findViewById(R.id.headerdate)).setText("Aktualisiert "
					+ date);

			// Display current Mensa name
			((TextView) findViewById(R.id.headermensa))
					.setText(this.mensa[selectedMensa].getName());
		} catch (Exception e) {
			Log.d("yom",
					"Exception while retrieving menu data: " + e.getMessage());
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Fehler beim Auslesen der Mensa-Webseite!\nWahrscheinlich wurde die Mensa-Webseite geändert (liegt leider ausserhalb unserer Kontrolle, bitte auf Update warten oder Mail an yeoldemensa@suepke.eu).\n\nDetail: "
							+ e)
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});
			builder.create();
			builder.show();
		}
	}
}