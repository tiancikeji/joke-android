package com.jokes.utils;

import java.util.Calendar;
import java.util.Date;

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
	 * 转换日期，获得今天之后n天的日期
	 * @param n
	 * @return
	 */
	public static String getDateAfterFormat_(int n) {  
		Calendar calendar = Calendar.getInstance(); 
		Date date = new Date();
		date = calendar.getTime();
		calendar.setTime(date);    
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + n); 
		return String.format("%1$04d%2$02d%3$02d", 
				calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH));
	}
}
