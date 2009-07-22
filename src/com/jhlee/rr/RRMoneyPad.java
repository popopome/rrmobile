package com.jhlee.zoomsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class RRMoneyPad extends View {

	private NinePatch mNumBtnBgPatch;
	private Rect mDrawingRect = new Rect();
	
	/** CTOR */
	public RRMoneyPad(Context ctx) {
		super(ctx);
		
		Bitmap bm = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.numpad_button);
		byte[] chunk = bm.getNinePatchChunk();
		mNumBtnBgPatch = new NinePatch(bm, chunk, null);
		
		
	}
	
	public RRMoneyPad(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	}
	public RRMoneyPad(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
	}
	
	/** Draw num pad with nine-path image */
	@Override
	protected void onDraw(Canvas canvas) {
		mDrawingRect.left = 0;
		mDrawingRect.top = 0;
		mDrawingRect.bottom = 100;
		mDrawingRect.right = 100;
		mNumBtnBgPatch.draw(canvas, mDrawingRect);
	}
	

	
}
