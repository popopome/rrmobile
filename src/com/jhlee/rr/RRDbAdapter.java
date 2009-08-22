package com.jhlee.rr;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RRDbAdapter {

	public static final String KEY_RECEIPT_IMG_FILE = "img_file";
	public static final String KEY_RECEIPT_SMALL_IMG_FILE = "small_img_file";
	public static final String KEY_RECEIPT_TAKEN_DATE = "taken_date";
	public static final String KEY_RECEIPT_TAKEN_TIME = "taken_time";
	public static final String KEY_RECEIPT_TOTAL = "total";

	private static final String DB_NAME = "RRDB";
	private static final int DB_VERSION = 7;
	private static final String TABLE_RECEIPT = "receipt";
	private static final String TABLE_MARKER = "marker";
	private static final String RECEIPT_TABLE_CREATE_SQL = "CREATE TABLE receipt("
			+ " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " img_file TEXT NOT NULL,"
			+ " small_img_file TEXT NOT NULL,"
			+ " taken_date TEXT NOT NULL,"
			+ " taken_time TEXT NOT NULL,"
			+ " geo_coding TEXT, "
			+ " total INTEGER NOT NULL," + " sync_id INTEGER);";
	private static final String MARKER_TABLE_CREATE_SQL = "CREATE TABLE marker("
			+ " marker_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " rid INTEGER NOT NULL, "
			+ " marker_name TEXT,"
			+ " marker_type INTEGER NOT NULL,"
			+ " x INTEGER NOT NULL, "
			+ " y INTEGER NOT NULL, "
			+ " width INTEGER, "
			+ " height INTEGER);";
	private static final String PHOTO_TAGS_TABLE_CREATE_SQL = "CREATE TABLE photo_tag("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ "tag_name TEXT NOT NULL);";
	
	private static final String TAG_SOURCE_TABLE_CREATE_SQL = "CREATE TABLE tag_source("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ "tag_name TEXT NOT NULL"
		+ ");";
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
		vals.put(KEY_RECEIPT_TAKEN_DATE, RRUtil.getTodayDateString());
		vals.put(KEY_RECEIPT_TAKEN_TIME, RRUtil.getCurrentTimeString());
		
		/** Set total money as zero.
		 *  0 means N/A.
		 */
		vals.put(KEY_RECEIPT_TOTAL, 0);

		return mDb.insert(TABLE_RECEIPT, null, vals);
	}

	/** Query receipt by daily */
	public Cursor queryReceiptByDaily() {
		Cursor c = mDb.query(TABLE_RECEIPT, new String[] {
				"_id",
				"COUNT(*) AS CNT",
				"SUM(TOTAL) AS TOTAL_EXPENSE",
				KEY_RECEIPT_IMG_FILE,
				KEY_RECEIPT_TAKEN_DATE }, null, null, KEY_RECEIPT_TAKEN_DATE,
				null, KEY_RECEIPT_TAKEN_DATE);
		if(c != null)
			c.moveToFirst();
		return c;
	}
	
	/**
	 * Query receipt information
	 * @param rid	Receipt id
	 * @return
	 */
	public Cursor queryReceipt(int rid) {
		Cursor c = mDb.query(TABLE_RECEIPT, null, "_id=" + rid, 
					null, null, null, null);
		if(c != null) 
			c.moveToFirst();
		
		return c;
	}
	
	/** Query all receipts
	 * 
	 * @return Cursor
	 */
	public Cursor queryAllReceipts() {
		return mDb.query(TABLE_RECEIPT, null, null, null, null, null, 
				KEY_RECEIPT_TAKEN_DATE + "," + KEY_RECEIPT_TAKEN_TIME );
	}
	
	/**
	 * Update total money
	 * @param rid
	 * @param dollars
	 * @param cents
	 */
	public void updateTotalMoney(int rid, int dollars, int cents) {
		int encoded = dollars * 100 + cents;
		ContentValues vals = new ContentValues();
		vals.put(KEY_RECEIPT_TOTAL, encoded);
		int numRows = mDb.update(TABLE_RECEIPT, vals, "_id="+Integer.toString(rid), null);
		if(numRows != 1) {
			Log.e(TAG, "Unable to update row:rid=" + Integer.toString(rid));
		}
	}
	
	/*
	 * Update date
	 */
	public boolean updateDate(Cursor cursor, long millis) {
		ContentValues vals = new ContentValues();
		
		Calendar tmpCalendar = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		tmpCalendar.clear();
		tmpCalendar.setTimeInMillis(millis);
		
		/* Date formatting */
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		formatter.setTimeZone(tmpCalendar.getTimeZone());
		String dateStr = formatter.format(tmpCalendar.getTime());
		
		/* Insert date to DB */
		vals.put(KEY_RECEIPT_TAKEN_DATE, dateStr);

		/* Assume 0th index is id */
		int rid = cursor.getInt(0);
		
		/* Update db */
		int numRows = mDb.update(TABLE_RECEIPT, vals, "_id="+Integer.toString(rid), null);
		if(numRows != 1) {
			Log.e(TAG, "Unable to update row for date:rid=" + Integer.toString(rid));
			return false;
		}
		
		return true;
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
			db.execSQL(PHOTO_TAGS_TABLE_CREATE_SQL);
			db.execSQL(TAG_SOURCE_TABLE_CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/** Drop table first */
			Log.v(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);
			db.execSQL("DROP TABLE IF EXISTS receipt");
			db.execSQL("DROP TABLE IF EXISTS marker");
			db.execSQL("DROP TABLE IF EXISTS photo_tags");
			db.execSQL("DROP TABLE IF EXISTS tag_source");
			
			this.onCreate(db);
		}
	}
}
