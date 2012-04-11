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

package de.feanor.yeoldemensa;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import de.feanor.yeoldemensa.data.Mensa;
import de.feanor.yeoldemensa.data.Mensa.Day;
import de.feanor.yeoldemensa.data.MensaFactory;

/**
 * Main class of the application.
 * 
 * @author Daniel SŸpke
 */
public class YeOldeMensa extends Activity {

	/**
	 * Version string is automatically displayed throughout the application.
	 * Always keep same with market version number!
	 */
	public static final String VERSION_PUBLIC = "1.4";

	/**
	 * Should be exactly the version as defined in the Manifest. Currently
	 * needed for update-dialog. TODO: Can the manifest version be accessed
	 * directly instead?
	 */
	public static final int VERSION_INTERNAL = 10;

	/**
	 * The application context, required e.g. in the MensaSQLiteHelper. Can be
	 * decoupled if necessary.
	 */
	public static Context context;

	// suepke: Commented out, keeps crashing my phone
	// public SimpleGSMHelper gsm = new SimpleGSMHelper();

	/** Currently displayed/selected mensa */
	private Mensa mensa;

	// One for each day of the week
	private MenuDayView[] menuDayView = new MenuDayView[5];

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("yom", "### Starting application ###");
		context = this;
		setContentView(R.layout.main);

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

		if (currentDay >= Calendar.MONDAY && currentDay <= Calendar.FRIDAY) {
			host.setCurrentTab(currentDay - 2);
		} else {
			host.setCurrentTab(Calendar.MONDAY - 2);
		}

		// Retrieve and load selected mensa
		SharedPreferences settings = getSharedPreferences("yom_prefs", 0);
		// TODO: set default to 0 and see the app crash...
		loadMensa(settings.getInt("selected mensa", 1), false);

		// Display updates
		int lastVersion = settings.getInt("previous version", -1);

		if (lastVersion != VERSION_INTERNAL) {
			UpdateDialog.getUpdateDialog(this).show();

			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("previous version", VERSION_INTERNAL);
			editor.commit();
		}
	}

	// TODO: Have a deeper look at this method (suepke)
	/**
	 * @param host
	 * @param title
	 * @param menuDayView2
	 * @return
	 */
	private TabSpec createTab(TabHost host, String title,
			final MenuDayView menuDayView2) {
		View view = getLayoutInflater().inflate(R.layout.tabs_bg, null);
		TextView textView = (TextView) view.findViewById(R.id.tabsText);
		textView.setText(title);

		return host.newTabSpec(title).setIndicator(view)
				.setContent(new TabHost.TabContentFactory() {

					@Override
					public View createTabContent(String tag) {
						return menuDayView2;
					}
				});
	}

	/**
	 * Returns the currently selected mensa
	 * 
	 * @return Mensa
	 */
	public Mensa getCurrentMensa() {
		return mensa;
	}

	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			if (mensa == null) {
				Toast.makeText(
						YeOldeMensa.this,
						"Keine Mensa ausgewŠhlt. Bitte erst eine Mensa in den Einstellungen auswŠhlen!",
						Toast.LENGTH_LONG).show();
			} else {
				loadMensa(mensa.getID(), true);
			}
			return true;

		case R.id.settings:
			settingsDialog();
			return true;

		case R.id.about:
			aboutDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// TODO: Settings dialogue is probably better kept in its own class
	/**
	 * Displays a dialogue when the user presses the settings menu button.
	 * Currently only offers the option to select the Mensa to be displayed.
	 * More options should follow.
	 */
	private void settingsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String[] mensaNames = null;

		try {
			// TODO: Don't depend on order
			mensaNames = MensaFactory.getMensaList().values()
					.toArray(new String[0]);
		} catch (JSONException e) {
			displayException(
					e,
					"Fehler im Datenformat auf yeoldemensa.de. Das sollte nicht passieren! Wir arbeiten wahrscheinlich schon dran... Falls es bis morgen nicht wieder lŠuft, schicke bitte eine Email an info@yeoldemensa.de!");
			return;
		} catch (UnknownHostException e) {
			displayException(e,
					"Fehler beim Auflšsen des Hostnamens, keine Internetverbindung vorhanden?");
			return;
		} catch (SocketException e) {
			displayException(e,
					"Fehler beim Auflšsen des Hostnamens, keine Internetverbindung vorhanden?");
			return;
		} catch (Exception e) {
			displayException(
					e,
					"Fehler beim Auslesen der Mensadaten von www.yeoldemensa.de! Wir arbeiten wahrscheinlich schon dran... Falls es bis morgen nicht wieder lŠuft, schicke bitte eine Email an info@yeoldemensa.de!");
			return;
		}

		builder.setTitle("Einstellungen");
		builder.setSingleChoiceItems(mensaNames, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int selectedMensaID) {
						Handler handler = new Handler() {

							@Override
							public void handleMessage(Message msg) {
								// Store selected mensa
								SharedPreferences settings = getSharedPreferences(
										"yom_prefs", 0);
								SharedPreferences.Editor editor = settings
										.edit();
								editor.putInt("selected mensa",
										selectedMensaID + 1);
								editor.commit();
								dialog.dismiss();

								// Todo: Mgdb
								String name = mensa.getName();
								if (name.startsWith("Magdeburg")
										|| name.startsWith("Werningerode")
										|| name.startsWith("Stendal")) {
									Toast.makeText(
											YeOldeMensa.this,
											"Diese Mensa unterstŸtzt bislang leider noch keine WochenplŠne und benštigt manuelles \"aktualisieren\" im MenŸ.\n\nWir arbeiten dran!",
											Toast.LENGTH_LONG).show();
								}
							}
						};

						loadMensa(selectedMensaID + 1, false, handler);
					}
				});
		builder.setCancelable(true);

		builder.create().show();
	}

	/**
	 * Displays a dialogue with info about the app. Selected from app menu.
	 */
	private void aboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// suepke: Location not working currently, just darkens screen
		// (Exception, probably).
		/*
		 * String distance = String.valueOf(gsm
		 * .getDistance(this.mensa[this.selectedMensa] .getCoordinates()));
		 */

		builder.setMessage(
				"Ye Olde Mensa v"
						+ VERSION_PUBLIC
						+ "\n\nCopyright 2010/2011\nby Daniel SŸpke\nContributions by Frederik Kramer und Markus Schneider\n\nDeine Mensa fehlt oder du hast einen Bug gefunden? Maile an info@yeoldemensa.de\n\nFolge uns auf Twitter:\nhttp://twitter.com/yeoldemensa\n\nHomepage und FAQ:\nhttp://www.yeoldemensa.de/ ")
				// +
				// "\n Die Entfernung\n zur ausgewŠhlten Mensa\n betrŠgt zur Zeit: "
				// + distance + "km")
				.setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});

		builder.create().show();
	}

	/**
	 * Refreshes the menu data (i.e. parses the web site) and displays any error
	 * messages. TODO: Include in refreshView()? Otherwise always both calls
	 * necessary. Maybe integrate FakeMenu into it and use a DEBUG constant
	 */
	private void loadMensa(final int mensaID, final boolean forceRefetch,
			final Handler... handlers) {
		final boolean showProgressDialog = (!MensaFactory.isUpToDate(mensaID) || forceRefetch);
		final ProgressDialog progressDialog;

		if (showProgressDialog) {
			progressDialog = ProgressDialog.show(this, "",
					"Hole Mensadaten, bitte warten...", true);
		} else {
			progressDialog = null;
		}

		new Thread() {

			@Override
			public void run() {
				try {
					mensa = MensaFactory.getMensa(mensaID, forceRefetch);
					for (Handler nextHandler : handlers) {
						nextHandler.sendEmptyMessage(0);
					}
					handler.sendEmptyMessage(0);
				} catch (Exception e) {
					Message exceptionMessage = Message.obtain();
					exceptionMessage.obj = e;
					exceptionHandler.sendMessage(exceptionMessage);
				}
			}

			private Handler exceptionHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					Exception e = (Exception) msg.obj;

					if (e instanceof SocketTimeoutException) {
						displayException(e,
								"Timeout-Fehler: Die Webseite ist offline (oder lŠdt langsamer als in "
										+ MensaFactory.TIMEOUT + "s)!");
					} else if (e instanceof UnknownHostException) {
						displayException(e,
								"Fehler beim Auflšsen des Hostnamens, keine Internetverbindung vorhanden?");
					} else if (e instanceof SocketException) {
						displayException(e,
								"Fehler beim Auflšsen des Hostnamens, keine Internetverbindung vorhanden?");
					} else {
						displayException(
								e,
								"Fehler beim Auslesen der Mensadaten von www.yeoldemensa.de! Wir arbeiten wahrscheinlich schon dran... Falls es bis morgen nicht wieder lŠuft, schicke bitte eine Email an info@yeoldemensa.de!");
					}

					if (showProgressDialog) {
						progressDialog.dismiss();
					}
				}
			};

			private Handler handler = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					updateMensaView();

					if (showProgressDialog) {
						progressDialog.dismiss();
					}
				}
			};

		}.start();
	}

	private void updateMensaView() {
		// Display last actualisation date
		String date = new SimpleDateFormat("dd.MM.yyyy HH:mm")
				.format(new Date());
		((TextView) findViewById(R.id.headerdate)).setText("Aktualisiert "
				+ date);

		// Display current Mensa name
		if (mensa == null) {
			Log.d("yom", "Updating screen for mensa null!");
			((TextView) findViewById(R.id.headermensa))
					.setText("Keine Mensa ausgewŠhlt...");
		} else {
			Log.d("yom", "Updating screen for mensa " + mensa.getName()
					+ " (id: " + mensa.getID() + ")");
			((TextView) findViewById(R.id.headermensa))
					.setText(mensa.getName());
		}

		// refresh View
		for (MenuDayView v : menuDayView) {
			v.refreshView();
		}
	}

	/**
	 * Displays an exception text to the user, along with explanatory error
	 * message. This error message should be understandable even to
	 * non-programmers and maybe help us when they give feedback.
	 * 
	 * Where Exceptions might occur, always use this method! App should never
	 * crash without an info to the user.
	 * 
	 * @param e
	 *            Exception to display
	 * @param errorMessage
	 *            (Commonly understandable) error message to display.
	 */
	private void displayException(Exception e, String errorMessage) {
		Log.e("yom", errorMessage + ": " + e.getMessage(), e);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(errorMessage + "\n\nDetail: " + e)
				.setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
}