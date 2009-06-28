package com.jhlee.rr;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

public class RRDbAdapter {

	private static final String KEY_RECEIPT_IMG_FILE = "img_file";
	private static final String KEY_RECEIPT_SMALL_IMG_FILE = "small_img_file";
	private static final String KEY_RECEIPT_TAKEN_DATE = "taken_date";

	private static final String DB_NAME = "RRDB";
	private static final int DB_VERSION = 2;
	private static final String TABLE_RECEIPT = "receipt";
	private static final String TABLE_MARKER = "marker";
	private static final String RECEIPT_TABLE_CREATE_SQL = "CREATE TABLE receipt("
			+ " rid INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " img_file TEXT NOT NULL,"
			+ " small_img_file TEXT NOT NULL,"
			+ " taken_date TEXT NOT NULL,"
			+ " geo_coding TEXT, "
			+ " total INTEGER," + " sync_id INTEGER);";
	private static final String MARKER_TABLE_CREATE_SQL = "CREATE TABLE marker("
			+ " marker_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " rid INTEGER NOT NULL, "
			+ " marker_name TEXT,"
			+ " marker_type INTEGER NOT NULL,"
			+ " x INTEGER NOT NULL, "
			+ " y INTEGER NOT NULL, "
			+ " width INTEGER, "
			+ " height INTEGER);";
	private static final String TAG = "RRDbAdapter";
	private DbHelper mDbHelper;
	private SQLiteDatabase mDb;

	/** CTOR */
	public RRDbAdapter(Context ctx) {
		mDbHelper = new DbHelper(ctx);
		mDb = mDbHelper.getWritableDatabase();

	}

	/** Insert receipt to database */
	public long insertReceipt(String imagePath, String smallImagePath) {
		ContentValues vals = new ContentValues();
		vals.put(KEY_RECEIPT_IMG_FILE, imagePath);
		vals.put(KEY_RECEIPT_SMALL_IMG_FILE, smallImagePath);

		/** Format current date/time */
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
		String takenDateStr = formatter.format(new Date());
		vals.put(KEY_RECEIPT_TAKEN_DATE, takenDateStr);

		return mDb.insert(TABLE_RECEIPT, null, vals);
	}

	/** Query receipt by daily */
	public Cursor queryReceiptByDaily() {
		return mDb.query(TABLE_RECEIPT, new String[] { KEY_RECEIPT_IMG_FILE,
				KEY_RECEIPT_TAKEN_DATE }, null, null, KEY_RECEIPT_TAKEN_DATE,
				null, KEY_RECEIPT_TAKEN_DATE);
	}

	/**
	 * The class maintains database and manages its version.
	 * 
	 * @author jhlee
	 */
	private static class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context ctx) {
			super(ctx, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
			 * Later sync feature is finalized, we'll update it. final String
			 * SYNC_TABLE_CREATE_SQL = "";
			 */
			db.execSQL(RECEIPT_TABLE_CREATE_SQL);
			db.execSQL(MARKER_TABLE_CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/** Drop table first */
			Log.v(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);
			db.execSQL("DROP TABLE IF EXISTS receipt");
			db.execSQL("DROP TABLE IF EXISTS marker");
		}
	}
}
