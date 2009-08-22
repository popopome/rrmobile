package com.jhlee.rr;

import com.jhlee.rr.RRCarouselFlowView.RRCarouselItem;

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
import android.view.Window;
import android.widget.Button;
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
	
	private float mZoomRatioAtDown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Remove title bar */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		/* Set content */
		this.setContentView(R.layout.rr_receipt_detail);

		/** Get receipt id */
		Intent i = this.getIntent();
		if (null == i) {
			this.showErrorMessage("Intent should be given");
			return;
		}
		mRID = (int) i.getExtras().getLong(RECEIPT_ID, -1);
		if (mRID == -1) {
			this.showErrorMessage("Proper rid is not given");
			return;
		}

		/** Get receipt information */
		mDbAdapter = new RRDbAdapter(this);
		refreshDbCursor();

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
		final RRZoomView zoomView = (RRZoomView) this.findViewById(R.id.rr_zoomview);
		zoomView.setBitmap(bmp);

		/* Set up money text view */
		refreshMoneyViewText();

		/* Set date */
		refreshDateView();

		final RRReceiptDetailActivity self = this;

		/* Initialize back button */
		initializeBackButton(self);
		/* Initialize money button */
		initializeMoneyButton(self);
		/* Initialize date pick button */
		initializeDatePickButton(self);
		
		/* Connect zoom button view & zoom view */
		final RRZoomButtonView zoomBtnView = (RRZoomButtonView)this.findViewById(R.id.rr_zoombutton_view);
		zoomBtnView.setOnZoomButtonEventListener(new RRZoomButtonView.OnZoomButtonEventListener() {
			public void onZoomButtonBeforeMoving(View view) {
				mZoomRatioAtDown = zoomView.getCurrentZoomRatio();
			}
			public void onZoomButtonMoved(View view, boolean isPlusButton, long distance) {
				float zoomRatioOffset = (float) (1.0 * (distance / (float)view.getHeight()));
				zoomView.zoomTo(mZoomRatioAtDown + zoomRatioOffset);
				zoomView.invalidate();
			}
			public void onZoomButtonAfterMoving(View view) {
				zoomView.invalidate();
			}
		});
		
		/*
		 * Request layout again. Expect view size is changed with proper content
		 */
		View rootView = this.findViewById(R.id.receipt_detail_layout);
		rootView.requestLayout();
	}

	/**
	 * Refresh date view
	 */
	private void refreshDateView() {
		TextView dateView = (TextView) findViewById(R.id.date_view);
		dateView.setText(mCursor.getString(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));
		dateView.invalidate();
	}

	private void refreshMoneyViewText() {
		TextView moneyView = (TextView) findViewById(R.id.money_view);
		int total = mCursor.getInt(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));
		moneyView.setText(RRUtil.formatMoney(total / 100, total % 100, true));
		moneyView.invalidate();
	}

	private void initializeMoneyButton(final RRReceiptDetailActivity self) {
		Button moneyButton = (Button) this.findViewById(R.id.button_numpad);
		moneyButton.setOnClickListener(new Button.OnClickListener() {
			/* Money input button is clicked */
			public void onClick(View v) {
				/* Get total amount of money */
				int packedTotal = mCursor.getInt(mCursor
						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));

				/* Show money input dialog */
				final RRMoneyInputDialog inputDlg = new RRMoneyInputDialog(self);
				inputDlg.setMoney(packedTotal / 100, packedTotal % 100);
				inputDlg
						.setOnDismissListener(new DialogInterface.OnDismissListener() {
							public void onDismiss(DialogInterface dialog) {
								/*
								 * Money input dialog is dismissed. Let's save
								 * if we should do
								 */
								if (inputDlg.isCanceled())
									return;

								/* Insert new total money to db */
								int rid = mCursor.getInt(0);
								mDbAdapter.updateTotalMoney(rid, inputDlg
										.getDollars(), inputDlg.getCents());
								/* Refresh db items */
								self.refreshDbCursor();
								self.refreshMoneyViewText();
							}
						});
				inputDlg.show();
			}
		});
	}

	private void initializeBackButton(final RRReceiptDetailActivity self) {
		Button backButton = (Button) this.findViewById(R.id.back_button);
		backButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				/* Finish activity */
				self.finish();
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

	/*
	 * Refresh db cursor
	 */
	private void refreshDbCursor() {

		mCursor = mDbAdapter.queryReceipt((int) mRID);
		if (null == mCursor) {
			this.showErrorMessage("Unable to get db cursor");
			return;
		}
		this.startManagingCursor(mCursor);
	}
	
	private void initializeDatePickButton(final RRReceiptDetailActivity self) {
		Button datePickBtn = (Button)findViewById(R.id.button_date_pick);
		datePickBtn.setOnClickListener(new View.OnClickListener() {
			/*
			 * Date pick button is clicked. 
			 * Show date pick dialog & change date
			 */
			public void onClick(View v) {
				final RRCalendarSelectDialog dlg = new RRCalendarSelectDialog(self);
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						if(false == dlg.isDateSelected())
							return;
						/* Update db. */
						mDbAdapter.updateDate(
								mCursor,
								dlg.getSelectedDateInMillis());
						/* Refresh db cursor */
						self.refreshDbCursor();
						self.refreshDateView();
												
					}
				});
				
				/* Get date information */
				String dateStr = mCursor.getString(mCursor.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE));
				dlg.setActiveDate(dateStr);
				
				/* Show dialog */
				dlg.show();
			}
		});
	}
	
	
}
