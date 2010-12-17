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
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.feanor.yeoldemensa;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.util.Log;
import de.feanor.htmltokenizer.SimpleHTMLTokenizer;

/**
 * @author Daniel Süpke
 * 
 */
public class MensaMenu {

	public static final int MENSA_UHLHORNSWEG = 0, MENSA_WECHLOY = 1;

	public static final int AUSGABE_A = 0, AUSGABE_B = 1, CULINARIUM = 2,
			BEILAGEN = 3;

	// Could be done more generic. On the other hand, why? It's a very specific
	// class and problem anyway.
	private List<String> u_ausgabeA = new ArrayList<String>();
	private List<String> u_ausgabeB = new ArrayList<String>();
	private List<String> u_culinarium = new ArrayList<String>();
	private List<String> u_beilagen = new ArrayList<String>();
	private List<String> w_ausgabeA = new ArrayList<String>();
	private List<String> w_beilagen = new ArrayList<String>();

	private String date = "";

	/**
	 * Adds a menu item of the given type for the given mensa.
	 * 
	 * @param mensa
	 *            Either MENSA_UHLHORNSWEG or MENSA_WECHLOY
	 * @param type
	 *            One of AUSGABE_A, AUSGABE_B, CULINARIUM, or BEILAGEN;
	 */
	public void add_menu_item(int mensa, int type, String menu_item) {
		Collection<String> menu_items = null;

		switch (type) {
		case AUSGABE_A:
			if (mensa == MENSA_UHLHORNSWEG)
				menu_items = u_ausgabeA;
			else if (mensa == MENSA_WECHLOY)
				menu_items = w_ausgabeA;
			break;

		case AUSGABE_B:
			if (mensa == MENSA_UHLHORNSWEG)
				menu_items = u_ausgabeB;
			break;

		case CULINARIUM:
			if (mensa == MENSA_UHLHORNSWEG)
				menu_items = u_culinarium;
			break;

		case BEILAGEN:
			if (mensa == MENSA_UHLHORNSWEG)
				menu_items = u_beilagen;
			else if (mensa == MENSA_WECHLOY)
				menu_items = w_beilagen;
			break;
		}

		// Avoid double BEILAGEN
		if (!menu_items.contains(menu_item)) {
			Log.d("yom", "mensa: " + mensa + ", type: " + type + ", item: "
					+ menu_item);
			menu_items.add(menu_item);
		}
	}

	/**
	 * Returns the menu items of the given type for the given mensa.
	 * 
	 * @param mensa
	 *            Either MENSA_UHLHORNSWEG or MENSA_WECHLOY
	 * @param type
	 *            One of AUSGABE_A, AUSGABE_B, CULINARIUM, or BEILAGEN;
	 * @return List of menu items.
	 */
	public List<String> getMenuItems(int mensa, int type) {
		List<String> menu_items = null;

		switch (type) {
		case AUSGABE_A:
			if (mensa == MENSA_UHLHORNSWEG)
				menu_items = u_ausgabeA;
			else if (mensa == MENSA_WECHLOY)
				menu_items = w_ausgabeA;
			break;

		case AUSGABE_B:
			if (mensa == MENSA_UHLHORNSWEG)
				menu_items = u_ausgabeB;
			break;

		case CULINARIUM:
			if (mensa == MENSA_UHLHORNSWEG)
				menu_items = u_culinarium;
			break;

		case BEILAGEN:
			if (mensa == MENSA_UHLHORNSWEG)
				menu_items = u_beilagen;
			else if (mensa == MENSA_WECHLOY)
				menu_items = w_beilagen;
			break;
		}

		return menu_items;
	}

	/**
	 * Parses the Studentenwerk's web page for the current menu.
	 * 
	 * @throws IOException
	 */
	public void refresh() throws IOException {
		u_ausgabeA.clear();
		u_ausgabeB.clear();
		u_culinarium.clear();
		u_beilagen.clear();
		w_ausgabeA.clear();
		w_beilagen.clear();

		SimpleHTMLTokenizer tokenizer = new SimpleHTMLTokenizer(
				new URL(
						"http://www.studentenwerk-oldenburg.de/speiseplan/oldenburg-heute.php"),
				"iso-8859-1");
		String element;
		int menu_type = 0;

		// Retrieve date
		while ((element = tokenizer.nextTag()) != null) {
			if (element.contains("h3")) {
				date = tokenizer.nextText();
				date = "Menü vom " + date.substring(19, 31);
				break;
			}
		}

		// MENSA_UHLHORNSWEG

		while ((element = tokenizer.nextText()) != null
				&& !element.startsWith("Mensa Wechloy")) {
			element = sanitizeHTML(element);

			if (element.startsWith("Ausgabe")) {
				// Ausgabe ignorieren
			} else if (element.startsWith("Alternativ"))
				menu_type = AUSGABE_A;
			else if (element.startsWith("Pasta"))
				menu_type = AUSGABE_A;
			else if (element.startsWith("Auswahl"))
				menu_type = AUSGABE_B;
			else if (element.startsWith("Schälchen")
					|| element.startsWith("Beilagen"))
				menu_type = BEILAGEN;
			else if (element.startsWith("Culinarium"))
				menu_type = CULINARIUM;
			else {
				String token = tokenizer.nextText();

				// TODO: Fucked up code. Fix with RegExp or whatever
				if (token != null) {
					if ((token.startsWith("1") || token.startsWith("2")
							|| token.startsWith("3") || token.startsWith("4")
							|| token.startsWith("5") || token.startsWith("6")
							|| token.startsWith("7") || token.startsWith("8") || token
							.startsWith("9")))
						element += " " + token;
					else
						tokenizer.pushBack();
				}

				this.add_menu_item(MENSA_UHLHORNSWEG, menu_type, element);
			}
		}

		// MENSA_WECHLOY

		// TODO: Currently assuming, Mensa Wechloy always
		// has two main items.
		int count = 0;

		menu_type = AUSGABE_A;

		while ((element = tokenizer.nextText()) != null
				&& !element.startsWith("Mensa Ofener")) {
			// TODO: Currently assuming, Mensa Wechloy always
			// has two main items.
			if (count++ == 2) {
				menu_type = BEILAGEN;
			}

			element = sanitizeHTML(element);

			String token = tokenizer.nextText();

			// TODO: Fucked up code. Fix with RegExp or whatever
			if (token != null) {
				if ((token.startsWith("1") || token.startsWith("2")
						|| token.startsWith("3") || token.startsWith("4")
						|| token.startsWith("5") || token.startsWith("6")
						|| token.startsWith("7") || token.startsWith("8") || token
						.startsWith("9")))
					element += " " + token;
				else
					tokenizer.pushBack();
			}

			this.add_menu_item(MENSA_WECHLOY, menu_type, element);
		}
	}

	/**
	 * Replacing html-entities with actual chars. Man the Studentenwerk's web
	 * site is really messed up.
	 */
	private String sanitizeHTML(String element) {
		element = element.replace("&auml;", "ä");
		element = element.replace("&Auml;", "Ä");
		element = element.replace("&uuml;", "ü");
		element = element.replace("&Uuml;", "Ü");
		element = element.replace("&ouml;", "ö");
		element = element.replace("&Ouml;", "Ö");

		return element;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
}
