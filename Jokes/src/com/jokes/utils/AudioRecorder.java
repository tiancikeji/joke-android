package com.jokes.utils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

public class AudioRecorder {
	
	private static final String DEBUG_TAG = "JOKE";
	
	private static final String DEFAULT_FILENAME = "sample";
	
	private AudioRecord mRecorder;
	private short[] mBuffer;
	private File mRawFile;
	
	private boolean mIsRecording;
	
	
	public AudioRecorder(){
	}
	
	
	public void startRecordingAudio(Context context){
		int bufferSize = AudioRecord.getMinBufferSize(AudioEncoder.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		mBuffer = new short[bufferSize];
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AudioEncoder.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		
		mIsRecording = true;
		
		mRecorder.startRecording();
		mRawFile = getFile("raw");
		startBufferedWrite(mRawFile);
	}
	
	/**
	 * 停止录音方法，必要调用
	 * Must call to stop recording
	 * 
	 */
	public File stopRecordingAudio(Context context){
		mRecorder.stop();
		File mp3Out = getFile("mp3");
		final int encodingResultCode = AudioEncoder.encode(mRawFile.getAbsolutePath(), mp3Out.getAbsolutePath());
		if(0 != encodingResultCode){
			Log.e(DEBUG_TAG, "Encoder failed to encoder with code = " + encodingResultCode);
			return null;
		}
		return mp3Out;
	}
	
	private void startBufferedWrite(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				DataOutputStream output = null;
				try {
					output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
					while (mIsRecording) {
						int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
						for (int i = 0; i < readSize; i++) {
							output.writeShort(mBuffer[i]);
						}
					}
				} catch (IOException e) {
					Log.e(DEBUG_TAG, "Exception on Buffered Mic Write = " + e);
				}
			}
		}).start();
	}
	
	private File getFile(final String suffix) {
		Time time = new Time();
		time.setToNow();
//		return new File(Environment.getExternalStorageDirectory(), time.format("%Y%m%d%H%M%S") + "." + suffix);
		return new File(Environment.getExternalStorageDirectory(), DEFAULT_FILENAME + "." + suffix);
	}

}
