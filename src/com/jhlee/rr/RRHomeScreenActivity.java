package com.jhlee.rr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jhlee.rr.RRChartBarStreamView.RRChartBarDataProvider;

public class RRHomeScreenActivity extends Activity {

	public class RRDayOfWeekExpenseDataProvider implements RRChartBarDataProvider {
		private static final int COL_DAY_OF_WEEK = 0;
		private static final int COL_TOTAL = 1;
		private Cursor mCursor;
		public RRDayOfWeekExpenseDataProvider() {
			mCursor = mDbAdapter.queryExpenseDayOfWeek();
			RRHomeScreenActivity.this.startManagingCursor(mCursor);
		}
		public long getBarMaxValue() {
			mCursor.moveToFirst();
			long maxValue = 0;
			while(false == mCursor.isAfterLast()) {
				maxValue = Math.max(maxValue, mCursor.getLong(COL_TOTAL));
				mCursor.moveToNext();
			}
			return maxValue;
		}

		public String getBarTitle(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_TOTAL);
			return RRUtil.formatMoney(expense/100, expense%100, true);
		}

		public long getBarValue(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_TOTAL);
			return (long)expense;
		}

		public String getBarValueName(int position) {
			mCursor.moveToPosition(position);
			switch(mCursor.getInt(COL_DAY_OF_WEEK)) {
			case 0:	return "SUN";
			case 1: return "MON";
			case 2: return "TUE";
			case 3: return "WED";
			case 4: return "THU";
			case 5: return "FRI";
			case 6: return "SAT";
			}
			return "N/A";
		}

		public int getCount() {
			return mCursor.getCount();
		}
	}
	public class RRDayByDayExpenseDataProvider implements RRChartBarDataProvider {
		private static final int COL_DATE = 0;
		private static final int COL_EXPENSE = 1;
		/* Minimum expense $10 */
		private static final long MINIMUM_MAX_EXPENSE = 1000;
		private long mMaxExpense;
		private Cursor mCursor;
		public RRDayByDayExpenseDataProvider() {
			mMaxExpense = Math.max(MINIMUM_MAX_EXPENSE, mDbAdapter.getMaxExpenseAmongEachDays());
			
			mCursor = mDbAdapter.queryExpenseDayByDay();
			mCursor.moveToFirst();
			while(mCursor.isAfterLast()) {
				mCursor.moveToNext();
			}
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
			return RRUtil.formatCalendar(mCursor.getLong(COL_DATE));
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
			return 5;
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
				View view = createViewFromLayout(R.layout.rr_topic_item_command);
				TextView textView = (TextView) view.findViewById(R.id.command_title);
				if(position == 0) {
					textView.setText("Take an Expense");
				}
				else {
					textView.setText("View/edit expenses");
				}
				
				textView.requestLayout();
				return view;
			}
				/* Show day-by-day expense */
			case 2:
				return createDayByDayExpenseGraph();
			case 3:
				return createDayOfWeekExpenseGraph();
			case 4:
				/* What is most expensive item */
			{
				View view = createViewFromLayout(R.layout.rr_chart_expense_detail);
				Cursor cursor = mDbAdapter.queryMostExpensiveExpense();
				if(cursor.getCount()<1) {
					TextView textView = (TextView) createViewFromLayout(R.layout.rr_empty_data);
					textView.setText("Most expensive item - N/A");
					return textView;
				}
				cursor.moveToFirst();
				/* Set title */
				TextView titleView = (TextView) view.findViewById(R.id.chart_title);
				titleView.setText("Most expensive expense:");
				
				/* Load expense image */
				String path = cursor.getString(cursor.getColumnIndex(RRDbAdapter.KEY_RECEIPT_SMALL_IMG_FILE));
				Bitmap bmp = BitmapFactory.decodeFile(path);
				ImageView imgView = (ImageView) view.findViewById(R.id.img_view);
				imgView.setImageBitmap(bmp);
				
				TextView mainInfoView = (TextView)view.findViewById(R.id.item_title);
				
				long dateInMillis = cursor.getLong(cursor.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE));
				String dateString = RRUtil.formatCalendar(dateInMillis);
				
				long expense = cursor.getLong(cursor.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));
				String expenseString = RRUtil.formatMoney(expense/100, expense%100, true);
				mainInfoView.setText(dateString + "\n" + expenseString);
				
				TextView tagView = (TextView)view.findViewById(R.id.item_tags);
				String tagString = mDbAdapter.queryReceiptTagsAsOneString(cursor.getInt(0));
				tagView.setText(tagString);
				
				cursor.close();
				return view;
			}
			}

			return null;
		}

		private View createViewFromLayout(int layoutId) {
			String infService = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater) RRHomeScreenActivity.this
					.getSystemService(infService);
			View view = li.inflate(layoutId,
					null, true);
			return view;
		}

		
		/**
		 * Create day-by-day expense graph
		 * @return
		 */
		private View createDayByDayExpenseGraph() {
			RRChartBarGraph graph = new RRChartBarGraph(RRHomeScreenActivity.this);
			graph.setChartBarDataProvider(new RRDayByDayExpenseDataProvider());
			graph.setBarWidth(45);
	        graph.setBarValueNameTextSize(9);
	        graph.setTitleTextSize(20);
	        graph.setXYAxisName("Date", "Money");
	        graph.setGraphTitle("Day by day expense\nThe graph shows how you spend out money for each day");
	        graph.setEmptyText("Day by day expense - N/A");
	        graph.setBarMaxHeight(100);
	        return graph;
		}
		
		/**
		 * Create day-by-day expense graph
		 * @return
		 */
		private View createDayOfWeekExpenseGraph() {
			RRChartBarGraph graph = new RRChartBarGraph(RRHomeScreenActivity.this);
			graph.setChartBarDataProvider(new RRDayOfWeekExpenseDataProvider());
			graph.setBarWidth(45);
	        graph.setBarValueNameTextSize(9);
	        graph.setTitleTextSize(20);
	        graph.setXYAxisName("DayOfWeek", "Money");
	        graph.setGraphTitle("Day of week expense\nWhich day do you usually spend money?");
	        graph.setEmptyText("Day of week expense - N/A");
	        graph.setBarMaxHeight(100);
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
		
		/* Remove window title */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.rr_homescreen);

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

	@Override
	protected void onResume() {
		super.onResume();
		
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
	}
	
	
}
