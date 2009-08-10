package com.jhlee.rr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Handle detail information about receipt
 * 
 * @author popopome
 */
public class RRReceiptDetailActivity extends Activity {
	public static final String RECEIPT_ID = "rid";
	private static final String TAG = "RREditor";
	private RRDbAdapter mDbAdapter;
	private Cursor mCursor;
	private int mRID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.rr_receipt_detail);

		

		/** Get receipt id */
		Intent i = this.getIntent();
		if (null == i) {
			this.showErrorMessage("Intent should be given");
			return;
		}
		mRID = (int)i.getExtras().getLong(RECEIPT_ID, -1);
		if (mRID == -1) {
			this.showErrorMessage("Proper rid is not given");
			return;
		}

		/** Get receipt information */
		mDbAdapter = new RRDbAdapter(this);
		mCursor = mDbAdapter.queryReceipt((int) mRID);
		if (null == mCursor) {
			this.showErrorMessage("Unable to get db cursor");
			return;
		}
		this.startManagingCursor(mCursor);

		/** Load receipt image */
		int colIndexImgFile = mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_IMG_FILE);
		int colCount = mCursor.getColumnCount();
		String imgFilePath = mCursor.getString(colIndexImgFile);
		Bitmap bmp = BitmapFactory.decodeFile(imgFilePath);
		if (null == bmp) {
			this.showErrorMessage("Unable to load file:" + imgFilePath);
			return;
		}

		/* Set bitmap */
		RRZoomView zoomView = (RRZoomView) this.findViewById(R.id.rr_zoomview);
		zoomView.setBitmap(bmp);

		/* Set up money text view */
		final RRReceiptDetailActivity self = this;

		final RRMoneyTextView moneyView = (RRMoneyTextView) this
				.findViewById(R.id.rr_money_text_view);

		/* Set saved money amount */
		int encodedTotal = mCursor.getInt(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));
		int dollars = encodedTotal / 100;
		int cents = encodedTotal % 100;
		moneyView.setTotalMoney(dollars, cents, false);
		
		/* Set date */
		RRDateTextView dateTextView = (RRDateTextView)this.findViewById(R.id.rr_date);
		String takenDate = mCursor.getString(mCursor.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE));
		dateTextView.setText(takenDate);

		/* Set listener */
		moneyView.setOnClickListener(new View.OnClickListener() {
			/*
			 * Money text view is clicked. We will show money input pad dialog.
			 */
			public void onClick(View v) {
				RRMoneyPadDialog dlg = new RRMoneyPadDialog(self);
				dlg.setMoney(moneyView.getDollars(), moneyView.getCents());
				dlg
						.setOnDismissListener(new DialogInterface.OnDismissListener() {
							public void onDismiss(DialogInterface dialog) {
								RRMoneyPadDialog moneyDlg = (RRMoneyPadDialog) dialog;
								int dollars = moneyDlg.getDollars();
								int cents = moneyDlg.getCents();
								moneyView.setTotalMoney(dollars, cents, false);
								moneyView.invalidate();

								/* Save data to db */
								mDbAdapter.updateTotalMoney(mRID, dollars, cents);
							}
						});
				dlg.show();
			}
		});
	}

	private void showErrorMessage(String msg) {
		Log.e(TAG, msg);

		final Activity self = this;
		new AlertDialog.Builder(this).setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.error).setMessage(msg).setPositiveButton(
						R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								self.finish();
							}
						}).show();
		return;
	}

}
