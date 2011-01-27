package de.compserve.helper;

import java.util.Calendar;

public class SimpleDateHelper {

	private Calendar cal1, cal2;

	public SimpleDateHelper() {

		// Initialize Calendar objects
		cal1 = Calendar.getInstance();
		cal2 = Calendar.getInstance();

		// Set the reference day
		this.setReferenceDate();
	}

	protected void setReferenceDate() {

		// Set Calendar object to reference point
		this.cal1.set(2000, 0, 1);
	}

	public int getDiffdays() {

		// Get reference points in milliseconds;
		long millis1 = this.cal1.getTimeInMillis();
		long millis2 = this.cal2.getTimeInMillis();

		// Compute difference between two date objects and convert them into
		// days
		int diffdays = Math.round((millis2 - millis1) / (1000 * 60 * 60 * 24));
		return diffdays;
	}

	public int getMonday() {

		// First day of the week is Monday
		int firstDay = this.cal2.getFirstDayOfWeek();

		// Monday is Sunday + 1, might not work outside Germany
		return firstDay;
	}

	public int getToday() {
		return this.cal2.get(Calendar.DAY_OF_WEEK);
	}

	public String[] getThisWeek() {

		// Calculate the offset to Monday of the actual week
		int diffDays = (this.getMonday() - this.getToday());
		this.cal2.add(Calendar.DAY_OF_MONTH, diffDays);

		// The working week has 6 workdays
		String[] thisWeek = new String[6];

		for (int i = 0; i <= 5; i++) {
			// Calculate the difference Days of the actual weekdays
			// according the reference date;
			thisWeek[i] = String.valueOf(this.getDiffdays());
			this.cal2.add(Calendar.DATE, 1);
		}

		return thisWeek;
	}
}
