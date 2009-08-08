package com.jhlee.rr;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Camera preview
 * 
 * @author popopome
 * 
 */
public class RRCameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;

	/** CTOR */
	public RRCameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializeInternal();
	}

	public RRCameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeInternal();
	}

	public RRCameraPreview(Context context) {
		super(context);
		initializeInternal();
	}

	/**
	 * Initialize internal variables
	 */
	private void initializeInternal() {
		/*
		 * Install a SurfaceHolder.Callback so we get notified when the
		 * underlying surface is created and destroyed
		 */
		mHolder = this.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	/**
	 * Surface has been created, acquire the camera and tell it where to draw
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		/*
		 * Now the size is known, set up the camera parameters and begin the
		 * preview
		 */
		Camera.Parameters params = mCamera.getParameters();
		params.setPreviewSize(w, h);
		mCamera.setParameters(params);
		mCamera.startPreview();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		/*
		 * Surface will be destroyed when we return, so stop the preview.
		 * Because the CameraDevice object is not a shared resource, it's very
		 * important to release it when the activity is paused
		 */
		mCamera.stopPreview();
		mCamera = null;
	}
	
	/**
	 * Take a picture from camera
	 */
	public void takeShot() {
		
	}

}
