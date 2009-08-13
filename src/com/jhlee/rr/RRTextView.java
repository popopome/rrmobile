package com.jhlee.rr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class RRTextView extends View {

	private static final int DEFAULT_TEXT_SIZE_IN_160_DPI = 18;
	private int mTextSize = DEFAULT_TEXT_SIZE_IN_160_DPI;
	private Typeface mFont;
	private Paint mPaint;
	private Rect mBoundsRect = new Rect();
	private OnClickListener mClickListener = null;

	private boolean mMouseDown = false;
	private boolean mMouseMovementInControl = false;
	private String mText = "";
	
	
	/** CTOR */
	public RRTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializeInternal(attrs);
	}

	public RRTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeInternal(attrs);
	}

	public RRTextView(Context context) {
		super(context);
		initializeInternal(null);
	}

	
	/**
	 * Initialize various resource objects
	 */
	private void initializeInternal(AttributeSet attrs) {
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
		/* Change DPI to pixel */
		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		int textSize = (int) (dm.density * mTextSize);
		mPaint.setTextSize(textSize);
		
		/* Set default text */
		this.setText(" ");
	}

	/**
	 * Draw Vertical center.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		int w = this.getWidth();
		int h = this.getHeight();

		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		int horzPadding = (int) dm.density * 10;

		int boundsWidth = mBoundsRect.width();
		int boundsHeight = mBoundsRect.height();
		
		int sx = 0;
		switch(mPaint.getTextAlign()) {
		case RIGHT:
			sx = w - horzPadding;
			break;
		case CENTER:
			sx = w / 2;
			break;
		default:
			sx = horzPadding;
			break;
		}
		int sy = (h - boundsHeight) >> 1;
		if(mMouseDown == true &&
				mMouseMovementInControl == true) {
			mPaint.setColor(Color.rgb(0, 100, 0));
		} else {
			mPaint.setColor(Color.WHITE);
		}
		canvas.drawText(mText, sx, sy + boundsHeight, mPaint);

		/** For debugging */
		canvas.drawLine(sx, sy + boundsHeight, sx + boundsWidth, sy
				+ boundsHeight, mPaint);
	}

	/**
	 * Update text
	 */
	public void setText(String text) {
		mText = text;
		
		/** Update bounds rectangle */
		if (mPaint != null) {
			mPaint.getTextBounds(mText, 0, mText.length(),
					mBoundsRect);
		}
		invalidate();
	}

	/** Measure view size */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		DisplayMetrics dm = this.getResources().getDisplayMetrics();

		/**
		 * Horizontal padding: 10 dpi Vertical padding: 10 dpi
		 */
		int horzPadding = (int) dm.density * 10;
		int vertPadding = (int) dm.density * 10;
		int minimumW = mBoundsRect.width() + horzPadding + horzPadding;
		int minimumH = mBoundsRect.height() + vertPadding + vertPadding;
		this.setMinimumWidth(minimumW);
		this.setMinimumHeight(minimumH);

		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		/*this.setMeasuredDimension(this.getMeasuredWidth(), minimumH);*/
		this.setMeasuredDimension(minimumW, minimumH);
	}

	/**
	 * Set click listener
	 */
	public void setOnClickListener(OnClickListener listener) {
		mClickListener = listener;
	}

	/**
	 * Touch event
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int x = (int) e.getX();
		int y = (int) e.getY();

		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mMouseDown = true;
			mMouseMovementInControl = true;
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMouseDown == true) {
				if (x < 0 || x > this.getWidth() || y < 0
						|| y > this.getHeight()) {
					mMouseMovementInControl = false;
				}
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			if(mMouseDown == true) {
				if(mMouseMovementInControl == true) {
					/* User has clicked the control */
					if(mClickListener != null)
						mClickListener.onClick(this);
				}
			}
			mMouseDown = false;
			mMouseMovementInControl = false;
			invalidate();
			break;
		}
		return true;
	}
	
	public void setTextHorzAlign(Paint.Align align) {
		if(mPaint != null) {
			mPaint.setTextAlign(align);
		}
	}
}
