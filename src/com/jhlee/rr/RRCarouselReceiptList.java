package com.jhlee.rr;

import android.app.Activity;
import android.os.Bundle;

public class RRCarouselReceiptList extends Activity {
	private RRCarouselFlowView mView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_carousel_receipt_list);
		
		RRCarouselFlowView view = (RRCarouselFlowView)findViewById(R.id.carouselView);
		view.initialize(10, 100, 100, 100, 3, 25);
		view.setFocusable(true);
	}
}
