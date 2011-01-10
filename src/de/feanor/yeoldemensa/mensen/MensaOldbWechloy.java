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

package de.feanor.yeoldemensa.mensen;

import java.io.IOException;
import java.net.URL;

import de.feanor.htmltokenizer.Element;
import de.feanor.htmltokenizer.SimpleHTMLTokenizer;
import de.feanor.yeoldemensa.Mensa;
import de.feanor.yeoldemensa.MenuItem;

/**
 * @author Daniel Süpke
 * 
 */
public class MensaOldbWechloy extends Mensa {

	public static double lat = 53.152147;
	public static double lng = 8.165046;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.feanor.yeoldemensa.Mensa#loadMenu(java.util.Map)
	 */
	@Override
	protected void loadMenu() throws IOException {
		SimpleHTMLTokenizer tokenizer = new SimpleHTMLTokenizer(
				new URL(
						"http://www.studentenwerk-oldenburg.de/speiseplan/wechloy.php"),
				"iso-8859-1");

		// Skip to next week instead?
		Element element;
		if (getNextWeek()) {
			while ((element = tokenizer.nextText()) != null
					&& !element.content.startsWith("Nächste Woche"))
				;
		}

		addColumn("gericht", "Hauptgericht", 140, tokenizer);
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Beilagen
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Gemüse
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Salat
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Suppe
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Dessert
	}

	private void addColumn(String delimeter, String type, int price,
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

	@Override
	public String getName() {
		return "Mensa Wechloy";
	}

	@Override
	public double[] getCoordinates() {
		double[] coordinates = new double[2];
		coordinates[0] = lat;
		coordinates[1] = lng;
		return coordinates;
	}
}
