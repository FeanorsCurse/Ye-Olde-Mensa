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

import static de.feanor.yeoldemensa.MensaMenu.AUSGABE_A;
import static de.feanor.yeoldemensa.MensaMenu.AUSGABE_B;
import static de.feanor.yeoldemensa.MensaMenu.BEILAGEN;
import static de.feanor.yeoldemensa.MensaMenu.CULINARIUM;
import static de.feanor.yeoldemensa.MensaMenu.MENSA_UHLHORNSWEG;
import static de.feanor.yeoldemensa.MensaMenu.MENSA_WECHLOY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;

/**
 * @author Daniel Süpke
 * 
 */
public class YeOldeMensa extends Activity {

	public static final String VERSION = "0.7";

	// currently selected mensa
	private int mensa = MENSA_UHLHORNSWEG;
	private MensaMenu mensaMenu = new MensaMenu();

	private MergeAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("YOM", "Starting application");

		refresh();
		// getFakeMenu(); // Use this for testing

		setContentView(R.layout.main);
		refreshView();
	}

	/**
	 * Provides test data without accessing the internet.
	 */
	@SuppressWarnings("unused")
	private void getFakeMenu() {
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, AUSGABE_A,
				"Alternativessen Blubb (1,40)");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, AUSGABE_A,
				"Pasta mit Klatsch Soße (2,00)");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, AUSGABE_A, "Paste (2,00)");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, AUSGABE_B,
				"Reste von letzter Woche");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, AUSGABE_B, "Ratte am Spieß");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, CULINARIUM,
				"Lecker teures Essen");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, CULINARIUM,
				"Teures Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_UHLHORNSWEG, CULINARIUM,
				"Teures Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_WECHLOY, AUSGABE_A,
				"Alternativessen Blubb (1,40)");
		mensaMenu.add_menu_item(MENSA_WECHLOY, AUSGABE_A,
				"Pasta mit Klatsch Soße (2,00)");
		mensaMenu.add_menu_item(MENSA_WECHLOY, AUSGABE_A, "Paste (2,00)");
		mensaMenu.add_menu_item(MENSA_WECHLOY, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_WECHLOY, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_WECHLOY, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_WECHLOY, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_WECHLOY, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_WECHLOY, BEILAGEN, "Schälchen Pampe");
		mensaMenu.add_menu_item(MENSA_WECHLOY, BEILAGEN, "Schälchen Pampe");
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
			refreshView();
			return true;

		case R.id.settings:
			final CharSequence[] items = { "Uhlhornsweg", "Wechloy" };

			builder = new AlertDialog.Builder(this);
			builder.setTitle("Einstellungen");
			builder.setSingleChoiceItems(items, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							mensa = item;
						}
					});
			builder.setCancelable(false);
			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							refreshView();
							dialog.dismiss();
						}
					});

			AlertDialog alert = builder.create();
			alert.show();
			return true;

		case R.id.about:
			Log.d("yom", "über");
			builder = new AlertDialog.Builder(this);
			builder
					.setMessage(
							"Ye Olde Mensa v"
									+ VERSION
									+ "\n\nCopyright 2010/2011\nby Daniel Süpke\n\nhttp://suepke.eu/")
					.setCancelable(false).setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});
			builder.create();
			builder.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Refreshes the menu data (i.e. parses the web site) and displays any error
	 * messages.
	 */
	private void refresh() {
		try {
			this.mensaMenu.refresh();
		} catch (IOException e) {
			Log.d("yom", "Exception while retrieving menu data: "
					+ e.getMessage());
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Fehler beim Auslesen der Mensa-Webseite: "
							+ e.getMessage()).setCancelable(false)
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

	/**
	 * Draws the menu based on the data in this.mensaMenu
	 */
	private void refreshView() {
		// TODO: Clean up this mess. Very slow and unintuitive use of the
		// mergeadapter contents due to a bug I was fighting with.
		ArrayAdapter<String> a;
		List<String> l;

		adapter = new MergeAdapter();

		if (mensa == MENSA_UHLHORNSWEG) {
			((TextView) findViewById(R.id.headermensa))
					.setText("Mensa Uhlhornsweg");

			l = new ArrayList<String>();
			l.add("Ausgabe A (Alternativ+Pasta)");
			a = new ArrayAdapter<String>(this, R.layout.list_header, l);
			adapter.addAdapter(a);
			a = new ArrayAdapter<String>(this, R.layout.list_item, mensaMenu
					.getMenuItems(MENSA_UHLHORNSWEG, AUSGABE_A));
			adapter.addAdapter(a);

			l = new ArrayList<String>();
			l.add("Ausgabe B (Auswahl)");
			a = new ArrayAdapter<String>(this, R.layout.list_header, l);
			adapter.addAdapter(a);
			a = new ArrayAdapter<String>(this, R.layout.list_item, mensaMenu
					.getMenuItems(MENSA_UHLHORNSWEG, AUSGABE_B));
			adapter.addAdapter(a);
		} else if (mensa == MENSA_WECHLOY) {
			((TextView) findViewById(R.id.headermensa))
					.setText("Mensa Wechloy");

			l = new ArrayList<String>();
			l.add("Tellergerichte");
			a = new ArrayAdapter<String>(this, R.layout.list_header, l);
			adapter.addAdapter(a);
			a = new ArrayAdapter<String>(this, R.layout.list_item, mensaMenu
					.getMenuItems(MENSA_WECHLOY, AUSGABE_A));
			adapter.addAdapter(a);
		}

		l = new ArrayList<String>();
		l.add("Beilagen (0,30/0,50)");
		a = new ArrayAdapter<String>(this, R.layout.list_header, l);
		adapter.addAdapter(a);
		a = new ArrayAdapter<String>(this, R.layout.list_item, mensaMenu
				.getMenuItems(mensa, BEILAGEN));
		adapter.addAdapter(a);

		if (mensa == MENSA_UHLHORNSWEG) {
			l = new ArrayList<String>();
			l.add("Culinarium");
			a = new ArrayAdapter<String>(this, R.layout.list_header, l);
			adapter.addAdapter(a);
			a = new ArrayAdapter<String>(this, R.layout.list_item, mensaMenu
					.getMenuItems(MENSA_UHLHORNSWEG, CULINARIUM));
			adapter.addAdapter(a);
		}

		adapter.notifyDataSetChanged();
		((ListView) findViewById(R.id.menu_list)).setAdapter(adapter);
		((TextView) findViewById(R.id.headerdate)).setText(mensaMenu.getDate());

		// Keep this for now, I think I can use it to improve the list creation
		// with mergeadapter
		/*
		 * if (mensa == 0) ((TextView) findViewById(R.id.headermensa))
		 * .setText("Mensa Uhlhornsweg"); else ((TextView)
		 * findViewById(R.id.headermensa)) .setText("Mensa Wechloy");
		 */
	}
}