package com.jokes.utils;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Tools {

	/**
	 * 获取日期并格式化
	 * @return
	 */
	public static String getTodayToString() {
		// 转换日期，获得今天之后n天的日期
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		date = calendar.getTime();
		calendar.setTime(date);
		return String.format("%1$04d-%2$02d-%3$02d",
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH));

	}
	
	/**
	 * 获取日期并格式化
	 * @return
	 */
	public static String getTodayFormat_() {
		// 转换日期，获得今天之后n天的日期
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		date = calendar.getTime();
		calendar.setTime(date);
		return String.format("%1$04d%2$02d%3$02d",
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH));

	}

	/**
	 * 转换日期，获得今天之后n天的日期
	 * @param n
	 * @return
	 */
	public static String getDateFormat_(String date){
		String temp_date = null;
		
		String[] array = date.split(" ")[0].split("-");
		temp_date = array[0]+array[1]+array[2];
		return temp_date;
				
	}
//	public static String getDateAfterFormat_(int n) {  
//		Calendar calendar = Calendar.getInstance(); 
//		Date date = new Date();
//		date = calendar.getTime();
//		calendar.setTime(date);    
//		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + n); 
//		return String.format("%1$04d%2$02d%3$02d", 
//				calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH));
//	}

	/**
	 * 判断wifi是否可用
	 */
	public static boolean isWiFiActive(Context _context){
		Context temp_context = _context.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) temp_context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivity != null){
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if(info != null){
				for(int i=0; i < info.length; i++){
					if(info[i].getTypeName().equals("WIFI") && info[i].isConnected()){
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 判断网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}


}
