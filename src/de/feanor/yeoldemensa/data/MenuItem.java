/**
 *   Ye Olde Mensa is an android application for displaying the current
 *   mensa plans of University Oldenburg on an android mobile phone.
 *   
 *   Copyright (C) 2009/2010 Daniel SÃ¼pke
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
package de.feanor.yeoldemensa.data;

import de.feanor.yeoldemensa.data.Mensa.Day;

/**
 * Data class for a single menu item.
 * 
 * @author Daniel SŸpke
 */
public class MenuItem {
	public Day day;
	public String type;
	public String item;
	public int price; // in cent, TODO: not supported yet

	/**
	 * Constructs new MenuItem without price (price is set to -1)
	 * 
	 * @param day
	 *            Day of the menuItem
	 * @param type
	 *            Type of the MenuItem (e.g. Ausgabe 1)
	 * @param item
	 *            Name of the MenuItem (e.g. Spaghetti)
	 */
	public MenuItem(Day day, String type, String item) {
		this.day = day;
		this.type = type;
		this.item = item;
		this.price = -1;
	}

	/**
	 * Constructs new MenuItem with price
	 * 
	 * @param day
	 *            Day of the menuItem
	 * @param type
	 *            Type of the MenuItem (e.g. Ausgabe 1)
	 * @param item
	 *            Name of the MenuItem (e.g. Spaghetti)
	 * @param price
	 *            Price in cent
	 */
	public MenuItem(Day day, String type, String item, int price) {
		this.day = day;
		this.type = type;
		this.item = item;
		this.price = price;
	}

	@Override
	public String toString() {
		return "Menuitem (Tag: " + day + ", type: \"" + type + "\", item: \""
				+ item + "\", price: " + price + ")";
	}
}
