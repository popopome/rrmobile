package com.jhlee.rr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;

public class RRTagStreamView extends Gallery {

	public interface RRTagDataProvider {
		public int getCount();
		public String getTag(int index);
		public boolean isChecked(int index);
		public void check(int index);
		public void uncheck(int index);
		public boolean addTag(String tag, boolean checked);
		public int findTag(String tagName);
	}
	public interface OnTagItemStateChangeListener {
		public void onTagItemStateChanged(String tag, boolean checked);
	}
	
	private static final int	TEXT_SIZE = 30;
	private RRTagDataProvider mTagProvider = null;
	private Paint mPaint = null;
	private Rect mTmpRect = new Rect();
	
	private OnTagItemStateChangeListener	mOnItemStateChangeListener = null;
	
	public RRTagStreamView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public RRTagStreamView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public RRTagStreamView(Context context) {
		super(context);
		initialize();
	}
	
	private void initialize() {
		mPaint = new Paint();
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(TEXT_SIZE);
		mPaint.setColor(Color.rgb(180, 180, 180));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int count = mTagProvider.getCount();
		if(count > 0) {
			super.onDraw(canvas);
			return;
		}
		
		/* Draw empty string */
		canvas.drawText("No Tags", getWidth()/2, getHeight()/2 + TEXT_SIZE / 2, mPaint);
		
	}

	/*
	 * Refresh all tags
	 */
	public void refreshTags() {
		int oldScrollX = this.getScrollX();
		int oldScrollY = this.getScrollY();
		
		this.setAdapter(new TagAdapter());
		this.requestLayout();
	}
	                         
	public void setTagProvider(RRTagDataProvider provider) {
		mTagProvider = provider;
		this.setAdapter(new TagAdapter());
	
		this.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				RRTagTextView tagTextView = (RRTagTextView)view;
				tagTextView.toggleCheck();
				tagTextView.invalidate();
				
				/* Mark to tag provider */
				if(tagTextView.isChecked())
					mTagProvider.check(position);
				else
					mTagProvider.uncheck(position);
				
				/* Notify to outside */
				if(mOnItemStateChangeListener != null) {
					mOnItemStateChangeListener.onTagItemStateChanged(tagTextView.getTagText(), tagTextView.isChecked());
				}
			}
		});
	}

	/**
	 * Provide tag information
	 *
	 */
	private class TagAdapter extends BaseAdapter {

		public int getCount() {
			return mTagProvider.getCount();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View view, ViewGroup arg2) {
			RRTagTextView tagTextView = (RRTagTextView)view;
			String tagString = mTagProvider.getTag(position);
			if(tagTextView == null) {
				tagTextView = new RRTagTextView(getContext());
				tagTextView.setTagText(tagString);
			} else {
				tagTextView.setTagText(tagString);
			}
			
			if(mTagProvider.isChecked(position)) {
				tagTextView.check();
			} else {
				tagTextView.uncheck();
			}
			
			return tagTextView;
		}
	}
	
	public void setOnTagItemStateChangeListener(OnTagItemStateChangeListener listener) {
		mOnItemStateChangeListener = listener;
	}
	
	/*
	 * Scroll to given tag
	 */
	public void scrollToTag(String tagName) {
		if(mTagProvider == null)
			return;
		
		int pos = mTagProvider.findTag(tagName);
		if(pos == -1)
			return;
		
		this.setSelection(pos, true);
	}

	/*
	 * Get active tag
	 */
	public String getActiveTag() {
		RRTagTextView view = (RRTagTextView) this.getSelectedView();
		if(null == view)
			return "";
		
		return view.getTagText();
	}
}
