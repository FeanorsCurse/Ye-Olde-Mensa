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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Süpke
 * 
 */
public abstract class Mensa {

	public enum Day {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
	}

	// Map<Day, Map<type, List<menuItem>>>
	// So, it's a map of weekdays with the menu-map, consiting of the menu types
	// and menu items
	private Map<Day, Map<String, List<String>>> menu = new LinkedHashMap<Day, Map<String, List<String>>>();

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
	protected void addMenuItem(MenuItem menuItem) {
		Map<String, List<String>> dayMenu = menu.get(menuItem.day);

		if (dayMenu == null) {
			dayMenu = new LinkedHashMap<String, List<String>>();
			menu.put(menuItem.day, dayMenu);
		}

		List<String> menuList = dayMenu.get(menuItem.type);

		if (menuList == null) {
			menuList = new ArrayList<String>();
			dayMenu.put(menuItem.type, menuList);
		}

		// Avoid double items
		if (!menuList.contains(menuItem.item)) {
			menuList.add(menuItem.item);
		}
	}

	public Map<String, List<String>> getMenuForDay(Day day) {
		return menu.get(day);
	}

	public List<String> getMenuforDayType(Day day, String type) {
		return menu.get(day).get(type);
	}

	public void refresh() throws IOException {
		menu.clear();

		// Set up hashmaps for each week day
		for (Day day : Day.values())
			menu.put(day, new LinkedHashMap<String, List<String>>());

		loadMenu();
	}

	/**
	 * Return true if there is no menu data for the given day.
	 * 
	 * @param day
	 *            Day to check
	 * @return True if no menu
	 */
	public boolean isEmpty(Day day) {
		return menu.get(day).isEmpty();
	}

	/**
	 * Return true, if it's currently weekend, so the next week's plans should
	 * be fetched instead of the current week.
	 * 
	 * @return True, if next week should be fetched
	 */
	protected boolean getNextWeek() {
		int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
	}

	/**
	 * Load the menu, usually by parsing a web site. Use addMenuItem(String
	 * type, String menuItem) to add items.
	 * 
	 * Use getNextWeek() to test if the current or next week's plans should be
	 * fetched.
	 * 
	 * @see Mensa.addMenuItem(String type, String menuItem)
	 * @throws IOException
	 */
	protected abstract void loadMenu() throws IOException;

	/***
	 * @return coordinates of the Mensa
	 */
	public abstract double[] getCoordinates() throws Exception;

	/**
	 * Return the name of the Mensa, e.g. "Mensa Oldenburg"
	 * 
	 * @return Name of the Mensa
	 */
	public abstract String getName();

	/**
	 * Return the name of the town where the Mensa is in. Used to build groups
	 * of Mensas in the same town.
	 * 
	 * @return Name of the Mensa's town
	 */
	// TODO
	//public abstract String getTown();
}
