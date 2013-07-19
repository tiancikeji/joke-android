package com.jokes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBase {
	public static final String DB_NAME = "Jokes.db";
	public static final int DATABASE_VERSION = 1;
	
	public static final String TB_OFFLINE = "tb_offline";
	public static final String OFFLINEID = "_id";//自增列
	public static final String OFFLINE_JOKE_ID = "offline_joke_id";//笑话id
	public static final String OFFLINE_UID = "offline_uid";
	public static final String OFFLINE_NUM_PLAYS = "offline_num_plays";
	public static final String OFFLINE_NUM_LIKES = "offline_num_likes";
	public static final String OFFLINE_LENGTH = "offline_length";
	public static final String OFFLINE_FULLAUDIO_URL = "offline_fullaudio_url";
	public static final String OFFLINE_FULLPICTURE_URL = "offline_fullpicture_url";
	public static final String OFFLINE_PICTURE_SIZE_IN_B = "offline_picture_size_in_b";
	public static final String OFFLINE_AUDIO_SIZE_IN_B = "offline_audio_size_in_b";
//	public static final String OFFLINE_CREATEAT = "createdAt";
	public static final String OFFLINE_APPROVAL_TIME = "offline_approval_time";
	
	private Context context;
	private SQLiteDatabase sqliteDatabase;
	private JokesDAO dao; 
	public DataBase(Context context){
		this.context = context;
		dao = new JokesDAO(context, DB_NAME, null, DATABASE_VERSION);
	}
	
	public void open(){
		if(sqliteDatabase == null){
			sqliteDatabase = dao.getWritableDatabase();
		}
	}
	
	public void close() {
		sqliteDatabase.close();
	}
	
	public boolean isOpen() {
		if (sqliteDatabase != null) {
			return sqliteDatabase.isOpen();
		}
		return false;
	}
	
	public void beginTransaction() {
		sqliteDatabase.beginTransaction();
	}

	public void endTransaction() {
		sqliteDatabase.setTransactionSuccessful();
		sqliteDatabase.endTransaction();
	}
	
	/**
	 * 保存数据
	 * @param values
	 * @return
	 */
	public long saveOffLineJokes(ContentValues values){
		return this.sqliteDatabase.insert(TB_OFFLINE, null, values);
	}
	/**
	 * 获取离线笑话
	 * @return
	 */
	public Cursor getOffLineJokes(){
		return this.sqliteDatabase.query(TB_OFFLINE, null, null, null, null, null, null);
	}
	/**
	 * 删除离线笑话
	 */
	public void deleteAllMyDriveTrip(){
		String sqlCmd = "delete from "+TB_OFFLINE;
		sqliteDatabase.execSQL(sqlCmd);
	}
	
	class JokesDAO extends SQLiteOpenHelper{

//		public JokesDAO(Context context) {
//			super(context, DB_NAME, null, DATABASE_VERSION);
//			// TODO Auto-generated constructor stub
//		}

		public JokesDAO(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("Create table "+TB_OFFLINE+
					"(_id integer primary key autoincrement,"
					+OFFLINE_JOKE_ID  +" text,"
					+OFFLINE_UID    +" text,"
					+OFFLINE_NUM_PLAYS  +" text,"
					+OFFLINE_NUM_LIKES  +" text,"
					+OFFLINE_LENGTH  +" text,"
					+OFFLINE_FULLAUDIO_URL   +" text,"
					+OFFLINE_FULLPICTURE_URL  +" text,"
					+OFFLINE_PICTURE_SIZE_IN_B  +" text,"
					+OFFLINE_APPROVAL_TIME +" text,"
					+OFFLINE_AUDIO_SIZE_IN_B + " text )"
			);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}

		
	}
}
