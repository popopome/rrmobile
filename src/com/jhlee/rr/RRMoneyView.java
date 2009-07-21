package com.jaeho.funui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class RRMoneyView extends View {

	private static final int	DEFAULT_TEXT_SIZE_IN_160_DPI	=	30;
	
	private Typeface mFont;
	private Paint mPaint;
	private int mDollars;
	private int mCents;
	private String mMoneyString;
	private Rect mBoundsRect = new Rect();

	/** CTOR */
	public RRMoneyView(Context ctx) {
		super(ctx);
		initializePaint(null);
		initializeData();
	}

	public RRMoneyView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializePaint(attrs);
		initializeData();
	}

	public RRMoneyView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initializePaint(attrs);
		initializeData();
	}

	private void initializeData() {
		setMoney(0, 0);
	}

	/** Initialize paint object */
	private void initializePaint(AttributeSet attrs) {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.WHITE);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(1);

		mFont = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
		mPaint.setTypeface(mFont);

		/** Determine text size with DPI value */
		int textSizeWith160Dpi = DEFAULT_TEXT_SIZE_IN_160_DPI;
		if (null != attrs) {
			textSizeWith160Dpi = attrs.getAttributeIntValue(null, "text_size_in_160dpi",
					textSizeWith160Dpi);
		}

		DisplayMetrics dm = new DisplayMetrics();
		((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
		int testSizeInPixel = (int) (dm.scaledDensity * textSizeWith160Dpi);
		mPaint.setTextSize(testSizeInPixel);
		dm = null;
	}

	/**
	 * Draw Vertical center.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		int w = this.getWidth();
		int h = this.getHeight();
		
		int boundsWidth = mBoundsRect.width();
		int boundsHeight = mBoundsRect.height();
		int sy = (h - boundsHeight) >> 1;
		int sx = (w - boundsWidth)>>1;

		canvas.drawText(mMoneyString, sx, sy + boundsHeight, mPaint);
		
		/** For debugging */
		canvas.drawLine(sx, sy+boundsHeight, sx+boundsWidth, sy+boundsHeight, mPaint);
	}

	/** Update money string from dollar & cents */
	private void updateMoneyString() {
		mMoneyString = "$" + Integer.toString(mDollars) + "."
				+ Integer.toString(mCents);
	}

	/** Set money */
	public void setMoney(int dollars, int cents) {
		mDollars = dollars;
		mCents = cents;

		updateMoneyString();

		/** Update bounds rectangle */
		if (mPaint != null) {
			mPaint.getTextBounds(mMoneyString, 0, mMoneyString.length(),
					mBoundsRect);
		}
	}

	/** Measure view size */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/**
		 * Horizontal padding: 10 dpi Vertical padding: 10 dpi
		 */
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int horzPadding = (int) dm.density * 10;
		int vertPadding = (int) dm.density * 10;
		dm = null;

		this.setMeasuredDimension(mBoundsRect.width() + horzPadding
						+ horzPadding, mBoundsRect.height() + vertPadding
						+ vertPadding);
	}
}
