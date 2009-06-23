package com.jhlee.rr;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class RREditor extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_editor);
		
		// Set image.
		ImageView imgView = (ImageView)this.findViewById(R.id.ReceiptView);
		imgView.setImageResource(R.drawable.sample_captured_receipt);
	}

}
