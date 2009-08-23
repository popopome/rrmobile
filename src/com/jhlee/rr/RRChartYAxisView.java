package com.jhlee.rr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class RRChartYAxisView extends View {
	private static final int AXIS_WIDTH = 15;
	private static final float AXIS_STROKE_WIDTH = 1.5f;
	private static final int AXIS_STROKE_COLOR = Color.DKGRAY;
	private static final int PADDING_HORZ = 3;
	private static final int PADDING_BASELINE = 30;
	private static final int PADDING_TOP = 3;
	private static final int PADDING_BOTTOM = 5;
	private static final float DEFAULT_Y_AXIS_NAME_TEXT_SIZE = 20.1f;
	private static final int AXIS_NAME_COLOR = Color.WHITE;

	private String mName;
	private Paint mPaint;
	private Path mPath;
	private int mLineColor = AXIS_STROKE_COLOR;

	public RRChartYAxisView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public RRChartYAxisView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public RRChartYAxisView(Context context) {
		this(context, null);
	}

	private void initialize() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(DEFAULT_Y_AXIS_NAME_TEXT_SIZE);
		mPath = new Path();
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Rect bounds = new Rect();
		mPaint.getTextBounds(mName, 00, mName.length(), bounds);
		/*
		 * Compute width. Text will be drawn as 90 rotated form
		 */
		int w = bounds.height() + AXIS_WIDTH + PADDING_HORZ + PADDING_HORZ;
		this.setMeasuredDimension(w, getMeasuredHeight());
	}

	/*
	 * Size is changed
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		makePath(mPath);
	}

	private void makePath(Path p) {
		int vw = getWidth();
		int vh = getHeight();
		
		int x = vw - AXIS_WIDTH/2;
		p.moveTo(x, vh);
		p.lineTo(x, 0);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		Paint p = mPaint;
		p.setStrokeWidth(AXIS_STROKE_WIDTH);
		p.setColor(mLineColor);
		p.setStyle(Paint.Style.STROKE);
		
		int vh = getHeight();
		int vw = getWidth();
		
		int y = vh - PADDING_BASELINE;
		canvas.drawLine(PADDING_HORZ, y, vw, y, p);
		int axisX = vw - AXIS_WIDTH/2;
		canvas.drawLine(axisX, vh-PADDING_BOTTOM, axisX, PADDING_TOP, p);
		
		/* Draw text on axis */
		p.setTextAlign(Paint.Align.RIGHT);
		p.setColor(AXIS_NAME_COLOR);
		p.setStyle(Paint.Style.FILL_AND_STROKE);
		p.setShadowLayer(2.0f, 0, 0, Color.BLACK);
		canvas.drawTextOnPath(mName, mPath, -10, -10, p);
		p.setShadowLayer(0.0f, 0, 0, Color.BLACK);
	}
	
	/*
	 * Set y-axis name
	 */
	public void setYAxisName(String name) {
		mName = name;
	}
	public void setLineColor(int color) {
		mLineColor = color;
	}
}
