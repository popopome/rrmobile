package com.jhlee.rr;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class RRTakeReceiptActivity extends Activity implements RRCameraPreview.OnPictureTakenListener {
	
	private static final String TAG = "RRTakeShot";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/* Full screen */
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
	}

	private String generateNewFileName() {
		return "__test__";
	}

	
	/**
	 * Picture is taken
	 */
	public void pictureTaken(Bitmap bmp) {
		Log.d(TAG, "Capture image from camera");
		final String TEMP_FILE_NAME = "temp_capture_image.jpg";
		boolean saveResult = false;
		String absPath = "";
		try {
			FileOutputStream stm = super.openFileOutput(TEMP_FILE_NAME, MODE_PRIVATE);
			saveResult = bmp.compress(CompressFormat.JPEG, 100, stm);			
			stm.flush();
			stm.close();
			
			File f = getFileStreamPath(TEMP_FILE_NAME);
			absPath = f.getPath();
		} catch(Exception e) {			
			Log.e(TAG, "Unable to save temporary image: " + e.toString());
			return;
		}
		
		if(saveResult == false) {
			Log.e(TAG, "unable to save image to file");
			return;
		}
		
		
		/** Go to RRCaptured activity.
		 *  Pass captured file name.
		 */
		Intent i = new Intent(this, RRCaptured.class);
		i.putExtra("PARAM_IMAGE_FILE", absPath);
		
		this.startActivity(i);
		this.finish();
		
	}

}
