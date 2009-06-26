package com.jhlee.rr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Captured image handilng activity
 * @author popopome
 *
 */
public class RRCaptured extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_captured);
		
		// Load sample image to view
		ImageView imgView = (ImageView)this.findViewById(R.id.CapturedImageView);
		imgView.setImageResource(R.drawable.sample_captured_receipt);
		
		
		final RRCaptured self = this;
		
		/** Initialize buttons */
		Button.OnClickListener btnTakeAnotherReceiptListener = new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(self, RRTakeShot.class);
				self.startActivity(i);
				self.finish();
			}
		};
		Button btnTakeAnotherReceipt = (Button)findViewById(R.id.ButtonTakeOtherReceipt);
		btnTakeAnotherReceipt.setOnClickListener(btnTakeAnotherReceiptListener);
		
		/** Initialize ok button */
		Button.OnClickListener btnOkListener = new Button.OnClickListener() {
			public void onClick(View v) {
				/** Save captured receipt to db */
				self.saveCapturedReceiptToDb();
				self.finish();
			}
		};
		Button btnOk = (Button)findViewById(R.id.ButtonOk);
		btnOk.setOnClickListener(btnOkListener);
		
		/** Initialize cancel button */
		Button.OnClickListener btnCancelListener = new Button.OnClickListener() {
			public void onClick(View v) {
				/** Just finish activity */
				self.finish();
			}
		};
		Button btnCancel = (Button)findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(btnCancelListener);
	}
	
	/** Save captured receipt to db */
	public void saveCapturedReceiptToDb() {
		
	}
}
