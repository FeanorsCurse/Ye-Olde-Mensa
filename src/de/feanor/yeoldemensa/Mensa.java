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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Süpke
 * 
 */
public abstract class Mensa {

	private Map<String, List<String>> menu = new HashMap<String, List<String>>();

	/**
	 * Adds a menu item to a the food items. Example: addMenuItem("Ausgabe A",
	 * "Spaghetti Napoli"); Usually used within loadMenu(); Duplicate items will
	 * not be added.
	 * 
	 * @param type
	 *            Type of food, will be used as header in the view
	 * @param menuItem
	 *            Menu item (food) to add
	 */
	protected void addMenuItem(String type, String menuItem) {
		List<String> menuList = menu.get(type);

		if (menuList == null) {
			menuList = new ArrayList<String>();
			menu.put(type, menuList);
		}

		// Avoid double items
		if (!menuList.contains(menuItem)) {
			menuList.add(menuItem);
		}
	}

	public Map<String, List<String>> getMenu() {
		return menu;
	}

	public List<String> getMenuItems(String type) {
		return menu.get(type);
	}

	public void refresh() throws IOException {
		menu.clear();
		loadMenu();
	}
	
	public boolean isEmpty() {
		return menu.isEmpty();
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		// TODO Support more than a single day
		return new SimpleDateFormat("dd.MM.yyyy").format(new Date());
	}

	/**
	 * Load the menu, usually by parsing a web site. Use addMenuItem(String
	 * type, String menuItem) to add items.
	 * 
	 * @see Mensa.addMenuItem(String type, String menuItem)
	 * @throws IOException
	 */
	protected abstract void loadMenu() throws IOException;

	/**
	 * Return the name of the Mensa, e.g. "Mensa Oldenburg"
	 * 
	 * @return Name of the Mensa
	 */
	protected abstract String getName();

}
