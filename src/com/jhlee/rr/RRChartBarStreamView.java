package com.jhlee.rr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;

public class RRChartBarStreamView extends Gallery {

	/**
	 * Abstract for RRChartBarDataProvider
	 */
	public interface RRChartBarDataProvider {
		public int getCount();

		public long getBarMaxValue();

		public String getBarTitle(int position);

		public String getBarValueName(int position);

		public long getBarValue(int position);
	}

	private static final int DEFAULT_BAR_WIDTH = 80;
	private static final int DEFAULT_BAR_HEIGHT = 200;
	private static final int BAR_VIEW_SPACING = -1;
	private static final int DEFAULT_BAR_VALUE_NAME_TEXT_SIZE = 16;
	private static final int DEFAULT_TITLE_TEXT_SIZE = 9;
	private static final int PADDING_BASELINE = 30;
	private static final int PADDING_RIGHT = 5;
	private static final float BASE_LINE_WIDTH = 1.5f;

	private RRChartBarDataProvider mDataProvider;
	private int mBarWidth = DEFAULT_BAR_WIDTH;
	private int mBarValueNameTextSize = DEFAULT_BAR_VALUE_NAME_TEXT_SIZE;
	private int mTitleTextSize = DEFAULT_TITLE_TEXT_SIZE;
	private int mBarColor = Color.LTGRAY;
	private int mBarEdgeColor = Color.DKGRAY;
	private int mBarHeight = DEFAULT_BAR_HEIGHT;
	
	private Paint mPaint = new Paint();

	public RRChartBarStreamView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public RRChartBarStreamView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public RRChartBarStreamView(Context context) {
		this(context, null);
	}

	private void initialize() {
		this.setSpacing(BAR_VIEW_SPACING);
		this.setGravity(0x03);
	}

	public void setChartBarDataProvider(RRChartBarDataProvider dataProvider) {
		mDataProvider = dataProvider;
		setAdapter(new ChartBarDataAdapter());
	}

	/*
	 * Set bar width
	 */
	public void setBarWidth(int barWidth) {
		mBarWidth = barWidth;
	}

	/**
	 * Chart bar data adapter
	 * 
	 * @author popopome
	 * 
	 */
	private class ChartBarDataAdapter extends BaseAdapter {
		public int getCount() {
			return mDataProvider.getCount();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			RRChartBarView barView = (RRChartBarView) convertView;
			if (barView == null) {
				barView = new RRChartBarView(RRChartBarStreamView.this
						.getContext());
			}
			barView.setBarWidth(mBarWidth);
			barView.setTitleTextSize(mTitleTextSize);
			barView.setBarValueNameTextSize(mBarValueNameTextSize);
			barView.setBarColor(mBarColor, mBarEdgeColor);
			barView.setData(mDataProvider.getBarTitle(position), mDataProvider
					.getBarValueName(position), mDataProvider.getBarMaxValue(),
					mDataProvider.getBarValue(position));

			return barView;
		}
	}

	/*
	 * Scroll gallery by its center position
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.setPadding(-w/2, this.getPaddingTop(), this.getPaddingRight(), this.getPaddingBottom());
		
//		this.scrollTo(w / 2 - mBarWidth / 2, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), mBarHeight);
	}

	/*
	 * Set default bar value name text size.
	 */
	public void setBarValueNameTextSize(int i) {
		mBarValueNameTextSize = i;
	}
	/* 
	 * Title text size
	 */
	public void setTitleTextSize(int i) {
		mTitleTextSize = i;
	}
	
	public void setBarColor(int barColor, int barEdgeColor) {
		mBarColor = barColor;
		mBarEdgeColor = barEdgeColor;
	}

	/*
	 * Draw base line
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint p = mPaint;
		p.setStyle(Paint.Style.STROKE);
		p.setColor(Color.DKGRAY);
		p.setAntiAlias(true);
		p.setStrokeWidth(BASE_LINE_WIDTH);
		
		int y = getHeight() - PADDING_BASELINE;
		int vw = getWidth() - PADDING_RIGHT;
		canvas.drawLine(0, y, vw, y, p);
	}
	
	public void setBarHeight(int barHeight) {
		mBarHeight = barHeight;
	}
	
	public void refreshData() {
		setChartBarDataProvider(mDataProvider);
	}
}
