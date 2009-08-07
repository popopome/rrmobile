package com.jhlee.rr;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * R2 topic list view
 * 
 * @author popopome
 * 
 */
public class R2TopicListView extends View {
	private static final String TAG = "R2TopicListView";

	public interface OnItemClickListener {
		public void onItemClicked(View view, long itemIndex);
	}

	/**
	 * Topic class. Hold topic information
	 */
	private class Topic {
		/* Topic Id */
		public long mId;
		public String mText;
		public Bitmap mTextBmp;
		public Bitmap mIconBmp;
	};

	private ArrayList<Topic> mTopicList = new ArrayList<Topic>();
	private Paint mPaint;
	private long mOffset;
	private long mMaxOffset = 0;
	private long mItemHeight = -1;

	private boolean mMouseDownFlag = false;
	private int mMouseLastY = 0;
	private int mMouseDownY = 0;
	private int mMouseMaxMovement = 0;
	private int mFocusItemIndex = -1;

	private Handler mHandler = new Handler();
	private Runnable mAlignTask = null;

	private RectF mFocusRoundRect = new RectF();
	private OnItemClickListener mItemClickListener = null;

	/** CTOR */
	public R2TopicListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializeInternal(attrs);
	}

	public R2TopicListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeInternal(attrs);
	}

	public R2TopicListView(Context context) {
		super(context);
	}

	/**
	 * Add topic
	 * 
	 * @param id
	 * @param text
	 * @param textColor
	 * @param bgColor
	 * @param iconBmp
	 * @return
	 */
	public boolean addTopic(long id, String text, int textColor, int bgColor,
			long iconId) {
		Topic t = new Topic();

		t.mId = id;
		t.mTextBmp = createTextBlurBitmap(text, mPaint, textColor, bgColor);
		if (null == t.mTextBmp) {
			Log.e(TAG, "unable to create text blur bitmap:" + text);
			return false;
		}
		t.mText = text;

		if (iconId != -1) {
			t.mIconBmp = BitmapFactory.decodeResource(this.getResources(),
					(int) iconId);
		}

		mTopicList.add(t);

		/* Update item height */
		if (mItemHeight < 0) {
			mItemHeight = t.mTextBmp.getHeight();
		}

		mMaxOffset = mTopicList.size() * mItemHeight - this.getHeight();
		if (mMaxOffset < 0)
			mMaxOffset = 0;

		return true;
	}

	/**
	 * View size is changed
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mMaxOffset = mTopicList.size() * mItemHeight - h;
		if (mMaxOffset < 0)
			mMaxOffset = 0;
	}

	/**
	 * Create text blur bitmap
	 * 
	 * @param text
	 */
	private Bitmap createTextBlurBitmap(String text, Paint paint,
			int textColor, int bgColor) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		Rect marginBounds = new Rect();
		paint.getTextBounds("A", 0, 1, marginBounds);

		/* Create text bitmap */
		int bmpW = bounds.width() + marginBounds.width() * 2;
		int bmpH = bounds.height() * 2;
		Bitmap bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
		bmp.eraseColor(Color.TRANSPARENT);

		int textDrawingX = bmpW / 2 - bounds.width() / 2;
		int textDrawingY = bmpH / 2 + bounds.height() / 2;

		Canvas canvas = new Canvas(bmp);

		/* Set up blur filter */
		MaskFilter blur = new BlurMaskFilter(8.0f, BlurMaskFilter.Blur.NORMAL);
		paint.setMaskFilter(blur);

		/* Make background text */
		paint.setColor(bgColor);
		canvas.drawText(text, textDrawingX, textDrawingY, mPaint);
		canvas.drawText(text, textDrawingX, textDrawingY, mPaint);
		canvas.drawText(text, textDrawingX, textDrawingY, mPaint);

		/* Make foreground text */
		paint.setMaskFilter(null);
		paint.setColor(textColor);
		canvas.drawText(text, textDrawingX, textDrawingY, mPaint);

		return bmp;
	}

	/**
	 * Initialize text
	 * 
	 * @param attrs
	 */
	private void initializeInternal(AttributeSet attrs) {
		/* Initialize paint object */
		mPaint = new Paint();
		mPaint.setAntiAlias(true);

		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		float fontSizeInPixel = dm.scaledDensity * 23.5f;
		/* Let's use custom font */
		/* Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); */
		Typeface font = Typeface.createFromAsset(this.getContext().getAssets(),
				"fonts/Complete in Him.ttf");

		mPaint.setTypeface(font);
		mPaint.setTextSize(fontSizeInPixel);

		/* Initialize offset */
		mOffset = 0;
		mMaxOffset = 0;
	}

	/**
	 * Draw
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		/* If topic is empty, we do not need to draw any thing */
		if (mTopicList.isEmpty()) {
			return;
		}

		long itemIndex = mOffset / mItemHeight;
		long y = itemIndex * mItemHeight - mOffset;

		long viewHeight = this.getHeight();
		long viewWidth = this.getWidth();

		long itemCnt = mTopicList.size();
		long paddingX = 0;
		long paddingY = 0;
		for (; y < viewHeight && itemIndex < itemCnt; ++itemIndex, y += mItemHeight) {
			if (itemIndex < 0)
				continue;

			paddingX = 0;
			paddingY = 0;

			if (itemIndex == mFocusItemIndex) {

				if (mMouseDownFlag == true) {
					paddingX = 2;
					paddingY = 2;
				}

				mPaint.setColor(Color.rgb(166, 213, 20));
				mFocusRoundRect.set(paddingX, y + paddingY, viewWidth
						+ paddingX, y + mItemHeight + paddingY);
				canvas.drawRoundRect(mFocusRoundRect, 5.0f, 5.0f, mPaint);
			}

			/*
			 * Draw each item Draw background icon first and then draw text
			 * bitmap.
			 */
			Topic t = mTopicList.get((int) itemIndex);
			if (t.mIconBmp != null) {
				int sx = t.mTextBmp.getWidth() - t.mIconBmp.getWidth() / 4;
				int sy = (int) ((y + mItemHeight / 2) - t.mIconBmp.getHeight() / 2);
				canvas.drawBitmap(t.mIconBmp, sx + paddingX, sy + paddingY,
						mPaint);
			}
			canvas.drawBitmap(t.mTextBmp, 0 + paddingX, y + paddingY, mPaint);
		}
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
			/* Cancel handler request */
			mHandler.removeCallbacks(mAlignTask);

			mMouseDownFlag = true;
			mMouseLastY = y;
			mMouseDownY = y;
			mMouseMaxMovement = 0;
			mFocusItemIndex = (int) ((y - mOffset) / mItemHeight);

			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMouseDownFlag == true) {
				int dy = mMouseLastY - y;
				int val = (int) (mOffset + dy);
				if (val < 0 || val > mMaxOffset)
					dy >>= 2;

				mOffset += dy;
				mMouseLastY = y;

				mMouseMaxMovement = Math.max(mMouseMaxMovement, Math.abs(y
						- mMouseDownY));
				if (mMouseMaxMovement > mItemHeight) {
					mFocusItemIndex = -1;
				}
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mMouseDownFlag == true) {
				startAlignAnimation();

				/* Notification for click */
				if ((mMouseMaxMovement <= mItemHeight)
						&& (mFocusItemIndex >= 0 && mFocusItemIndex < mTopicList
								.size())) {
					if (mItemClickListener != null) {
						mItemClickListener.onItemClicked(this, mFocusItemIndex);
					}
				}
			}
			mMouseDownFlag = false;
			break;
		}

		return true;
	}

	/**
	 * Start align animation
	 */
	private void startAlignAnimation() {
		final R2TopicListView view = this;
		if (mAlignTask == null) {
			mAlignTask = new Runnable() {
				public void run() {
					boolean stop = false;
					if (mOffset < 0) {
						int dy = (int) (((-mOffset) * 10) >> 4);
						if (dy == 0) {
							mOffset = 0;
							stop = true;
						} else {
							mOffset += dy;
						}
					} else {
						if (mOffset > mMaxOffset) {
							int dy = (int) (((mOffset - mMaxOffset) * 10) >> 4);
							if (dy == 0) {
								mOffset = mMaxOffset;
								stop = true;
							} else {
								mOffset -= dy;
							}
						}
					}

					if (stop == false) {
						/* Repost event */
						mHandler.postDelayed(this, 200);
					}

					view.invalidate();
				}
			};

		}

		mHandler.post(mAlignTask);
	}

	/**
	 * Register item click listener
	 * 
	 * @param listener
	 */
	void setItemClickListener(OnItemClickListener listener) {
		mItemClickListener = listener;
	}

}
