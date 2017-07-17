package com.zby.chest.model;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteHelp extends SQLiteOpenHelper {

	protected SqliteHelp(Context mContext) {
		super(mContext, "LockData", null,2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + DeviceTable.Table_Name);
		createTable(db);
	}
	
	private void createTable(SQLiteDatabase db) {
		db.execSQL(DeviceTable.getTable());
	}
	
	

}
