package com.jhlee.rr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RRZoomButtonView extends View {

	private static final int DEFAULT_ALPHA = 64;

	private Bitmap mButtonBmp;
	private Paint mPaint;
	private int mYPlus;
	private int mYMinus;
	private int mButtonWidth;
	private int mButtonHeight;

	private int mMouseDownY;

	private long mMousePausedTime;
	private long mMouseLastTime;
	private int mMousePausedY;

	private Rect mSrcRect = new Rect();
	private Rect mDstRect = new Rect();

	private Handler mAniHandler = new Handler();
	private Runnable mFlyButtonTask = null;
	private Runnable mButtonFadeInTask = null;

	private long mFlyButton_Pixels = 0;
	private long mFlyButton_Direction = 0;
	private long mFlyButton_CenterY = 0;
	private boolean mFlyButton_PlusButton;

	/** CTOR */
	public RRZoomButtonView(Context ctx) {
		super(ctx);
		initialize(ctx);
	}

	public RRZoomButtonView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initialize(ctx);
	}

	public RRZoomButtonView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initialize(ctx);

	}

	private void initialize(Context ctx) {
		mButtonBmp = BitmapFactory.decodeResource(ctx.getResources(),
				R.drawable.zoom_button_icon);
		mButtonWidth = mButtonBmp.getWidth();
		mButtonHeight = mButtonBmp.getHeight() / 2;

		mPaint = new Paint();
		mPaint.setAlpha(DEFAULT_ALPHA);
	}

	/** Draw buttons */
	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);

		/** 2 is margin */
		int vw = getWidth();
		int xPos = vw - mButtonWidth - 2;

		drawPlusButton(canvas, xPos, mYPlus);
		drawMinusButton(canvas, xPos, mYMinus);
	}

	private void drawMinusButton(Canvas canvas, int xPos, int yPos) {
		mSrcRect.set(0, mButtonHeight, mButtonWidth, mButtonHeight
				+ mButtonHeight);
		mDstRect.set(xPos, yPos, xPos + mButtonWidth, yPos + mButtonHeight);
		canvas.drawBitmap(mButtonBmp, mSrcRect, mDstRect, mPaint);
	}

	private void drawPlusButton(Canvas canvas, int xPos, int yPos) {
		mSrcRect.set(0, 0, mButtonWidth, mButtonHeight);
		mDstRect.set(xPos, yPos, xPos + mButtonWidth, yPos + mButtonHeight);
		canvas.drawBitmap(mButtonBmp, mSrcRect, mDstRect, mPaint);
	}

	/** Size is changed */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mYPlus = h / 2 - mButtonHeight;
		mYMinus = h / 2;
	}

	/** Touch event */
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int curX = (int) e.getX();
		int curY = (int) e.getY();

		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			/* Button fade in */
			buttonFadeIn();

			mYPlus = curY - mButtonHeight;
			mYMinus = curY;
			mMouseDownY = curY;
			mMousePausedY = curY;
			mMousePausedTime = SystemClock.uptimeMillis();
			mMouseLastTime = mMousePausedTime;

			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			long curTime = SystemClock.uptimeMillis();
			boolean isMovementPaused = (curTime - mMouseLastTime) > 123;
			if (isMovementPaused) {
				mMousePausedTime = curTime;
				mMousePausedY = curY;
			}
			mMouseLastTime = curTime;

			int alpha = mPaint.getAlpha() + (255 - mPaint.getAlpha()) / 3;
			alpha = Math.min(alpha, 255);
			mPaint.setAlpha(alpha);
			if (alpha == 255) {
				/** Stop fade in animation */
				mAniHandler.removeCallbacks(mButtonFadeInTask);
			}

			/* Move plus button */
			if (curY < mMouseDownY) {
				int offset = mMouseDownY - curY;
				mYPlus = mMouseDownY - mButtonHeight - (offset * 80 / 100);
				mYMinus = mMouseDownY;
			} else if (curY > mMouseDownY) {
				int offset = curY - mMouseDownY;
				mYMinus = mMouseDownY + (offset * 80 / 100);
				mYPlus = mMouseDownY - mButtonHeight;
			} else {
				mYPlus = curY - mButtonHeight;
				mYMinus = curY;
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			long t = Math.max(5, SystemClock.uptimeMillis() - mMousePausedTime);
			int dy = Math.abs(curY - mMousePausedY);
			if (dy < 20)
				dy = 0;

			/** If dy is enough big, */
			/** speed * 20ms */
			long pixels = Math.abs(dy * 20 / t);
			if (pixels > getHeight() / 3)
				pixels = getHeight() / 3;

			boolean isPlusButton = (curY < mMouseDownY);
			flyButtons(isPlusButton, pixels, curY < mMousePausedY ? -1 : 1,
					mMouseDownY);
			break;
		}
		return true;
	}

	/** Fly buttons */
	private void flyButtons(boolean plusButton, long pixels, long direction,
			long yPos) {
		mFlyButton_Pixels = pixels;
		mFlyButton_Direction = direction;
		mFlyButton_CenterY = yPos;
		mFlyButton_PlusButton = plusButton;

		if (null == mFlyButtonTask) {
			mFlyButtonTask = new Runnable() {
				
				public void run() {
					mFlyButton_Pixels = (mFlyButton_Pixels * 850) >> 10;
					if (mFlyButton_PlusButton) {
						mYPlus = mYPlus
								+ (int) (mFlyButton_Direction * mFlyButton_Pixels);
						if (mYPlus + mButtonHeight >= mFlyButton_CenterY)
							mYPlus = (int) mFlyButton_CenterY - mButtonHeight;
						if (mYPlus < 0)
							mYPlus = 0;
					} else {
						mYMinus = mYMinus
								+ (int) (mFlyButton_Direction * mFlyButton_Pixels);
						if (mYMinus <= mFlyButton_CenterY)
							mYMinus = (int) mFlyButton_CenterY;
						if (mYMinus + mButtonHeight > getHeight())
							mYMinus = getHeight() - mButtonHeight;
					}

					/** Alpha is drecreased at 33/128 ratio */
					int alpha = mPaint.getAlpha()
							- (((mPaint.getAlpha()) * 33) >> 7);
					mPaint.setAlpha(alpha);

					invalidate();
					if (alpha < 1) {
						mAniHandler.removeCallbacks(this);
						return;
					}

					mAniHandler.postAtTime(this,
							SystemClock.uptimeMillis() + 50);
				}
			};
		}

		mAniHandler.removeCallbacks(mButtonFadeInTask);
		mAniHandler.removeCallbacks(mFlyButtonTask);
		mAniHandler.post(mFlyButtonTask);
	}

	/**
	 * Button fade in
	 */
	private void buttonFadeIn() {
		if (null == mButtonFadeInTask) {
			mButtonFadeInTask = new Runnable() {

				public void run() {
					/** Alpha is drecreased at 33/128 ratio */
					int alpha = mPaint.getAlpha() + ((255 * 33) >> 7);
					alpha = Math.min(alpha, 255);
					mPaint.setAlpha(alpha);
					invalidate();
					if (alpha == 255)
						return;

					mAniHandler.postAtTime(this,
							SystemClock.uptimeMillis() + 50);
				}
			};
		}

		mAniHandler.removeCallbacks(mFlyButtonTask);
		mAniHandler.removeCallbacks(mButtonFadeInTask);
		mAniHandler.post(mButtonFadeInTask);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mButtonWidth, MeasureSpec
				.getSize(heightMeasureSpec));
	}

}
