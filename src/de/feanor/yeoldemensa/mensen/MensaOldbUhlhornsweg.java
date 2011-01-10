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

package de.feanor.yeoldemensa.mensen;

import java.io.IOException;
import java.net.URL;

import android.util.Log;

import de.feanor.htmltokenizer.Element;
import de.feanor.htmltokenizer.SimpleHTMLTokenizer;
import de.feanor.yeoldemensa.Mensa;
import de.feanor.yeoldemensa.MenuItem;

/**
 * @author Daniel Süpke
 * 
 */
public class MensaOldbUhlhornsweg extends Mensa {

	public static double lat = 53.147372;
	public static double lng = 8.179326;

	@Override
	protected void loadMenu() throws IOException {
		SimpleHTMLTokenizer tokenizer;

		tokenizer = setupTokenizer("http://www.studentenwerk-oldenburg.de/speiseplan/uhlhornsweg-ausgabe-a.php");
		addColumnAusgabeA("1,40", "Ausgabe A (Alternativ/Pasta)", 140,
				tokenizer);
		addColumnAusgabeA("2,00", "Ausgabe A (Alternativ/Pasta)", 200,
				tokenizer);

		tokenizer = setupTokenizer("http://www.studentenwerk-oldenburg.de/speiseplan/uhlhornsweg-ausgabe-b.php");
		addColumnAusgabeBC("gericht", "Ausgabe B", -1, tokenizer);

		tokenizer = setupTokenizer("http://www.studentenwerk-oldenburg.de/speiseplan/culinarium.php");
		addColumnAusgabeBC("gericht", "Culinarium", -1, tokenizer);
		addColumnAusgabeBC("Beilagen", "Beilagen (0,30)", 45, tokenizer);
		addColumnAusgabeBC("Gemüse", "Beilagen (0,30)", 45, tokenizer);
		addColumnAusgabeBC("Salat", "Beilagen (0,30)", 45, tokenizer);
		addColumnAusgabeBC("Dessert", "Beilagen (0,30)", 45, tokenizer);

		// That's not optimal... parse the page a second time to add the Beilagen at the end.
		tokenizer = setupTokenizer("http://www.studentenwerk-oldenburg.de/speiseplan/uhlhornsweg-ausgabe-a.php");
		addColumnAusgabeA("0,30", "Beilagen (0,30)", 30, tokenizer); // Salat
		addColumnAusgabeA("0,30", "Beilagen (0,30)", 30, tokenizer); // Suppe
		addColumnAusgabeA("0,30", "Beilagen (0,30)", 30, tokenizer); // Dessert
		tokenizer = setupTokenizer("http://www.studentenwerk-oldenburg.de/speiseplan/uhlhornsweg-ausgabe-b.php");
		addColumnAusgabeBC("0,30", "Beilagen (0,30)", 30, tokenizer); // Beilagen
		addColumnAusgabeBC("0,30", "Beilagen (0,30)", 30, tokenizer); // Gemüse
		addColumnAusgabeBC("0,30", "Beilagen (0,30)", 30, tokenizer); // Salat
		addColumnAusgabeBC("0,30", "Beilagen (0,30)", 30, tokenizer); // Suppe
		addColumnAusgabeBC("0,30", "Beilagen (0,30)", 30, tokenizer); // Dessert
}

	/**
	 * Sets up the tokenizer and skips to the next week if indicated
	 */
	private SimpleHTMLTokenizer setupTokenizer(String url) throws IOException {
		SimpleHTMLTokenizer tokenizer = new SimpleHTMLTokenizer(new URL(url),
				"iso-8859-1");

		// Skip to next week instead?
		Element element;
		if (getNextWeek()) {
			while ((element = tokenizer.nextText()) != null
					&& !element.content.startsWith("Nächste Woche"))
				;
		}

		return tokenizer;
	}

	/**
	 * Parse Ausgabe A. Website structure differs from Ausgabe B and Culinarium :-/
	 * @param delimeter
	 * @param type
	 * @param price
	 * @param tokenizer
	 */
	private void addColumnAusgabeA(String delimeter, String type, int price,
			SimpleHTMLTokenizer tokenizer) {
		Element element;

		// Skip to beginning based on delimeter
		while ((element = tokenizer.nextText()) != null
				&& !element.content.equals(delimeter))
			;
		while ((element = tokenizer.nextTag()) != null
				&& !element.content.equals("/td"))
			;

		// Start adding items for each week day
		for (int i = 0; i < 5; i++) {
			element = tokenizer.nextElement();

			while (element.isText() || !element.content.equals("/td")) {
				if (element.isText()) {
					this.addMenuItem(new MenuItem(Day.values()[i], type,
							element.content, price));
				}
				element = tokenizer.nextElement();
			}
		}
	}

	/**
	 * Parse Ausgabe B and Culinarium. Website structure differs from Ausgabe A :-/
	 * @param delimeter
	 * @param type
	 * @param price
	 * @param tokenizer
	 */
	private void addColumnAusgabeBC(String delimeter, String type, int price,
			SimpleHTMLTokenizer tokenizer) {
		Element element;

		// Skip to beginning based on delimeter
		while ((element = tokenizer.nextText()) != null
				&& !element.content.equals(delimeter))
			;
		while ((element = tokenizer.nextTag()) != null
				&& !element.content.equals("/td"))
			;

		Log.d("yom", "Skipped");

		boolean inCell = false;
		boolean finished = false;

		// Start adding items for each week day
		for (int i = 0; i < 5; i++) {
			finished = false;
			inCell = false;

			// Next element needs to be ignored to remove leading td
			element = tokenizer.nextElement();

			while (!finished) {
				element = tokenizer.nextElement();

				if (element.isText() && !startsWithNumber(element.content)) {
					this.addMenuItem(new MenuItem(Day.values()[i], type,
							element.content, price));
				} else {
					// If a td is encountered, we are in an inner Cell. Do not
					// stop at next /td
					if (element.content.startsWith("td"))
						inCell = true;
					else if (element.content.equals("/td") && inCell)
						inCell = false;
					else if (element.content.equals("/td") && !inCell)
						finished = true;
				}
			}
		}
	}

	private boolean startsWithNumber(String string) {
		for (int i = 0; i < 10; i++) {
			if (string.startsWith(Integer.toString(i))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getName() {
		return "Mensa Uhlhornsweg";
	}

	@Override
	public double[] getCoordinates() {
		double[] coordinates = new double[2];
		coordinates[0] = lat;
		coordinates[1] = lng;
		return coordinates;
	}
}
