package de.feanor.yeoldemensa;

import de.feanor.yeoldemensa.Mensa.Day;

public class MenuItem {
	public Day day;
	public String type;
	public String item;
	public int price; // in cent, TODO: not supported yet
	
	public MenuItem(Day day, String type, String item) {
		this.day = day;
		this.type = type;
		this.item = item;
		this.price = -1;
	}
	
	public MenuItem(Day day, String type, String item, int price) {
		this.day = day;
		this.type = type;
		this.item = item;
		this.price = price;
	}	
}
