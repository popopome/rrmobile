package com.jhlee.rr;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;

import com.jhlee.rr.RRCarouselFlowView.RRCarouselActiveItemClickListener;
import com.jhlee.rr.RRCarouselFlowView.RRCarouselItem;
import com.jhlee.rr.RRCarouselFlowView.RRCarouselItemCustomDrawer;

public class RRReceiptListActivity extends Activity implements
		RRCarouselItemCustomDrawer, RRCarouselActiveItemClickListener {
	private RRCarouselFlowView mView;
	private RRDbAdapter mAdapter;
	private Cursor mCursor;
	private HashMap<String, Bitmap> mBmpPool = new HashMap<String, Bitmap>();

	private Matrix mMatrixZoomToFit = new Matrix();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_carousel_receipt_list);

		/* Collect receipt data from database */
		mAdapter = new RRDbAdapter(this);
		if (mCursor != null) {
			mCursor.close();
		}
		mCursor = mAdapter.queryAllReceipts();
		int numOfReceipts = mCursor.getCount();
		this.startManagingCursor(mCursor);

		/* Initialize carousel view */
		RRCarouselFlowView view = (RRCarouselFlowView) findViewById(R.id.carouselView);
		view.initialize(numOfReceipts, 120, 160, 100, 9, 25);
		view.setFocusable(true);
		view.setActiveItemClickListener(this);

		view.setCarouselItemCustomDrawer(this);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	/**
	 * Custome draw for CAROUSL item
	 */
	public void onDraw(View view, Canvas canvas, RRCarouselItem item) {
		
		/* If image file name was not fetched from DB,
		 * get it.
		 */
		if (item.img_file_name == null) {
			mCursor.moveToFirst();
			mCursor.moveToPosition(item.seq);
			item.img_file_name = mCursor.getString(mCursor
					.getColumnIndex(RRDbAdapter.KEY_RECEIPT_SMALL_IMG_FILE));
		}

		/* Look up bitmap image from bitmap pool */
		Bitmap bmp = null;
		if (true == mBmpPool.containsKey(item.img_file_name))
			bmp = mBmpPool.get(item.img_file_name);
		else {
			/* Load bitmap from file */
			bmp = BitmapFactory.decodeFile(item.img_file_name);
			mBmpPool.put(item.img_file_name, bmp);
		}

		int x = item.x - (item.w >> 1);
		int y = item.y - (item.h >> 1);

		/* Compute zoom to fit matrix */
		int drawingH = item.h * 2 / 3;
		zoomToFit(x, y, item.w, drawingH, bmp, mMatrixZoomToFit);
		canvas.drawBitmap(bmp, mMatrixZoomToFit, null);
		
		/* Draw reflection image */
		float[] matValues = new float[9];
		mMatrixZoomToFit.getValues(matValues);
		mMatrixZoomToFit.postTranslate(-matValues[2], -matValues[5]);
		mMatrixZoomToFit.postScale(1.0f, -1.0f);
		mMatrixZoomToFit.postTranslate(matValues[2], matValues[5]+drawingH+drawingH);

		canvas.drawBitmap(bmp, mMatrixZoomToFit, null);
	}

	/**
	 * Update matrix in order to show bitmap on center
	 */
	private void zoomToFit(int dstX, int dstY, int dstW, int dstH,
			Bitmap srcBmp, Matrix mat) {
		RectF rcBmp = new RectF(0.0f, 0.0f, (float) srcBmp.getWidth(),
				(float) srcBmp.getHeight());
		RectF rcScreen = new RectF(dstX, dstY, dstX + dstW, dstY + dstH);

		mat.reset();
		mat.setRectToRect(rcBmp, rcScreen, Matrix.ScaleToFit.CENTER);
	}

	/**
	 * Item is clicked within carousel item
	 */
	public void onClicked(RRCarouselFlowView view, RRCarouselItem item) {
		mCursor.moveToPosition(item.seq);
		long rid = mCursor.getInt(0);
		
		/** See receipt list */
		Intent i = new Intent(this, RRReceiptDetailActivity.class);
		i.putExtra(RRReceiptDetailActivity.RECEIPT_ID, rid);
		this.startActivity(i);
	}
}
