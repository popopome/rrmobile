package com.jhlee.rr;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

public class RRDayList extends ListActivity {

	private RRDbAdapter mDbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_day_list);

		/**
		 * Build adapter and set it for current list view Each view will show
		 * COUNT/TOTAL/DATE.
		 */
		mDbAdapter = new RRDbAdapter(this);
		Cursor c = mDbAdapter.queryReceiptByDaily();
		this.startManagingCursor(c);

		String[] from = { "CNT", "TOTAL_EXPENSE",
				RRDbAdapter.KEY_RECEIPT_TAKEN_DATE };
		int[] to = { R.id.Count, R.id.Total, R.id.Date };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.rr_day_list_item, c, from, to);
		this.setListAdapter(adapter);
	}

	/** Day list item is clicked */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		/** I just launch one day receipt list activity */
		Intent i = new Intent(this, RRReceiptList.class);
		this.startActivity(i);
	}
}
