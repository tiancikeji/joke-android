package com.jokes.utils;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

public class AudioUtils {
	
	private static final String DEBUG_TAG = "JOKE";
	
	public static void streamAudio(MediaPlayer mp, final String url, OnPreparedListener listener) 
			throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		mp.setDataSource(url);
		mp.prepareAsync();
		mp.setOnPreparedListener(listener);
		mp.start();
	}



}
