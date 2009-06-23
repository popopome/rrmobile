package com.jhlee.rr;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
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
}