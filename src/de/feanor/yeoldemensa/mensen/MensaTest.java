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
import java.util.Random;

import android.content.Context;
import de.feanor.yeoldemensa.Mensa;
import de.feanor.yeoldemensa.MenuItem;

public class MensaTest extends Mensa {

	public MensaTest(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double[] getCoordinates() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fetchMenu() throws IOException {
		for (Day day : Day.values()) {
			addMenuItem(new MenuItem(day, "Alternativ/Pasta",
					"Alternativessen Blubb (1,40)"));
			addMenuItem(new MenuItem(day, "Alternativ/Pasta",
					"Pasta mit Klatsch Soße (2,00)"));
			addMenuItem(new MenuItem(day, "Alternativ/Pasta", "Paste (2,00)"));

			addMenuItem(new MenuItem(day, "Auswahl", "Ratte am Spieß"));
			addMenuItem(new MenuItem(day, "Auswahl", "Vegane Pampe"));

			int rand = new Random().nextInt(4) + 1;

			for (int i = 0; i < rand; i++) {
				addMenuItem(new MenuItem(day, "Beilagen", "Schälchen Pampe "
						+ (i + 1)));
			}

			if (day == Day.MONDAY)
				addMenuItem(new MenuItem(day, "Beilagen",
						"Montagsschälchen Pampe"));

			addMenuItem(new MenuItem(day, "Culinarium", "Lecker teures Essen"));
			addMenuItem(new MenuItem(day, "Culinarium",
					"Teures Schälchen Pampe"));
		}
	}

	@Override
	public String getName() {
		return "Test-Mensa";
	}

	@Override
	public int getID() {
		return -1;
	}
}
