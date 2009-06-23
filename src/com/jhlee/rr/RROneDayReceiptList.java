package com.jhlee.rr;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;

public class RROneDayReceiptList extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_one_day_receipt_list);
		
		// Initialize adapter
		HashMap<String, String> item00 = new HashMap<String, String>();
		item00.put("date", "May 19, 2009");
		item00.put("time", "10:00 am");
		item00.put("price", "$50.00");
		item00.put("tag", "grocery, coffee");
		item00.put("syncStatus", "Synced");
		
		HashMap<String, String> item01 = new HashMap<String, String>();
		item01.put("date", "May 18, 2009");
		item01.put("time", "09:30 am");
		item01.put("price", "$30.00");
		item01.put("tag", "dinning");
		item01.put("syncStatus", "Synced");
		
		ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
		itemList.add(item00);
		itemList.add(item01);
		
		SimpleAdapter adapter = new SimpleAdapter(this, itemList, R.layout.rr_day_list_item, 
				new String[] { "date", "time", "price", "tag", "syncStatus" },
				new int[] { R.id.Count, R.id.Date, R.id.Total });
		this.setListAdapter(adapter);
	}
	

}
