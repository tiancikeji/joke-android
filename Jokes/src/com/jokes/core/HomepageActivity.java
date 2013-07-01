package com.jokes.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.jokes.objects.Joke;
import com.jokes.objects.Like;
import com.jokes.utils.ApiRequests;
import com.jokes.utils.AudioUtils;
import com.jokes.utils.DataManagerApp;
import com.jokes.utils.HandlerCodes;
import com.jokes.utils.ImageDownLoadTask;

public class HomepageActivity extends Activity implements OnClickListener,AnimationListener,
	OnPreparedListener, OnCompletionListener ,OnBufferingUpdateListener{

	private static final String DEBUG_TAG = "JOKE";
	private static final int PLAY_NEXT = 100001;
	private static final int CHANGEVOLUME = 100002;

	Button button_setting;//设置按钮
	Button button_refresh;//刷新
	Button button_record;//录音按钮
	Button button_favorite_big;//图片中间收藏按钮
	Button button_favorite_small;//收藏按钮
	TextView textview_numlikes;
	FrameLayout framelayout_play;//播放按钮
	LinearLayout linearlayout_volume;//播放音量状态
	Button button_share;//分享按钮
	FrameLayout framelayout_date;
	TextView textview_date;//日期
	ImageView imageview_pic;//笑话图片
	SeekBar seekbar;//播放进度条
	TextView textview_duration;//时长
	TextView textview_playCount;//播放次数

	ImageView imageview_volume_1;
	ImageView imageview_volume_2;
	ImageView imageview_volume_3;
	ImageView imageview_volume_4;
	ImageView imageview_volume_5;
	ImageView imageview_volume_6;
	ImageView imageview_volume_7;
	ImageView imageview_volume_8;
	ImageView imageview_volume_9;
	ImageView imageview_volume_10;
	ImageView imageview_volume_11;
	ImageView imageview_volume_12;
	ImageView imageview_volume_13;
	
	LinearLayout linearlayout_progressdialog;//正在加载提示

	private List<Joke> jokeList;
	private Like like;
	private Joke jokeCurrent;//正在播放的音频 Play the audio
	private List<Joke> jokeLikeList;
	private int jokeIndex = 0;//当前播放索引
	
	private int date = 0;//用来记录 当前笑话 日期
	boolean isGetJokeSuccesss = true;//记录第一次获取笑话列表失败
	private boolean isPlay = false;//判断是否正在播放
	private boolean isPaused = false;
	Animation myAnimation_Alpha;

	MediaPlayer mediaPlayer;
	Context context;
	
	TimerTask mTimerTask;
	private Timer mTimer;//播放进度条使用timer
	CountDownTimer countDownTimer;//播放动画效果的倒计时
	long countDownTime = 0;//倒计时剩余时间
	
	WakeLock wakelock = null;//保持程序部睡眠
	
	//用来控制音频动画效果
	int count = 0;
	boolean add = true;
	
	private Handler mainHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {

			switch(msg.what){
			case HandlerCodes.GET_JOKES_SUCCESS:
				Log.d(DEBUG_TAG, "Jokes success message received, printing... size = "+jokeList.size());
				if(jokeList.size() == 0){
					//刷新笑话或获取笑话列表失败，则日期减一获取，再次失败则提示明天再来
					if(!isGetJokeSuccesss){
						//明天再来
						if(jokeCurrent != null)
							Toast.makeText(context,"你已经听到底了，明天再来听吧",Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(context,"获取笑话列表失败",Toast.LENGTH_SHORT).show();
						linearlayout_progressdialog.setVisibility(View.GONE);
					}else{
						isGetJokeSuccesss = false;
						ApiRequests.getJokes(mainHandler, jokeList,DataManagerApp.uid,getDateAfterFormat_(date--));
					}
				}else{
				jokeIndex = 0;
				if(!isPlay){
					loadJoke();
						linearlayout_progressdialog.setVisibility(View.GONE);
					}
				}
				break;
			case HandlerCodes.GET_JOKES_FAILURE:
				linearlayout_progressdialog.setVisibility(View.GONE);
				//刷新笑话或获取笑话列表失败，则日期减一获取，再次失败则提示明天再来
				if(!isGetJokeSuccesss){
					//明天再来
					if(jokeCurrent != null)
						Toast.makeText(context,"你已经听到底了，明天再来听吧",Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(context,"获取笑话列表失败",Toast.LENGTH_SHORT).show();
					linearlayout_progressdialog.setVisibility(View.GONE);
				}else{
					isGetJokeSuccesss = false;
					ApiRequests.getJokes(mainHandler, jokeList,DataManagerApp.uid,getDateAfterFormat_(date--));
				}
				
				break;
			case HandlerCodes.LIKE_SUCCESS:
				Log.d(DEBUG_TAG, "Like Succes " + like);
				jokeCurrent.setIsLike(true);
				button_favorite_big.setVisibility(View.VISIBLE);
				myAnimation_Alpha.setAnimationListener(HomepageActivity.this);
				button_favorite_big.startAnimation(myAnimation_Alpha);

				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_1);
				break;
			case HandlerCodes.LIKE_FAILURE:
				break;
			case HandlerCodes.UNLIKE_SUCCESS:
				jokeCurrent.setIsLike(false);
				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_2);
				break;
			case HandlerCodes.UNLIKE_FAILURE:

				break;
			case HandlerCodes.GET_LIKEJOKES_SUCCESS:

				break;
			case HandlerCodes.GET_LIKEJOKES_FAILURE:
				break;
			case PLAY_NEXT:
				//判断笑话是否还有下一条
				if(jokeList.size()-1 >= jokeIndex && jokeList.size()>0){
					textview_date.setVisibility(View.GONE);
					loadJoke();
					playJoke();
				}
				//当未播放笑话剩下一条时，加载新笑话
				if(jokeList.size() - (jokeIndex+1) == 0){
					ApiRequests.getJokes(mainHandler, jokeList,DataManagerApp.uid,getDateAfterFormat_(date--));
				}
				break;
			case CHANGEVOLUME:
				changeView(count);
				break;
			case HandlerCodes.CREATE_JOKE_SUCCESS:
				Log.d(DEBUG_TAG, "Create joke success");
				break;
			case HandlerCodes.CREATE_JOKE_FAILURE:
				Log.d(DEBUG_TAG, "Create joke failure");
				break;
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.homepage_activity);
		context = getApplicationContext();
		/*
		final String uid = Installation.id(this);
		jokeList = new ArrayList<Joke>();
		like = new Like();
		joke = new Joke(); 
		joke.setName("Test Name");
		joke.setDescription("Testing Joke");
		ApiRequests.getJokes(mainHandler, jokeList, uid);

		File imageFile = new File(getFilesDir().getAbsolutePath() + "image.png");
		FileOutputStream out;
		try {
			out = new FileOutputStream(imageFile);
			Bitmap bmp =  BitmapFactory.decodeResource(getResources(), R.drawable.btn_back);
			bmp.compress(Bitmap.CompressFormat.JPEG, 30, out);
			ApiRequests.addJoke(mainHandler, joke, imageFile, new File("/storage/emulated/0/sample.mp3"), uid);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		initView();
		initAnim();
		initValues();

		initMediaPlayer();

		jokeList = new ArrayList<Joke>();
		like = new Like();
		jokeLikeList = new ArrayList<Joke>();
		
//		ApiRequests.getJokes(mainHandler, jokeList,DataManagerApp.uid, getDateAfterFormat_(date));
		ApiRequests.getJokes(mainHandler, jokeList,DataManagerApp.uid, "20130626");

		if(loadSettingTime().equals(getTodayToString())){
			//不是今天第一次进入
			framelayout_date.setVisibility(View.GONE);
		}else{
			//今天第一次进入
			textview_date.setText(getTodayToString());
			deleteSettingTime();
			saveSettingTime("true");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(isPlay){
			AudioUtils.stopPlaying(mediaPlayer);
		}
		if(null != mTimer){
			mTimer.cancel();
			mTimer = null;
		}
	}

	/**
	 * 监听动画播放完成
	 */
	@Override
	public void onAnimationEnd(Animation arg0) {
		button_favorite_big.setVisibility(View.GONE);

	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub

	}

	private void initView(){
		button_setting = (Button)findViewById(R.id.homepage_button_setting);
		button_refresh = (Button)findViewById(R.id.homepage_button_refresh);
		button_record = (Button)findViewById(R.id.homepage_button_record);
		button_favorite_big = (Button)findViewById(R.id.homepage_button_favorite_big);
		button_favorite_big.setVisibility(View.GONE);
		button_favorite_small = (Button)findViewById(R.id.homepage_button_favorite_small);
		textview_numlikes = (TextView)findViewById(R.id.homepage_textview_numlikes);
		framelayout_play = (FrameLayout)findViewById(R.id.homepage_framelayout_play);
		linearlayout_volume = (LinearLayout)findViewById(R.id.homepage_linearlayout_volume);
		button_share = (Button)findViewById(R.id.homepage_button_share);
		framelayout_date = (FrameLayout)findViewById(R.id.homepage_framelayout_date);
		textview_date = (TextView)findViewById(R.id.homepage_textview_date);
		imageview_pic = (ImageView)findViewById(R.id.homepage_imageview_pic);
		seekbar = (SeekBar)findViewById(R.id.homepage_seekbar_progress);

		textview_duration = (TextView)findViewById(R.id.homepage_textview_duration);
		textview_playCount = (TextView)findViewById(R.id.homepage_textview_playcount);

		button_setting.setOnClickListener(this);
		button_refresh.setOnClickListener(this);
		button_record.setOnClickListener(this);
		button_favorite_small.setOnClickListener(this);
		framelayout_play.setOnClickListener(this);
		button_share.setOnClickListener(this);

		imageview_volume_1 =  (ImageView)findViewById(R.id.homepage_imageview_volume_1);
		imageview_volume_2 =  (ImageView)findViewById(R.id.homepage_imageview_volume_2);
		imageview_volume_3 =  (ImageView)findViewById(R.id.homepage_imageview_volume_3);
		imageview_volume_4 =  (ImageView)findViewById(R.id.homepage_imageview_volume_4);
		imageview_volume_5 =  (ImageView)findViewById(R.id.homepage_imageview_volume_5);
		imageview_volume_6 =  (ImageView)findViewById(R.id.homepage_imageview_volume_6);
		imageview_volume_7 =  (ImageView)findViewById(R.id.homepage_imageview_volume_7);
		imageview_volume_8 =  (ImageView)findViewById(R.id.homepage_imageview_volume_8);
		imageview_volume_9 =  (ImageView)findViewById(R.id.homepage_imageview_volume_9);
		imageview_volume_10 =  (ImageView)findViewById(R.id.homepage_imageview_volume_10);
		imageview_volume_11 =  (ImageView)findViewById(R.id.homepage_imageview_volume_11);
		imageview_volume_12 =  (ImageView)findViewById(R.id.homepage_imageview_volume_12);
		imageview_volume_13 =  (ImageView)findViewById(R.id.homepage_imageview_volume_13);
		linearlayout_progressdialog = (LinearLayout)findViewById(R.id.homepage_linearlayout_progressdialog);
	}

	private void initMediaPlayer(){
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);  
		mediaPlayer.setOnPreparedListener(this);
	}

	private void initAnim(){
		myAnimation_Alpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha_heart);	
	}

	private void initValues(){
		TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		DataManagerApp.uid = tm.getDeviceId();
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.homepage_button_setting:
			Intent intent1 = new Intent(HomepageActivity.this,SettingActivity.class);
			startActivity(intent1);
			break;
		case R.id.homepage_button_refresh:
			date = 0;
			ApiRequests.getJokes(mainHandler, jokeList,DataManagerApp.uid, getDateAfterFormat_(date));
			break;
		case R.id.homepage_button_record:
			Intent intent2 = new Intent(HomepageActivity.this,RecordActivity.class);
			startActivity(intent2);
			break;
		case R.id.homepage_button_favorite_small:
			if(!jokeCurrent.getIsLike()){
				ApiRequests.likeJoke(mainHandler, jokeCurrent.getId(), jokeCurrent.getUserId(), like);

				//假操作
				jokeCurrent.setIsLike(true);
				button_favorite_big.setVisibility(View.VISIBLE);
				myAnimation_Alpha.setAnimationListener(HomepageActivity.this);
				button_favorite_big.startAnimation(myAnimation_Alpha);
				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_1);
				textview_numlikes.setText((jokeList.get(jokeIndex).getNumLikes()+1)+"");
			}else{
				ApiRequests.unlikeJoke(mainHandler, jokeCurrent.getId(), jokeCurrent.getUserId());
				if(jokeList.get(jokeIndex).getNumLikes() != 0)
				textview_numlikes.setText((jokeList.get(jokeIndex).getNumLikes()-1)+"");
				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_2);
			}

			break;
		case R.id.homepage_button_share:
			break;
		case R.id.homepage_framelayout_play:
			if(jokeList.size() > 0){
				if(isPlay && !isPaused){
					pauseJoke();
				}else{
					playJoke();
				}
			}else{
				Log.e(DEBUG_TAG, "无笑话列表");
			}
			break;

		}

	}


	private void loadJoke(){
		if(jokeList != null && jokeList.size()>0){
		if(jokeList.get(jokeIndex).getFullPictureUrl() != null && !jokeList.get(jokeIndex).getFullPictureUrl().equals("null")){
			new ImageDownLoadTask(jokeList.get(jokeIndex).getId(),
					ApiRequests.buildAbsoluteUrl(jokeList.get(jokeIndex).getFullPictureUrl()), this).execute(imageview_pic);
		}
		Joke joke = jokeList.get(jokeIndex);
		Log.d(DEBUG_TAG, "Load Joke Called " + joke);
		textview_duration.setText(joke.getLength() + "\"");
			if(jokeList.get(jokeIndex).getIsLike()){
			button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_1);
		}else{
			button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_2);
				textview_duration.setText(jokeList.get(jokeIndex).getLength() + "\"");
			}
			textview_playCount.setText(jokeList.get(jokeIndex).getNumPlays()+"");
			textview_numlikes.setText(jokeList.get(jokeIndex).getNumLikes()+"");
		}
	}

	/**
	 * 开始
	 */
	private void playJoke(){
		try {
			linearlayout_volume.setVisibility(View.VISIBLE);
			jokeCurrent = jokeList.get(jokeIndex);
			//判断之前的倒计时是否完成，如果countDownTime不等于0，说明用户之前是暂停笑话，而不是笑话播放完成
			if(countDownTime <=0){
				startPlayCounttimer(jokeCurrent.getLength());
			}else{
				startPlayCounttimer((int)countDownTime);
			}
			
			
			textview_duration.setText(jokeCurrent.getLength()+"\"");
			//Log.d(DEBUG_TAG, "isPlay = " + isPlay + " , index_joke = " + index_joke);
			if(!isPlay && !isPaused){
				isPlay = true;
				AudioUtils.prepareStreamAudio(mediaPlayer, ApiRequests.buildAbsoluteUrl(jokeCurrent.getFullAudioUrl()), this);	
				mTimer = new Timer();
				mTimerTask = getTimerTask();
				mTimer.schedule(mTimerTask, 0, 1000);
			}else if(isPaused){
				mediaPlayer.start();
				isPaused = false;
			} else{
				
				//mediaPlayer.start();
				//isPlay = true;
			}
		} catch (IllegalArgumentException e) {
			Log.e(DEBUG_TAG, "Exception in PlayJoke " + e + ", " + e.getMessage());
		} catch (SecurityException e) {
			Log.e(DEBUG_TAG, "Exception in PlayJoke " + e + ", " + e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(DEBUG_TAG, "Exception in PlayJoke " + e + ", " + e.getMessage());
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "Exception in PlayJoke " + e + ", " + e.getMessage());
		}
		framelayout_play.setBackgroundResource(R.drawable.btn);
		textview_playCount.setVisibility(View.GONE);
	}

	/**
	 * 暂停
	 */
	private void pauseJoke(){
		//暂停笑话的同时暂停动画效果
		if(null != countDownTimer){
			countDownTimer.cancel();
		}
		//暂停，释放锁
		releaseWakeLock();
		//暂停笑话
		Log.d(DEBUG_TAG, "Pause Joke called");
//		isStartAnim = false;
		AudioUtils.pausePlaying(mediaPlayer);
		isPaused = true;
		//		linearlayout_volume.setVisibility(View.GONE);
		//		framelayout_play.setBackgroundResource(R.drawable.playback_play);
		//		textview_duration.setText("无数据");
		//		textview_playCount.setVisibility(View.VISIBLE);
		//		textview_playCount.setText("无数据");
	}


	/**
	 * 停止
	 */
	private void stopJoke(){
		//暂停笑话
		isPlay = false;
		AudioUtils.stopPlaying(mediaPlayer);
		textview_playCount.setVisibility(View.VISIBLE);
		textview_playCount.setText(jokeList.get(jokeIndex).getNumPlays()+"");
		textview_duration.setText(jokeCurrent.getLength()+"\"");
		linearlayout_volume.setVisibility(View.GONE);
		framelayout_play.setBackgroundResource(R.drawable.playback_play);
	}

	/**
	 * 未开始
	 */
	private void currentJoke(){
		linearlayout_volume.setVisibility(View.GONE);
		framelayout_play.setBackgroundResource(R.drawable.playback_play);
		textview_duration.setText("150"+"\"");
		textview_playCount.setVisibility(View.VISIBLE);
		textview_playCount.setText("3456");
	}

	/**
	 * 播放中动画效果
	 */
	private void startPlayCounttimer(int time){
		if(null != countDownTimer){
			countDownTimer.cancel();
		}
		//启动倒计时时给程序加锁，爆出cpu运行
		acquireWakeLock();
		
		countDownTimer = new CountDownTimer((time+1) * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
				countDownTime = millisUntilFinished;
			//通知改变动画
			if(add){
				if(count > 7){
					add = false;
					count--;
				}else{
					mainHandler.sendEmptyMessage(CHANGEVOLUME);
					count++;
				}

			}else if(!add){
				if(count < 0){
					add = true;
					count++;
				}else{
					mainHandler.sendEmptyMessage(CHANGEVOLUME);
					count--;
				}
			}
			}

			public void onFinish() {
				countDownTime = 0;
				//倒计时结束，释放锁
				releaseWakeLock();
			}
		}.start();
	}

	/**
	 * 判断笑话是否已喜欢
	 */
	private boolean isLike(Joke joke){
		if(jokeLikeList.size()>0){
			for(int i=0; i<jokeLikeList.size();i++){
				if(joke.getId()==jokeLikeList.get(i).getId()){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 保存是否是第一次进入程序
	 */
	public void saveSettingTime(String isFrist){
		SharedPreferences mShared = null;
		mShared = getSharedPreferences("Jokes", Context.MODE_PRIVATE);  
		Editor editor = mShared.edit();  
		editor.putString("DATE", getTodayToString()); 
		editor.putString("ISFRIST", isFrist);
		editor.commit();  
	}

	public String loadSettingTime(){
		SharedPreferences mShared = null;
		mShared = getSharedPreferences("Jokes", Context.MODE_PRIVATE);
		return mShared.getString("DATE", "");
	}

	public void deleteSettingTime(){
		SharedPreferences mShared = null;
		mShared = getSharedPreferences("Jokes",Context.MODE_PRIVATE);
		Editor editor = mShared.edit();
		editor.remove("DATE");
		editor.remove("ISFRIST");
		editor.commit();
	}

	public static String getTodayToString() {
		// 转换日期，获得今天之后n天的日期
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		date = calendar.getTime();
		calendar.setTime(date);
		return String.format("%1$04d-%2$02d-%3$02d",
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH));

	}

	/**
	 * 根据音量改变控件大小
	 */
	private void changeView(int volume){
		switch(volume){
		case 0:{
			imageview_volume_1.setBackgroundResource(R.drawable.vertical_red_1);
			imageview_volume_2.setBackgroundResource(R.drawable.vertical_red_2);
			imageview_volume_3.setBackgroundResource(R.drawable.vertical_red_3);
			imageview_volume_4.setBackgroundResource(R.drawable.vertical_red_4);
			imageview_volume_5.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_6.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_7.setBackgroundResource(R.drawable.vertical_red_7);
			imageview_volume_8.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_9.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_10.setBackgroundResource(R.drawable.vertical_red_4);
			imageview_volume_11.setBackgroundResource(R.drawable.vertical_red_3);
			imageview_volume_12.setBackgroundResource(R.drawable.vertical_red_2);
			imageview_volume_13.setBackgroundResource(R.drawable.vertical_red_1);
		}
		break;
		case 1:{
			imageview_volume_1.setBackgroundResource(R.drawable.vertical_gray_1);
			imageview_volume_2.setBackgroundResource(R.drawable.vertical_red_2);
			imageview_volume_3.setBackgroundResource(R.drawable.vertical_red_3);
			imageview_volume_4.setBackgroundResource(R.drawable.vertical_red_4);
			imageview_volume_5.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_6.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_7.setBackgroundResource(R.drawable.vertical_red_7);
			imageview_volume_8.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_9.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_10.setBackgroundResource(R.drawable.vertical_red_4);
			imageview_volume_11.setBackgroundResource(R.drawable.vertical_red_3);
			imageview_volume_12.setBackgroundResource(R.drawable.vertical_red_2);
			imageview_volume_13.setBackgroundResource(R.drawable.vertical_gray_1);
		}
		break;
		case 2:{
			imageview_volume_1.setBackgroundResource(R.drawable.vertical_gray_1);
			imageview_volume_2.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_3.setBackgroundResource(R.drawable.vertical_red_3);
			imageview_volume_4.setBackgroundResource(R.drawable.vertical_red_4);
			imageview_volume_5.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_6.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_7.setBackgroundResource(R.drawable.vertical_red_7);
			imageview_volume_8.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_9.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_10.setBackgroundResource(R.drawable.vertical_red_4);
			imageview_volume_11.setBackgroundResource(R.drawable.vertical_red_3);
			imageview_volume_12.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_13.setBackgroundResource(R.drawable.vertical_gray_1);
		}
		break;
		case 3:{
			imageview_volume_1.setBackgroundResource(R.drawable.vertical_gray_1);
			imageview_volume_2.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_3.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_4.setBackgroundResource(R.drawable.vertical_red_4);
			imageview_volume_5.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_6.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_7.setBackgroundResource(R.drawable.vertical_red_7);
			imageview_volume_8.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_9.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_10.setBackgroundResource(R.drawable.vertical_red_4);
			imageview_volume_11.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_12.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_13.setBackgroundResource(R.drawable.vertical_gray_1);
		}
		break;
		case 4:{
			imageview_volume_1.setBackgroundResource(R.drawable.vertical_gray_1);
			imageview_volume_2.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_3.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_4.setBackgroundResource(R.drawable.vertical_gray_4);
			imageview_volume_5.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_6.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_7.setBackgroundResource(R.drawable.vertical_red_7);
			imageview_volume_8.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_9.setBackgroundResource(R.drawable.vertical_red_5);
			imageview_volume_10.setBackgroundResource(R.drawable.vertical_gray_4);
			imageview_volume_11.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_12.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_13.setBackgroundResource(R.drawable.vertical_gray_1);
		}
		break;
		case 5:{
			imageview_volume_1.setBackgroundResource(R.drawable.vertical_gray_1);
			imageview_volume_2.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_3.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_4.setBackgroundResource(R.drawable.vertical_gray_4);
			imageview_volume_5.setBackgroundResource(R.drawable.vertical_gray_5);
			imageview_volume_6.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_7.setBackgroundResource(R.drawable.vertical_red_7);
			imageview_volume_8.setBackgroundResource(R.drawable.vertical_red_6);
			imageview_volume_9.setBackgroundResource(R.drawable.vertical_gray_5);
			imageview_volume_10.setBackgroundResource(R.drawable.vertical_gray_4);
			imageview_volume_11.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_12.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_13.setBackgroundResource(R.drawable.vertical_gray_1);
		}
		break;
		case 6:{
			imageview_volume_1.setBackgroundResource(R.drawable.vertical_gray_1);
			imageview_volume_2.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_3.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_4.setBackgroundResource(R.drawable.vertical_gray_4);
			imageview_volume_5.setBackgroundResource(R.drawable.vertical_gray_5);
			imageview_volume_6.setBackgroundResource(R.drawable.vertical_gray_6);
			imageview_volume_7.setBackgroundResource(R.drawable.vertical_red_7);
			imageview_volume_8.setBackgroundResource(R.drawable.vertical_gray_6);
			imageview_volume_9.setBackgroundResource(R.drawable.vertical_gray_5);
			imageview_volume_10.setBackgroundResource(R.drawable.vertical_gray_4);
			imageview_volume_11.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_12.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_13.setBackgroundResource(R.drawable.vertical_gray_1);
		}
		break;
		case 7:{
			imageview_volume_1.setBackgroundResource(R.drawable.vertical_gray_1);
			imageview_volume_2.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_3.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_4.setBackgroundResource(R.drawable.vertical_gray_4);
			imageview_volume_5.setBackgroundResource(R.drawable.vertical_gray_5);
			imageview_volume_6.setBackgroundResource(R.drawable.vertical_gray_6);
			imageview_volume_7.setBackgroundResource(R.drawable.vertical_gray_7);
			imageview_volume_8.setBackgroundResource(R.drawable.vertical_gray_6);
			imageview_volume_9.setBackgroundResource(R.drawable.vertical_gray_5);
			imageview_volume_10.setBackgroundResource(R.drawable.vertical_gray_4);
			imageview_volume_11.setBackgroundResource(R.drawable.vertical_gray_3);
			imageview_volume_12.setBackgroundResource(R.drawable.vertical_gray_2);
			imageview_volume_13.setBackgroundResource(R.drawable.vertical_gray_1);
		}
		break;
		}

	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		isPlay = true;
		arg0.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.reset();
		jokeIndex++;
		isPlay = false;
		isPaused = false;
		mTimer.cancel();
		mTimerTask.cancel();
		// 播放下一条
		mainHandler.sendEmptyMessage(PLAY_NEXT);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
		seekbar.setSecondaryProgress(bufferingProgress);  
		//int currentProgress=seekbar.getMax()*mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration();  
		//Log.d(currentProgress+"% play", bufferingProgress + "% buffer");
	}
	
	private TimerTask getTimerTask(){
		return new TimerTask() {  
			@Override  
			public void run() {  
				if(mediaPlayer==null)  
					return;  
				if (mediaPlayer.isPlaying() && seekbar.isPressed() == false) {  
					handleProgress.sendEmptyMessage(0);  
				}  
			}  
		};  
	}

	Handler handleProgress = new Handler() {  
		public void handleMessage(Message msg) {  

			int position = mediaPlayer.getCurrentPosition();  
			int duration = mediaPlayer.getDuration();  

			if (duration > 0) {  
				long pos = seekbar.getMax() * position / duration;  
				seekbar.setProgress((int) pos);  
			}  
		};  
	}; 
	
	/**
	 * 给程序加锁，保持CPU 运转，屏幕和键盘灯有可能是关闭的
	 */
	private void acquireWakeLock(){
		if(null == wakelock){
			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
			wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "COUNTDOWNWAKELOCE");
			if(null != wakelock){
				wakelock.acquire();
			}
		}
	}
	
	/**
	 * 释放倒计时锁
	 */
	private void releaseWakeLock(){
		if(null != wakelock){
			wakelock.release();
			wakelock = null;
		}
	}
	
	//转换日期，获得今天之后n天的日期
		public static String getDateAfterFormat_(int n) {  
		    Calendar calendar = Calendar.getInstance(); 
		    Date date = new Date();
			date = calendar.getTime();
			calendar.setTime(date);    
			calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + n); 
		    return String.format("%1$04d%2$02d%3$02d", 
					calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH));
		} 
		
}



