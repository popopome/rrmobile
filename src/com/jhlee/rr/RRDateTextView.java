package com.jhlee.rr;

import android.content.Context;
import android.util.AttributeSet;

public class RRDateTextView extends RRTextView {

	private static final String SAMPLE_DATE = "08-22-2009";
	
	/** CTOR */
	public RRDateTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setText(SAMPLE_DATE);
		
	}

	public RRDateTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setText(SAMPLE_DATE);
	}

	public RRDateTextView(Context context) {
		super(context);
		this.setText(SAMPLE_DATE);
	}
	
}
