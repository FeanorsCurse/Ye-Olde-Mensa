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

import de.feanor.yeoldemensa.mensen.*;

/**
 * @author Daniel Süpke
 * 
 */
public class YeOldeMensa extends Activity {

	public static final String VERSION = "0.7";

	// ADD YOUR MENSA HERE, THE REST IS DONE THROUGH MAGIC
	private Mensa[] mensa = { new MensaOldbUhlhornsweg(), new MensaOldbWechloy(), new MensaMagdbCampus(), new MensaMagdbHerren(), new MensaWerninger(), new MensaStendal()};

	// currently selected mensa
	private int selectedMensa = 0;

	private MergeAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		refresh();
		//useFakeMensa(); // Use this for testing

		setContentView(R.layout.main);
		refreshView();
	}

	/**
	 * Provides test data without accessing the internet.
	 */
	@SuppressWarnings("unused")
	private void useFakeMensa() {

		selectedMensa = 0;
		mensa = new Mensa[1];
		mensa[0] = new Mensa() {

			@Override
			protected void loadMenu() throws IOException {
				addMenuItem("Alternativ/Pasta", "Alternativessen Blubb (1,40)");
				addMenuItem("Alternativ/Pasta", "Pasta mit Klatsch Soße (2,00)");
				addMenuItem("Alternativ/Pasta", "Paste (2,00)");

				addMenuItem("Auswahl", "Ratte am Spieß");
				addMenuItem("Auswahl", "Vegane Pampe");

				addMenuItem("Beilagen", "Schälchen Pampe");
				addMenuItem("Beilagen", "Schälchen Pampe2");
				addMenuItem("Beilagen", "Schälchen Pampe3");
				addMenuItem("Beilagen", "Schälchen Pampe4");
				addMenuItem("Beilagen", "Schälchen Pampe5");
				addMenuItem("Beilagen", "Schälchen Pampe6");
				addMenuItem("Beilagen", "Schälchen Pampe7");
				addMenuItem("Beilagen", "Schälchen Pampe8");
				addMenuItem("Beilagen", "Schälchen Pampe9");

				addMenuItem("Culinarium", "Lecker teures Essen");
				addMenuItem("Culinarium", "Teures Schälchen Pampe");
			}

			@Override
			protected String getName() {
				return "Test Mensa";
			}
		};

		try {
			mensa[0].refresh();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
							refreshView();
							dialog.dismiss();
						}
					});

			AlertDialog alert = builder.create();
			alert.show();
			return true;

		case R.id.about:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Ye Olde Mensa v"
							+ VERSION
							+ "\n\nCopyright 2010/2011\nby Daniel Süpke\n\nhttp://suepke.eu/")
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
			this.mensa[selectedMensa].refresh();
		} catch (IOException e) {
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

	/**
	 * Draws the menu based on the data in this.mensaMenu
	 */
	private void refreshView() {
		// TODO: Clean up this mess. Very slow and unintuitive use of the
		// mergeadapter contents due to a bug I was fighting with.
		ArrayAdapter<String> a;

		adapter = new MergeAdapter();

		((TextView) findViewById(R.id.headermensa))
				.setText(mensa[selectedMensa].getName());

		for (String menuType : mensa[selectedMensa].getMenu().keySet()) {
			List<String> list = new ArrayList<String>();
			list.add(menuType);
			a = new ArrayAdapter<String>(this, R.layout.list_header, list);
			adapter.addAdapter(a);
			a = new ArrayAdapter<String>(this, R.layout.list_item,
					mensa[selectedMensa].getMenuItems(menuType));
			adapter.addAdapter(a);
		}

		adapter.notifyDataSetChanged();
		((ListView) findViewById(R.id.menu_list)).setAdapter(adapter);
		((TextView) findViewById(R.id.headerdate)).setText(mensa[selectedMensa]
				.getDate());

		// Keep this for now, I think I can use it to improve the list creation
		// with mergeadapter
		/*
		 * if (mensa == 0) ((TextView) findViewById(R.id.headermensa))
		 * .setText("Mensa Uhlhornsweg"); else ((TextView)
		 * findViewById(R.id.headermensa)) .setText("Mensa Wechloy");
		 */
	}
}