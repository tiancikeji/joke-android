package com.jokes.core;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.jokes.utils.ApiRequests;
import com.jokes.utils.AudioRecorder;
import com.jokes.utils.AudioUtils;
import com.jokes.utils.Constant;
import com.jokes.utils.HandlerCodes;
import com.jokes.utils.UmengAnaly;
import com.umeng.analytics.MobclickAgent;

public class FeedbackActivity extends Activity implements OnClickListener, OnCompletionListener{
	private static final int CHANGEVOLUME = 100001;
	Button button_back;
	Button button_send;
	LinearLayout linearlayout_record;
	Button button_record;
	Button button_play;
	
	LinearLayout linearlayout_bar;
	ImageView imageview_bar;//加载中动画

	ImageView imageview_volume;
	
	ImageView imageview_point_1;
	ImageView imageview_point_2;
	ImageView imageview_point_3;
	ImageView imageview_point_4;
	ImageView imageview_point_5;
	ImageView imageview_point_6;
	ImageView imageview_point_7;
	ImageView imageview_point_8;
	RelativeLayout relativeLayout_dialog;
	
	//private boolean isPlaying = false;
	private AudioRecorder audioRecorder;
	private File mp3RecordedFile;
	private MediaPlayer mediaPlayer;
	private boolean isPlaying = false;
	private boolean isPaused = false;
	
	//用来控制录音动画效果
	int count = 0;
	boolean isStartAnim = false;
		
	private AnimationDrawable animationDrawable;
	private Timer mTimer;
	private TimerTask mTimerTask;
	
	Handler mainHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case HandlerCodes.CLOSE:
			{
				FeedbackActivity.this.finish();
				break;
			}
			case HandlerCodes.CREATE_FEEDBACK_SUCCESS:
			{
				Toast.makeText(FeedbackActivity.this, "已经上传，谢谢反馈", Toast.LENGTH_LONG).show();
				button_send.setEnabled(false);
				CountDownTimer timer = new CountDownTimer(3000, 3000) {
					@Override
					public void onTick(long arg0) {
					}
					
					@Override
					public void onFinish() {
						mainHandler.sendEmptyMessage(HandlerCodes.CLOSE);
					}
				}; 
				timer.start();
				break;
			}
			case HandlerCodes.CREATE_FEEDBACK_FAILURE:
				Log.e("JOKE", "feedback failed to send");
				break;
			case CHANGEVOLUME:
				changePointView(count);
				break;
			}
		
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.feedback_activity);
		init();
		animationDrawable = (AnimationDrawable) ((ImageView)findViewById(R.id.feedback_imageview_volume)).getDrawable();
		
		//友盟统计：进入意见反馈
		UmengAnaly.AnalyOnClickFeedback(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			relativeLayout_dialog.setVisibility(View.VISIBLE);
		}
		return false;
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.feedback_button_back:
			finish();
			break;
		case R.id.feedback_button_send:
		{
			ApiRequests.addFeedback(mainHandler, mp3RecordedFile, Constant.uid);
			Toast.makeText(this, "正在发布...", Toast.LENGTH_SHORT).show();
			button_send.setEnabled(false);
			//友盟统计：发布意见反馈
			UmengAnaly.AnalyOnClickFeedbackSend(this);
			break;
		}
		case R.id.feedback_button_record:
			//点击开始录音，再次点击停止了录音
			if((Boolean)button_record.getTag()){
				isStartAnim = false;
				button_record.setTag(false);
				linearlayout_record.setVisibility(View.GONE);
				button_record.setBackgroundResource(R.drawable.btn_record_activity_record);
				button_send.setVisibility(View.VISIBLE);
				button_play.setVisibility(View.VISIBLE);
				mp3RecordedFile = audioRecorder.stopRecordingAudio(this);
				displayLengthOfAudioFile();
				//友盟统计：录制音频
				UmengAnaly.AnalyOnClickFeedbaceRocord(this);
			}else{
				isStartAnim = true;
				linearlayout_bar.setVisibility(View.VISIBLE);
				startPlayAnim();
				audioRecorder = new AudioRecorder();
				audioRecorder.startRecordingAudio(this);
				button_record.setTag(true);
				button_record.setBackgroundResource(R.drawable.btn_record_activity_record_1);
			}
			break;
		case R.id.feedback_button_play:
			if(isPlaying && !isPaused){
				isPlaying = false;
				isPaused = true;
				AudioUtils.pausePlaying(mediaPlayer);
				animationDrawable.stop();
			}else{
				if(mediaPlayer == null){
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setOnCompletionListener(this);
				}
				if(!isPlaying && mp3RecordedFile != null && !isPaused){
					isPlaying = true;
					AudioUtils.startPlaying(mediaPlayer, mp3RecordedFile.getAbsolutePath());
					
					button_play.setBackgroundResource(R.drawable.btn);
					imageview_volume.setVisibility(View.VISIBLE);
					animationDrawable.start();
					mTimer = new Timer();
					mTimerTask = getTimerTask();
					mTimer.schedule(mTimerTask, 0, 1000);
				}else if(isPaused){
					isPlaying = true;
					mediaPlayer.start();
					isPaused = false;
					animationDrawable.start();
				}
			}
			
			break;
		}
		
	}
	
	private void init(){
		button_back = (Button)findViewById(R.id.feedback_button_back);
		button_send = (Button)findViewById(R.id.feedback_button_send);
		button_send.setVisibility(View.GONE);
		linearlayout_record = (LinearLayout)findViewById(R.id.feedback_linearlayout_record);
		button_record = (Button)findViewById(R.id.feedback_button_record);
		button_record.setTag(false);//设置按钮是否在录音状态
		button_play = (Button)findViewById(R.id.feedback_button_play);
		
		linearlayout_bar = (LinearLayout)findViewById(R.id.feedback_linearlayout_bar);
		linearlayout_bar.setVisibility(View.GONE);
		imageview_bar = (ImageView)findViewById(R.id.record_imageview_bar);

		imageview_volume = (ImageView)findViewById(R.id.feedback_imageview_volume);
		
		imageview_point_1 = (ImageView)findViewById(R.id.feedback_imageview_point_1);
		imageview_point_2 = (ImageView)findViewById(R.id.feedback_imageview_point_2);
		imageview_point_3 = (ImageView)findViewById(R.id.feedback_imageview_point_3);
		imageview_point_4 = (ImageView)findViewById(R.id.feedback_imageview_point_4);
		imageview_point_5 = (ImageView)findViewById(R.id.feedback_imageview_point_5);
		imageview_point_6 = (ImageView)findViewById(R.id.feedback_imageview_point_6);
		imageview_point_7 = (ImageView)findViewById(R.id.feedback_imageview_point_7);
		imageview_point_8 = (ImageView)findViewById(R.id.feedback_imageview_point_8);
		
		relativeLayout_dialog = (RelativeLayout)findViewById(R.id.feedback_relativelayout_dialog);
		
		button_back.setOnClickListener(this);
		button_send.setOnClickListener(this);
		button_record.setOnClickListener(this);
		button_play.setOnClickListener(this);
	}
	
	private void displayLengthOfAudioFile(){
		button_play.setText(AudioUtils.getAudioFileLength(mp3RecordedFile) + "\"");
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		mediaPlayer.release();
		mediaPlayer = null;
		isPaused = false;
		isPlaying = false;
		
		animationDrawable.stop();
		imageview_volume.setVisibility(View.GONE);
		button_play.setBackgroundResource(R.drawable.playback_play);
		button_play.setText(AudioUtils.getAudioFileLength(mp3RecordedFile) + "\"");
		
		if(null != mTimer){
			mTimer.cancel();
		}
	}
	
	/**
	 * 录音动画效果
	 */
	private void startPlayAnim(){

		new Thread(new Runnable(){

			@Override
			public void run() {
				while(isStartAnim){
					mainHandler.sendEmptyMessage(CHANGEVOLUME);
					if(count == 8){
						count = 0;
					}else{
						count++;
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}).start();
	}
	
	/**
	 * 根据count改变point状态
	 * @param count
	 */
	private void changePointView(int count){
		switch(count){
		case 0:{
			imageview_point_1.setBackgroundResource(R.drawable.point1);
			imageview_point_2.setBackgroundResource(R.drawable.point1);
			imageview_point_3.setBackgroundResource(R.drawable.point1);
			imageview_point_4.setBackgroundResource(R.drawable.point1);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 1:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point1);
			imageview_point_3.setBackgroundResource(R.drawable.point1);
			imageview_point_4.setBackgroundResource(R.drawable.point1);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 2:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point1);
			imageview_point_4.setBackgroundResource(R.drawable.point1);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 3:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point1);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 4:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 5:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point2);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 6:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point2);
			imageview_point_6.setBackgroundResource(R.drawable.point2);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 7:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point2);
			imageview_point_6.setBackgroundResource(R.drawable.point2);
			imageview_point_7.setBackgroundResource(R.drawable.point2);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 8:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point2);
			imageview_point_6.setBackgroundResource(R.drawable.point2);
			imageview_point_7.setBackgroundResource(R.drawable.point2);
			imageview_point_8.setBackgroundResource(R.drawable.point2);
		}
			break;
		}
	}
	
	/**
	 * 重新录制 
	 * @param view
	 */
	public void onAgainRecordButtonClick(View view){
		onCloseDialogButtonClick(relativeLayout_dialog);
		button_send.setTag(true);
		button_send.setVisibility(View.GONE);
		button_record.setTag(false);//设置录音状态
		button_record.setBackgroundResource(R.drawable.btn_record_activity_record);
		linearlayout_record.setVisibility(View.VISIBLE);
		linearlayout_bar.setVisibility(View.GONE);
		mp3RecordedFile = null;
		
	}
	
	/**
	 * 取消录音
	 * @param view
	 */
	public void onCancelSendButtonClick(View view){
		finish();
	}
	
	/**
	 * 取消
	 */
	public void onCloseDialogButtonClick(View view){
		relativeLayout_dialog.setVisibility(View.GONE);
	}
	private TimerTask getTimerTask(){
		return new TimerTask() {  
			@Override  
			public void run() {  
				if(mediaPlayer==null)  
					return;  
				if (mediaPlayer.isPlaying() && !isPaused) {  
					handleProgress.sendEmptyMessage(0);  
				}  
			}  
			
			Handler handleProgress = new Handler() {  
				public void handleMessage(Message msg) {  

					int duration = mediaPlayer.getDuration();  

					if (duration > 0) {  
						//动态修改播放剩余时长,判断播放进度和剩余时长是否大于0
						if(mediaPlayer.isPlaying() && Integer.parseInt(button_play.getText().toString().split("\"")[0]) != 0){
							
							button_play.setText((Integer.parseInt(button_play.getText().toString().split("\"")[0])-1)+"\"");
						}
					}  
				};  
			};
		};  
	}

}