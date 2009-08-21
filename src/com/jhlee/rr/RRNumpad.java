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

public class RRNumpad extends View {

	public static final int SYMBOL_EMPTY = -1;
	public static final int SYMBOL_0 = 0;
	public static final int SYMBOL_1 = 1;
	public static final int SYMBOL_2 = 2;
	public static final int SYMBOL_3 = 3;
	public static final int SYMBOL_4 = 4;
	public static final int SYMBOL_5 = 5;
	public static final int SYMBOL_6 = 6;
	public static final int SYMBOL_7 = 7;
	public static final int SYMBOL_8 = 8;
	public static final int SYMBOL_9 = 9;
	public static final int SYMBOL_CANCEL = 10;
	public static final int SYMBOL_OK = 11;
	public static final int SYMBOL_CLEAR = 12;
	public static final int SYMBOL_BACK = 13;
	public static final int SYMBOL_DOT = 14;

	public interface OnNumPadClickListener {
		public void onNumPadClicked(View view, int symbol);
	}

	private static final String TAG = "RRMoneyPad";

	private static final int COL_COUNT = 4;
	private static final int ROW_COUNT = 4;

	/* Symbol width & height. */
	private static final int SYMBOL_WIDTH = 64;
	private static final int SYMBOL_HEIGHT = 64;
	/* View width is static */
	private static final int NUMPAD_WIDTH = SYMBOL_WIDTH * COL_COUNT;
	private static final int NUMPAD_HEIGHT = SYMBOL_HEIGHT * ROW_COUNT;

	/*
	 * ---------------------------------------------------------------------
	 * MEMBER VARIABLES
	 * ---------------------------------------------------------------------
	 */
	private Paint mPaint;
	private int mBtnW = 0;
	private int mBtnH = 0;

	private int mMouseDownSymbol = -1;
	private int mHitSymbol = -1;

	private OnNumPadClickListener mClickListener = null;

	private Bitmap mSymbolBmp;
	private int[] mSymbolList = new int[] { SYMBOL_1, SYMBOL_2, SYMBOL_3,
			SYMBOL_BACK, SYMBOL_4, SYMBOL_5, SYMBOL_6, SYMBOL_CLEAR, SYMBOL_7,
			SYMBOL_8, SYMBOL_9, SYMBOL_CANCEL, SYMBOL_EMPTY, SYMBOL_0,
			SYMBOL_DOT, SYMBOL_OK };

	/** CTOR */
	public RRNumpad(Context ctx) {
		super(ctx);
		initializeResources(ctx);
	}

	public RRNumpad(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializeResources(ctx);
	}

	public RRNumpad(Context ctx, AttributeSet attrs, int defStyle) {
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

		/*
		 * Initialize symbol bitmap Symbol bitmap has all bitmap resource to
		 * draw the pad
		 */
		mSymbolBmp = BitmapFactory.decodeResource(ctx.getResources(),
				R.drawable.numpad_symbols);
	}

	/*
	 * Draw numpad with various symbol.
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		int vw = this.getWidth();
		int vh = this.getHeight();
		int btnx = 0;
		int btny = 0;

		/* Prepare source & dest rectangle */
		Rect srcRect = new Rect();
		Rect dstRect = new Rect();
		srcRect.top = 0;
		srcRect.bottom = SYMBOL_HEIGHT;

		/* 3x4 pad */
		int symbolIndex = 0;
		boolean focused = false;
		for (int row = 0; row < ROW_COUNT; ++row) {
			btnx = 0;
			for (int col = 0; col < COL_COUNT; ++col) {

				/* Draw symbol at here */
				int symbol = mSymbolList[symbolIndex];
				focused = (symbol == mHitSymbol)
						&& (mHitSymbol != SYMBOL_EMPTY);
				int symbolX = getSymbolX(symbol, focused);

				srcRect.left = symbolX;
				srcRect.right = symbolX + SYMBOL_WIDTH;

				dstRect.set(btnx, btny, btnx + mBtnW, btny + mBtnH);
				canvas.drawBitmap(mSymbolBmp, srcRect, dstRect, mPaint);

				/* Advance next drawing position & symbol index */
				btnx += mBtnW;
				++symbolIndex;
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
			int hitSymbol = this.hitTest(x, y);
			if (hitSymbol != -1) {
				mMouseDownSymbol = hitSymbol;
				mHitSymbol = hitSymbol;
				switch (mHitSymbol) {
				/* Those symbol will be checked in touch up event later */
				case SYMBOL_OK:
				case SYMBOL_CANCEL:
				case SYMBOL_CLEAR:
					break;
				default:
					if (mClickListener != null) {
						mClickListener.onNumPadClicked(this, mHitSymbol);
					}
				}
				this.invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMouseDownSymbol != -1) {
				mHitSymbol = this.hitTest(x, y);
				this.invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mMouseDownSymbol != -1) {
				mHitSymbol = this.hitTest(x, y);
				/* When touch goes up, only ok/cancel command is checked.
				 * Any other buttons is alreay checked in mouse down case.
				 */
				if (mHitSymbol == SYMBOL_OK || mHitSymbol == SYMBOL_CANCEL || mHitSymbol == SYMBOL_CLEAR) {
					/* If symbol is clicked, */
					if (mHitSymbol == mMouseDownSymbol) {
						if (mClickListener != null) {
							mClickListener.onNumPadClicked(this, mHitSymbol);
						}
					}
				}
			}
			mHitSymbol = -1;
			mMouseDownSymbol = -1;
			this.invalidate();
		}

		return true;
	}

	/**
	 * Hit test. Return symbol if succeeded. Else return -1;
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

		int symbolIndex = row * COL_COUNT + col;
		if (symbolIndex >= mSymbolList.length)
			return -1;

		return mSymbolList[symbolIndex];
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
	 * Measure numpad width & height
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.setMeasuredDimension(NUMPAD_WIDTH, NUMPAD_HEIGHT);
	}

	/**
	 * Get symbol X
	 * 
	 * @param symbol
	 * @return
	 */
	private int getSymbolX(int symbol, boolean focused) {
		return symbol * SYMBOL_WIDTH
				+ (focused ? mSymbolBmp.getWidth() / 2 : 0);
	}

	/**
	 * Set click listener
	 * 
	 * @param listener
	 */
	public void setOnNumPadClickListener(OnNumPadClickListener listener) {
		mClickListener = listener;
	}
}
