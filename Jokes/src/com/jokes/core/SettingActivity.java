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
import java.util.ArrayList;
import java.util.List;
import com.jokes.database.DataBase;
import com.jokes.objects.Joke;
import com.jokes.utils.ApiRequests;
import com.jokes.utils.Constant;
import com.jokes.utils.DownloadAudioJokeTask;
import com.jokes.utils.HandlerCodes;
import com.jokes.utils.OfflineImageDownLoadTask;
import com.jokes.utils.Tools;
import com.jokes.utils.UmengAnaly;
import com.umeng.analytics.MobclickAgent;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

	//下载apk的大小
	private int downloadAPKSize = 0;  	//已经下载文件大小
	private int apkFileSize = 0; 			//文件大小
	private String apkFileName;			//文件名称
	private File fileAPK; 				//下载的文件
	private String apkUrl ;				//下载地址
	boolean isDownloadAPK = false;		//是否正在下载
	boolean isDownloadOffline = false;	//是否正在下载离线包

	List<Joke> jokesList;
	private int jokesSize = 0;
	private int downloadJokesSize = 0;
	private int imagedownloadcount = 0;//用来记录下载图片个数
	private int jokedownloadcount = 0;

	Handler mainHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case HandlerCodes.CHECK_UPDATE_SUCCESS:
				Bundle bundle = msg.getData();
				String current_version = bundle.getString("current_version");
				apkUrl = bundle.getString("url");
				if(current_version != null && !current_version.equals("")){
					if(isUpdate(current_version)){
						//退出此页面提示是否退出，退出正在下载文件失败
						new AlertDialog.Builder(SettingActivity.this).setTitle("一听到底有新版本可升级").setMessage("版本号为：v"+current_version)
						.setPositiveButton("立即更新",new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,int whichButton)
							{
								textview_updatepercent.setVisibility(View.VISIBLE);
								//启动线程下载apk
								downloadAPK(ApiRequests.buildAbsoluteUrl(apkUrl));
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
						Toast.makeText(SettingActivity.this,"当前版本为最新版本",Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(SettingActivity.this,"当前版本为最新版本",Toast.LENGTH_SHORT).show();
				}

				break;
			case HandlerCodes.CHECK_UPDATE_FAILURE:
				Toast.makeText(SettingActivity.this,"检查版本更新失败！",Toast.LENGTH_SHORT).show();
				isDownloadAPK = false;
				break;
			case HandlerCodes.DOWNLOAD_APK:
				//当收到更新视图消息时，计算已完成下载百分比，同时更新进度条信息  
				int progress = (Double.valueOf((downloadAPKSize * 1.0 / apkFileSize * 100))).intValue();  
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
				Toast.makeText(SettingActivity.this,"请检查网络连接！",Toast.LENGTH_SHORT).show();
				break;
			case HandlerCodes.GET_JOKES_SUCCESS:
				isDownloadOffline = true;
				jokesSize = 0;
				downloadJokesSize = 0;
				imagedownloadcount = 0;//用来记录下载图片个数
				jokedownloadcount = 0;
				textview_downloadpercent.setVisibility(View.VISIBLE);
				saveOfflineJokesToDB(jokesList);
				//获取要下载文件大小
				jokesSize = getOfflineDownLoadSize(jokesList);
				//下载图片
				downloadImage();
				//下载笑话
				downloadJoke();
				break;
			case HandlerCodes.GET_JOKES_FAILURE:
				Toast.makeText(SettingActivity.this,"获取离线列表失败",Toast.LENGTH_SHORT).show();
				break;
			case HandlerCodes.DOWNLOADIMAGE_SUCCESS://下载图片成功，没下载成功一张图片都通知，继续下载下一张
				//下载完成，刷新已下载文件大小
				downloadJokesSize = downloadJokesSize + jokesList.get(imagedownloadcount).getPictureSizeInB();
				imagedownloadcount++;
				downloadImage();
				break;
			case HandlerCodes.DOWNLOAD_OFFLINE_PICTURE:
				int temp_progress = (Double.valueOf((downloadJokesSize * 1.0 / jokesSize * 100))).intValue();  
				if (temp_progress == 100) {
					textview_downloadpercent.setText("下载完成");
					isDownloadOffline = false;
				} else {
					textview_downloadpercent.setText(temp_progress+"%");
				}
				break;
			case HandlerCodes.DOWNLOAD_OFFLINE_JOKE:

				Bundle temp_bundle = msg.getData();
				int temp_download = temp_bundle.getInt("downloadsize");
				downloadJokesSize = downloadJokesSize + temp_download;

				int temp_progress_1 = (Double.valueOf((downloadJokesSize * 1.0 / jokesSize * 100))).intValue();  
				if (temp_progress_1 == 100) {  
					textview_downloadpercent.setText("下载完成");
					isDownloadOffline = false;
				} else {  
					textview_downloadpercent.setText(temp_progress_1+"%");
				}  
				break;
			case HandlerCodes.DOWNLOAD_OFFLINE_JOKE_FINISH:
				//下载完成一条笑话后继续下载，直到jokesList条全部下载完成
				jokedownloadcount++;
				downloadJoke();
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
			Toast.makeText(SettingActivity.this, "缓存已清除", Toast.LENGTH_SHORT).show();
			//友盟统计：清除缓存
			UmengAnaly.AnalyCache(this);
			break;
		case R.id.setting_framelayout_offlinedownload:
			if(isDownloadOffline){
				Toast.makeText(SettingActivity.this, "正在下载离线数据包，请耐心等待", Toast.LENGTH_SHORT).show();
			}else{
				if(Tools.isNetworkAvailable(SettingActivity.this)){
					if(!Tools.isWiFiActive(SettingActivity.this)){
						//退出此页面提示是否退出，退出正在下载文件失败
						new AlertDialog.Builder(this).setTitle("提示").setMessage("您现在不是处于wifi环境下，离线下载可能需要消耗大量流量，是否要下载?")
						.setPositiveButton("是",new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,int whichButton)
							{
								//获取离线笑话列表
								jokesList = new ArrayList<Joke>();
								ApiRequests.getJokes(mainHandler, jokesList, Constant.uid, 1, true);

								//友盟统计：下载离线
								UmengAnaly.AnalyOffLineDownload(SettingActivity.this);
							}
						}).setNegativeButton("否",new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,int whichButton)
							{

							}
						}).show();
					}else{
						//获取离线笑话列表
						jokesList = new ArrayList<Joke>();
						ApiRequests.getJokes(mainHandler, jokesList, Constant.uid, 1, true);

						//友盟统计：下载离线
						UmengAnaly.AnalyOffLineDownload(SettingActivity.this);
					}
				}else{
					Toast.makeText(SettingActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
				}
			}

			break;
		case R.id.setting_framelayout_feedback:
			Intent intent = new Intent(SettingActivity.this,FeedbackActivity.class);
			startActivity(intent);
			break;
		case R.id.setting_framelayout_update:
			if(isDownloadAPK){
				Toast.makeText(SettingActivity.this, "正在下载", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(SettingActivity.this, "正在检测最新版本", Toast.LENGTH_SHORT).show();
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
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(isDownloadAPK&&isDownloadOffline){
				//退出此页面提示是否退出，退出正在下载文件失败
				new AlertDialog.Builder(this).setTitle("提示").setMessage("正在下载数据包，退出将下载失败。确定退出设置页面？")
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
		downloadAPKSize = 0;
		apkFileSize = 0;
		//启动下载线程
		new downloadAPKTask(url).start();
	}

	public class downloadAPKTask extends Thread {
		String urlStr;

		public downloadAPKTask(String urlStr){
			this.urlStr = urlStr;
		}

		@Override
		public void run() {
			//获取下载文件名
			String[] array = urlStr.split("/");
			apkFileName = array[array.length-1];
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
			//判断SD卡是否存在，如果存在则升级文件保存到SD卡
			if(sdCardExist){
				//获取SD卡目录
				String jokeDir = Environment.getExternalStorageDirectory()+"/jokes/";
				File tmpJokeFile = new File(jokeDir);
				if(!tmpJokeFile.exists()){
					tmpJokeFile.mkdir();
				}
				String downloadDir = Environment.getExternalStorageDirectory()+"/jokes/download/";
				File tmpFile = new File(downloadDir);  
				if (!tmpFile.exists()) {  
					tmpFile.mkdir();  
				} 
				fileAPK = new File(downloadDir+ apkFileName);
				if(fileAPK.length() != 0){
					fileAPK.delete();
					fileAPK = new File(downloadDir+ apkFileName);
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
							apkFileSize = conn.getContentLength();

							while ((count = is.read(buf)) != -1) {
								//下载进度
								downloadAPKSize = downloadAPKSize + count;
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
			}else{
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
							FileOutputStream fos = context.openFileOutput(apkFileName, Context.MODE_PRIVATE);

							byte[] buf = new byte[1024];
							int count = 0;  

							conn.setReadTimeout(10000);
							conn.connect();  
							//获取下载文件的总大小  
							apkFileSize = conn.getContentLength();

							while ((count = is.read(buf)) != -1) {
								//下载进度
								downloadAPKSize = downloadAPKSize + count;
								fos.write(buf, 0, count); 
								mainHandler.sendEmptyMessage(HandlerCodes.DOWNLOAD_APK);
							}

							fileAPK = context.getDir(apkFileName, Context.MODE_PRIVATE);
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


	}

	/**
	 * 安装软件
	 * @param file
	 */
	private void openFile(File file) {   
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

	/**
	 * 保存离线 列表到数据库
	 */
	private void saveOfflineJokesToDB(List<Joke> list){
		DataBase db = new DataBase(this);
		db.open();
		db.beginTransaction();
		db.deleteAllMyDriveTrip();
		ContentValues values;
		for(int i = 0; i < list.size(); i++){
			values = new ContentValues();
			values.put(DataBase.OFFLINE_AUDIO_SIZE_IN_B, ""+list.get(i).getAudioSizeInB());
			values.put(DataBase.OFFLINE_FULLAUDIO_URL, ""+list.get(i).getFullAudioUrl());
			values.put(DataBase.OFFLINE_FULLPICTURE_URL, ""+list.get(i).getFullPictureUrl());
			values.put(DataBase.OFFLINE_JOKE_ID, ""+list.get(i).getId());
			values.put(DataBase.OFFLINE_LENGTH, ""+list.get(i).getLength());
			values.put(DataBase.OFFLINE_NUM_LIKES, ""+list.get(i).getNumLikes());
			values.put(DataBase.OFFLINE_NUM_PLAYS, ""+list.get(i).getNumPlays());
			values.put(DataBase.OFFLINE_PICTURE_SIZE_IN_B, ""+list.get(i).getPictureSizeInB());
			values.put(DataBase.OFFLINE_UID, ""+list.get(i).getUserId());
			values.put(DataBase.OFFLINE_APPROVAL_TIME, ""+list.get(i).getApprovalTime());
			db.saveOffLineJokes(values);
		}
		db.endTransaction();
		db.close();
	}

	/**
	 * 计算离线下载的笑话所有资源大小
	 */
	private int getOfflineDownLoadSize(List<Joke> _list){
		int size = 0;
		for(int i=0; i < _list.size(); i++){
			size = size + (_list.get(i).getAudioSizeInB()+ _list.get(i).getPictureSizeInB());
		}
		return size;
	}

	/**
	 * 下载离线图片
	 */
	private void downloadImage(){
		if(jokesList.size()-1 >= imagedownloadcount)
			new OfflineImageDownLoadTask(ApiRequests.buildAbsoluteUrl(jokesList.get(imagedownloadcount).getFullPictureUrl()),this,mainHandler).execute(textview_cache);

	}

	/*
	 * 下载笑话
	 */
	private void downloadJoke(){
		if(jokesList.size()-1 >= jokedownloadcount){
			new DownloadAudioJokeTask(ApiRequests.buildAbsoluteUrl(jokesList.get(jokedownloadcount).getFullAudioUrl()),SettingActivity.this,mainHandler).start();
		}
	}
}