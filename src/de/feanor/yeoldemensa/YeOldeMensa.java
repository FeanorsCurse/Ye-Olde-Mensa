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
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import de.compserve.gsmhelper.SimpleGSMHelper;
import de.feanor.yeoldemensa.Mensa.Day;
import de.feanor.yeoldemensa.mensen.MensaMagdbCampus;
import de.feanor.yeoldemensa.mensen.MensaMagdbHerren;
import de.feanor.yeoldemensa.mensen.MensaOldbUhlhornsweg;
import de.feanor.yeoldemensa.mensen.MensaOldbWechloy;
import de.feanor.yeoldemensa.mensen.MensaStendal;
import de.feanor.yeoldemensa.mensen.MensaWerninger;

/**
 * @author Daniel Süpke
 * 
 */
public class YeOldeMensa extends Activity {

	public static final String VERSION = "0.9";
	public SimpleGSMHelper gsm = new SimpleGSMHelper();

	// ADD YOUR MENSA HERE, THE REST IS DONE THROUGH MAGIC
	private Mensa[] mensa = { new MensaOldbUhlhornsweg(),
			new MensaOldbWechloy(), new MensaMagdbCampus(),
			new MensaMagdbHerren(), new MensaWerninger(), new MensaStendal() };

	// Use this for testing
	// private Mensa[] mensa = { new MensaTest() };

	// currently selected mensa index
	private int selectedMensa = 0;

	// One for each day of the week
	private MenuDayView[] menuDayView = new MenuDayView[5];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		
		// Load current Mensa
		refresh();

		// setCurrentCoordinates;
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		gsm.setMobileLocation(tm);

		// Set up tabs
		TabHost host = (TabHost) findViewById(R.id.tabhost);
		host.setup();

		for (int i = 0; i < 5; i++) {
			menuDayView[i] = new MenuDayView(this, Day.values()[i]);
		}

		host.addTab(host.newTabSpec("Mo").setIndicator("Montag")
				.setContent(new TabHost.TabContentFactory() {

					public View createTabContent(String tag) {
						return menuDayView[0];
					}
				}));
		host.addTab(host.newTabSpec("Di").setIndicator("Dienstag")
				.setContent(new TabHost.TabContentFactory() {

					public View createTabContent(String tag) {
						return menuDayView[1];
					}
				}));
		host.addTab(host.newTabSpec("Mi").setIndicator("Mittwoch")
				.setContent(new TabHost.TabContentFactory() {

					public View createTabContent(String tag) {
						return menuDayView[2];
					}
				}));
		host.addTab(host.newTabSpec("Do").setIndicator("Donnerstag")
				.setContent(new TabHost.TabContentFactory() {

					public View createTabContent(String tag) {
						return menuDayView[3];
					}
				}));
		host.addTab(host.newTabSpec("Fr").setIndicator("Freitag")
				.setContent(new TabHost.TabContentFactory() {

					public View createTabContent(String tag) {
						return menuDayView[4];
					}
				}));

		// adjust tab size. Unsure how this looks in different resolutions
		host.getTabWidget().getChildAt(0).getLayoutParams().height = 60;
		host.getTabWidget().getChildAt(1).getLayoutParams().height = 60;
		host.getTabWidget().getChildAt(2).getLayoutParams().height = 60;
		host.getTabWidget().getChildAt(3).getLayoutParams().height = 60;
		host.getTabWidget().getChildAt(4).getLayoutParams().height = 60;
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
			refresh();
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
							refresh();

							for (int i = 0; i < 5; i++) {
								menuDayView[i].refreshView();
							}
							// refreshView();
							dialog.dismiss();
						}
					});

			AlertDialog alert = builder.create();
			alert.show();
			return true;

		case R.id.about:
			builder = new AlertDialog.Builder(this);
			try {

				String distance = String.valueOf(gsm
						.getDistance(this.mensa[this.selectedMensa]
								.getCoordinates()));

				builder.setMessage(
						"Ye Olde Mensa v"
								+ VERSION
								+ "\n\nCopyright 2010/2011\nby Daniel Süpke, Frederik Kramer\n\nhttp://suepke.eu/ "
								+ "\n Die Entfernung\n zur ausgewählten Mensa\n beträgt zur Zeit: "
								+ distance + "km")
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
	private void refresh() {
		try {
			this.mensa[selectedMensa].refresh();

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
					"Fehler beim Auslesen der Mensa-Webseite: "
							+ e.getMessage())
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