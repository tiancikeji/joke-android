package com.jokes.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Handler;

public class OfflineImageDownLoadTask extends AsyncTask<Object, Object, Object> {
//	private ImageView imageView;
	private String iconurl;
	Bitmap bmp;
	Context context;
	String fileName = "";
	
	Handler settingActivitHandler;
	
	public OfflineImageDownLoadTask(String url, Context context,Handler handler) {
		this.iconurl = url;
		this.context = context;
		this.settingActivitHandler = handler;
		
		//获取下载文件名
		String[] array = url.split("/");
		fileName = array[array.length-1];
	}
	
	@Override
	protected void onPostExecute(Object result) {
//		if (result != null && imageView != null) {
//			this.imageView.setImageBitmap((Bitmap) result);
//			this.imageView = null;
//		}
		if(result != null && settingActivitHandler != null){
			settingActivitHandler.sendEmptyMessage(HandlerCodes.DOWNLOADIMAGE_SUCCESS);
			settingActivitHandler.sendEmptyMessage(HandlerCodes.DOWNLOAD_OFFLINE_PICTURE);
		}
	}

	@Override
	protected Object doInBackground(Object... views) {
		bmp = downImage(iconurl,context);
//		this.imageView = (ImageView) views[0];
		return bmp;
	}
	
	public Bitmap downImage(String ImageUrl, Context context){
		InputStream ism = null;
		URLConnection conn = null;
		FileOutputStream output = null;
		Bitmap bitmap = null;
		
		//在本地读取文件，如果存在，则直接解析，否则从网络获取
		String[] fileList = context.fileList();
		for(int i = 0;i<fileList.length;i++){
			if(fileList[i].equals(fileName)){
				try {
					ism = context.openFileInput(fileName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				bitmap = BitmapFactory.decodeStream(ism);
				try {
					if(ism != null){
						ism.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		if(bitmap == null){
			try {
				//连接网络
				URL url = new URL(ImageUrl);
				conn = url.openConnection();
				conn.connect();
				ism = conn.getInputStream();
				
				bitmap = BitmapFactory.decodeStream(ism);

				try {
					if(ism != null){
						ism.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				//保存文件
				if(bitmap != null){

					output = context.openFileOutput(fileName, Context.MODE_PRIVATE);
					bitmap.compress(CompressFormat.JPEG, 50, output);
					output.flush();
					output.close();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
			
		return bitmap;
	}
	
}
