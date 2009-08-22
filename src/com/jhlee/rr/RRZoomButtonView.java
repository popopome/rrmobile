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
import android.util.MonthDisplayHelper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

public class RRZoomButtonView extends View {

	private static final int DEFAULT_ALPHA = 64;

	/*
	 * Gesture handling class
	 */
	class ZoomButtonGesture extends SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			int alpha = mPaint.getAlpha() + (255 - mPaint.getAlpha()) / 3;
			alpha = Math.min(alpha, 255);
			mPaint.setAlpha(alpha);
			if (alpha == 255) {
				/** Stop fade in animation */
				mAniHandler.removeCallbacks(mButtonFadeTask);
			}

			/* Move plus or minus button */
			int curY = (int) e2.getY();
			int downY = (int) e1.getY();

			if (curY < downY) {
				int offset = downY - curY;
				mYPlus = downY - mButtonHeight - (offset * 80 / 100);
				mYMinus = downY;
			} else if (curY > downY) {
				int offset = curY - downY;
				mYMinus = downY + (offset * 80 / 100);
				mYPlus = downY - mButtonHeight;
			} else {
				mYPlus = curY - mButtonHeight;
				mYMinus = curY;
			}

			if (mButtonEventListener != null) {
				mButtonEventListener.onZoomButtonMoved(RRZoomButtonView.this,
						curY < downY ? true : false, downY - curY);
			}

			invalidate();
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {

			if (mButtonEventListener != null)
				mButtonEventListener
						.onZoomButtonBeforeMoving(RRZoomButtonView.this);

			int curY = (int) e.getY();
			mYPlus = curY - mButtonHeight;
			mYMinus = curY;

			/* Button fade in */
			buttonFadeIn();
			invalidate();
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			boolean isPlusButton = (e2.getY() < e1.getY());
			int pixels = (int) (0.05f * Math.abs(velocityY));
			/* Maximum pixels */
			pixels = Math.min(pixels, getHeight() / 32);
			flyButtons(isPlusButton, pixels, velocityY < 0 ? -1 : 1, (int) e1
					.getY());
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			buttonFadeOut();
			return true;
		}

	}

	/**
	 * Zoom moved button listener
	 */
	public interface OnZoomButtonEventListener {
		public void onZoomButtonBeforeMoving(View view);

		public void onZoomButtonMoved(View view, boolean isPlusButton, long l);

		public void onZoomButtonAfterMoving(View view);
	}

	private Bitmap mButtonBmp;
	private Paint mPaint;
	private int mYPlus;
	private int mYMinus;
	private int mButtonWidth;
	private int mButtonHeight;

	private Rect mSrcRect = new Rect();
	private Rect mDstRect = new Rect();

	private Handler mAniHandler = new Handler();
	private Runnable mFlyButtonTask = null;
	private Runnable mButtonFadeTask = null;
	private boolean mButtonFadeIn = true;

	private long mFlyButton_Pixels = 0;
	private long mFlyButton_Direction = 0;
	private long mFlyButton_CenterY = 0;
	private boolean mFlyButton_PlusButton;

	private GestureDetector mGesture = new GestureDetector(
			new ZoomButtonGesture());
	private OnZoomButtonEventListener mButtonEventListener = null;

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
		mButtonWidth = mButtonBmp.getWidth() / 2;
		mButtonHeight = mButtonBmp.getHeight();

		mPaint = new Paint();
		mPaint.setAlpha(DEFAULT_ALPHA);
	}

	/** Draw buttons */
	@Override
	protected void onDraw(Canvas canvas) {
		/** 2 is margin */
		int vw = getWidth();
		int xPos = vw - mButtonWidth - 2;

		drawPlusButton(canvas, xPos, mYPlus);
		drawMinusButton(canvas, xPos, mYMinus);
	}

	private void drawMinusButton(Canvas canvas, int xPos, int yPos) {
		mSrcRect.set(mButtonWidth, 0, mButtonBmp.getWidth(), mButtonBmp
				.getHeight());
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

		if (mGesture.onTouchEvent(e))
			return true;

		switch (e.getAction()) {
		case MotionEvent.ACTION_UP:
			if (mButtonEventListener != null)
				mButtonEventListener
						.onZoomButtonAfterMoving(RRZoomButtonView.this);
			buttonFadeOut();
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
					int oldAlpha = mPaint.getAlpha();
					int alpha = oldAlpha
							- (((oldAlpha) * 33) >> 7);
					mPaint.setAlpha(alpha);

					if (mButtonEventListener != null) {
						if (mFlyButton_PlusButton)
							mButtonEventListener.onZoomButtonMoved(
									RRZoomButtonView.this,
									mFlyButton_PlusButton, mFlyButton_CenterY
											- mYPlus);
						else
							mButtonEventListener.onZoomButtonMoved(
									RRZoomButtonView.this,
									mFlyButton_PlusButton, mFlyButton_CenterY
											- mYMinus);
					}

					invalidate();
					if (alpha == oldAlpha || alpha <= 0) {
						mPaint.setAlpha(0);
						if (mButtonEventListener != null)
							mButtonEventListener
									.onZoomButtonAfterMoving(RRZoomButtonView.this);
						mAniHandler.removeCallbacks(this);
						return;
					}

					mAniHandler.postAtTime(this,
							SystemClock.uptimeMillis() + 50);
				}
			};
		}

		mAniHandler.removeCallbacks(mButtonFadeTask);
		mAniHandler.removeCallbacks(mFlyButtonTask);
		mAniHandler.post(mFlyButtonTask);
	}

	/**
	 * Button fade in
	 */
	private void buttonFadeIn() {
		buttonFade(true);
	}

	private void buttonFadeOut() {
		buttonFade(false);
	}

	private void buttonFade(boolean fadeIn) {
		mButtonFadeIn = fadeIn;
		if (null == mButtonFadeTask) {
			mButtonFadeTask = new Runnable() {
				public void run() {
					/** Alpha is drecreased at 33/128 ratio */
					int dir = (mButtonFadeIn) ? 1 : -1;
					int alpha = mPaint.getAlpha() + dir * (((255 * 33) >> 7));
					alpha = Math.min(alpha, 255);
					alpha = Math.max(alpha, 0);
					mPaint.setAlpha(alpha);
					invalidate();
					if (alpha == 255)
						return;
					else if (alpha == 0)
						return;

					mAniHandler.postAtTime(this,
							SystemClock.uptimeMillis() + 50);
				}
			};
		}
		mAniHandler.removeCallbacks(mFlyButtonTask);
		mAniHandler.removeCallbacks(mButtonFadeTask);
		mAniHandler.post(mButtonFadeTask);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mButtonWidth, MeasureSpec
				.getSize(heightMeasureSpec));
	}

	/*
	 * Set event listener
	 */
	public void setOnZoomButtonEventListener(
			OnZoomButtonEventListener evtListener) {
		mButtonEventListener = evtListener;
	}
}
