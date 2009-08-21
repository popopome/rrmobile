package com.jhlee.rr;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class RRCalendarSelectDialog extends Dialog {
	private static final String TAG = "RRCalendarSelectDialog";
	private boolean 	mIsSelected = false;
	private long 	mSelectedDateInMillis = -1;
	
	public RRCalendarSelectDialog(Context context) {
		super(context);
		/* Start with no title */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.rr_celendar_select_dialog);
		
		final RRCalendarStreamView calView = (RRCalendarStreamView)findViewById(R.id.calendar_stream_view);
		final RRCalendarSelectDialog self = this;
		
		/*
		 * Set up OK button
		 */
		Button okBtn = (Button)findViewById(R.id.select_button);
		okBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				self.mIsSelected = true;
				mSelectedDateInMillis = calView.getSelectedDateInMillis();
				self.dismiss();
			}
		});
		
		/*
		 * Set up CANCEL button
		 */
		Button cancelBtn = (Button)findViewById(R.id.cancel_button);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				self.cancel();
			}
		});
		
		/*
		 * Set up Go to today
		 */
		Button todayBtn = (Button)findViewById(R.id.move_to_today);
		todayBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				calView.moveToToday();
			}
		});
	}
	
	public long getSelectedDateInMillis() {
		return mSelectedDateInMillis;
	}
	
	public boolean isDateSelected() {
		return mIsSelected;
	}
	
	public void setActiveDate(String dateStr) {
		final RRCalendarStreamView calView = (RRCalendarStreamView)findViewById(R.id.calendar_stream_view);
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		long millis = 0;
		try {
			millis = formatter.parse(dateStr).getTime();
		} catch(ParseException e) {
			Log.e(TAG, "Unable to parse date string:dateStr" + dateStr);
			return;
		}
		calView.moveToDateInMillis(millis);
	}
}
