package com.jhlee.rr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
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
	private RRDbAdapter	mDbAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_captured);
		
		/** Initialize db adapter */
		mDbAdapter = new RRDbAdapter(this);

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
				Intent i = new Intent(self, RRTakeReceiptActivity.class);
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
				if(true == self.saveCapturedReceiptToDb(self.mCapturedFile)) {
					self.showSaveSuccessMessageAndFinishActivity();
					return;
				}
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
		String newFilePath = generateUniqueReceiptImagePath("");
		if(false == copyFile(capturedFile, newFilePath)) {
			Log.e(TAG, "unable to copy image file:" + capturedFile);
			return false;
		}
		
		/** Create small image file */
		String newSmallImageFilePath = createSmallReceiptImage(newFilePath);
		if(newSmallImageFilePath.length() == 0) {
			Log.e(TAG, "Unable to create small image file:" + newFilePath);
			new File(newFilePath).delete();
			return false;
		}
		
		/** Add a row */
		long rowId = mDbAdapter.insertReceipt(newFilePath, newSmallImageFilePath);
		if(rowId == -1) {
			Log.e(TAG, "Unable to save receipt image data to db");			
			new File(newFilePath).delete();
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
	private String generateUniqueReceiptImagePath(String prefix) {
		File receiptDir = this.getFileStreamPath(RECEIPT_SAVING_FOLDER_NAME);		
		String absPath = receiptDir.getAbsolutePath();
		
		String dateString = RRUtil.getTodayDateString();
		String uuidString = UUID.randomUUID().toString();
		
		File f = new File(absPath + "/" + prefix + dateString + "-" + uuidString + ".jpg");
		return f.getAbsolutePath();
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
	
	private void showSaveSuccessMessageAndFinishActivity() {
		final Activity self = this;
		
		/** Show message box */
		Dialog dlg = new AlertDialog.Builder(this)
		.setIcon(R.drawable.alert_dialog_icon)
		.setTitle(R.string.db_insertion_success_dialog_title)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		        /* User clicked OK so do some stuff */
		    	self.finish();
		    }
		})		
		.create();
		dlg.show();
	}
	
	/**
	 * Create small receipt image
	 * @param imageFilePath	Original receipt image file path
	 * @return
	 */
	private String createSmallReceiptImage(String imageFilePath) {		
		Bitmap bmp = BitmapFactory.decodeFile(imageFilePath);
		if(null == bmp) {
			Log.e(TAG, "Unable to load bitmap from file:" + imageFilePath);
			return "";
		}
		
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		
		/** Small image width/height:
		 *  120
		 */
		int smallW = 120;
		int smallH = 120;
		if(w > h) {
			/** w:120 = h:? 
			 *  w*? = 120*h
			 *  ? = 120*h/w
			 */
			smallH = 120 * h/w;
		} else {
			smallW = 120 * w/h;
		}
		
		Bitmap smallBmp = Bitmap.createScaledBitmap(bmp, smallW, smallH, true);
		if(null == smallBmp) {
			Log.e(TAG, "Unable to create scaled bitmap");
			return "";
		}
		
		/** Save small image */
		String smallImagePath = generateUniqueReceiptImagePath("small-");
		
		try {
			FileOutputStream ostm = new FileOutputStream(smallImagePath);
			smallBmp.compress(CompressFormat.JPEG, 85, ostm);
			ostm.flush();
			ostm.close();
		} catch(Exception e) {
			Log.e(TAG, "Unable to save small image bitmap to file:smallImagePath=" + smallImagePath);
			return "";
		}	
		
		return smallImagePath;
	}
	

}
