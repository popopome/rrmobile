package com.jhlee.rr;

import android.app.Activity;
import android.os.Bundle;
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
		
	}
}
