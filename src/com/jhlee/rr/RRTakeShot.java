package com.jhlee.rr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RRTakeShot extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_takeshot);
		
		/** Register button click callback function */
		final Activity activity = this;
		Button.OnClickListener btnClickListener = new Button.OnClickListener() {
			public void onClick(View arg0) {
				/** Go to RRCaptured activity */
				Intent i = new Intent(activity, RRCaptured.class);
				activity.startActivity(i);
				activity.finish();
			}
		};
		Button btnTakeShot = (Button)this.findViewById(R.id.ButtonTakeShot);
		btnTakeShot.setOnClickListener(btnClickListener);
	}
}
