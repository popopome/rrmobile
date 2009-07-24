package com.jhlee.zoomsample;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;

public class RRMoneyPadDialog extends Dialog {

	/** CTOR */
	public RRMoneyPadDialog(Context ctx) {
		super(ctx);
		
		/* Initialize display metrics. */
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)ctx).getWindowManager().getDefaultDisplay().getMetrics(dm);
		RRMoneyView.initializeDisplayMetrics(dm);
		
		/* Start with no title */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* Initialize dialog */
		this.setContentView(R.layout.moneypad_dialog);
		
		
		
		
	}
	
	
}
