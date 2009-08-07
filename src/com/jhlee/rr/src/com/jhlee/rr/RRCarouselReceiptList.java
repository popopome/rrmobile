package com.jhlee.rr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.jhlee.rr.RRCarouselFlowView.RRCarouselActiveItemClickListener;
import com.jhlee.rr.RRCarouselFlowView.RRCarouselItem;

public class RRCarouselReceiptList extends Activity {
	private RRCarouselFlowView mView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_carousel_receipt_list);

		RRCarouselFlowView view = (RRCarouselFlowView)findViewById(R.id.carouselView);
		view.initialize(100, 100, 100, 100, 9, 25);
		view.setFocusable(true);
		view.setActiveItemClickListener(new RRCarouselActiveItemClickListener() {
			public void onClicked(RRCarouselFlowView view, RRCarouselItem item) {
				final String itemSeq = Integer.toString(item.seq);
				
				new AlertDialog.Builder(RRCarouselReceiptList.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.app_name)
                .setMessage(itemSeq)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked OK so do some stuff */
                    }
                })
                .create().show();
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
}
