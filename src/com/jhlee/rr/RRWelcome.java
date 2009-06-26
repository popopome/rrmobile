package com.jhlee.rr;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class RRWelcome extends ListActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rr_welcome);

		HashMap<String, String> takeShotMap = new HashMap<String, String>();
		takeShotMap.put("itemType", "takeShot");
		
		HashMap<String, String> seeReceiptsMap = new HashMap<String, String>();
		seeReceiptsMap.put("itemType", "seeReceipts");		
		
		ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
		itemList.add(takeShotMap);
		itemList.add(seeReceiptsMap);
		
		SimpleAdapter itemAdapter = new SimpleAdapter(this, itemList,
				R.layout.rr_welcome_item, new String[] { "itemType" },
				new int[] { R.id.RRWelcomeItem });
		this.setListAdapter(itemAdapter);
	}

	/** Item is clicked */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = null;
		switch(position) {
		case 0:
			/** Move to take shot activity */
			i = new Intent(this, RRTakeShot.class);
			this.startActivity(i);
			break;
		case 1:
			/** See receipt list */
			i = new Intent(this, RRDayList.class);
			this.startActivity(i);
			break;
		}
	}
}