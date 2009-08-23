package com.jhlee.rr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jhlee.rr.RRChartBarStreamView.RRChartBarDataProvider;

public class RRHomeScreenActivity extends Activity {

	public class RRDayByDayExpenseDataProvider implements RRChartBarDataProvider {
		private static final int COL_DATE = 0;
		private static final int COL_EXPENSE = 1;
		private long mMaxExpense;
		private Cursor mCursor;
		public RRDayByDayExpenseDataProvider() {
			mMaxExpense = mDbAdapter.getMaxExpense();
			mCursor = mDbAdapter.queryExpenseDayByDay();
			RRHomeScreenActivity.this.startManagingCursor(mCursor);
		}
		public long getBarMaxValue() {
			return mMaxExpense;
		}

		public String getBarTitle(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_EXPENSE);
			return RRUtil.formatMoney(expense/100, expense%100, true);
		}

		public long getBarValue(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_EXPENSE);
			return (int)expense;
		}

		public String getBarValueName(int position) {
			mCursor.moveToPosition(position);
			return mCursor.getString(COL_DATE);
		}

		public int getCount() {
			return mCursor.getCount();
		}
		
	}
	
	/*
	 * Topic adapter
	 */
	private class TopicListAdapter extends BaseAdapter {

		public int getCount() {
			return 3;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			switch (position) {
			case 0:
			case 1: {
				String infService = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater li;
				li = (LayoutInflater) RRHomeScreenActivity.this
						.getSystemService(infService);
				View view = li.inflate(R.layout.rr_topic_item_command,
						null, true);
				TextView textView = (TextView) view.findViewById(R.id.command_title);
				if(position == 0) {
					textView.setText("Take an Expense");
				}
				else {
					textView.setText("View all receipts");
				}
				
				textView.requestLayout();
				return view;
			}
				/* Show day-by-day expense */
			case 2:
				return createDayByDayExpenseGraph();
			}

			return null;
		}

		
		/**
		 * Create day-by-day expense graph
		 * @return
		 */
		private View createDayByDayExpenseGraph() {
			RRChartBarGraph graph = new RRChartBarGraph(RRHomeScreenActivity.this);
			graph.setChartBarDataProvider(new RRDayByDayExpenseDataProvider());
			graph.setBarWidth(80);
	        graph.setBarValueNameTextSize(9);
	        graph.setTitleTextSize(20);
	        graph.setXYAxisName("Date", "Money");
	        graph.setGraphTitle("Day by day expense\nThe graph shows how you spend out money for each day");
	        graph.setBarMaxHeight(150);
	        return graph;
		}
	};

	private TopicListAdapter mTopicAdapter = new TopicListAdapter();
	private RRDbAdapter mDbAdapter;

	/**
	 * CTOR
	 */
	public RRHomeScreenActivity() {
		super();
	}

	/**
	 * Created
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rr_homescreen);
		
		/* Initialize Db */
		mDbAdapter = new RRDbAdapter(this);
		
		ListView v = (ListView) findViewById(R.id.topic_list);
		v.setAdapter(mTopicAdapter);
		v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Intent i = null;
				Context ctx = RRHomeScreenActivity.this;
				switch ((int) position) {
				case 0:
					/** Move to take shot activity */
					i = new Intent(ctx, RRTakeReceiptActivity.class);
					ctx.startActivity(i);
					break;
				case 1:
					/** See receipt list */
					i = new Intent(ctx, RRReceiptListActivity.class);
					ctx.startActivity(i);
					break;
				}
			}
		});

		/*
		 * RRTopicListView v = (RRTopicListView)findViewById(R.id.topic_list);
		 * v.addTopic(0, "Take a Receipt", Color.WHITE, Color.BLACK, -1);
		 * v.addTopic(1, "View Receipts", Color.WHITE, Color.BLACK, -1);
		 * 
		 * 
		 * v.setItemClickListener(new RRTopicListView.OnItemClickListener() {
		 * public void onItemClicked(View view, long itemIndex) { Intent i =
		 * null; Context ctx = view.getContext(); switch((int)itemIndex) { case
		 * 0:
		 *//** Move to take shot activity */
		/*
		 * i = new Intent(ctx, RRTakeReceiptActivity.class);
		 * ctx.startActivity(i); break; case 1:
		 *//** See receipt list */
		/*
		 * i = new Intent(ctx, RRReceiptListActivity.class);
		 * ctx.startActivity(i); break; } } });
		 */
	}
}
