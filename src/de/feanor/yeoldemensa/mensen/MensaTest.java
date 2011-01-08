package de.feanor.yeoldemensa.mensen;

import java.io.IOException;
import java.util.Random;

import android.util.Log;

import de.feanor.yeoldemensa.Mensa;
import de.feanor.yeoldemensa.MenuItem;

public class MensaTest extends Mensa {

	@Override
	public double[] getCoordinates() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void loadMenu() throws IOException {
		Log.d("yom", "loading menu");
		
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
				addMenuItem(new MenuItem(day, "Beilagen", "Schälchen Pampe " + (i+1)));
			}
			
			if (day == Day.MONDAY)
				addMenuItem(new MenuItem(day, "Beilagen", "Montagsschälchen Pampe"));

			addMenuItem(new MenuItem(day, "Culinarium", "Lecker teures Essen"));
			addMenuItem(new MenuItem(day, "Culinarium",
					"Teures Schälchen Pampe"));
		}
	}

	@Override
	protected String getName() {
		return "Test-Mensa";
	}

}
