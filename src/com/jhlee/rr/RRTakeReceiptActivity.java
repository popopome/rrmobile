package com.jhlee.rr;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class RRTakeReceiptActivity extends Activity implements RRCameraPreview.OnPictureTakenListener {
	
	private static final String TAG = "RRTakeShot";
	
	private RRImageStorageManager	mImgStg = new RRImageStorageManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/* Check whether storage is available or not */
		if(false == mImgStg.open(this)) {
			Log.e(TAG, "unable to open image storage");
			Toast.makeText(this, "Please insert sd card first", Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}
		
		/* Screen orientation to landscape */
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		/* Full screen */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(R.layout.rr_takeshot);
		
		/* Get preview object */
		final RRCameraPreview preview = (RRCameraPreview)findViewById(R.id.rr_cam_preview);
		
		/** Register button click callback function */
		final RRTakeReceiptActivity self = this;
		Button.OnClickListener btnClickListener = new Button.OnClickListener() {
			public void onClick(View arg0) {
				preview.takePicture(self);
			}
		};
		Button btnTakeShot = (Button) this.findViewById(R.id.ButtonTakeShot);
		btnTakeShot.setOnClickListener(btnClickListener);
		
		/* Give default focus to button */
		btnTakeShot.requestFocus();

	}

	/**
	 * Picture is taken
	 */
	public void pictureTaken(Bitmap capturedBmp) {
		Log.d(TAG, "Capture image from camera");
		
		/* Rotate 90 degree */
		Matrix m = new Matrix();
		m.setRotate(90);
		Bitmap bmp = Bitmap.createBitmap(capturedBmp, 0, 0, capturedBmp.getWidth(),
				capturedBmp.getHeight(), m, false);
		
		/* Check storage card availability */
		final String TEMP_FILE_NAME = "temp_capture_image.jpg";
		File outputFile = new File(mImgStg.getBasePath(), TEMP_FILE_NAME);
		
		boolean saveResult = false;
		String absPath = "";
		try {
			/* Create new file */
			/* outputFile.createNewFile();*/
			/*FileOutputStream stm = new FileOutputStream(outputFile.getAbsolutePath());*/
			
			FileOutputStream stm = this.openFileOutput(TEMP_FILE_NAME, MODE_PRIVATE);
			saveResult = bmp.compress(CompressFormat.JPEG, 85, stm);			
			stm.flush();
			stm.close();
			
			/* Get absolute path */
			absPath = outputFile.getAbsolutePath();
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Unable to save temporary image: " + e.toString());
			/* Show error message */
			Toast.makeText(this, "Unable to save captured file", Toast.LENGTH_SHORT).show();
			/* Finish activity */
			this.finish();
			return;
		}
		
		if(saveResult == false) {
			Log.e(TAG, "unable to save image to file");
			return;
		}
		
		
		/** Go to RRCaptured activity.
		 *  Pass captured file name.
		 */
		Intent i = new Intent(this, RRCaptureConfirmActivity.class);
		i.putExtra("PARAM_IMAGE_FILE", absPath);
		
		this.startActivity(i);
		this.finish();
		
	}

}
