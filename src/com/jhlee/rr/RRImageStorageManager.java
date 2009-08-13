package com.jhlee.rr;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class RRImageStorageManager {

	private static final String TAG = "RrimageStorageManager";
	/* Image storage path */
	private File mImageStoragePath;
	private Context mCtx;

	/**
	 * Check whether storage card is available or not
	 * 
	 * @return
	 */
	public static boolean isStorageCardAvailable() {
		String storageState = Environment.getExternalStorageState();
		return storageState.contains("mounted");
	}

	/*
	 * Open storage
	 */
	public boolean open(Context ctx) {
		/* Keep context */
		mCtx = ctx;
		
		if (false) {
			if (false == isStorageCardAvailable()) {
				Log.e(TAG, "Storage is not available");
				return false;
			}

			File sd = Environment.getExternalStorageDirectory();
			String sdPath = sd.getAbsolutePath();
			String r2StoragePath = sdPath + "r2";

			mImageStoragePath = new File(r2StoragePath);
			if (mImageStoragePath.exists() == false) {
				/* Let's create r2 storage path */
				if (false == mImageStoragePath.mkdirs()) {
					Log.e(TAG, "Unable to create storage path");
					return false;
				}
			}
		}
		
		File f = ctx.getFileStreamPath("temp");
		mImageStoragePath = f.getParentFile();
				
		return true;
	}

	/*
	 * Return bas epath
	 */
	public String getBasePath() {
		return mImageStoragePath.getAbsolutePath();
	}
}
