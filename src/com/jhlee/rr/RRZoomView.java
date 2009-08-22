package com.jhlee.rr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Zoom view
 * 
 * @author popopome
 * 
 */
public class RRZoomView extends View {
	private static final int OP_MODE_NONE = 0;
	private static final int OP_MODE_ZOOM = 1;
	private static final int OP_MODE_PAN = 2;

	private Bitmap mBmp = null;
	private Matrix mMat = new Matrix();
	private Matrix mInvMat = new Matrix();
	private float[] mRawMatElems = new float[9];

	private int mMouseLastX = 0;
	private int mMouseLastY = 0;

	private int mOpMode = OP_MODE_NONE;
	private RectF mBmpRectMapped = new RectF();

	/** Double-tap detection */
	private long mLastTouchTime = -1;
	private float mZoomMinRatio = 0.0f;
	private float mZoomFillRatio = 0.0f;
	
	private float[] mAlignOffset = new float[]{0.0f, 0.0f};
	private Handler mAniHandler = new Handler();
	private Runnable mPanAndZoomAniTask =null;
	
	private Runnable mZoomAniTask = null;
	private boolean  mDoubleTapZoomIn = false;
	
	private Paint mPaint = null;

	/** CTOR */
	public RRZoomView(Context ctx) {
		this(ctx, null);
	}
	public RRZoomView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializePaint();

		initializePanAndZoomAnimationTask();
		initializeZoomAnimationTask();
	}
	
	public RRZoomView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initializePaint();
		initializePanAndZoomAnimationTask();
		initializeZoomAnimationTask();
	}
	
	private void initializePaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(false);
		mPaint.setDither(false);
	}
	private void initializeZoomAnimationTask() {
		mZoomAniTask = new Runnable() {
			public void run() {
				float cur = getCurrentZoomRatio();
				if(mDoubleTapZoomIn) {
					float offset = (mZoomFillRatio - cur)*35/100;
					if(offset < 0.01f) {
						zoomTo(mZoomFillRatio);
						align();
						invalidate();
						return;
					}
					
					float zoomRatio = cur + offset;
					zoomTo(zoomRatio);
				} else {
					/** Zoom out to fit-to-screen */
					float offset = (cur - mZoomMinRatio)*35/100;
					if(offset<0.01f) {
						zoomToFit();
						align();
						invalidate();
						return;
					}
					
					float zoomRatio = cur - offset;
					zoomTo(zoomRatio);
					
				}
				
				align();
				invalidate();
				mAniHandler.postAtTime(this, SystemClock.uptimeMillis() + 50);
			}
		};
	}
	private void initializePanAndZoomAnimationTask() {
		mPanAndZoomAniTask = new Runnable() {
			public void run() {
				boolean result1 = alignStep();
				boolean result2 = zoomStep();
				if(result1 == false || result2 == false) {
					mAniHandler.postAtTime(this, SystemClock.uptimeMillis() + 50);
				}
			}
			private boolean zoomStep() {
				if(false == isZoomRatioLessThanMinZoom())
					return true;
				
				float curZoomRatio = getCurrentZoomRatio();
				float offset = (mZoomMinRatio - curZoomRatio)*15/100;
				if(offset < 0.01f) {
					zoomToFit();
					invalidate();
					return true;
				}
				
				zoomTo(curZoomRatio + offset);
				invalidate();
				return false;
			}
			private boolean alignStep() {
				computeAlignOffset(mAlignOffset);
				
				mAlignOffset[0] = mAlignOffset[0] * 15/100;
				mAlignOffset[1] = mAlignOffset[1] * 15/100;
				if((Math.abs(mAlignOffset[0])<1.0f) &&
				   (Math.abs(mAlignOffset[1])<1.0f)) {
					mAniHandler.removeCallbacks(mPanAndZoomAniTask);
					align();
					invalidate();
					return true;
				}
				
				panRel(mAlignOffset[0], mAlignOffset[1]);
				invalidate();
				return false;
			}
		};
	}

	/** Set bitmap */
	public void setBitmap(Bitmap bmp) {
		mBmp = bmp;
	}

	/** Draw image */
	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setDither(true);
		canvas.drawBitmap(mBmp, mMat, mPaint);
	}

	/** Touch event */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int curX = (int) event.getX();
		int curY = (int) event.getY();

		int viewW = this.getWidth();
		int viewH = this.getHeight();

		final int ZOOM_AREA_HORZ_WIDTH = 40;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			long thisTime = SystemClock.uptimeMillis();
			
			/** Double tap check */
			/*
			if(thisTime - mLastTouchTime < 250) {
				if(getCurrentZoomRatio() == mZoomMinRatio) {
					animationZoomIn();
				} else {
					animationZoomOut();
				}
			} else 
			*/
			if ((curX >= viewW - ZOOM_AREA_HORZ_WIDTH) && (curX <= viewW)) {
				mOpMode = OP_MODE_ZOOM;
				mMouseLastX = curX;
				mMouseLastY = curY;
			} else {
				mOpMode = OP_MODE_PAN;
				mMouseLastX = curX;
				mMouseLastY = curY;
			}
			
			mLastTouchTime = thisTime;
			break;
		case MotionEvent.ACTION_MOVE:
			if (OP_MODE_ZOOM == mOpMode) {
				if(getCurrentZoomRatio() > mZoomMinRatio*2/3) {
					int dy = mMouseLastY - curY;
					zoomRel(1.0f + dy*0.005f);
				} else {
					animationToAlign();
				}
				align();
				mMouseLastX = curX;
				mMouseLastY = curY;
				invalidate();
			} else if (OP_MODE_PAN == mOpMode) {
				panRel(curX - mMouseLastX, curY - mMouseLastY);
				mMouseLastX = curX;
				mMouseLastY = curY;
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if(OP_MODE_ZOOM == mOpMode) {
				animationToAlign();
			} else if(OP_MODE_PAN == mOpMode) {
				animationToAlign();
			}
			mOpMode = OP_MODE_NONE;
			break;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_A:
			zoomRel(1.1f);
			invalidate();
			break;
		case KeyEvent.KEYCODE_Z:
			zoomRel(0.9f);
			invalidate();
			break;
		}
		return true;
	}

	/** 
	 * Align bitmap
	 */
	public void align() {
		if(mBmp == null)
			return;
		computeAlignOffset(mAlignOffset);
		mMat.postTranslate(mAlignOffset[0], mAlignOffset[1]);
	}

	/**
	 * Compute align offset
	 * @param offset
	 * @return
	 */
	private void computeAlignOffset(float[] offset) {
		offset[0] = 0.0f;
		offset[1] = 0.0f;
		
		mBmpRectMapped.left = 0;
		mBmpRectMapped.top = 0;
		mBmpRectMapped.right = (float)(mBmp.getWidth());
		mBmpRectMapped.bottom = (float)(mBmp.getHeight());
		
		mMat.mapRect(mBmpRectMapped);

		float bw = mBmpRectMapped.width();
		float bh = mBmpRectMapped.height();
		
		float vw = (float)getWidth();
		float vh = (float)getHeight();
		
		
		if(bw<vw) {
			/** Always center align */
			offset[0] = vw/2 - mBmpRectMapped.centerX();
		} else if(mBmpRectMapped.left>0) {
			offset[0] = -mBmpRectMapped.left;
		} else if(mBmpRectMapped.right<vw) {
			offset[0] = vw - mBmpRectMapped.right;
		}
		
		if(bh<vh) {
			/** Always center align */
			offset[1] = vh/2 - mBmpRectMapped.centerY();
		} else if(mBmpRectMapped.top>0) {
			offset[1] = -mBmpRectMapped.top;
		} else if(mBmpRectMapped.bottom<vh) {
			offset[1] = vh - mBmpRectMapped.bottom;
		}
	}
	
	public float getCurrentZoomRatio() {
		mMat.getValues(mRawMatElems);
		return mRawMatElems[Matrix.MSCALE_X];
	}
	private boolean isZoomRatioLessThanMinZoom() {
		if(getCurrentZoomRatio() < mZoomMinRatio)
			return true;
		return false;
	}
	
	/**
	 * Start animation to align
	 */
	private void animationToAlign() {
		computeAlignOffset(mAlignOffset);
		boolean isAligned = (mAlignOffset[0] == 0.0f) && (mAlignOffset[1] == 0.0f);
		boolean isZoomLessThanMinZoom = isZoomRatioLessThanMinZoom();
		if(isAligned && isZoomLessThanMinZoom == false) {
			return;
		}
		
		mAniHandler.removeCallbacks(mZoomAniTask);
		mAniHandler.removeCallbacks(mPanAndZoomAniTask);
		mAniHandler.post(mPanAndZoomAniTask);

	}
	private void animationZoomIn() {
		mDoubleTapZoomIn = true;
		mAniHandler.removeCallbacks(mPanAndZoomAniTask);
		mAniHandler.removeCallbacks(mZoomAniTask);
		mAniHandler.post(mZoomAniTask);
	}
	private void animationZoomOut() {
		mDoubleTapZoomIn = false;
		mAniHandler.removeCallbacks(mPanAndZoomAniTask);
		mAniHandler.removeCallbacks(mZoomAniTask);
		mAniHandler.post(mZoomAniTask);
	}
	
	private void decreaseByCenterPos() {
		mMat.postTranslate(-(float) (getWidth() / 2),
				-(float) (getHeight() / 2));
	}

	private void increaseByCenterPos() {
		mMat.postTranslate((float) (getWidth() / 2), (float) (getHeight() / 2));
	}

	public void zoomRel(float ratio) {
		decreaseByCenterPos();
		mMat.postScale(ratio, ratio);
		increaseByCenterPos();
		if(getCurrentZoomRatio() < this.mZoomMinRatio) {
			zoomToFit();
		}
		align();
	}
	public void zoomTo(float ratio) {
		float relRatio = ratio/getCurrentZoomRatio();
		zoomRel(relRatio);
	}

	private void panRel(float offsetX, float offsetY) {
		mMat.postTranslate(offsetX, offsetY);
	}

	/**
	 * View size is changed
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		zoomToFit();
		invalidate();
	}

	/**
	 * Update matrix in order to show bitmap on center
	 */
	private void zoomToFit() {
		int w = getWidth();
		int h = getHeight();
		RectF rcBmp = new RectF(0.0f, 0.0f, (float) mBmp.getWidth(),
				(float) mBmp.getHeight());
		RectF rcScreen = new RectF(0.0f, 0.0f, (float) w, (float) h);
		
		mMat.reset();
		mMat.setRectToRect(rcBmp, rcScreen, Matrix.ScaleToFit.FILL);
		mZoomFillRatio = getCurrentZoomRatio();
		
		mMat.reset();
		mMat.setRectToRect(rcBmp, rcScreen, Matrix.ScaleToFit.CENTER);
		mZoomMinRatio = getCurrentZoomRatio();
	}
	
	

}
