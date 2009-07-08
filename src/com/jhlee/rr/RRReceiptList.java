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

public class RRReceiptList extends ListActivity {
	private RRDbAdapter mAdapter;
	private Cursor mCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_receipt_list);

		mAdapter = new RRDbAdapter(this);

		if (mCursor != null) {
			mCursor.close();
		}
		mCursor = mAdapter.queryAllReceipts();
		this.startManagingCursor(mCursor);

		
		String[] from = { RRDbAdapter.KEY_RECEIPT_TAKEN_DATE,
				RRDbAdapter.KEY_RECEIPT_TAKEN_TIME,
				RRDbAdapter.KEY_RECEIPT_TOTAL };
		int[] to = { R.id.TakenDate, R.id.TakenTime, R.id.Total };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.rr_receipt_list_item, mCursor, from, to);
		this.setListAdapter(adapter);
	}

	/**
	 * Receipt is clicked. Go to editor view.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		/** Pass receipt id */
		Intent i = new Intent(this, RREditor.class);
		i.putExtra("EDIT_RID", id);
		this.startActivity(i);
	}
}
