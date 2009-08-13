package com.jhlee.rr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class RRCarouselFlowView extends View {

	public abstract interface RRCarouselItemCustomDrawer {
		public abstract void onDraw(android.view.View view, Canvas canvas,
				RRCarouselItem item);
	}

	public abstract interface RRCarouselActiveItemClickListener {
		public abstract void onClicked(RRCarouselFlowView view,
				RRCarouselItem item);
	}

	public class RRCarouselItem {
		public int seq;
		/** Not scaled into view coordinate */
		public int virtual_x;
		public double scale;
		/** All values below are scaled into view coordinates */
		public double z;
		public int x;
		public int y;
		public int w;
		public int h;

		public int color;
		/* Image file name */
		public String img_file_name;
	}

	/**
	 * If z order is greater than another z-order value, we want to draw it
	 * later.
	 */
	private class RRCarouselItemComparator implements
			Comparator<RRCarouselItem> {
		public int compare(RRCarouselItem o1, RRCarouselItem o2) {
			if (o1.z < o2.z)
				return 1;
			if (o1.z == o2.z)
				return 0;
			return -1;
		}
	}

	private static int FINGER_CLICK_THRESHOLD = 20;

	private int mItemCnt;
	private int mItemWidth;
	private int mItemHeight;

	private ArrayList<RRCarouselItem> mItems;
	private ArrayList<RRCarouselItem> mSortedItems;

	private RRCarouselItem mLeftEdgeItem;
	private RRCarouselItem mRightEdgeItem;

	private int mCameraXPos;
	private int mTargetCamXPos;
	private int mFocalLength;

	private Paint mPaint;
	private int mHyperbolaA;
	private int mHyperbolaB;

	private int mLastMouseX;
	private int mMouseDownX;

	private RRCarouselActiveItemClickListener mActiveItemClickListener = null;
	private RRCarouselItemCustomDrawer mCustomDrawer = null;

	/** Animation handler */
	private Handler mAnimationHandler = new Handler();
	private Runnable mCameraMovingTask = new Runnable() {

		public void run() {
			int offset = (mTargetCamXPos - mCameraXPos) * 25 / 100;
			if (offset == 0) {
				return;
			}
			moveCameraRel(offset);
			invalidate();
			mAnimationHandler.postAtTime(this, SystemClock.uptimeMillis() + 50);
		}
	};

	/**
	 * CTOR
	 * 
	 * @param ctx
	 *            Android context
	 */
	public RRCarouselFlowView(Context ctx) {
		super(ctx);
		initializePaint();
	}

	/**
	 * CTOR
	 * 
	 * @param ctx
	 *            Android context
	 * @param attrs
	 *            Attributes from Xml
	 */
	public RRCarouselFlowView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializePaint();
	}

	/**
	 * CTOR
	 * 
	 * @param ctx
	 *            Android context
	 * @param attrs
	 *            Attributes from Xml
	 * @param defStyle
	 *            ???
	 */
	public RRCarouselFlowView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initializePaint();
	}

	/**
	 * Initialize paint object and its properties
	 */
	private void initializePaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFF0000);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(1);

	}

	/**
	 * Initialize view & items
	 * 
	 * @param itemCount
	 *            Total item count
	 * @param itemWidth
	 *            Item width
	 * @param itemHeight
	 *            Item height
	 * @param focalLength
	 * @param hyperbola_A
	 * @param hyperbola_B
	 */
	public void initialize(int itemCount, int itemWidth, int itemHeight,
			int focalLength, int hyperbola_A, int hyperbola_B) {
		mItemCnt = itemCount;
		mItemWidth = itemWidth;
		mItemHeight = itemHeight;
		mFocalLength = focalLength;
		mHyperbolaA = hyperbola_A;
		mHyperbolaB = hyperbola_B;

		mLeftEdgeItem = new RRCarouselItem();
		mRightEdgeItem = new RRCarouselItem();

		mItems = new ArrayList<RRCarouselItem>();
		mSortedItems = new ArrayList<RRCarouselItem>();

		int virtual_offset = 0;
		RRCarouselItem item;
		for (int i = 0; i < this.mItemCnt; ++i) {
			item = new RRCarouselItem();
			item.seq = i;
			item.virtual_x = virtual_offset;

			int r = ((int) Math.abs(Math.random() * 256)) % 255;
			int g = ((int) Math.abs(Math.random() * 256)) % 255;
			int b = ((int) Math.abs(Math.random() * 256)) % 255;
			item.color = 0xFF000000 | r << 16 | g << 8 | b;
			mItems.add(item);

			virtual_offset += mItemWidth;
		}

		this.moveCamera(0);
	}

	public void setActiveItemClickListener(
			RRCarouselActiveItemClickListener activeItemClickListener) {
		mActiveItemClickListener = activeItemClickListener;
	}

	public void setItemDrawer(RRCarouselItemCustomDrawer drawer) {
		mCustomDrawer = drawer;
	}

	/**
	 * Move camera relative amount
	 * 
	 * @param offset
	 */
	public void moveCameraRel(int offset) {
		moveCamera(mCameraXPos + offset);
	}

	/**
	 * Move camera
	 * 
	 * @param newXPos
	 *            x position in logical coordinate
	 */
	public void moveCamera(int newXPos) {
		mCameraXPos = newXPos;
		int offset = 0;

		/** View center pos */
		int screenCenterX = this.getWidth() >> 1;
		int screenCenterY = this.getHeight() >> 1;

		/** Update each edge poitns. */
		updateBothEdgePointsPos(newXPos);

		/** Update all carousel items */
		for (RRCarouselItem item : mItems) {

			/** Update screen position */
			updateItemScreenPos(newXPos, screenCenterX, screenCenterY, item);
		}

		/** Sort carousel items by its z-order */
		mSortedItems.clear();
		mSortedItems.addAll(mItems);
		Collections.sort(mSortedItems, new RRCarouselItemComparator());
	}

	/**
	 * Update item screen position
	 * 
	 * @param newXPos
	 * @param centerX
	 * @param centerY
	 * @param item
	 */
	private void updateItemScreenPos(int newXPos, int centerX, int centerY,
			RRCarouselItem item) {

		if (item.virtual_x < mLeftEdgeItem.virtual_x) {
			item.z = mLeftEdgeItem.z + Math.abs(item.virtual_x);
			item.scale = mLeftEdgeItem.scale;
			item.x = mLeftEdgeItem.x
					- (int) ((mLeftEdgeItem.virtual_x - item.virtual_x) * item.scale);
			item.y = centerY;
			item.w = mLeftEdgeItem.w;
			item.h = mLeftEdgeItem.h;
			return;
		} else if (item.virtual_x > mRightEdgeItem.virtual_x) {
			item.z = mRightEdgeItem.z + Math.abs(item.virtual_x);
			item.scale = mRightEdgeItem.scale;
			item.x = mRightEdgeItem.x
					+ (int) ((item.virtual_x - mRightEdgeItem.virtual_x) * item.scale);
			item.y = centerY;
			item.w = mRightEdgeItem.w;
			item.h = mRightEdgeItem.h;
			return;
		}

		int itemNewX = item.virtual_x - newXPos;
		item.z = mHyperbolaA * Math.sqrt(itemNewX * itemNewX + 1) / mHyperbolaB;
		item.scale = mFocalLength / (mFocalLength + item.z);
		item.x = centerX + (int) ((item.virtual_x - newXPos) * item.scale);
		item.y = centerY;
		item.w = (int) (this.mItemWidth * item.scale);
		item.h = (int) (this.mItemHeight * item.scale);
	}

	/**
	 * Update both edge poitns
	 * 
	 * @param newXPos
	 */
	private void updateBothEdgePointsPos(int newXPos) {
		mLeftEdgeItem.virtual_x = newXPos - (int) (mItemWidth * 3.5);
		mRightEdgeItem.virtual_x = newXPos + (int) (mItemHeight * 3.5);

		int centerX = this.getWidth() / 2;
		int centerY = this.getHeight() / 2;
		updateItemScreenPos(newXPos, centerX, centerY, mLeftEdgeItem);
		updateItemScreenPos(newXPos, centerX, centerY, mRightEdgeItem);

	}

	/** Draw carousel view */
	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawColor(Color.LTGRAY);

		/**
		 * If there is custom drawer, then use it.
		 */
		if (null != mCustomDrawer) {
			for (RRCarouselItem item : mSortedItems) {
				mCustomDrawer.onDraw(this, canvas, item);
			}
			return;
		}

		/** Default drawing */
		mPaint.setColor(Color.WHITE);
		Rect r = new Rect();
		for (RRCarouselItem item : mSortedItems) {
			r.left = item.x - item.w / 2 + 10;
			r.top = item.y - item.h / 2 + 10;
			r.right = r.left + item.w - 10;
			r.bottom = r.top + item.h - 10;
			mPaint.setColor(item.color);
			canvas.drawRect(r, mPaint);
		}
	}

	/** Key down */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			moveCameraRel(-10);
			this.invalidate();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			moveCameraRel(10);
			this.invalidate();
			break;
		case KeyEvent.KEYCODE_A:
			mHyperbolaA--;
			if (mHyperbolaA < 1)
				mHyperbolaA = 1;
			moveCameraRel(0);
			invalidate();
			break;
		case KeyEvent.KEYCODE_Z:
			mHyperbolaA++;
			moveCameraRel(0);
			invalidate();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * View size is changed
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (w < h) {
			mItemHeight = mItemWidth = w * 2 / 3;
		} else {
			mItemHeight = mItemWidth = h * 2 / 3;
		}

		/** Get active item */
		RRCarouselItem activeItem = getActiveItem();

		/** Update each item's virtual position */
		for (RRCarouselItem item : this.mItems) {
			item.virtual_x = item.seq * mItemWidth;
		}

		if(activeItem != null)
			this.moveCameraRel(activeItem.x);
	}

	/**
	 * Motion event
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int curX = (int) event.getX();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMouseX = curX;
			mMouseDownX = curX;
			break;
		case MotionEvent.ACTION_MOVE:
			int offset = mLastMouseX - curX;
			moveCameraRel(offset);
			mLastMouseX = curX;
			invalidate();
			break;
		case MotionEvent.ACTION_UP:

			/** Align to current active item */
			RRCarouselItem item = null;
			if (Math.abs(curX - mMouseDownX) < FINGER_CLICK_THRESHOLD) {
				item = this.findUnderDevicePoint(curX);
				if (null == item)
					item = getActiveItem();
				else {
					if (item == getActiveItem()) {
						/** Active item is clicked */
						if (null != mActiveItemClickListener) {
							mActiveItemClickListener.onClicked(this, item);
							return true;
						}
					}
				}
			} else {
				item = getActiveItem();
			}

			if(null != item) {
				animationTo(item.seq * mItemWidth);
				invalidate();
			}
			break;
		}
		return true;
	}

	/**
	 * Return active item
	 * 
	 * @return
	 */
	private RRCarouselItem getActiveItem() {
		if(mSortedItems.isEmpty())
			return null;
		
		return mSortedItems.get(mSortedItems.size() - 1);
	}

	/** Animation */
	private void animationTo(int targetCamXPos) {
		mAnimationHandler.removeCallbacks(mCameraMovingTask);
		mTargetCamXPos = targetCamXPos;
		mAnimationHandler.postDelayed(mCameraMovingTask, 50);
	}

	/**
	 * Find under device point Reverse iterator is more effective method to find
	 * item
	 * */
	private RRCarouselItem findUnderDevicePoint(int devX) {
		RRCarouselItem foundItem = null;
		for (RRCarouselItem item : mSortedItems) {
			if ((item.x - item.w / 2 <= devX)
					&& (devX <= (item.x + item.w / 2)))
				foundItem = item;
		}
		return foundItem;
	}

	/**
	 * Set custom drawer
	 * @param customDrawer
	 */
	public void setCarouselItemCustomDrawer(RRCarouselItemCustomDrawer customDrawer) {
		mCustomDrawer = customDrawer;
	}
}
