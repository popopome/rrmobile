package com.jhlee.rr;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RRDbAdapter {

	private static final int DB_VERSION = 11;

	/* KEYS for RECEIPT TABLE */
	public static final String KEY_RECEIPT_IMG_FILE = "img_file";
	public static final String KEY_RECEIPT_SMALL_IMG_FILE = "small_img_file";
	public static final String KEY_RECEIPT_TAKEN_DATE = "taken_date";
	public static final String KEY_RECEIPT_TAKEN_DATE_AS_STRING = "taken_date_as_string";
	public static final String KEY_RECEIPT_TAKEN_DAY_OF_WEEK = "taken_day_of_week";
	public static final String KEY_RECEIPT_TAKEN_DAY_OF_MONTH = "taken_day_of_month";
	public static final String KEY_RECEIPT_TOTAL = "total";

	/* KEYS for TAG SOURCE TABLE */
	public static final String KEY_TAG_SOURCE_ID = "_id";
	public static final String KEY_TAG_SOURCE_TAG = "tag_name";

	/* KEYS for PHOTO TAG TABLE */
	public static final String KEY_PHOTO_TAG_ID = "_id";
	public static final String KEY_PHOTO_TAG_TAG = "tag_name";
	public static final String KEY_PHOTO_TAG_RECEIPT_ID = "receipt_id";
	public static final int COL_PHOTO_TAG_TAG = 1;

	private static final String DB_NAME = "RRDB";

	private static final String TABLE_RECEIPT = "receipt";
	private static final String TABLE_MARKER = "marker";
	private static final String TABLE_TAG_SOURCE = "tag_source";
	private static final String TABLE_PHOTO_TAG = "photo_tag";
	private static final String RECEIPT_TABLE_CREATE_SQL = "CREATE TABLE receipt("
			+ " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " img_file TEXT NOT NULL,"
			+ " small_img_file TEXT NOT NULL,"
			+ " taken_date INTEGER NOT NULL,"
			+ " taken_date_as_string TEXT NOT NULL,"
			+ " taken_day_of_week INTEGER NOT NULL,"
			+ " taken_day_of_month INTEGER NOT NULL,"
			+ " geo_coding TEXT, "
			+ " total INTEGER NOT NULL,"
			+ " sync_id INTEGER);";
	private static final String MARKER_TABLE_CREATE_SQL = "CREATE TABLE marker("
			+ " marker_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " rid INTEGER NOT NULL, "
			+ " marker_name TEXT,"
			+ " marker_type INTEGER NOT NULL,"
			+ " x INTEGER NOT NULL, "
			+ " y INTEGER NOT NULL, "
			+ " width INTEGER, "
			+ " height INTEGER);";
	/* The table maintains tags which are set by user for specified receipt */
	private static final String PHOTO_TAGS_TABLE_CREATE_SQL = "CREATE TABLE photo_tag("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "tag_name TEXT NOT NULL," + "receipt_id INTEGER NOT NULL);";

	private static final String TAG_SOURCE_TABLE_CREATE_SQL = "CREATE TABLE tag_source("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "tag_name TEXT NOT NULL" + ");";
	private static final String TAG = "RRDbAdapter";

	private static final long TAG_STRING_FORMAT_MULTI_LINE = 1;
	private static final long TAG_STRING_FORMAT_COMMA_SEP = 2;

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
		Calendar today = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		long todayInMillis = today.getTimeInMillis();
		/* Insert date information */
		vals.put(KEY_RECEIPT_TAKEN_DATE, todayInMillis);
		vals.put(KEY_RECEIPT_TAKEN_DATE_AS_STRING, RRUtil.formatGMTCalendar(todayInMillis));
		vals.put(KEY_RECEIPT_TAKEN_DAY_OF_WEEK, today.get(Calendar.DAY_OF_WEEK));
		vals.put(KEY_RECEIPT_TAKEN_DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));

		/**
		 * Set total money as zero. 0 means N/A.
		 */
		vals.put(KEY_RECEIPT_TOTAL, 0);

		return mDb.insert(TABLE_RECEIPT, null, vals);
	}

	/** Query receipt by daily */
	public Cursor queryReceiptByDaily() {
		Cursor c = mDb.query(TABLE_RECEIPT, new String[] { "_id",
				"COUNT(*) AS CNT", "SUM(TOTAL) AS TOTAL_EXPENSE",
				KEY_RECEIPT_IMG_FILE, KEY_RECEIPT_TAKEN_DATE }, null, null,
				KEY_RECEIPT_TAKEN_DATE_AS_STRING, null, KEY_RECEIPT_TAKEN_DATE);
		if (c != null)
			c.moveToFirst();
		return c;
	}

	/**
	 * Query receipt information
	 * 
	 * @param rid
	 *            Receipt id
	 * @return
	 */
	public Cursor queryReceipt(int rid) {
		Cursor c = mDb.query(TABLE_RECEIPT, null, "_id=" + rid, null, null,
				null, null);
		if (c != null)
			c.moveToFirst();

		return c;
	}

	/**
	 * Query all receipts
	 * 
	 * @return Cursor
	 */
	public Cursor queryAllReceipts() {
		return mDb.query(TABLE_RECEIPT, null, null, null, null, null,
				KEY_RECEIPT_TAKEN_DATE);
	}

	/**
	 * Update total money
	 * 
	 * @param rid
	 * @param dollars
	 * @param cents
	 */
	public void updateTotalMoney(int rid, int dollars, int cents) {
		int encoded = dollars * 100 + cents;
		ContentValues vals = new ContentValues();
		vals.put(KEY_RECEIPT_TOTAL, encoded);
		int numRows = mDb.update(TABLE_RECEIPT, vals, "_id="
				+ Integer.toString(rid), null);
		if (numRows != 1) {
			Log.e(TAG, "Unable to update row:rid=" + Integer.toString(rid));
		}
	}

	/*
	 * Update date
	 */
	public boolean updateDate(Cursor cursor, long millis) {
		ContentValues vals = new ContentValues();

		Calendar tmpCalendar = new GregorianCalendar(new SimpleTimeZone(0,
				"GMT"));
		tmpCalendar.clear();
		tmpCalendar.setTimeInMillis(millis);

		/* Insert date to DB */
		vals.put(KEY_RECEIPT_TAKEN_DATE, tmpCalendar.getTimeInMillis());
		vals.put(KEY_RECEIPT_TAKEN_DATE_AS_STRING, RRUtil.formatGMTCalendar(tmpCalendar.getTimeInMillis()));
		vals.put(KEY_RECEIPT_TAKEN_DAY_OF_WEEK, tmpCalendar.get(Calendar.DAY_OF_WEEK));
		vals.put(KEY_RECEIPT_TAKEN_DAY_OF_MONTH, tmpCalendar.get(Calendar.DAY_OF_MONTH));

		/* Assume 0th index is id */
		int rid = cursor.getInt(0);

		/* Update db */
		int numRows = mDb.update(TABLE_RECEIPT, vals, "_id="
				+ Integer.toString(rid), null);
		if (numRows != 1) {
			Log.e(TAG, "Unable to update row for date:rid="
					+ Integer.toString(rid));
			return false;
		}

		return true;
	}

	/*
	 * Query all tags from TAG SOURCE Sort by tag name
	 */
	public Cursor queryAllTags() {
		return mDb.query(TABLE_TAG_SOURCE, null, null, null, null, null,
				"tag_name");
	}

	/*
	 * Create tag into TAG SOURCE
	 */
	public boolean createTag(String tagName) {
		tagName = tagName.toLowerCase();

		/* Check duplication */
		if (-1 != findTag(tagName)) {
			Log.v(TAG, "Tag already exists:tagName=" + tagName);
			return true;
		}

		ContentValues vals = new ContentValues();
		vals.put(KEY_TAG_SOURCE_TAG, tagName);
		long rowId = mDb.insert(TABLE_TAG_SOURCE, null, vals);
		return (rowId != -1) ? true : false;
	}

	/*
	 * Find tag within TAG SOURCE
	 */
	public long findTag(String tagName) {
		tagName = tagName.toLowerCase();

		Cursor cursor = null;
		try {
			cursor = mDb.query(TABLE_TAG_SOURCE, null, "tag_name='" + tagName
					+ "'", null, null, null, null);
		} catch (SQLiteException err) {
			err.printStackTrace();
			Log.e(TAG, "Query is failed: tagName=" + tagName);
			return -1;
		}

		if (null == cursor)
			return -1;

		long result = -1;
		if (cursor.getCount() == 1) {
			/* First column is id */
			cursor.moveToFirst();
			result = cursor.getInt(0);
		}

		cursor.close();
		cursor = null;
		return result;
	}

	public boolean addTagToReceipt(long receiptId, String tagName) {
		tagName = tagName.toLowerCase();

		/* Check dup. */
		if (true == doesReceiptHaveTag(receiptId, tagName))
			return true;

		ContentValues vals = new ContentValues();
		vals.put(KEY_PHOTO_TAG_TAG, tagName);
		vals.put(KEY_PHOTO_TAG_RECEIPT_ID, receiptId);
		long id = mDb.insert(TABLE_PHOTO_TAG, null, vals);
		if (id == -1) {
			Log.e(TAG, "Unable to add tag to receipt:tag=" + tagName);
			return false;
		}

		return true;

	}

	/*
	 * Check given receipt has specified tag.
	 */
	public boolean doesReceiptHaveTag(long receiptId, String tagName) {
		tagName = tagName.toLowerCase();

		boolean bresult = true;
		Cursor cursor = mDb.query(TABLE_PHOTO_TAG, null, "tag_name='" + tagName
				+ "' and receipt_id=" + Long.toString(receiptId), null, null,
				null, null);
		if (null == cursor)
			bresult = false;
		else {
			if (1 != cursor.getCount())
				bresult = false;

			cursor.close();
		}

		return bresult;
	}

	/*
	 * Remove tag from given receipt
	 */
	public boolean removeTagFromReceipt(long receiptId, String tagName) {
		tagName = tagName.toLowerCase();

		if (false == doesReceiptHaveTag(receiptId, tagName)) {
			/* We have no item to delete */
			Log.v(TAG, "No item is found within photo tag db:tagName="
					+ tagName);
			return false;
		}

		int cnt = mDb.delete(TABLE_PHOTO_TAG, "tag_name='" + tagName
				+ "' and receipt_id=" + Long.toString(receiptId), null);
		if (cnt == 0) {
			Log.v(TAG, "Unable to delete tag from photo:tagName=" + tagName);
			return false;
		}

		return true;
	}

	public String queryReceiptTagsAsMultiLineString(long receiptId) {
		return queryReceiptTagsAsString(receiptId, TAG_STRING_FORMAT_MULTI_LINE);
	}

	/*
	 * Query receipt tags as one line string.
	 */
	public String queryReceiptTagsAsOneString(long receiptId) {
		return queryReceiptTagsAsString(receiptId, TAG_STRING_FORMAT_COMMA_SEP);
	}

	private String queryReceiptTagsAsString(long receiptId, long format) {
		Cursor cursor = mDb.query(TABLE_PHOTO_TAG, null, "receipt_id="
				+ Long.toString(receiptId), null, null, null, "tag_name");
		if (null == cursor)
			return "";

		if (cursor.getCount() < 1) {
			cursor.close();
			return "";
		}

		StringBuilder sb = new StringBuilder();
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			sb.append(cursor.getString(COL_PHOTO_TAG_TAG));
			if (false == cursor.isLast()) {
				if (format == TAG_STRING_FORMAT_MULTI_LINE)
					sb.append("\n");
				else if (format == TAG_STRING_FORMAT_COMMA_SEP)
					sb.append(",");
			}
			cursor.moveToNext();
		}

		cursor.close();
		cursor = null;
		String tagStr = sb.toString();
		sb = null;
		return tagStr;
	}

	public long getMaxExpense() {
		Cursor cursor = mDb.query(TABLE_RECEIPT, new String[] { "max(total)" },
				null, null, null, null, null);
		if (null == cursor)
			return 0;
		
		long val = 0;
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			val = cursor.getLong(0);
		}
		cursor.close();
		return val;
	}

	public Cursor queryExpenseDayByDay() {
		Cursor cursor = mDb.query(TABLE_RECEIPT, new String[] { "taken_date",
				"sum(total)" }, null, null, KEY_RECEIPT_TAKEN_DATE_AS_STRING, null, KEY_RECEIPT_TAKEN_DATE);
		return cursor;
	}
	
	public Cursor queryMostExpensiveExpense() {
		Cursor cursor = mDb.query(TABLE_RECEIPT, null,
				"total = (select max(total) from receipt)",
				null, null, null, null);
		return cursor;
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
			db.execSQL("DROP TABLE IF EXISTS photo_tag");
			db.execSQL("DROP TABLE IF EXISTS tag_source");

			this.onCreate(db);
		}
	}

}
