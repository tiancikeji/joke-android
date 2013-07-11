package com.jokes.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class DownloadAudioJokeTask extends Thread {
		String urlStr;
		String fileName;
		File fileJoke;
		int fileSize;
		int downloadSize = 0;
		Handler mainHandler;
		Context context;
		
		public DownloadAudioJokeTask(String urlStr,Context context,final Handler mainHandler){
			this.urlStr = urlStr;
			this.context = context;
			this.mainHandler = mainHandler;
			
			//获取下载文件名
			String[] array = urlStr.split("/");
			fileName = array[array.length-1];
		}

		@Override
		public void run() {
			//获取下载文件名
			String[] array = urlStr.split("/");
			fileName = array[array.length-1];
			
//			//获取SD卡目录
//			String downloadDir = Environment.getExternalStorageDirectory()+"/jokes/offline/";
//			File tmpFile = new File(downloadDir);  
//			if (!tmpFile.exists()) {  
//				tmpFile.mkdir();  
//			} 
//			fileJoke = new File(downloadDir+ fileName);
//			if(fileJoke.length() != 0){
//				fileJoke.delete();
//				fileJoke = new File(downloadDir+ fileName);
//			}
			
			FileOutputStream fos = null;
			
			try {  
				URL url = new URL(urlStr);  
				try {  
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
					
					InputStream is = conn.getInputStream();
					
					if (conn == null) {
						//通知handler更新组件
						mainHandler.sendEmptyMessage(HandlerCodes.CONNECTION_FAILURE);
		            }else{
//		            	FileOutputStream fos = new FileOutputStream(fileJoke);  
		            	fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
						
		            	byte[] buf = new byte[1024];
						int count = 0;  
						
						conn.setReadTimeout(10000);
						conn.connect();  
					    //获取下载文件的总大小  
						fileSize = conn.getContentLength();
						
						while ((count = is.read(buf)) != -1) {
								//下载进度
							downloadSize = downloadSize + count;
							
							Message msg = new Message();
							Bundle bundle = new Bundle();
							bundle.putInt("downloadsize", count);
							
							fos.write(buf, 0, count); 
							
//							Log.e("DownloadAudioJokeTask", ""+downloadSize);
							
							msg.setData(bundle);
							msg.what = HandlerCodes.DOWNLOAD_OFFLINE_JOKE;
							mainHandler.sendMessage(msg);
							
							if(downloadSize == fileSize){
								mainHandler.sendEmptyMessage(HandlerCodes.DOWNLOAD_OFFLINE_JOKE_FINISH);
							}
						}  
						conn.disconnect();
						fos.flush();
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
