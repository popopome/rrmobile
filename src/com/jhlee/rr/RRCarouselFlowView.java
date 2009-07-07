package com.jhlee.rr;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

public class RRCarouselFlowView extends View {

	private class RRCarouselItem {
		/** Not scaled into view coordinate */
		public int virtual_x;
		public double scale;
		/** All values below are scaled into view coordinates */
		public double z;
		public int x;
		public int y;
		public int w;
		public int h;
	}

	/**
	 * If z order is greater than another z-order value, we want to draw it
	 * later.
	 */
	private class RRCarouselItemComparator implements
			Comparator<RRCarouselItem> {
		public int compare(RRCarouselItem o1, RRCarouselItem o2) {
			if(o1.z < o2.z)
				return 1;
			if(o1.z == o2.z)
				return 0;
			return -1;
		}
	}

	private int mItemCnt;
	private int mItemWidth;
	private int mItemHeight;
	private RRCarouselItem[] mItems;

	private int mCameraXPos;
	private int mFocalLength;
		
	private Paint mPaint;
	private int mHyperbolaA;
	private int mHyperbolaB;

	public RRCarouselFlowView(Context ctx) {
		super(ctx);
		initializePaint();
	}
	public RRCarouselFlowView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializePaint();
	}
	public RRCarouselFlowView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initializePaint();
	}
	
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
	
	
	public void initialize(int itemCount, int itemWidth, int itemHeight, int focalLength,
			int hyperbola_A,
			int hyperbola_B) {
		mItemCnt = itemCount;
		mItemWidth = itemWidth;
		mItemHeight = itemHeight;
		mFocalLength = focalLength;
		mHyperbolaA = hyperbola_A;
		mHyperbolaB = hyperbola_B;

		regenerateItems();
		
		this.moveCamera(0);
	}
	private void regenerateItems() {
		mItems = null;
		mItems = new RRCarouselItem[mItemCnt];
		int virtual_offset = 0;
		for (int i=0;i<this.mItemCnt;++i) {
			mItems[i] = new RRCarouselItem();
			mItems[i].virtual_x = virtual_offset;
			virtual_offset += mItemWidth;
		}
	}

	public void moveCameraRel(int offset) {
		moveCamera(mCameraXPos + offset);
	}
	public void moveCamera(int newXPos) {
		mCameraXPos = newXPos;
		int offset = 0;

		/** View center pos */
		int centerX = this.getWidth() >> 1;
		int centerY = this.getHeight() >> 1;

		/** Update all carousel items */
		for (RRCarouselItem item : mItems) {
			item.z = mHyperbolaA 
					* Math.sqrt((item.virtual_x - newXPos)
							* (item.virtual_x - newXPos) + 1) / mHyperbolaB;
			item.scale = mFocalLength / (mFocalLength + item.z);
			item.x = centerX + (int)((item.virtual_x - newXPos) * item.scale);
			item.y = centerY;
			item.w = (int)(this.mItemWidth * item.scale);
			item.h = (int)(this.mItemHeight * item.scale);

			offset += mItemWidth;
		}

		/** Sort carousel items by its z-order */
		Arrays.sort(mItems, new RRCarouselItemComparator());
	}

	/** Draw carousel view */
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);

		canvas.drawColor(Color.LTGRAY);
		
		mPaint.setColor(Color.WHITE);
		Rect r = new Rect();
		for (RRCarouselItem item : mItems) {
			r.left = item.x - item.w/2;
			r.top = item.y - item.h/2;
			r.right = r.left + item.w;
			r.bottom = r.top + item.h;
			canvas.drawRect(r, mPaint);
		}
	}

	/** Key down */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
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
			if(mHyperbolaA<1)
				mHyperbolaA=1;
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
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		if(w<h) {
			mItemHeight = mItemWidth = w*2/3;
		} else {
			mItemHeight = mItemWidth = h*2/3;
		}
		this.regenerateItems();
		this.moveCameraRel(0);
	}
}
