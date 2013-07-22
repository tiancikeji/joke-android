package com.jokes.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.handmark.pulltorefresh.extras.viewpager.PullToRefreshViewPager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.jokes.database.DataBase;
import com.jokes.ext.VerticalViewPager;
import com.jokes.ext.VerticalViewPager.OnPageChangeListener;
import com.jokes.objects.Joke;
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
	OnRefreshListener<VerticalViewPager>, OnAudioFocusChangeListener{
	
	private static final String DEBUG_TAG = "JOKE";
	private static final int DATE_STR_LEN_MINUS_TIME = 11;

	Button button_setting;//设置按钮
	Button button_refresh;//刷新
	Button button_record;//录音按钮
	Button button_favorite_big;//图片中间收藏按钮
//	Button button_favorite_small;//收藏按钮
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
	
	RelativeLayout relativeLayout_share;
	
	private List<Joke> offlineJokeList;;//保存离线笑话列表
	private List<Joke> jokeList;

	//分享
	private IWXAPI weChatShareApi;

	boolean isGetJokeSuccesss = true;//记录第一次获取笑话列表失败
	Animation myAnimation_Alpha;

	MediaPlayer mediaPlayer;
	TimerTask mTimerTask;
	private Timer mTimer;//播放进度条使用timer
	CountDownTimer countDownTimer;//播放动画效果的倒计时
	long countDownTime = 0;//倒计时剩余时间

	private boolean isOnline = false;//用来判断是否处在有网络状态,true为有网络，false为离线状态
	
	private static String UID; //TODO save this so it doesn't change 
	
	//For Paging through joke list
    private JokePageAdapter jokePageAdapter;
    private com.jokes.ext.VerticalViewPager viewPager;
	private PullToRefreshViewPager mPullToRefreshViewPager;
	private int currentPagingJokePage = 1;
	private WakeLock wakeLock;
	
	private Handler mainHandler = new Handler(){
		Button button_favorite_small;
		LinearLayout linearlayout_like_small;
		@Override
		public void handleMessage(Message msg) {

			switch(msg.what){
			case HandlerCodes.GET_JOKES_SUCCESS:
			{
				Log.d(DEBUG_TAG, "Jokes success message received, printing... size = " + jokeList.size());
				mPullToRefreshViewPager.onRefreshComplete();
				if(currentPagingJokePage <= 1){
					refreshWakeLock();
					jokePageAdapter = new JokePageAdapter(
							HomepageActivity.this.getSupportFragmentManager(),
							HomepageActivity.this, jokeList, mediaPlayer,
							HomepageActivity.this, mainHandler, UID, weChatShareApi,isOnline, wakeLock);
					viewPager.setAdapter(jokePageAdapter);
					if (jokeList.size() > 0) {
						TextView dateTextView = (TextView) findViewById(R.id.homepage_textview_date);
						dateTextView.setText(jokeList.get(0).getApprovalTime()
								.substring(0, DATE_STR_LEN_MINUS_TIME));
					}
				} else {
					//jokePageAdapter.addToJokeListAndRefresh(jokeList);
					jokePageAdapter.notifyDataSetChanged();
				}
				break;
			}
			case HandlerCodes.GET_JOKES_FAILURE:
				Toast.makeText(HomepageActivity.this,"获取笑话列表失败",Toast.LENGTH_SHORT).show();
				break;
			case HandlerCodes.GET_JOKES_NULL:
				Toast.makeText(HomepageActivity.this,"你已经听到底了，明天再来听吧",Toast.LENGTH_SHORT).show();
				break;
			case HandlerCodes.GET_JOKES_REFRESH_SUCCESS:
				mPullToRefreshViewPager.onRefreshComplete();
				refreshWakeLock();
				jokePageAdapter = new JokePageAdapter(
						HomepageActivity.this.getSupportFragmentManager(),
						HomepageActivity.this, jokeList, mediaPlayer,
						HomepageActivity.this, mainHandler, UID, weChatShareApi,
						isOnline, wakeLock);
				viewPager.setAdapter(jokePageAdapter);
				if (jokeList.size() > 0) {
					TextView dateTextView = (TextView) findViewById(R.id.homepage_textview_date);
					dateTextView.setText(jokeList.get(0).getApprovalTime()
							.substring(0, DATE_STR_LEN_MINUS_TIME));
				}
				Bundle bundle = new Bundle();
				int temp_count = bundle.getInt("update_count");
				if(temp_count>0)
				Toast.makeText(HomepageActivity.this, "更新了"+temp_count+"条笑话", Toast.LENGTH_SHORT).show();
				break;
			case HandlerCodes.LIKE_SUCCESS:
				Button button_favorite_big = (Button)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_button_favorite_big);
				linearlayout_like_small = ((LinearLayout)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_linearlayout_favorite_small));
				linearlayout_like_small.setClickable(true);
				linearlayout_like_small.setTag(true);
				button_favorite_small = (Button)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_button_favorite_small);
				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_1);
				
				button_favorite_big.setVisibility(View.VISIBLE);
				myAnimation_Alpha = AnimationUtils.loadAnimation(HomepageActivity.this, R.anim.anim_alpha_heart);
				myAnimation_Alpha.setAnimationListener(HomepageActivity.this);
				myAnimation_Alpha.setDuration(JokePageAdapter.LIKE_BTN_ANI_LEN);
				button_favorite_big.startAnimation(myAnimation_Alpha);
				
				TextView textview_numlikes = (TextView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_textview_numlikes);
				textview_numlikes.setText((Integer.parseInt(textview_numlikes.getText().toString())+1)+"");
				break;
			case HandlerCodes.LIKE_FAILURE:
//				Toast.makeText(HomepageActivity.this,"喜欢失败",Toast.LENGTH_SHORT).show();
				linearlayout_like_small = ((LinearLayout)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_linearlayout_favorite_small));
				linearlayout_like_small.setClickable(true);
				break;
			case HandlerCodes.UNLIKE_SUCCESS:
				button_favorite_small = (Button)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_button_favorite_small);
				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_2);
				linearlayout_like_small = ((LinearLayout)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_linearlayout_favorite_small));
				linearlayout_like_small.setClickable(true);
				linearlayout_like_small.setTag(false);
				TextView temp_textview = (TextView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_textview_numlikes);
				if(Integer.parseInt(temp_textview.getText().toString()) != 0){
					temp_textview.setText((Integer.parseInt(temp_textview.getText().toString())-1)+"");
				}
				
				break;
			case HandlerCodes.UNLIKE_FAILURE:
				linearlayout_like_small = ((LinearLayout)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_linearlayout_favorite_small));
				linearlayout_like_small.setClickable(true);
				break;
			case HandlerCodes.GET_LIKEJOKES_SUCCESS:

				break;
			case HandlerCodes.GET_LIKEJOKES_FAILURE:
				break;
			case HandlerCodes.CREATE_JOKE_SUCCESS:
				Log.d(DEBUG_TAG, "Create joke success");
				break;
			case HandlerCodes.CREATE_JOKE_FAILURE:
				Log.d(DEBUG_TAG, "Create joke failure");
				break;
			case HandlerCodes.MESSAGE_SHARE:
				startAnimShare();
				break;
			case HandlerCodes.ADD_PLAY_FAILURE:
				Log.e(DEBUG_TAG, "Error adding play");
				break;
			case HandlerCodes.ADD_PLAY_SUCCESS:
				Log.d(DEBUG_TAG, "Add play success");
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
		setUid();

		initMediaPlayer();

		relativeLayout_share = (RelativeLayout)findViewById(R.id.homepage_dialog_timeout);//选择分享方式的linearlayout
		relativeLayout_share.setTag(false);//分享选择框处于隐藏状态
		
		weChatShareApi = WeChatShare.regToWx(this);
		weChatShareApi.handleIntent(getIntent(), this);
		
		mPullToRefreshViewPager = (PullToRefreshViewPager) findViewById(R.id.mainJokeListPager);
		mPullToRefreshViewPager.setOnRefreshListener(this);

		viewPager = mPullToRefreshViewPager.getRefreshableView();
		viewPager.setOnPageChangeListener(this);
		
		viewPager.setPadding(0, 5, 0, 15);
		//viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
		viewPager.setVerticalFadingEdgeEnabled(true);
		viewPager.setFadingEdgeLength(10);
		
		
		jokeList = new ArrayList<Joke>();
		
		//判断是否有网络
		if(Tools.isNetworkAvailable(HomepageActivity.this)){
			isOnline = true;
//			ApiRequests.getJokes(mainHandler, jokeList, UID , 0, true);
			ApiRequests.getJokes(mainHandler, jokeList, UID, Tools.getTodayFormat_(), 0, true);
			Log.e("请求日期", Tools.getTodayFormat_()+"【"+0+"】");
		}else{
			isOnline = false;
			offlineJokeList = getOfflineJokesList();
			setOfflineJokesToJokeList();
			mainHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
		}
//		acquireWakeLock();
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
		if(null != mTimer){
			mTimer.cancel();
			mTimer = null;
		}
		
//		releaseWakeLock();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//判断分享选择框是否显示，如果现实则关闭选择框
			if((Boolean)relativeLayout_share.getTag()){
				relativeLayout_share.setVisibility(View.GONE);
				relativeLayout_share.setTag(false);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
		
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
	
	@SuppressLint("NewApi")
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
		mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
	}


	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.homepage_button_setting:
			Intent intent1 = new Intent(HomepageActivity.this,SettingActivity.class);
			startActivity(intent1);
			break;
		case R.id.homepage_button_record:
			Intent intent2 = new Intent(HomepageActivity.this,RecordActivity.class);
			startActivity(intent2);
			break;
		}

	}

	/**

	 * 未开始
	 */
	/*
	private void currentJoke(){
		linearlayout_volume.setVisibility(View.GONE);
		framelayout_play.setBackgroundResource(R.drawable.playback_play);
		textview_duration.setText(jokeList.get(jokeIndex).getLength()+"\"");
		textview_playCount.setVisibility(View.VISIBLE);
		textview_playCount.setText(jokeList.get(jokeIndex).getNumPlays()+"");
	}
*/
	/**
	 * 播放中动画效果
	 */
	private void startPlayCounttimer(int time){
		if(null != countDownTimer){
			countDownTimer.cancel();
		}

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
>>>>>>> working on wakelock, not yet finished
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
	
	private void refreshWakeLock(){
		if(null != wakeLock && wakeLock.isHeld()){
			wakeLock.release();
		} else {
			PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
			wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		}
	}


	@Override
	public void onPrepared(MediaPlayer arg0) {
		refreshWakeLock();
		wakeLock.acquire();
		arg0.start();
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
		    AudioManager.AUDIOFOCUS_GAIN);
		
		jokePageAdapter.startPlayAnimation();
		Joke joke = jokePageAdapter.getCurrentJoke();
		//检查联网
		if(Tools.isNetworkAvailable(HomepageActivity.this)){
			ApiRequests.addPlay(mainHandler, joke, Constant.uid);
		}
		
		//Fix the length of the joke if it is wrong, a temp fix for uploading using web version not having length
		/*View view = jokePageAdapter.getCurrentView();
		Joke joke = jokePageAdapter.getJokeFromView(view);
		int length = (int) Math.round(((float)arg0.getDuration() / 1000.0)); 
		joke.setLength(length);
		((TextView)view.findViewById(R.id.homepage_textview_duration)).setText(length + "\"");*/
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
		if(wakeLock.isHeld()){
			wakeLock.release();
		}
		AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_imageview_volume)).getDrawable();
		animationDrawable.stop();
		((ImageView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_imageview_volume)).setVisibility(View.GONE);
		framelayout_play = (FrameLayout)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_framelayout_play);
		framelayout_play.setBackgroundResource(R.drawable.playback_play);
		
		//音频播放完成，播放次数+1,并且显示播放次数
		TextView textview_numplays = ((TextView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_textview_playcount));
		textview_numplays.setVisibility(View.VISIBLE);
		textview_numplays.setText(jokePageAdapter.getCurrentJoke().getNumPlays()+"播放");
		//重新给音频长度控件赋值
		textview_duration = (TextView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_textview_duration);
		textview_duration.setText(jokePageAdapter.getCurrentJoke().getLength()+"\"");
	}

	
//	/**
//	 * 给程序加锁，保持CPU 运转，屏幕和键盘灯有可能是关闭的
//	 */
//	private void acquireWakeLock(){
//		if(null == wakelock){
//			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
//			wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "COUNTDOWNWAKELOCE");
//			if(null != wakelock){
//				wakelock.acquire();
//			}
//		}
//	}
//
//	/**
//	 * 释放倒计时锁
//	 */
//	private void releaseWakeLock(){
//		if(null != wakelock){
//			wakelock.release();
//			wakelock = null;
//		}
//	}

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
			TextView dateTextView = (TextView)findViewById(R.id.homepage_textview_date);
			Joke joke = jokePageAdapter.getCurrentJoke();
			dateTextView.setText(joke.getApprovalTime().substring(0, DATE_STR_LEN_MINUS_TIME));
			//TextView playCountTextView = (TextView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_textview_playcount);
			//playCountTextView.setText(String.valueOf(joke.getNumPlays()));
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
			//如果有网络则正常浏览，如果无网络，处在离线状态则浏览利息那
			if(Tools.isNetworkAvailable(HomepageActivity.this)){
				if(offlineJokeList != null && offlineJokeList.size() != 0){
					//用户下载了离线数据，应该先播放离线 的数据
					setOfflineJokesToJokeList();
					mainHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
				}else{
					//无离线数据，且有网络时：上拉
					if(refreshView.getCurrentMode() == com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.PULL_FROM_END){
						currentPagingJokePage++;
//						ApiRequests.getJokes(mainHandler, jokeList, UID, currentPagingJokePage, false);
						ApiRequests.getJokes(mainHandler, jokeList, UID, Tools.getDateFormat_(jokeList.get(jokeList.size()-1).getApprovalTime()), 1, false);
						Log.e("请求日期", Tools.getDateFormat_(jokeList.get(jokeList.size()-1).getApprovalTime())+"【"+1+"】");
					} else {
						currentPagingJokePage = 1;
//						ApiRequests.getJokes(mainHandler, jokeList, UID, currentPagingJokePage, true);
						ApiRequests.getJokes(mainHandler, jokeList, UID, Tools.getTodayFormat_(), 0, true);
						Log.e("请求日期", Tools.getTodayFormat_()+"【"+0+"】");
						
					}
				}
			}else{
				if(offlineJokeList != null && offlineJokeList.size() != 0){
					//用户下载了离线数据，应该先播放离线 的数据
					currentPagingJokePage++;
					setOfflineJokesToJokeList();
					mainHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
				}else{
					//通知到底了
					
				}
			}
			
			
			
		}
		
		/**
		 * 加载分享选择框动画
		 */
		private void startAnimShare(){
//			relativeLayout_share = (RelativeLayout)findViewById(R.id.homepage_dialog_timeout);//选择分享方式的linearlayout
			relativeLayout_share.setTag(true);//分享选择框显示
			relativeLayout_share.setVisibility(View.VISIBLE);
			
			//判断是否支持分享到朋友圈
			if(!WeChatShare.checkIsShareToFriendsCircle(weChatShareApi)){
				Button button_shareToFriendsCircle = (Button)findViewById(R.id.homepage_button_friendscircle);
				button_shareToFriendsCircle.setVisibility(View.GONE);
			}
//			int yOffset  = (int)(relativeLayout_share.getHeight() * HomepageActivity.this.getResources().getDisplayMetrics().density);
//
//			Animation animation = new TranslateAnimation(0F,0F, yOffset,0);
//			animation.setDuration(2000);               //设置动画持续时间              
//			animation.setRepeatCount(0);    
//			animation.setAnimationListener(new AnimationListener(){
//
//				@Override
//				public void onAnimationEnd(Animation arg0) {
//					relativeLayout_share.setVisibility(View.VISIBLE);
//				}
//
//				@Override
//				public void onAnimationRepeat(Animation arg0) {
//				}
//
//				@Override
//				public void onAnimationStart(Animation arg0) {
//				}
//				
//			});
//			relativeLayout_share.setVisibility(View.VISIBLE);
//			relativeLayout_share.startAnimation(animation);
			
		}
	
	/**
	 * 分享到朋友圈
	 */
	public void onShareToFriendsCircleButtonClick(View view){
		relativeLayout_share.setVisibility(View.GONE);
		relativeLayout_share.setTag(false);
		WeChatShare.sendMusicToFriendsCircle(weChatShareApi, 
				HomepageActivity.this.getResources(), 
				HomepageActivity.this, 
				""+(((Button)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_button_share)).getTag()),
				""+(((ImageView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_imageview_pic)).getTag()));
		
	}
	
	/**
	 * 分享到微信好友
	 */
	public void onShareToFriendButtonClick(View view){
		relativeLayout_share.setVisibility(View.GONE);
		relativeLayout_share.setTag(false);
		WeChatShare.sendMusicToFriend(weChatShareApi, 
				HomepageActivity.this.getResources(), 
				HomepageActivity.this, 
				""+(((Button)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_button_share)).getTag()),
				""+(((ImageView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_imageview_pic)).getTag()));
	}
	
	/**
	 * 取消分享
	 */
	public void onShareToCancelButtonClick(View view){
		relativeLayout_share = (RelativeLayout)findViewById(R.id.homepage_dialog_timeout);//选择分享方式的linearlayout
		relativeLayout_share.setTag(false);//控件没有显示
		relativeLayout_share.setVisibility(View.GONE);
		
//		int yOffset  = (int)(relativeLayout_share.getHeight() * HomepageActivity.this.getResources().getDisplayMetrics().density);//偏移
//
//		Animation animation = new TranslateAnimation(0F,0F, 0,yOffset);
//		animation.setDuration(2000);               //设置动画持续时间              
//		animation.setRepeatCount(0);    
//		animation.setAnimationListener(new AnimationListener(){
//
//			@Override
//			public void onAnimationEnd(Animation arg0) {
//				relativeLayout_share.setVisibility(View.GONE);
//			}
//
//			@Override
//			public void onAnimationRepeat(Animation arg0) {
//			}
//
//			@Override
//			public void onAnimationStart(Animation arg0) {
//			}
//			
//		});
//		relativeLayout_share.startAnimation(animation);
		
	}
	
	/**
	 * 从数据库读取离线笑话列表
	 */
	private List<Joke> getOfflineJokesList(){
		List<Joke> list = new ArrayList<Joke>();
		
		DataBase db = new DataBase(HomepageActivity.this);
		db.open();
		db.beginTransaction();
		Cursor cursor = db.getOffLineJokes();
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			Joke tempJoke;
			int index_joke_id = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_JOKE_ID);
			int index_audio_size_in_b = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_AUDIO_SIZE_IN_B);
			int index_fullaudio_url = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_FULLAUDIO_URL);
			int index_fullpicture_url = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_FULLPICTURE_URL);
			int index_length = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_LENGTH);
			int index_num_likes = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_NUM_LIKES);
			int index_num_plays = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_NUM_PLAYS);
			int index_picture_size_in_b = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_PICTURE_SIZE_IN_B);
			int index_joke_uid = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_UID);
			int index_createat = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_APPROVAL_TIME);
			do{
				tempJoke = new Joke();
				tempJoke.setId(Integer.parseInt(cursor.getString(index_joke_id)));
				tempJoke.setAudioSizeInB(Integer.parseInt(cursor.getString(index_audio_size_in_b)));
				tempJoke.setFullAudioUrl(cursor.getString(index_fullaudio_url));
				tempJoke.setFullPictureUrl(cursor.getString(index_fullpicture_url));
				tempJoke.setLength(Integer.parseInt(cursor.getString(index_length)));
				tempJoke.setNumLikes(Integer.parseInt(cursor.getString(index_num_likes)));
				tempJoke.setNumPlays(Integer.parseInt(cursor.getString(index_num_plays)));
				tempJoke.setPictureSizeInB(Integer.parseInt(cursor.getString(index_picture_size_in_b)));
				tempJoke.setUserId(cursor.getString(index_joke_uid));
				tempJoke.setApprovalTime(cursor.getString(index_createat));
				list.add(tempJoke);
				
				cursor.moveToNext();
			}while(!cursor.isAfterLast());
		}
		cursor.close();
		db.endTransaction();
		db.close();
		
		return list;
	}
	
	/*
	 * 包离线笑话数据赋值给jokeList
	*/
	private void setOfflineJokesToJokeList(){
		if(offlineJokeList.size() > 0){
			int count = 0;
			for(int i=0;i < 5;i++){
				if(count < 5 && offlineJokeList.size() > 0){
					jokeList.add(offlineJokeList.get(0));
					offlineJokeList.remove(0);
					count++;
				}else{
					break;
				}
			}
		}

		
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		if(focusChange == AudioManager.AUDIOFOCUS_LOSS){
			mediaPlayer.pause();
			if(wakeLock.isHeld()){
				wakeLock.release();
			}
		}
	}
}



