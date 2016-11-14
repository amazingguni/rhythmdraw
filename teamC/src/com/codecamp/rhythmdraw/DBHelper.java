package com.codecamp.rhythmdraw;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{
	public DBHelper(Context context){
		super(context, "theraphy.db",null,30);
	}	
	public void onCreate(SQLiteDatabase db){
		//Mp3 리스트
		db.execSQL("CREATE TABLE mp3list (mp3name TEXT UNIQUE, duration INTEGER, title TEXT);"); 
		//검출된 Beat 데이터
		db.execSQL("CREATE TABLE beat (mp3name TEXT, time INTEGER);");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){	
		db.execSQL("DROP TABLE IF EXISTS mp3list");		
		db.execSQL("DROP TABLE IF EXISTS beat");
		onCreate(db);
	}

}

