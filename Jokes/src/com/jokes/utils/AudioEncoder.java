package com.jokes.utils;

public class AudioEncoder {
	
	public static final int NUM_CHANNELS = 1;
	public static final int SAMPLE_RATE = 16000;
	public static final int BITRATE = 128;
	public static final int MODE = 1;
	public static final int QUALITY = 2;
	
	
	static {
        System.loadLibrary("mp3lame");
    }
    private native void initEncoder(int numChannels, int sampleRate, int bitRate, int mode, int quality);
    private native void destroyEncoder();
    private native int encodeFile(String sourcePath, String targetPath);
    
    public static int encode(String sourcepath, String targetpath){
    	AudioEncoder encoder = new AudioEncoder();
    	encoder.initEncoder(NUM_CHANNELS, SAMPLE_RATE, BITRATE, MODE, QUALITY);
    	int result = encoder.encodeFile(sourcepath, targetpath);
    	encoder.destroyEncoder();
    	return result;
    }
    
    
}
