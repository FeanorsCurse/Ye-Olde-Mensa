package de.feanor.yeoldemensa;

import java.util.ArrayList;
import java.util.List;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.commonsware.cwac.merge.MergeAdapter;

import de.feanor.yeoldemensa.Mensa.Day;

public class MenuDayView extends ListView {

	// TODO: This is really sucky. Really need to fix it. Came from the strange
	// tab handling in android, but there should be a better way
	private YeOldeMensa context;

	private MergeAdapter adapter;
	private Day day;

	public MenuDayView(YeOldeMensa context, Day day) {
		super(context);

		this.context = context;
		this.day = day;

		refreshView();
	}

	/**
	 * Refreshes the menu view based on the data in
	 * YeOldeMensa.getCurrentMensa()
	 */
	public void refreshView() {
		// TODO: Clean up this mess. Very slow and unintuitive use of the
		// mergeadapter contents due to a bug I was fighting with.
		ArrayAdapter<String> a;

		adapter = new MergeAdapter();

		if (context.getCurrentMensa().isEmpty()) {
			List<String> list = new ArrayList<String>();

			list.add("Kein Menü gefunden. Mensa geschlossen?");
			a = new ArrayAdapter<String>(context, R.layout.list_header, list);
			adapter.addAdapter(a);
		}

		for (String menuType : context.getCurrentMensa().getMenuForDay(day)
				.keySet()) {
			List<String> list = new ArrayList<String>();
			list.add(menuType);
			a = new ArrayAdapter<String>(context, R.layout.list_header, list);
			adapter.addAdapter(a);
			a = new ArrayAdapter<String>(context, R.layout.list_item, context
					.getCurrentMensa().getMenuforDayType(day, menuType));
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
