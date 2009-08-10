package com.jhlee.rr;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;

public class RRMoneyTextView extends RRTextView {

	private int mDollars;
	private int mCents;
	private boolean mUseDotOnly = false;
	private String mMoneyString;

	/** CTOR */
	public RRMoneyTextView(Context ctx) {
		super(ctx);
		initializeTotalMoney();
		super.setTextHorzAlign(Paint.Align.RIGHT);
	}

	public RRMoneyTextView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializeTotalMoney();
		super.setTextHorzAlign(Paint.Align.RIGHT);
	}

	public RRMoneyTextView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initializeTotalMoney();
		super.setTextHorzAlign(Paint.Align.RIGHT);
	}

	/**
	 * Initialize total money
	 */
	private void initializeTotalMoney() {
		setTotalMoney(0, -1, false);
	}
	/**
	 * Update money string from dollar & cents
	 */
	private void updateMoneyString() {
		String text = "$" + Integer.toString(mDollars);

		boolean dotExists = false;
		if (mUseDotOnly) {
			text = text + ".";
			dotExists = true;
		}
		if (mCents >= 0) {
			if (false == dotExists) {
				text = text + ".";
			}
			text = text + Integer.toString(mCents);
		}
		
		super.setText(text);
	}

	/**
	 * Set total money
	 * 
	 * @param dollars
	 *            Dollars
	 * @param cents
	 *            Cents. if the value is less than 0, we do not use cent
	 * @param useDotOnly
	 *            Only dollar and dot is used. Cents is not used.
	 */
	public void setTotalMoney(int dollars, int cents, boolean useDotOnly) {
		mDollars = dollars;
		mCents = cents;
		mUseDotOnly = useDotOnly;

		updateMoneyString();

		
	}
	public int getDollars() {
		return mDollars;
	}
	public int getCents() {
		return mCents;
	}
}
