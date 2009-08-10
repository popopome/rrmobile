package com.jhlee.rr;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;

public class RRMoneyPadDialog extends Dialog {
	private int mDollars = 0;
	private int mCents = 0;
	/** CTOR */
	public RRMoneyPadDialog(Context ctx) {
		super(ctx);
		
		/* Start with no title */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* Initialize dialog */
		this.setContentView(R.layout.moneypad_dialog);

		/* Add money change listener */
		final RRMoneyTextView moneyView = (RRMoneyTextView) findViewById(R.id.moneyview);
		RRMoneyPad pad = (RRMoneyPad) findViewById(R.id.moneypad);
		pad.setMoneyChangeListener(new RRMoneyPad.OnMoneyChangedListener() {
			/**
			 * Apply money change data to total money view
			 */
			public void onMoneyChanged(int val, int scale) {
				int dollars = 0;
				int cents = -1;
				boolean useDotOnly = false;
				if (scale > 0) {
					dollars = val / scale;
					if (scale == 1) {
						useDotOnly = true;
					} else {
						cents = val % scale;
					}
				} else {
					dollars = val;
				}

				moneyView.setTotalMoney(dollars, cents, useDotOnly);
				moneyView.invalidate();
			}
		});

		/* Set money pad command listener */
		final Dialog self = this;
		pad
				.setMoneyPadCommandListener(new RRMoneyPad.OnMoneyPadCommandListener() {
					public void onCommandButtonClicked(int btnCommand) {
						if (btnCommand == RRMoneyPad.BTN_CANCEL)
							self.cancel();
						else
							self.dismiss();
					}
				});
	}
	
	/* Set money */
	public void setMoney(int dollars, int cents) {
		RRMoneyTextView moneyView = (RRMoneyTextView) findViewById(R.id.moneyview);
		moneyView.setTotalMoney(dollars, cents, true);
	}
	public int getDollars() {
		RRMoneyTextView moneyView = (RRMoneyTextView) findViewById(R.id.moneyview);
		return moneyView.getDollars();
	}
	public int getCents() {
		RRMoneyTextView moneyView = (RRMoneyTextView) findViewById(R.id.moneyview);
		return moneyView.getCents();
	}
}