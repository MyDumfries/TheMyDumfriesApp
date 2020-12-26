package com.mydumfries.themydumfriesapp;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/** Helper to the database, manages versions and creation */
public class EventDataSQLHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "mydumfries.db";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "places";
	public static final String TABLE3 = "places2";
	public static final String TABLE4 = "tempplaces";
	
	// Columns
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String ADDR1 = "addr1";
	public static final String ADDR2 = "addr2";
	public static final String POSTCODE = "postcode";
	public static final String CAT1 = "cat1";
	public static final String CAT2 = "cat2";
	public static final String CAT3 = "cat3";
	public static final String CAT4 = "cat4";
	public static final String CAT5 = "cat5";
	public static final String DESCRIPTION = "description";
	public static final String URL = "url";
	public static final String MORE = "more";
	public static final String LAT = "lat";
	public static final String LNG = "lng";
	public static final String PLACENAME="Placename";

	public EventDataSQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key, " + NAME + " text, " + ADDR1
				+ " text, " + ADDR2 + " text, " + POSTCODE + " text, "
				+ CAT1	+ " text, " + CAT2 + " text, " + CAT3 + " text, "
				+ CAT4 + " text, " + CAT5 + " text, "
				+ DESCRIPTION + " text, " + URL + " text, "
				+ MORE + " text, " + LAT + " text, " + LNG + " text);";
		Log.d("EventsData", "onCreate: " + sql); 
		db.execSQL(sql);
		String sql2 = "create table " + TABLE3 + "( " + BaseColumns._ID
				+ " integer primary key, " + PLACENAME + " text, "
				+ LNG + " text, " + LAT + " text);";
		Log.d("EventsData", "onCreate: " + sql2);
		db.execSQL(sql2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;
		String sql = null;
		if (oldVersion == 1)
			sql = "alter table " + TABLE + " add COLUMN timestamp text;";
		if (oldVersion == 2)
			sql = "";

		Log.d("EventsData", "onUpgrade	: " + sql);
		if (sql != null)
			db.execSQL(sql);
	}
}