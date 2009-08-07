package com.jhlee.rr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class RREditor extends Activity {
	private static final String TAG	= "RREditor";
	private RRDbAdapter mDbAdapter;
	private Cursor mCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_editor);
		
		mDbAdapter = new RRDbAdapter(this);
		
		/** Get receipt id */
		Intent i = this.getIntent();
		if(null == i) {
			this.showErrorMessage("Intent should be given");
			return;
		}
		long rid = i.getExtras().getLong("EDIT_RID", -1);
		if(rid == -1) {
			this.showErrorMessage("Proper rid is not given");
			return;
		}
		
		/** Get receipt information */
		mCursor = mDbAdapter.queryReceipt((int)rid);
		if(null == mCursor) {
			this.showErrorMessage("Unable to get db cursor");
			return;
		}
		this.startManagingCursor(mCursor);
		
		/** Load receipt image */
		int colIndexImgFile = mCursor.getColumnIndex(RRDbAdapter.KEY_RECEIPT_IMG_FILE);
		int colCount = mCursor.getColumnCount();
		String imgFilePath = mCursor.getString(colIndexImgFile);
		Bitmap bmp = BitmapFactory.decodeFile(imgFilePath);
		if(null == bmp) {
			this.showErrorMessage("Unable to load file:" + imgFilePath);
			return;
		}
		
		// Set image.
		ImageView imgView = (ImageView)this.findViewById(R.id.ReceiptView);
		imgView.setImageBitmap(bmp);
	}
	
	private void showErrorMessage(String msg) {
		Log.e(TAG, msg);
		
		final Activity self = this;
        new AlertDialog.Builder(this)
        .setIcon(R.drawable.alert_dialog_icon)
        .setTitle(R.string.error)
        .setMessage(msg)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {                
            	self.finish();
            }
        }).show();	
		return;
	}
	
}
