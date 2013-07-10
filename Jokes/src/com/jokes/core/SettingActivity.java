package com.jokes.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.jokes.mywidget.MyToast;
import com.jokes.utils.ApiRequests;
import com.jokes.utils.Constant;
import com.jokes.utils.HandlerCodes;
import com.jokes.utils.UmengAnaly;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

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

	MyToast toast;
	
	//下载apk的大小
	private int downloadedSize = 0;  	//已经下载文件大小
	private int fileSize = 0; 			//文件大小
	private String fileName;			//文件名称
	private File fileAPK; 				//下载的文件
	private String url ;				//下载地址
	boolean isDownloadAPK = false;		//是否正在下载
	
	Handler mainHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case HandlerCodes.CHECK_UPDATE_SUCCESS:
				Bundle bundle = msg.getData();
				String current_version = bundle.getString("current_version");
				url = bundle.getString("url");
				if(isUpdate(current_version)){
					//退出此页面提示是否退出，退出正在下载文件失败
					new AlertDialog.Builder(SettingActivity.this).setTitle("一听到底有新版本可升级").setMessage("版本号为：v"+current_version)
					.setPositiveButton("立即更新",new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,int whichButton)
						{
							textview_updatepercent.setVisibility(View.VISIBLE);
							//启动线程下载apk
							downloadAPK(ApiRequests.buildAbsoluteUrl(url));
						}
					}).setNegativeButton("以后再说",new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,int whichButton)
						{
							textview_updatepercent.setVisibility(View.GONE);
						}
					}).show();
					
				}else{
					isDownloadAPK = false;
					toast = new MyToast(SettingActivity.this,"当前版本为最新版本");
					toast.startMyToast();
				}
				
				break;
			case HandlerCodes.CHECK_UPDATE_FAILURE:
				toast = new MyToast(SettingActivity.this,"检查版本更新失败！");
				toast.startMyToast();
				isDownloadAPK = false;
				break;
			case HandlerCodes.DOWNLOAD_APK:
				//当收到更新视图消息时，计算已完成下载百分比，同时更新进度条信息  
				int progress = (Double.valueOf((downloadedSize * 1.0 / fileSize * 100))).intValue();  
				if (progress == 100) {  
					textview_updatepercent.setText("下载完成");
					isDownloadAPK = false;
					openFile(fileAPK);
				} else {  
					textview_updatepercent.setText(progress+"%");
				}  
				break;
			case HandlerCodes.CONNECTION_FAILURE:
				isDownloadAPK = false;
				toast = new MyToast(SettingActivity.this,"请检查网络连接！");
				toast.startMyToast();
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.setting_activity);

		context = getApplicationContext();
		init();
		setView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.setting_button_back:
			if(isDownloadAPK){
				//退出此页面提示是否退出，退出正在下载文件失败
				new AlertDialog.Builder(this).setTitle("提示").setMessage("确定退出设置页面？退出 此页面软件升级将停止。")
				.setPositiveButton("确定",new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						finish();
					}
				}).setNegativeButton("取消",new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						
					}
				}).show();
			}else{
				finish();
			}
			break;
		case R.id.setting_linearlayout_cache:
			String a[] = null;
			//清除数据
			a = context.fileList();
			for(int i = 0; i < a.length;i++){
				context.deleteFile(a[i]);
			}
			textview_cache.setText("清除缓存（已用0M）");
			//			MyToast toast = new MyToast(SettingActivity.this,"缓存已清除");
			//			toast.startMyToast();
			//友盟统计：清除缓存
			UmengAnaly.AnalyCache(this);
			break;
		case R.id.setting_framelayout_offlinedownload:
			//友盟统计：下载离线
			UmengAnaly.AnalyOffLineDownload(this);
			break;
		case R.id.setting_framelayout_feedback:
			Intent intent = new Intent(SettingActivity.this,FeedbackActivity.class);
			startActivity(intent);
			break;
		case R.id.setting_framelayout_update:
			if(isDownloadAPK){
				toast = new MyToast(SettingActivity.this,"正在下载");
				toast.startMyToast();
			}else{
				Toast.makeText(SettingActivity.this, "正在检测最新版本", Toast.LENGTH_LONG).show();
				//toast = new MyToast(SettingActivity.this,"正在检测最新版本");
				//toast.startMyToast();
				ApiRequests.checkAppUpdate(mainHandler);
			}
			
			break;
		case R.id.setting_framelayout_contactus:
			Intent intent1 = new Intent(SettingActivity.this,ContactusActivity.class);
			startActivity(intent1);
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(isDownloadAPK){
				//退出此页面提示是否退出，退出正在下载文件失败
				new AlertDialog.Builder(this).setTitle("提示").setMessage("确定退出设置页面？退出 此页面软件升级将停止。")
				.setPositiveButton("确定",new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						finish();
					}
				}).setNegativeButton("取消",new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						
					}
				}).show();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void init(){
		button_back = (Button)findViewById(R.id.setting_button_back);
		linearlayot_cache = (LinearLayout)findViewById(R.id.setting_linearlayout_cache);
		textview_cache = (TextView)findViewById(R.id.setting_textview_cache);
		framelayout_offlinedownload = (FrameLayout)findViewById(R.id.setting_framelayout_offlinedownload);
		textview_offlinedownload = (TextView)findViewById(R.id.setting_textview_offlinedownload);
		textview_downloadpercent = (TextView)findViewById(R.id.setting_textview_downloadpercent);
		textview_downloadpercent.setVisibility(View.GONE);
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

	/**
	 * 下载apk
	 */
	private void downloadAPK(String url){
		//清空所有数据，重新下载
		fileAPK = null;
		downloadedSize = 0;
		fileSize = 0;
//		//获取SD卡目录
//		String downloadDir = Environment.getExternalStorageDirectory()+"/jokes/download/";
//		File file = new File(downloadDir);
//		//创建下载目录
//		if(!file.exists()){
//			file.mkdirs();
//		}
//		//获取下载文件名
//		String[] array = url.split("/");
//		fileName = array[array.length-1];

		//启动下载线程
		new downloadTask(url).start();
	}

	public class downloadTask extends Thread {
		String urlStr;

		public downloadTask(String urlStr){
			this.urlStr = urlStr;
		}

		@Override
		public void run() {
			//获取下载文件名
			String[] array = urlStr.split("/");
			fileName = array[array.length-1];
			
			//获取SD卡目录
			String downloadDir = Environment.getExternalStorageDirectory()+"/jokes/download/";
			File tmpFile = new File(downloadDir);  
			if (!tmpFile.exists()) {  
				tmpFile.mkdir();  
			} 
			fileAPK = new File(downloadDir+ fileName);
			if(fileAPK.length() != 0){
				fileAPK.delete();
				fileAPK = new File(downloadDir+ fileName);
			}
			
			try {  
				URL url = new URL(urlStr);  
				try {  
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
					
					InputStream is = conn.getInputStream();
					
					if (conn == null) {
						//通知handler更新组件
						mainHandler.sendEmptyMessage(HandlerCodes.CONNECTION_FAILURE);
		            }else{
		            	isDownloadAPK = true;
		            	FileOutputStream fos = new FileOutputStream(fileAPK);  
						byte[] buf = new byte[1024];
						int count = 0;  
						
						conn.setReadTimeout(10000);
						conn.connect();  
						//获取下载文件的总大小  
						fileSize = conn.getContentLength();
						
						while ((count = is.read(buf)) != -1) {
								//下载进度
								downloadedSize = downloadedSize + count;
									fos.write(buf, 0, count); 
							mainHandler.sendEmptyMessage(HandlerCodes.DOWNLOAD_APK);
						}  
						conn.disconnect();
						fos.close();  
						is.close();
		            }
					  
				} catch (IOException e) {  
					e.printStackTrace(); 
				}  
			} catch (MalformedURLException e) {  
				e.printStackTrace(); 
			}  
		}


	}

	/**
	 * 安装软件
	 * @param file
	 */
	private void openFile(File file) {  
        Log.e("OpenFile", file.getName());  
        Intent intent = new Intent();  
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
        intent.setAction(android.content.Intent.ACTION_VIEW);  
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive"); 
        startActivity(intent); 
    }

	 /**
	  * 判断软件是否需要升级
	  */
	private boolean isUpdate(String version){
		float new_version = Float.parseFloat(version);
		float old_version = Float.parseFloat(Constant.VERSION);
		if(new_version>old_version){
			return true;
		}else{
			return false;
		}
	}
}