/**
 *   Ye Olde Mensa is an android application for displaying the current
 *   mensa plans of University Oldenburg on an android mobile phone.
 *   
 *   Copyright (C) 2009-2013 Daniel Süpke
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

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import de.feanor.yeoldemensa.data.MensaFactory;

/**
 * @author dsuepke
 * 
 */
public class ExceptionHandler {

	public static void handleException(Exception e) {
		if (e instanceof JSONException) {
			displayException(
					e,
					"Fehler im Datenformat auf "
							+ YeOldeMensa.HOST
							+ ". Das sollte nicht passieren! Wir arbeiten wahrscheinlich schon dran... Falls es bis morgen nicht wieder lüuft, schicke bitte eine Email an "
							+ YeOldeMensa.CONTACT_EMAIL + "!");
		} else if (e instanceof SocketTimeoutException) {
			displayException(e,
					"Timeout-Fehler: Die Webseite ist offline (oder lüdt langsamer als in "
							+ MensaFactory.TIMEOUT + "s)!");
		} else if (e instanceof UnknownHostException) {
			displayException(e,
					"Fehler beim Auflüsen des Hostnamens, keine Internetverbindung vorhanden?");
		} else if (e instanceof SocketException) {
			displayException(e,
					"Fehler beim Auflüsen des Hostnamens, keine Internetverbindung vorhanden?");
		} else {
			displayException(
					e,
					"Fehler beim Auslesen der Mensadaten von "
							+ YeOldeMensa.HOST
							+ "! Wir arbeiten wahrscheinlich schon dran... Falls es bis morgen nicht wieder lüuft, schicke bitte eine Email an info@yeoldemensa.de!");
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
	public static void displayException(Exception e, String errorMessage) {
		Log.e("yom", errorMessage + ": " + e.getMessage(), e);

		AlertDialog.Builder builder = new AlertDialog.Builder(
				YeOldeMensa.context);
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
