package com.jhlee.rr;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class RRMoneyInputDialog extends Dialog {

	private class MoneySymbolHandler {
		String mMoneyText = "";

		void reset() {
			mMoneyText = "";
		}

		void insert(int symbol) {
			if (symbol == RRNumpad.SYMBOL_BACK) {
				if (mMoneyText.length() > 0) {
					mMoneyText = mMoneyText.substring(0,
							mMoneyText.length() - 1);
				}
				return;
			}

			if (symbol == RRNumpad.SYMBOL_CLEAR) {
				this.reset();
				return;
			}
			if (symbol == RRNumpad.SYMBOL_DOT) {
				/*
				 * If dot is already inserted, we donot need duplicated dot
				 * value
				 */
				if (true == mMoneyText.contains(".")) {
					return;
				}

				if (mMoneyText.length() == 0) {
					/* Empty string. We attach 0 before dot. */
					mMoneyText = "0.";
				} else {
					mMoneyText += ".";
				}
				return;
			}
			if (symbol >= RRNumpad.SYMBOL_0 && symbol <= RRNumpad.SYMBOL_9) {
				/*
				 * If dot is already inserted, program only accepts 1/100
				 * precision.
				 */
				if (true == mMoneyText.contains(".")) {
					int pos = mMoneyText.lastIndexOf('.');
					int numOfCentsChar = mMoneyText.length() - pos;
					if (numOfCentsChar > 2) {
						/* We cannot accept more */
						return;
					}
				} else if (mMoneyText.length() == 1
						&& mMoneyText.charAt(0) == '0') {
					/*
					 * If first character was zero, we do not need to append new
					 * character. Here we just replace it with new character.
					 */
					mMoneyText = "" + (char) (((int) '0') + symbol);
					return;
				}

				mMoneyText += (char) (((int) '0') + symbol);
				return;
			}
		}

		public String getMoneyString() {
			return mMoneyText;
		}
	}

	private MoneySymbolHandler mMoneySymbolHandler = new MoneySymbolHandler();
	private String mMoneyString = "";
	private boolean mIsCanceled = false;

	/** CTOR */
	public RRMoneyInputDialog(Context ctx) {
		super(ctx);

		/* Start with no title */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* Initialize dialog */
		this.setContentView(R.layout.rr_money_input_dialog);

		/* Money text view */
		final TextView moneyTextView = (TextView) this
				.findViewById(R.id.money_text);
		moneyTextView.setGravity(0x05 | 0x10);

		/* Set money pad command listener */
		final RRMoneyInputDialog dlg = this;
		RRNumpad numpad = (RRNumpad) findViewById(R.id.numpad);
		numpad.setOnNumPadClickListener(new RRNumpad.OnNumPadClickListener() {
			public void onNumPadClicked(View view, int symbol) {
				if (symbol == RRNumpad.SYMBOL_CANCEL) {
					dlg.cancel();
					mIsCanceled = true;
					return;
				}
				if (symbol == RRNumpad.SYMBOL_OK) {
					dlg.dismiss();
					mIsCanceled = false;
					return;
				}
				mMoneySymbolHandler.insert(symbol);
				mMoneyString = mMoneySymbolHandler.getMoneyString();
				if (mMoneyString.length() == 0)
					moneyTextView.setText("");
				else
					moneyTextView.setText("$" + mMoneyString);
				moneyTextView.invalidate();
			}
		});
	}

	/**
	 * Set money
	 * 
	 * @param dollars
	 * @param cents
	 */
	public void setMoney(int dollars, int cents) {
		/* 0.0 money means empty data */
		if(dollars == 0 && cents == 0) {
			mMoneyString = "";
			return;
		}
		
		mMoneyString = RRUtil.formatMoney(dollars, cents, false);
		TextView moneyView = (TextView) findViewById(R.id.money_text);
		moneyView.setText("$" + mMoneyString);
	}

	public int getDollars() {
		/* Do not use first $ */
		return new Double(mMoneyString).intValue();
	}

	public int getCents() {
		int pos = mMoneyString.indexOf('.');
		if(-1 == pos)
			return 0;
		
		String centsStr = mMoneyString.substring(pos+1);
		if(centsStr.length() == 0 || centsStr == null)
			return 0;
		
		Integer cents = new Integer(centsStr);
		/*
		if(centsStr.length() == 1) {
			cents = cents * 10;
		}
		*/
		
		return cents;
	}
	
	/**
	 * Check dialog's cancel action
	 * @return
	 */
	public boolean isCanceled() {
		return mIsCanceled;
	}
}