/**
 *   Ye Olde Mensa is an android application for displaying the current
 *   mensa plans of University Oldenburg on an android mobile phone.
 *   
 *   Copyright (C) 2009-2013 Daniel Süpke
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
import java.util.List;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.commonsware.cwac.merge.MergeAdapter;

import de.feanor.yeoldemensa.data.Mensa;
import de.feanor.yeoldemensa.data.Mensa.Day;

/**
 * Provides the view for a tab representing one day of a Mensa.
 * 
 * @author Daniel Süpke
 */
public class MenuDayView extends ListView {

	// TODO: This is really sucky. Came from the strange
	// tab handling in android, but there should be a better way
	private YeOldeMensa context;
	private MergeAdapter adapter;

	private Day day;

	/**
	 * 
	 * @param context
	 * @param day
	 *            The day this View is for
	 */
	public MenuDayView(YeOldeMensa context, Day day) {
		super(context);

		this.context = context;
		this.day = day;
	}

	/**
	 * Refreshes the menu view based on the data in
	 * YeOldeMensa.getCurrentMensa()
	 */
	public void refreshView() {
		Mensa mensa = context.getCurrentMensa();

		// TODO: Clean up this mess. Very slow and unintuitive use of the
		// mergeadapter contents due to a bug I was fighting with.
		ArrayAdapter<String> a;

		adapter = new MergeAdapter();

		// Should fix a rare bug, when no mensa has been selected. Not sure if
		// this can occur here at all
		if (mensa == null) {
			List<String> list = new ArrayList<String>();
			a = new ArrayAdapter<String>(context, R.layout.list_header, list);
			adapter.addAdapter(a);
			adapter.notifyDataSetChanged();
			this.setAdapter(adapter);
			return;
		}

		// If there is no menu data, display message to user and set up empty
		// dummy adapter to avoid error
		if (mensa.isEmpty(day)) {
			List<String> list = new ArrayList<String>();
			list.add("Kein Menü gefunden. Mensa geschlossen?");
			a = new ArrayAdapter<String>(context, R.layout.list_header, list);
			adapter.addAdapter(a);
		}

		for (String menuType : mensa.getMenuForDay(day).keySet()) {
			List<String> list = new ArrayList<String>();
			list.add(menuType);
			a = new ArrayAdapter<String>(context, R.layout.list_header, list);
			adapter.addAdapter(a);
			a = new ArrayAdapter<String>(context, R.layout.list_item,
					mensa.getMenuforDayType(day, menuType));
			adapter.addAdapter(a);
		}

		adapter.notifyDataSetChanged();
		this.setAdapter(adapter);

		// Keep this for now, I think I can use it to improve the list creation
		// with mergeadapter
		/*
		 * if (mensa == 0) ((TextView) findViewById(R.id.headermensa))
		 * .setText("Mensa Uhlhornsweg"); else ((TextView)
		 * findViewById(R.id.headermensa)) .setText("Mensa Wechloy");
		 */
	}
}
