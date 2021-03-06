package com.jokes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;

public class AudioUtils {
	
	private static final String DEBUG_TAG = "JOKE";
	
	public static void prepareStreamAudio(MediaPlayer mp, final String url, OnPreparedListener listener) 
			throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		mp.setDataSource(url);
		mp.prepareAsync();
//		mp.setOnPreparedListener(listener);
	}
	
	/**
	 * 开始播放本地音频
	 */
	public static void startPlayOffline(MediaPlayer mPlayer,String fileName){
		try {
			mPlayer.setDataSource((new FileInputStream(new File(fileName))).getFD());
//			mPlayer.setDataSource(fileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	/**
	 * 开始播放
	 */
	public static void startPlaying(MediaPlayer mPlayer,String fileName){
		if(mPlayer == null){
			mPlayer = new MediaPlayer();
		}
		try {
			mPlayer.setDataSource(fileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			Log.e(DEBUG_TAG, "Error playing audio" + e + " " + e.getMessage());
		} catch (SecurityException e) {
			Log.e(DEBUG_TAG, "Error playing audio" + e + " " + e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(DEBUG_TAG, "Error playing audio" + e + " " + e.getMessage());
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "Error playing audio" + e + " " + e.getMessage());
		}
		
	}
	
	
	
	/**
	 * 停止播放
	 */
	public static void stopPlaying(MediaPlayer mPlayer){
		mPlayer.stop();
		mPlayer.reset();
		//mPlayer.release();
	}
	
	/**
	 * 暂停播放
	 */
	public static void pausePlaying(MediaPlayer mPlayer){
		if(mPlayer != null){
			mPlayer.pause();
		}
	}
	
	private static int getAudioFileLength(File audio, MediaPlayer mp){
		try {
			mp.setDataSource(audio.getAbsolutePath());
			mp.prepare(); // might be optional
		} catch (IllegalArgumentException e) {
			Log.e(DEBUG_TAG, "Error determining audio length " + e);
			return 0;
		} catch (SecurityException e) {
			Log.e(DEBUG_TAG, "Error determining audio length " + e);
			return 0;
		} catch (IllegalStateException e) {
			Log.e(DEBUG_TAG, "Error determining audio length " + e);
			return 0; 
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "Error determining audio length " + e);
			return 0;
		}
		int length = (int) Math.round(((float)mp.getDuration() / 1000.0)); 
		mp.reset();
		mp.release();
		return length;
	}
	
	public static int getAudioFileLength(File audio){
		return getAudioFileLength(audio, new MediaPlayer());
	}
	
	/**
	 * 获取播放本地文件的路径
	 */
	public static String getAudioFilePath(Context context,String fileName){
		File file = context.getFilesDir();
		String path = file.getPath() +"/"+ fileName;
		Log.d(DEBUG_TAG, path);
		return path;
	}


}
