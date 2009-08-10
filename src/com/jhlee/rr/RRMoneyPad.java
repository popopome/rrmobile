package com.jhlee.rr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class RRMoneyPad extends View {

	public static final int BTN_1 = 0;
	public static final int BTN_2 = 1;
	public static final int BTN_3 = 2;
	public static final int BTN_CLEAR = 3;
	public static final int BTN_4 = 4;
	public static final int BTN_5 = 5;
	public static final int BTN_6 = 6;
	public static final int BTN_CANCEL = 7;
	public static final int BTN_7 = 8;
	public static final int BTN_8 = 9;
	public static final int BTN_9 = 10;
	public static final int BTN_ENTER = 11;
	public static final int BTN_DOT = 12;
	public static final int BTN_0 = 13;
	public static final int BTN_BACK = 14;

	public interface OnMoneyChangedListener {
		public void onMoneyChanged(int val, int scale);
	}

	public interface OnMoneyPadCommandListener {
		public void onCommandButtonClicked(int btnCommand);
	}

	private static final String TAG = "RRMoneyPad";
	private static final int COL_COUNT = 4;
	private static final int ROW_COUNT = 4;
	private static final int MAX_BUTTON_INDEX = (COL_COUNT * ROW_COUNT) - 1;

	/*
	 * ---------------------------------------------------------------------
	 * MEMBER VARIABLES
	 * ---------------------------------------------------------------------
	 */
	private Paint mPaint;
	private NinePatch mNumBtnBgPatch;
	private NinePatch mNumBtnSelBgPatch;

	private Rect mDrawingRect = new Rect();
	private Rect mBoundsRect = new Rect();
	private String[] mBtnText = new String[] { "1", "2", "3", "Clear", "4",
			"5", "6", "Cancel", "7", "8", "9", "Enter", ".", "0", "Back", " " };
	private int[] mBtnValues = new int[] { 1, 2, 3, -1, 4, 5, 6, -2, 7, 8, 9,
			-3, -4, 0, -5, -6 };
	private Typeface mFont;

	private int mBtnW = 0;
	private int mBtnH = 0;

	private int mMouseDownBtnIndex = -1;
	private int mHittedBtnIndex = -1;

	private int mValue = 0;
	private int mScale = 0;

	private OnMoneyChangedListener mChangeListener = null;
	private OnMoneyPadCommandListener mCommandListener = null;

	/** CTOR */
	public RRMoneyPad(Context ctx) {
		super(ctx);
		initializeResources(ctx);
	}

	public RRMoneyPad(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializeResources(ctx);
	}

	public RRMoneyPad(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initializeResources(ctx);
	}

	/**
	 * Initialize all resources
	 * 
	 * @param ctx
	 */
	private void initializeResources(Context ctx) {
		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		mPaint.setAntiAlias(true);

		Bitmap bm = BitmapFactory.decodeResource(ctx.getResources(),
				R.drawable.numpad);
		byte[] chunk = bm.getNinePatchChunk();
		mNumBtnBgPatch = new NinePatch(bm, chunk, null);
		bm = null;
		chunk = null;

		bm = BitmapFactory.decodeResource(ctx.getResources(),
				R.drawable.numpad_sel);
		chunk = bm.getNinePatchChunk();
		mNumBtnSelBgPatch = new NinePatch(bm, chunk, null);

		mFont = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
		mPaint.setTypeface(mFont);
	}

	/** Draw num pad with nine-path image */
	@Override
	protected void onDraw(Canvas canvas) {

		int vw = this.getWidth();
		int vh = this.getHeight();

		int col = 0;
		int row = 0;
		int btnx = 0;
		int btny = 0;

		/** Set font size with btn width */
		int textSize = Math.max(20, Math.min(mBtnW, mBtnH) - 20);
		mPaint.setTextSize(textSize);

		int textW = 0;
		int textH = 0;
		int btnIndex = 0;
		for (row = 0; row < ROW_COUNT; ++row) {
			btnx = 0;
			for (col = 0; col < COL_COUNT; ++col, ++btnIndex) {
				mDrawingRect.left = btnx;
				mDrawingRect.top = btny;
				mDrawingRect.right = btnx + mBtnW;
				mDrawingRect.bottom = btny + mBtnH;

				/** Draw basic background */
				if (btnIndex == mHittedBtnIndex
						&& mHittedBtnIndex == mMouseDownBtnIndex) {
					mDrawingRect.offset(1, 1);
					mNumBtnSelBgPatch.draw(canvas, mDrawingRect);
				} else {
					mNumBtnBgPatch.draw(canvas, mDrawingRect);
				}

				mPaint.getTextBounds(mBtnText[btnIndex], 0, 1, mBoundsRect);
				textW = mBoundsRect.width();
				textH = mBoundsRect.height();

				canvas.drawText(mBtnText[row * COL_COUNT + col], mDrawingRect
						.centerX()
						- textW / 2,
						mDrawingRect.centerY() - textH / 2 + textH, mPaint);
				btnx += mBtnW;
			}
			btny += mBtnH;
		}
	}

	/**
	 * Mouse touch
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int btnIndex = this.hitTest(x, y);
			if (btnIndex != -1) {
				mMouseDownBtnIndex = btnIndex;
				mHittedBtnIndex = btnIndex;

				this.onBtnPressed(mHittedBtnIndex);
				this.invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMouseDownBtnIndex != -1) {
				mHittedBtnIndex = this.hitTest(x, y);
				this.invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			mHittedBtnIndex = -1;
			mMouseDownBtnIndex = -1;
			this.invalidate();
		}

		return true;
	}

	/**
	 * Hit test
	 * 
	 * @param x
	 * @param y
	 * @return button index
	 */
	private int hitTest(int x, int y) {
		if (x < 0 || y < 0)
			return -1;

		int col = x / mBtnW;
		int row = y / mBtnH;

		int btnIndex = row * COL_COUNT + col;
		if (btnIndex > MAX_BUTTON_INDEX)
			return -1;

		return btnIndex;
	}

	/**
	 * View size is changed
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mBtnW = w / COL_COUNT;
		mBtnH = h / ROW_COUNT;
	}

	/**
	 * Numpad button is pressed
	 * 
	 * @param btnIndex
	 */
	private void onBtnPressed(int btnIndex) {
		switch (btnIndex) {
		case BTN_1:
		case BTN_2:
		case BTN_3:
		case BTN_4:
		case BTN_5:
		case BTN_6:
		case BTN_7:
		case BTN_8:
		case BTN_9:
			mValue *= 10;
			mValue += mBtnValues[btnIndex];
			mScale *= 10;
			break;
		/* . is pressed */
		case BTN_DOT:
			if (mScale == 0)
				mScale = 1;
			break;
		/* 0 key is pressed */
		case BTN_0:
			mValue *= 10;
			mScale *= 10;
			break;
		/* Back is pressed */
		case BTN_BACK:
			mValue /= 10;
			mScale /= 10;
			break;
		/* Clear data */
		case BTN_CLEAR:
			this.clearData();
			break;
		/* Cancel or Enter */
		case BTN_CANCEL:
		case BTN_ENTER:
			if (null != mCommandListener)
				mCommandListener.onCommandButtonClicked(btnIndex);
			break;
		}

		Log.v(TAG, "mValue: " + mValue + " mScale: " + mScale);

		/* Notify money changing event */
		if (null != mChangeListener) {
			mChangeListener.onMoneyChanged(mValue, mScale);
		}
	}

	/**
	 * Clear internal data
	 */
	public void clearData() {
		mValue = 0;
		mScale = -1;
	}

	/**
	 * Set money change listener
	 * 
	 * @param listener
	 */
	public void setMoneyChangeListener(OnMoneyChangedListener listener) {
		mChangeListener = listener;
	}

	/**
	 * Set command listener
	 * 
	 * @param listener
	 */
	public void setMoneyPadCommandListener(OnMoneyPadCommandListener listener) {
		mCommandListener = listener;
	}
	
}
