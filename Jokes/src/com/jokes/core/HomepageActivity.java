package com.jokes.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import android.support.v4.app.FragmentActivity;
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

import com.handmark.pulltorefresh.extras.viewpager.PullToRefreshViewPager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.jokes.ext.VerticalViewPager;
import com.jokes.ext.VerticalViewPager.OnPageChangeListener;
import com.jokes.objects.Joke;
import com.jokes.objects.Like;
import com.jokes.share.WeChatShare;
import com.jokes.utils.ApiRequests;
import com.jokes.utils.AudioUtils;
import com.jokes.utils.Constant;
import com.jokes.utils.HandlerCodes;
import com.jokes.utils.Installation;
import com.jokes.utils.JokePageAdapter;
import com.jokes.utils.Tools;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.umeng.analytics.MobclickAgent;

public class HomepageActivity extends FragmentActivity implements OnClickListener,AnimationListener,
	OnPreparedListener, OnCompletionListener , IWXAPIEventHandler, OnBufferingUpdateListener, OnPageChangeListener, 
	OnRefreshListener<VerticalViewPager>{
	
	private static final String DEBUG_TAG = "JOKE";
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
	private int jokeIndex = 0;//当前播放索引

	//分享
	private IWXAPI weChatShareApi;
	private int page = 2;//当前页为page-1

	boolean isGetJokeSuccesss = true;//记录第一次获取笑话列表失败
	Animation myAnimation_Alpha;

	MediaPlayer mediaPlayer;
	TimerTask mTimerTask;
	private Timer mTimer;//播放进度条使用timer
	CountDownTimer countDownTimer;//播放动画效果的倒计时
	long countDownTime = 0;//倒计时剩余时间

	WakeLock wakelock = null;//保持程序部睡眠
	private static String UID; //TODO save this so it doesn't change 
	
	//For Paging through joke list
    private JokePageAdapter jokePageAdapter;
    private com.jokes.ext.VerticalViewPager viewPager;
	private PullToRefreshViewPager mPullToRefreshViewPager;
	
	private Handler mainHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {

			switch(msg.what){
			case HandlerCodes.GET_JOKES_SUCCESS:
				Log.d(DEBUG_TAG, "Jokes success message received, printing... size = " + jokeList.size());
				mPullToRefreshViewPager.onRefreshComplete();
				//linearlayout_progressdialog.setVisibility(View.GONE);
				 jokePageAdapter = new JokePageAdapter(HomepageActivity.this.getSupportFragmentManager(), 
						 HomepageActivity.this, jokeList, mediaPlayer, HomepageActivity.this, mainHandler, UID);
			     viewPager.setAdapter(jokePageAdapter);
				
				/*
				page++;
				jokeIndex = 0;
				if(!isPlay){
					loadJoke();
					linearlayout_progressdialog.setVisibility(View.GONE);
				}*/
				break;
			case HandlerCodes.GET_JOKES_FAILURE:
//				linearlayout_progressdialog.setVisibility(View.GONE);
				if(page == 0){
					Toast.makeText(HomepageActivity.this,"获取笑话列表失败",Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(HomepageActivity.this,"你已经听到底了，明天再来听吧",Toast.LENGTH_SHORT).show();
				}

				break;
			case HandlerCodes.LIKE_SUCCESS:
				Button button_favorite_big = (Button)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_button_favorite_big);
				Button button_favorite_small = (Button)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_button_favorite_small);
				button_favorite_big.setVisibility(View.VISIBLE);
				myAnimation_Alpha = AnimationUtils.loadAnimation(HomepageActivity.this, R.anim.anim_alpha_heart);
				myAnimation_Alpha.setAnimationListener(HomepageActivity.this);
				myAnimation_Alpha.setDuration(JokePageAdapter.LIKE_BTN_ANI_LEN);
				button_favorite_big.startAnimation(myAnimation_Alpha);
				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_1);
				button_favorite_small.setTag(true);
				TextView textview_numlikes = (TextView)currentPagerView.findViewById(R.id.homepage_textview_numlikes);
				textview_numlikes.setText((Integer.parseInt(textview_numlikes.getText().toString())+1)+"");
				break;
			case HandlerCodes.LIKE_FAILURE:
				break;
			case HandlerCodes.UNLIKE_SUCCESS:
				Button temp_button_favorite_small = (Button)currentPagerView.findViewById(R.id.homepage_button_favorite_small);
				TextView temp_textview = (TextView)currentPagerView.findViewById(R.id.homepage_textview_numlikes);
				if(Integer.parseInt(temp_textview.getText().toString()) != 0){
					temp_textview.setText((Integer.parseInt(temp_textview.getText().toString())-1)+"");
				}
				temp_button_favorite_small.setTag(false);
				temp_button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_2);
				break;
			case HandlerCodes.UNLIKE_FAILURE:

				break;
			case HandlerCodes.GET_LIKEJOKES_SUCCESS:

				break;
			case HandlerCodes.GET_LIKEJOKES_FAILURE:
				break;
			case CHANGEVOLUME:
				//changeView(count);
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
//		MobclickAgent.onError(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.homepage_activity);
		setUid();
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

		//initView();
		//initAnim();
		//initValues();

		initMediaPlayer();


		mPullToRefreshViewPager = (PullToRefreshViewPager) findViewById(R.id.mainJokeListPager);
		mPullToRefreshViewPager.setOnRefreshListener(this);

		viewPager = mPullToRefreshViewPager.getRefreshableView();
		viewPager.setOnPageChangeListener(this);
		jokeList = new ArrayList<Joke>();
		ApiRequests.getJokes(mainHandler, jokeList, UID , 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mediaPlayer.isPlaying()){
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
		(jokePageAdapter.getCurrentView().findViewById(R.id.homepage_button_favorite_big)).setVisibility(View.GONE);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub

	}
	
	private void setUid(){
		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		final String preferencesUidString = preferences.getString(Constant.PREFERENCES_UID_KEY, null);
		if(null == preferencesUidString){
			UID = Installation.id(this);
			Editor editor = preferences.edit();
			editor.putString(Constant.PREFERENCES_UID_KEY, UID);
			editor.apply();
		} else {
			UID = preferencesUidString;
		}
		Constant.uid = UID;
	}

	
	private void initMediaPlayer(){
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);  
		mediaPlayer.setOnPreparedListener(this);
	}


	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.homepage_button_setting:
			Intent intent1 = new Intent(HomepageActivity.this,SettingActivity.class);
			startActivity(intent1);
			break;
		case R.id.homepage_button_refresh:
			page = 2;
			ApiRequests.getJokes(mainHandler, jokeList, UID, page);
			break;
		case R.id.homepage_button_record:
			Intent intent2 = new Intent(HomepageActivity.this,RecordActivity.class);
			startActivity(intent2);
			break;
		case R.id.homepage_button_favorite_small:
			if(!jokeCurrent.getIsLike()){
				ApiRequests.likeJoke(mainHandler, jokeCurrent.getId(), jokeCurrent.getUserId());

				//假操作，先改变界面，用户体验好，后台ApiRequests.likeJoke；
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
			WeChatShare.sendAppInfo(weChatShareApi, HomepageActivity.this.getResources(), HomepageActivity.this);

			break;
		case R.id.homepage_framelayout_play:
			/*
			if(jokeList.size() > 0){
				if(isPlay && !isPaused){
					pauseJoke();
				}else{
					playJoke();
				}
			}else{
				Log.e(DEBUG_TAG, "无笑话列表");
			}
			break;*/

		}

	}

	/**
	 * 未开始
	 */
	private void currentJoke(){
		linearlayout_volume.setVisibility(View.GONE);
		framelayout_play.setBackgroundResource(R.drawable.playback_play);
		textview_duration.setText(jokeList.get(jokeIndex).getLength()+"\"");
		textview_playCount.setVisibility(View.VISIBLE);
		textview_playCount.setText(jokeList.get(jokeIndex).getNumPlays()+"");
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

		/*
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

			public void onFinish() {
				countDownTime = 0;
				//倒计时结束，释放锁
				releaseWakeLock();
			}
		}.start();
			}*/
	}

	/**
	 * 保存是否是第一次进入程序
	 */
	public void saveSettingTime(String isFrist){
		SharedPreferences mShared = null;
		mShared = getSharedPreferences("Jokes", Context.MODE_PRIVATE);  
		Editor editor = mShared.edit();  
		editor.putString("DATE", Tools.getTodayToString()); 
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


	@Override
	public void onPrepared(MediaPlayer arg0) {
		arg0.start();
	}
	
	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
		//seekbar.setSecondaryProgress(bufferingProgress);  
		//int currentProgress=seekbar.getMax()*mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration();  
		//Log.d(currentProgress+"% play", bufferingProgress + "% buffer");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.reset();
		jokePageAdapter.resetPlayer();
		//播放结束，先将播放状态还原为未播放状态
		/*
		currentJoke();
		mp.reset();
		jokeIndex++;
		isPlay = false;
		isPaused = false;		mTimer.cancel();
		mTimerTask.cancel();
		// 播放下一条
		mainHandler.sendEmptyMessage(PLAY_NEXT);*/
	}
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

	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			goToGetMsg();		
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			goToShowMsg((ShowMessageFromWX.Req) req);
			break;
		default:
			break;
		}
	}
	@Override
	public void onResp(BaseResp resp) {
		int result = 0;

		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = R.string.errcode_success ;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}

		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	}

	private void goToGetMsg() {
		/*Intent intent = new Intent(this, GetFromWXActivity.class);
			intent.putExtras(getIntent());
			startActivity(intent);
			finish();*/
		Log.d(DEBUG_TAG, "Go Get Msg WeChat");
	}

	private void goToShowMsg(ShowMessageFromWX.Req showReq) {
		Log.d(DEBUG_TAG, "Show Msg WeChat");

		/*
			WXMediaMessage wxMsg = showReq.message;		
			WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

			StringBuffer msg = new StringBuffer(); 
			msg.append("description: ");
			msg.append(wxMsg.description);
			msg.append("\n");
			msg.append("extInfo: ");
			msg.append(obj.extInfo);
			msg.append("\n");
			msg.append("filePath: ");
			msg.append(obj.filePath);

			Intent intent = new Intent(this, ShowFromWXActivity.class);
			//intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
			//intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
			//intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
			startActivity(intent);
			finish();*/
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			mediaPlayer.reset();
			jokePageAdapter.resetPlayer();
		}

		@Override
		public void onPageSelected(int position) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
			
		}
		
		public void onSettingsButtonClick(View view){
			Intent intent1 = new Intent(HomepageActivity.this,SettingActivity.class);
			startActivity(intent1);
		}
		
		public void onRecordButtonClick(View view){
			Intent intent2 = new Intent(HomepageActivity.this,RecordActivity.class);
			startActivity(intent2);
		}

		@Override
		public void onRefresh(PullToRefreshBase<VerticalViewPager> refreshView) {
			ApiRequests.getJokes(mainHandler, jokeList, UID, 0);
			
		}
		
		
}



