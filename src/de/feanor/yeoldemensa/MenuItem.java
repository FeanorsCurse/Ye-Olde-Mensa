package de.feanor.yeoldemensa;

import de.feanor.yeoldemensa.Mensa.Day;

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
