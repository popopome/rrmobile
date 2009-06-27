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
import android.widget.Button;

public class RRTakeShot extends Activity {
	
	private static final String TAG = "RRTakeShot";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_takeshot);

		/** Register button click callback function */
		final RRTakeShot self = this;
		Button.OnClickListener btnClickListener = new Button.OnClickListener() {
			public void onClick(View arg0) {
				/** Capture image from camera.
				 *  Mock up code just returns temporary file path
				 */
				String capturedFile = self.captureIt();
				if (capturedFile.length() == 0) {
					return;
				}

				/** Go to RRCaptured activity.
				 *  Pass captured file name.
				 */
				Intent i = new Intent(self, RRCaptured.class);
				i.putExtra("PARAM_IMAGE_FILE", capturedFile);
				
				self.startActivity(i);
				self.finish();
			}
		};
		Button btnTakeShot = (Button) this.findViewById(R.id.ButtonTakeShot);
		btnTakeShot.setOnClickListener(btnClickListener);
	}

	private String generateNewFileName() {
		return "__test__";
	}

	/**
	 * Capture image from camera
	 * @return
	 */
	private String captureIt() {
		Log.d(TAG, "Capture image from camera");
		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.sample_captured_receipt);
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
			return "";
		}
		
		if(saveResult == false) {
			Log.e(TAG, "unable to save image to file");
			return "";
		}
		
		return absPath;
	}
}
