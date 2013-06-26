package com.jokes.utils;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

public class AudioUtils {
	
	private static final String DEBUG_TAG = "JOKE";
	
	public static void prepareStreamAudio(MediaPlayer mp, final String url, OnPreparedListener listener) 
			throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		mp.setDataSource(url);
		mp.prepareAsync();
		mp.setOnPreparedListener(listener);
	}


	/**
	 * 开始播放
	 */
	public static void startPlaying(MediaPlayer mPlayer,String fileName){
//		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(fileName);
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
	 * 停止播放
	 */
	public static void stopPlaying(MediaPlayer mPlayer){
		mPlayer.release();
//		mPlayer = null;
	}

}
