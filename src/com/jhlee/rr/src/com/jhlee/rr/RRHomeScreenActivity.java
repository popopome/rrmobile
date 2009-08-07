package com.jhlee.rr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class RRHomeScreenActivity extends Activity {

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
		
		R2TopicListView v = (R2TopicListView)findViewById(R.id.rr_topiclist);
        v.addTopic(0, "Take a Receipt", Color.WHITE, Color.BLACK, -1);
        v.addTopic(1, "View Receipts", Color.WHITE, Color.BLACK, -1);
        
        
        v.setItemClickListener(new R2TopicListView.OnItemClickListener() {
			public void onItemClicked(View view, long itemIndex) {
				Intent i = null;
				Context ctx = view.getContext();
				switch((int)itemIndex) {
				case 0:
					/** Move to take shot activity */
					i = new Intent(ctx, RRTakeShot.class);
					ctx.startActivity(i);
					break;
				case 1:
					/** See receipt list */
					i = new Intent(ctx, RRDayList.class);
					ctx.startActivity(i);
					break;
				}
			}
        	
        });

	}

	

}
