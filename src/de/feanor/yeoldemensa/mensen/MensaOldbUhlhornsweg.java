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

import de.feanor.htmltokenizer.SimpleHTMLTokenizer;
import de.feanor.yeoldemensa.Mensa;
import de.feanor.yeoldemensa.MenuItem;

/**
 * @author Daniel Süpke
 * 
 */
public class MensaOldbUhlhornsweg extends Mensa {

	public static final int AUSGABE_A = 0, AUSGABE_B = 1, CULINARIUM = 2,
			BEILAGEN = 3;
	public static double lat = 53.147372;
	public static double lng = 8.179326;

	@Override
	protected void loadMenu() throws IOException {
		SimpleHTMLTokenizer tokenizer = new SimpleHTMLTokenizer(
				new URL(
						"http://www.studentenwerk-oldenburg.de/speiseplan/uhlhornsweg-ausgabe-a.php"),
				"iso-8859-1");
		String element;

		while ((element = tokenizer.nextText()) != null
				&& !element.equals("1,40"))
			;

		for (int i = 0; i < 5; i++) {
			this.addMenuItem(new MenuItem(Day.values()[i], "Alternativ (1,40)",
					tokenizer.nextText()));
		}

		while ((element = tokenizer.nextText()) != null
				&& !element.equals("2,00"))
			;

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				this.addMenuItem(new MenuItem(Day.values()[i], "Pasta (2,00)",
						tokenizer.nextText()));
			}
		}
	}

	@Override
	protected String getName() {
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
