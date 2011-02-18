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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data class for storing Mensa data (the Mensa itselfs and menu items for the
 * week)
 * 
 * @author Daniel Süpke
 */
public final class Mensa {

	public enum Day {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
	}

	// Map<Day, Map<type, List<menuItem>>>
	// So, it's a map of weekdays with the menu-map, consisting of the menu
	// types and menu items
	private Map<Day, Map<String, List<String>>> menu = new LinkedHashMap<Day, Map<String, List<String>>>();

	// TODO: Seems strange setting validTo like this
	private Date validTo = new GregorianCalendar(1970, 1, 1).getTime();

	private Date lastActualised;
	private String name;
	private double longitude, latitude;

	private final int id;

	/**
	 * Constructor with unique id.
	 * 
	 * @param id
	 *            Must be unique
	 */
	public Mensa(int id) {
		this.id = id;

		// Set up hashmaps for each week day
		for (Day day : Day.values())
			menu.put(day, new LinkedHashMap<String, List<String>>());
	}

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
	public void addMenuItem(MenuItem menuItem) {
		Map<String, List<String>> dayMenu = menu.get(menuItem.day);

		if (dayMenu == null) {
			dayMenu = new LinkedHashMap<String, List<String>>();
			menu.put(menuItem.day, dayMenu);
		}

		menuItem.item = menuItem.item.replace('"', '\'');

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

	/**
	 * @return coordinates of the Mensa
	 */
	public double[] getCoordinates() {
		return new double[] { longitude, latitude };
	}

	/**
	 * Returns the unique ID of the mensa
	 * 
	 * @return ID
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return the lastActualised
	 */
	public Date getLastActualised() {
		return lastActualised;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return the menu
	 */
	public Map<Day, Map<String, List<String>>> getMenu() {
		return menu;
	}

	/**
	 * Returns the menu for the given day in a Map. The Map keys are the menu
	 * types, with a List of menu item strings.
	 * 
	 * @param day
	 *            Day to get the menu for
	 * @return Map in form <menu_type, List<menu_item>>
	 */
	public Map<String, List<String>> getMenuForDay(Day day) {
		// Check if data is valid
		if (validTo.before(new Date())) {
			throw new RuntimeException("Warning: Used outdated Mensa data!");
		}

		return menu.get(day);
	}

	/**
	 * Returns the menu items for the given day and menu type.
	 * 
	 * @param day
	 *            Day to get the menu for
	 * @param type
	 *            Type to get the menu items for
	 * @return List of menu items
	 */
	public List<String> getMenuforDayType(Day day, String type) {
		// Check if data is valid
		if (validTo.before(new Date())) {
			throw new RuntimeException("Warning: Used outdated Mensa data!");
		}

		// if (menu.get(day).get(type) == null)
		// return Arrays.asList(new String[] { type + " nicht gefunden" });

		return menu.get(day).get(type);
	}

	/**
	 * Return the name of the Mensa, e.g. "Mensa Oldenburg"
	 * 
	 * @return Name of the Mensa
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the validTo
	 */
	public Date getValidTo() {
		return validTo;
	}

	/**
	 * Return true if there are no menu items for the given day. Note: Will
	 * still return true, if there are menu types.
	 * 
	 * @param day
	 *            Day to check
	 * @return True if no menu
	 */
	public boolean isEmpty(Day day) {
		for (String type : menu.get(day).keySet()) {
			if (!menu.get(day).get(type).isEmpty())
				return false;
		}

		return true;
	}

	/**
	 * @param lastActualised
	 *            the lastActualised to set
	 */
	public void setLastActualised(Date lastActualised) {
		this.lastActualised = lastActualised;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @param menu
	 *            the menu to set
	 */
	public void setMenu(Map<Day, Map<String, List<String>>> menu) {
		this.menu = menu;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param validTo
	 *            the validTo to set
	 */
	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("Mensa " + name + " (id = " + id
				+ ") {\n");

		for (Day day : Day.values()) {
			buffer.append("  " + day + ":\n");

			for (String menuitemType : getMenuForDay(day).keySet()) {
				buffer.append("    " + menuitemType + ":\n");

				for (String menuitem : getMenuforDayType(day, menuitemType)) {
					buffer.append("      - " + menuitem + "\n");
				}
			}
		}

		return buffer.toString();
	}
}
