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

import de.feanor.htmltokenizer.SimpleHTMLTokenizer;
import de.feanor.yeoldemensa.Mensa;

/**
 * @author Daniel Süpke
 * 
 */
public class MensaOldbWechloy extends Mensa {

	public static final int HAUPTGERICHTE = 0, BEILAGEN = 1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.feanor.yeoldemensa.Mensa#loadMenu(java.util.Map)
	 */
	@Override
	protected void loadMenu() throws IOException {
		SimpleHTMLTokenizer tokenizer = new SimpleHTMLTokenizer(
				new URL(
						"http://www.studentenwerk-oldenburg.de/speiseplan/oldenburg-heute.php"),
				"iso-8859-1");
		String element;
		int menuType = HAUPTGERICHTE;

		// TODO: Currently assuming, Mensa Wechloy always
		// has two main items.
		int count = 0;

		// Skip to Wechloy start
		while ((element = tokenizer.nextText()) != null
				&& !element.startsWith("Mensa Wechloy"))
			;

		while ((element = tokenizer.nextText()) != null
				&& !element.startsWith("Mensa Ofener")) {
			// TODO: Currently assuming, Mensa Wechloy always
			// has two main items.
			if (count++ == 2) {
				menuType = BEILAGEN;
			}

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

			this.addMenuItem(getMenuType(menuType), element);
		}
	}

	private String getMenuType(int menuType) {
		switch (menuType) {
		case HAUPTGERICHTE:
			return "Hauptgerichte";
		case BEILAGEN:
			return "Beilagen (0,30)";
		}

		return null;
	}

	@Override
	protected String getName() {
		return "Mensa Wechloy";
	}

}
