package com.jokes.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.util.Log;

public class AudioUtils {
	
	private static final String DEBUG_TAG = "JOKE";
	
	public static void streamAudio(MediaPlayer mp, final String url, OnPreparedListener listener) 
			throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		mp.setDataSource(url);
		mp.prepareAsync();
		mp.setOnPreparedListener(listener);
		mp.start();
	}
	
	public static String startRecordingAudio(MediaRecorder recorder, String recordedFilename, 
			Context context, OnInfoListener listener){
		// Prepare recorder source and type
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		
		// File to which audio should be recorded
		File outputFile = context.getFileStreamPath(recordedFilename);
		
		recorder.setOutputFile(outputFile.getAbsolutePath());
		Log.d(DEBUG_TAG, "outputfile : " + outputFile.getAbsolutePath());

		// Get ready!
		try {
			recorder.prepare();
			recorder.setOnInfoListener(listener);
			recorder.start();
		} catch (IllegalStateException e) {
			Log.e(DEBUG_TAG, "OnRecord " + e);
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "OnRecord " + e);
		}
		return outputFile.getAbsolutePath();
	}
	
	/**
	 * 停止录音方法，必要调用
	 * Must call to stop recording
	 * 
	 * @param recorder, MediaRecorder,注意，必要用startRecordingAudio同一个MediaRecorder，要不肯定会发生错误。
	 * @param recordedFilename, startRecordingAudio 返回的文件名
	 */
	public static void stopRecordingAudio(MediaRecorder recorder, String recordedFilename){
		recorder.stop();
		recorder.reset();
		recorder.release();
		
		/*
		File recorderFile = new File(recordedFilename);
		Log.d(DEBUG_TAG, "file size = " + recorderFile.getTotalSpace());
		
		MediaPlayer mp = new MediaPlayer();
		try {
			mp.setDataSource(recordedFilename);
			mp.prepare();
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
		mp.start();
		*/
	}

	/**
	 * 开始播放
	 */
	public static void startPlaying(MediaPlayer mPlayer,String fileName){
		mPlayer = new MediaPlayer();
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
