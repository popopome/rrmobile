package com.jhlee.rr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RRDbAdapter {

	private static final String DB_NAME = "RRDB";
	private static final int DB_VERSION = 1;
	private static final String RECEIPT_TABLE_CREATE_SQL = "CREATE TABLE receipt("
			+ " rid INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " img_file TEXT NOT NULL,"
			+ " taken_date DATE NOT NULL,"
			+ " geo_coding TEXT, " + " total INTEGER," + " sync_id INTEGER);";
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
	private SQLiteDatabase mDb;
	private DbHelper	mDbHelper;

	/** CTOR */
	public RRDbAdapter(Context ctx) {
		mDbHelper = new DbHelper(ctx, mDb);
	}

	/**
	 * The class maintains database and manages its version.
	 * 
	 * @author jhlee
	 */
	private static class DbHelper extends SQLiteOpenHelper {
		private SQLiteDatabase mDb;

		public DbHelper(Context ctx, SQLiteDatabase db) {
			super(ctx, DB_NAME, null, DB_VERSION);
			mDb = db;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
			 * Later sync feature is finalized, we'll update it. final String
			 * SYNC_TABLE_CREATE_SQL = "";
			 */
			mDb.execSQL(RECEIPT_TABLE_CREATE_SQL);
			mDb.execSQL(MARKER_TABLE_CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/** Drop table first */
			Log.v(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);
			mDb.execSQL("DROP TABLE IF EXISTS receipt");
			mDb.execSQL("DROP TABLE IF EXISTS marker");
		}
	}
}
