package com.jokes.utils;


public class HandlerCodes {
	
	public static final int GET_JOKES_SUCCESS = 0;
	public static final int GET_JOKES_FAILURE = 1;
	public static final int GET_JOKES_REFRESH_SUCCESS = 28;//更新笑话成功，笑话有更新的情况下使用
	
	public static final int LIKE_SUCCESS = 2;
	public static final int LIKE_FAILURE = 3;
	
	public static final int UNLIKE_SUCCESS = 4;
	public static final int UNLIKE_FAILURE = 5;
	
	public static final int IMG_UPLOAD_SUCCESS = 6;
	public static final int IMG_UPLOAD_FAILURE = 7;
	
	public static final int AUDIO_UPLOAD_SUCCESS = 8;
	public static final int AUDIO_UPLOAD_FAILURE = 9;
	
	public static final int CREATE_JOKE_SUCCESS = 10;
	public static final int CREATE_JOKE_FAILURE = 11;
	
	public static final int GET_LIKEJOKES_SUCCESS = 12;
	public static final int GET_LIKEJOKES_FAILURE = 13;
	
	public static final int CREATE_FEEDBACK_SUCCESS = 14;
	public static final int CREATE_FEEDBACK_FAILURE = 15;
	

	public static final int MESSAGE_SHARE = 16;//通知开启分享功能
	
	public static final int ADD_PLAY_SUCCESS = 17;
	public static final int ADD_PLAY_FAILURE = 18;
	
	public static final int CHECK_UPDATE_SUCCESS = 19;//检查升级成功
	public static final int CHECK_UPDATE_FAILURE = 20;//检查升级失败
	
	public static final int DOWNLOAD_APK = 21;//下载apk进度更新
	
	public static final int CONNECTION_FAILURE = 22;//联网失败
	
	public static final int DOWNLOADIMAGE_SUCCESS = 23;//下载图片成功
	public static final int DOWNLOAD_OFFLINE_PICTURE = 24;//下载离线数据进度更新
	public static final int DOWNLOAD_OFFLINE_JOKE = 25;//下载离线数据进度更新
	public static final int DOWNLOAD_OFFLINE_JOKE_FINISH = 26;//一条笑话下载完成
	
	public static final int GET_JOKES_NULL = 27;//获取笑话列表为空，用户以获取所有笑话。
	
	public static final int CLOSE = 99;


}
