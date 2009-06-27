package com.jhlee.rr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Captured image handilng activity
 * 
 * @author popopome
 * 
 */
public class RRCaptured extends Activity {

	private static final String TAG = "RRCaptured";
	private static final String RECEIPT_SAVING_FOLDER_NAME = "receipts";
	private String mCapturedFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_captured);

		/** Get passed file path from bundle */
		Intent i = getIntent();
		Bundle b = i.getExtras();
		if (b != null) {
			mCapturedFile = b.getString("PARAM_IMAGE_FILE");
			if (mCapturedFile.length() == 0) {
				/** Invalid parameter */
				Log.e(TAG, "invalid image file path is passed");
			}
		}
		if (mCapturedFile.length() == 0) {
			this.finish();
			return;
		}
		Log.v(TAG, "Load captured file:" + mCapturedFile);
		
		
		/** Load bitmap from passed file */
		Bitmap bmp = null;
		try {
			FileInputStream stm = new FileInputStream(mCapturedFile);
			bmp = BitmapFactory.decodeStream(stm);
			if (null == bmp) {
				Log.e(TAG, "Unable to load image:" + mCapturedFile);
				this.finish();
				return;
			}
			stm.close();
		} catch (Exception e) {
			Log.e(TAG, "unable to load image from given stream:file="
					+ mCapturedFile + ":error=" + e.getMessage());
			this.finish();
			return;
		}

		/** Load image to view */
		ImageView imgView = (ImageView) this
				.findViewById(R.id.CapturedImageView);
		imgView.setImageBitmap(bmp);

		final RRCaptured self = this;

		/** Initialize buttons */
		Button.OnClickListener btnTakeAnotherReceiptListener = new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(self, RRTakeShot.class);
				self.startActivity(i);
				self.finish();
			}
		};
		Button btnTakeAnotherReceipt = (Button) findViewById(R.id.ButtonTakeOtherReceipt);
		btnTakeAnotherReceipt.setOnClickListener(btnTakeAnotherReceiptListener);

		/** Initialize ok button */
		Button.OnClickListener btnOkListener = new Button.OnClickListener() {
			public void onClick(View v) {
				/** Save captured receipt to db */
				self.saveCapturedReceiptToDb(self.mCapturedFile);
				self.finish();
			}
		};
		Button btnOk = (Button) findViewById(R.id.ButtonOk);
		btnOk.setOnClickListener(btnOkListener);

		/** Initialize cancel button */
		Button.OnClickListener btnCancelListener = new Button.OnClickListener() {
			public void onClick(View v) {
				/** Just finish activity */
				self.finish();
			}
		};
		Button btnCancel = (Button) findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(btnCancelListener);
	}

	/** Save captured receipt to db */
	private boolean saveCapturedReceiptToDb(String capturedFile) {
		
		/** Prepare image file */
		prepareSavingDirectoryIfNotExists();
		String newFilePath = generateNewReceiptImagePath();
		if(false == copyFile(capturedFile, newFilePath)) {
			Log.e(TAG, "unable to copy image file:" + capturedFile);
			return false;
		}
		
		return true;
	}
	/** Prepare saving directory */
	private boolean prepareSavingDirectoryIfNotExists() {
		File file = this.getFileStreamPath(RECEIPT_SAVING_FOLDER_NAME);
		if(true == file.exists()) {
			return true;
		}
		
		Log.v(TAG, "Receipt saving folder does not exist. Let's make directory");		
		if(false == file.mkdir()) {
			Log.e(TAG, "Unable to create receipts folder");
			return false;
		}

		Log.v(TAG, "Succeed to make receipt saving directory:" + file.getAbsolutePath());
		return true;
	}
	
	/** Generate file path */
	private String generateNewReceiptImagePath() {
		File receiptDir = this.getFileStreamPath(RECEIPT_SAVING_FOLDER_NAME);		
		String absPath = receiptDir.getAbsolutePath();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-");
		String dateString = formatter.format(new Date());
		File f = null;
		for(int i=0;;++i) {
			String filePath = dateString + i;
			f = new File(absPath + "/" + filePath + ".jpg");
			/*f = this.getFileStreamPath(absPath + "/" + filePath + ".jpg");*/
			if(false == f.exists()) {
				break;				
			}
		}
		if(f != null)
			return f.getAbsolutePath();
		
		Log.e(TAG, "Unable to generate receipt file path");
		return "";
	}

	/** Copy file */
	private boolean copyFile(String srcFilePath, String dstFilePath) {
		Log.d(TAG, "Start copying file");
		try {			
			FileInputStream istm = new FileInputStream(srcFilePath);
			FileOutputStream ostm = new FileOutputStream(dstFilePath);
			
			byte[] buffer = new byte[4096];
			int length = 0;
			while((length = istm.read(buffer)) > 0) {
				ostm.write(buffer, 0, length);				
			}
			
			ostm.flush();
			ostm.close();
			istm.close();
		} catch(Exception e) {
			Log.e(TAG, "copyFile got exception:" + e.getMessage());
			return false;
		}
		
		Log.d(TAG, "Succeeded copying file");
		return true;
	}

}
