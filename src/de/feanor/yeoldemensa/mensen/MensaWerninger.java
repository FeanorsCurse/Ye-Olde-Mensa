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
import java.util.Calendar;

import android.content.Context;
import de.feanor.htmltokenizer.Element;
import de.feanor.htmltokenizer.SimpleHTMLTokenizer;
import de.feanor.yeoldemensa.Mensa;
import de.feanor.yeoldemensa.MenuItem;

/**
 * @author Frederik Kramer
 * 
 */
public class MensaWerninger extends Mensa {

	public static final int HAUPTGERICHTE = 0, BEILAGEN = 1;
	public static double lat = 52.141074;
	public static double lng = 11.64834;

	public MensaWerninger(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.feanor.yeoldemensa.Mensa#loadMenu(java.util.Map)
	 */
	@Override
	protected void fetchMenu() throws IOException {
		// TODO: Currently only supports day plans, not week
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); 
		Day day = Day.values()[dow-2];
		if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY)
			day = Day.MONDAY;

		SimpleHTMLTokenizer tokenizer = new SimpleHTMLTokenizer(
				new URL(
						"http://www.studentenwerk-magdeburg.de/deutsch/essen_trinken/speiseplan/seiten/mensa_wernigerode.aspx"),
				"utf-8");
		Element element;
		String output;
		int menuType = HAUPTGERICHTE;

		// Skip to Hauptgerichte Start
		while ((element = tokenizer.nextText()) != null) {
			if (((element.content.startsWith("Essen1"))
					|| (element.content.startsWith("Essen2"))
					|| (element.content.startsWith("Essen3")) || (element.content
					.startsWith("Essen4")))
					&& ((element = tokenizer.nextText()) != null)) {
				if ((output = tokenizer.nextText().content) != null) {
					output = element.content + " " + output;
				} else {
					output = element.content;
				}
				this.addMenuItem(new MenuItem(day, getMenuType(menuType),
						output));
			}
			if (element.content.startsWith("Beilagen:")
					&& element.content.length() > 9) {
				String element2 = element.content.replaceAll("Beilagen: ", "");
				String[] elements = element2.split(", ");
				menuType = BEILAGEN;
				for (int i = 0; i < elements.length; i++) {
					this.addMenuItem(new MenuItem(day, getMenuType(menuType),
							elements[i]));
				}
			}
		}
	}

	private String getMenuType(int menuType) {
		switch (menuType) {
		case HAUPTGERICHTE:
			return "Hauptgerichte";
		case BEILAGEN:
			return "Beilagen";
		}

		return null;
	}

	@Override
	public String getName() {
		return "Werningerode";
	}

	@Override
	public double[] getCoordinates() {
		double[] coordinates = new double[2];
		coordinates[0] = lat;
		coordinates[1] = lng;
		return coordinates;
	}

	@Override
	public int getID() {
		return 4;
	}
}
