package com.jokes.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jokes.mywidget.MyToast;
import com.jokes.utils.DataManagerApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingActivity extends Activity implements OnClickListener{

	Button button_back;//返回按钮
	LinearLayout linearlayot_cache;
	TextView textview_cache;//缓存
	FrameLayout framelayout_offlinedownload;//离线下载布局
	TextView textview_offlinedownload;
	TextView textview_downloadpercent;//下载百分比
	FrameLayout framelayout_feedback;//用户反馈
	FrameLayout framelayout_update;//更新
	TextView textview_updatepercent;//更新包下载进度
	FrameLayout framelayout_contactus;//联系我们
	
	Context context;
	DataManagerApp dataManagerApp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.setting_activity);
		
		context = getApplicationContext();
		dataManagerApp = ((DataManagerApp)context);
		init();
		setView();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.setting_button_back:
			finish();
			break;
		case R.id.setting_linearlayout_cache:
			String a[] = null;
			//清除数据
			a = context.fileList();
			for(int i = 0; i < a.length;i++){
				context.deleteFile(a[i]);
			}
			textview_cache.setText("清除缓存（已用0M）");
			MyToast toast = new MyToast(context,"缓存已清除");
			toast.startMyToast();
			break;
		case R.id.setting_framelayout_offlinedownload:
			break;
		case R.id.setting_framelayout_feedback:
			Intent intent = new Intent(SettingActivity.this,FeedbackActivity.class);
			startActivity(intent);
			break;
		case R.id.setting_framelayout_update:
			break;
		case R.id.setting_framelayout_contactus:
			break;
		}
	}
	
	private void init(){
		button_back = (Button)findViewById(R.id.setting_button_back);
		linearlayot_cache = (LinearLayout)findViewById(R.id.setting_linearlayout_cache);
		textview_cache = (TextView)findViewById(R.id.setting_textview_cache);
		framelayout_offlinedownload = (FrameLayout)findViewById(R.id.setting_framelayout_offlinedownload);
		textview_offlinedownload = (TextView)findViewById(R.id.setting_textview_offlinedownload);
		textview_downloadpercent = (TextView)findViewById(R.id.setting_textview_downloadpercent);
		framelayout_feedback = (FrameLayout)findViewById(R.id.setting_framelayout_feedback);
		framelayout_update = (FrameLayout)findViewById(R.id.setting_framelayout_update);
		textview_updatepercent = (TextView)findViewById(R.id.setting_textview_updatepercent);
		textview_updatepercent.setVisibility(View.GONE);
		framelayout_contactus = (FrameLayout)findViewById(R.id.setting_framelayout_contactus);
	
		button_back.setOnClickListener(this);
		linearlayot_cache.setOnClickListener(this);
		framelayout_offlinedownload.setOnClickListener(this);
		framelayout_feedback.setOnClickListener(this);
		framelayout_update.setOnClickListener(this);
		framelayout_contactus.setOnClickListener(this);
	}
	
	private void setView(){
		int cache = getCache();
		textview_cache.setText("清除缓存（已用"+cache+"）");
	}
	
	/**
	 * 获取缓存大小
	 */
	private int getCache(){
		//清空上次的缓冲数据
		int cache = 0;
		String b[] = null;
		FileInputStream fin;
		b = this.fileList();
		for(int i = 0; i < b.length;i++){
			try {
				fin = openFileInput(b[i]);
				try {
					cache += fin.available();
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
		return cache;
	}

}