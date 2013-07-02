package com.jokes.utils;

import com.umeng.analytics.MobclickAgent;

import android.content.Context;

public class UmengAnaly {

	/**
	 * 友盟统计：清除缓存 
	 * @param context
	 */
	public static void AnalyCache(Context context){
		MobclickAgent.onEvent(context, "Cache");
	}
	/**
	 * 友盟统计：离线下载
	 * @param context
	 */
	public static void AnalyOffLineDownload(Context context){
		MobclickAgent.onEvent(context, "Offline_download");
	}
	/**
	 * 友盟统计：下拉刷新
	 * @param context
	 */
	public static void AnalyRefrash(Context context){
		MobclickAgent.onEvent(context, "Refresh");
	}
	/**
	 * 友盟统计：分享微博 
	 * @param context
	 */
	public static void AnalyShareWX(Context context){
		MobclickAgent.onEvent(context, "Share_WX");
	}
	/**
	 * 友盟统计：进入用户反馈
	 * @param context
	 */
	public static void AnalyOnClickFeedback(Context context){
		MobclickAgent.onEvent(context, "OnClick_feedback");
	}
	/**
	 * 友盟统计：用户录制反馈音频
	 * @param context
	 */
	public static void AnalyOnClickFeedbaceRocord(Context context){
		MobclickAgent.onEvent(context, "OnClick_feedback_rocord");
	}
	/**
	 * 友盟统计：发送用户反馈 
	 * @param context
	 */
	public static void AnalyOnClickFeedbackSend(Context context){
		MobclickAgent.onEvent(context, "OnClick_feedback_send");
	}
	/**
	 * 友盟统计：进入录音 
	 * @param context
	 */
	public static void AnalyOnClickRecord(Context context){
		MobclickAgent.onEvent(context, "OnClick_record");
	}
	/**
	 * 友盟统计：在发布笑话页面录音 
	 * @param context
	 */
	public static void AnalyOnClickRecordRecord(Context context){
		MobclickAgent.onEvent(context, "OnClick_record_record");
	}
	/**
	 * 友盟统计：添加图片
	 * @param context
	 */
	public static void AnalyOnClickRecordAddPic(Context context){
		MobclickAgent.onEvent(context, "OnClick_record_addPic");
	}
	/**
	 * 友盟统计：发布笑话 
	 * @param context
	 */
	public static void AnalyOnClickRecordSend(Context context){
		MobclickAgent.onEvent(context, "OnClick_record_send");
	}
}
