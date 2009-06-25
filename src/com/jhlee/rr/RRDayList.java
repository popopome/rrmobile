package com.jhlee.rr;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class RRDayList extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.setContentView(R.layout.rr_day_list);
		
		
		// Initialize adapter
		HashMap<String, String> item00 = new HashMap<String, String>();
		item00.put("date", "May 19, 2009");
		item00.put("total", "$50.00");
		item00.put("count", "3");
		
		HashMap<String, String> item01 = new HashMap<String, String>();
		item01.put("date", "May 18, 2009");
		item01.put("total", "$33.00");
		item01.put("count", "2");
		
		ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
		itemList.add(item00);
		itemList.add(item01);
		
		SimpleAdapter adapter = new SimpleAdapter(this, itemList, R.layout.rr_day_list_item, 
				new String[] { "count", "date", "total" },
				new int[] { R.id.Count, R.id.Date, R.id.Total });
		this.setListAdapter(adapter);
	}

	/** Day list item is clicked */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		/** I just launch one day receipt list activity */
		Intent i = new Intent(this, RROneDayReceiptList.class);
		this.startActivity(i);
	}
}
