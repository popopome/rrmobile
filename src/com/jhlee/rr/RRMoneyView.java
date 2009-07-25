package com.jhlee.zoomsample;

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

	private static final int DEFAULT_TEXT_SIZE_IN_160_DPI = 30;
	private static DisplayMetrics mDm = null;

	public static void initializeDisplayMetrics(DisplayMetrics dm) {
		mDm = dm;
	}

	private int mTextSize = DEFAULT_TEXT_SIZE_IN_160_DPI;
	private Typeface mFont;
	private Paint mPaint;
	private int mDollars;
	private int mCents;
	private boolean mUseDotOnly = false;
	private String mMoneyString;
	private Rect mBoundsRect = new Rect();

	/** CTOR */
	public RRMoneyView(Context ctx) {
		super(ctx);
		initializeResources(null);
		initializeTotalMoney();
	}

	public RRMoneyView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializeResources(attrs);
		initializeTotalMoney();
	}

	public RRMoneyView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initializeResources(attrs);
		initializeTotalMoney();
	}

	/**
	 * Initialize total money
	 */
	private void initializeTotalMoney() {
		setTotalMoney(0, -1, false);
	}

	/**
	 * Initialize various resource objects
	 */
	private void initializeResources(AttributeSet attrs) {
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
		if (null != attrs) {
			mTextSize = attrs.getAttributeIntValue(null, "text_size_in_160dpi",
					DEFAULT_TEXT_SIZE_IN_160_DPI);
		}

		int textSize = (int) (mDm.density * mTextSize);
		mPaint.setTextSize(textSize);

	}

	/**
	 * Draw Vertical center.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		int w = this.getWidth();
		int h = this.getHeight();

		int horzPadding = (int) mDm.density * 10;

		int boundsWidth = mBoundsRect.width();
		int boundsHeight = mBoundsRect.height();
		int sy = (h - boundsHeight) >> 1;
		int sx = (w - boundsWidth) - horzPadding;

		canvas.drawText(mMoneyString, sx, sy + boundsHeight, mPaint);

		/** For debugging */
		canvas.drawLine(sx, sy + boundsHeight, sx + boundsWidth, sy
				+ boundsHeight, mPaint);
	}

	/** 
	 * Update money string from dollar & cents 
	 */
	private void updateMoneyString() {
		mMoneyString = "$" + Integer.toString(mDollars);

		boolean dotExists = false;
		if (mUseDotOnly) {
			mMoneyString = mMoneyString + ".";
			dotExists = true;
		}
		if(mCents >= 0) {
			if(false == dotExists) {
				mMoneyString = mMoneyString + ".";
			}
			mMoneyString = mMoneyString + Integer.toString(mCents);
		}
	}

	/**
	 * Set total money
	 * @param dollars		Dollars
	 * @param cents			Cents. if the value is less than 0, we do not use cent
	 * @param useDotOnly	Only dollar and dot is used. Cents is not used.
	 */
	public void setTotalMoney(int dollars, int cents, boolean useDotOnly) {
		mDollars = dollars;
		mCents = cents;
		mUseDotOnly = useDotOnly;
		
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
		int horzPadding = (int) mDm.density * 10;
		int vertPadding = (int) mDm.density * 10;
		int minimumW = mBoundsRect.width() + horzPadding + horzPadding;
		int minimumH = mBoundsRect.height() + vertPadding + vertPadding;
		this.setMinimumWidth(minimumW);
		this.setMinimumHeight(minimumH);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.setMeasuredDimension(this.getMeasuredWidth(), minimumH);
	}
}
