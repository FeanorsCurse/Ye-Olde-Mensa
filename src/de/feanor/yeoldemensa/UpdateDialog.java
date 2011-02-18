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

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * @author Daniel Süpke
 */
public class UpdateDialog {

	private static Map<Integer, String> updates = new HashMap<Integer, String>();

	private UpdateDialog() {
	}

	// TODO: Display all udpate infos since last updated version
	/**
	 * Displays the latest update description.
	 * 
	 * @param context
	 *            Parent window of Dialog
	 * @return Dialog to be shown
	 */
	public static AlertDialog getUpdateDialog(Context context) {
		setupVersions();

		AlertDialog dialog = new AlertDialog.Builder(context).setMessage(
				updates.get(YeOldeMensa.VERSION_INTERNAL)).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).create();

		return dialog;
	}

	private static void setupVersions() {
		updates
				.put(
						8,
						"Updates in Version 1.2:\n\n"
								+ "- Wochenansicht jetzt auch für Nicht-Oldenburger-Mensen (Frederik, Markus)\n"
								+ "- Mensapläne werden jetzt direkt von yeoldemensa.de geholt, dadurch:\n"
								+ "  * App kleiner und schneller\n"
								+ "  * Mensa-Updates ohne App-Updates möglich\n"
								+ "  * keine Mensapläne, wenn der yeoldemensa-Server abschmiert ;)\n"
								+ "- Datenbank beschleunigt\n\n"
								+ "Bitte bedenkt, dass wir die neue Funktionalität noch testen. Wenn es Probleme geben sollte, schreibt bitte eine Mail an info@yeoldemensa.de!");

		updates
				.put(
						7,
						"Updates in Version 1.0:\n\n"
								+ "- Wochenansicht für Oldenburger Mensen (andere Mensen folgen in Kürze!)\n"
								+ "- Merkt sich die zuletzt gewählte Mensa\n"
								+ "- Mensapläne werden zwischengespeichert, dadurch deutlich schneller sobald die Pläne einmal geladen wurden\n"
								+ "- Diverse Bugfixes (bei weiteren Bugs bitte Mail an info@yeoldemensa.de!)\n"
								+ "- Homepage und Twitter-Account:\nhttp://twitter.com/yeoldemensa\nhttp://www.yeoldemensa.de");

	}
}
