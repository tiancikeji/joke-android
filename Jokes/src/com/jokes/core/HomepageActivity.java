package com.jokes.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
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
	
//	LinearLayout linearlayout_share;//选择分享方式的linearlayout
	RelativeLayout relativeLayout_share;
	
	private List<Joke> offlineJokeList;;//保存离线笑话列表
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
	
	private boolean isOnline = false;//用来判断是否处在有网络状态,true为有网络，false为离线状态
	
	private static String UID; //TODO save this so it doesn't change 
	
	//For Paging through joke list
    private JokePageAdapter jokePageAdapter;
    private com.jokes.ext.VerticalViewPager viewPager;
	private PullToRefreshViewPager mPullToRefreshViewPager;
	private int currentPagingJokePage = 1;
	
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
//					jokePageAdapter = new JokePageAdapter(
//							HomepageActivity.this.getSupportFragmentManager(),
//							HomepageActivity.this, jokeList, mediaPlayer,
//							HomepageActivity.this, mainHandler, UID, weChatShareApi);
					jokePageAdapter = new JokePageAdapter(
							HomepageActivity.this.getSupportFragmentManager(),
							HomepageActivity.this, jokeList, mediaPlayer,
							HomepageActivity.this, mainHandler, UID, weChatShareApi,isOnline);
					viewPager.setAdapter(jokePageAdapter);
					if (jokeList.size() > 0) {
						TextView dateTextView = (TextView) findViewById(R.id.homepage_textview_date);
						dateTextView.setText(jokeList.get(0).getUpdatedAt()
								.substring(0, DATE_STR_LEN_MINUS_TIME));
					}
				} else {
					//jokePageAdapter.addToJokeListAndRefresh(jokeList);
					jokePageAdapter.notifyDataSetChanged();
				}
				break;
			}
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
			case CHANGEVOLUME:
				//changeView(count);
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
			ApiRequests.getJokes(mainHandler, jokeList, UID , 0, true);
		}else{
			isOnline = false;
			offlineJokeList = getOfflineJokesList();
			setOfflineJokesToJokeList();
			mainHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
		}
		
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
			ApiRequests.getJokes(mainHandler, jokeList, UID, page, true);
			break;
		case R.id.homepage_button_record:
			Intent intent2 = new Intent(HomepageActivity.this,RecordActivity.class);
			startActivity(intent2);
			break;
		case R.id.homepage_button_favorite_small:
//			if(!jokeCurrent.getIsLike()){
//				ApiRequests.likeJoke(mainHandler, jokeCurrent.getId(), jokeCurrent.getUserId());
//
//				//假操作，先改变界面，用户体验好，后台ApiRequests.likeJoke；
//				jokeCurrent.setIsLike(true);
//				button_favorite_big.setVisibility(View.VISIBLE);
//				myAnimation_Alpha.setAnimationListener(HomepageActivity.this);
//				button_favorite_big.startAnimation(myAnimation_Alpha);
//				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_1);
//				textview_numlikes.setText((jokeList.get(jokeIndex).getNumLikes()+1)+"");
//			}else{
//				ApiRequests.unlikeJoke(mainHandler, jokeCurrent.getId(), jokeCurrent.getUserId());
//				if(jokeList.get(jokeIndex).getNumLikes() != 0)
//				textview_numlikes.setText((jokeList.get(jokeIndex).getNumLikes()-1)+"");
//				button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_2);
//			}
			
			break;
		case R.id.homepage_button_share:
//			WeChatShare.sendAppInfo(weChatShareApi, HomepageActivity.this.getResources(), HomepageActivity.this);

			break;
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
		AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_imageview_volume)).getDrawable();
		animationDrawable.stop();
		((ImageView)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_imageview_volume)).setVisibility(View.GONE);
		framelayout_play = (FrameLayout)jokePageAdapter.getCurrentView().findViewById(R.id.homepage_framelayout_play);
		framelayout_play.setBackgroundResource(R.drawable.playback_play);
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
			TextView dateTextView = (TextView)findViewById(R.id.homepage_textview_date);
			Joke joke = jokePageAdapter.getCurrentJoke();
			dateTextView.setText(joke.getUpdatedAt().substring(0, DATE_STR_LEN_MINUS_TIME));
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
					//无离线数据，且有网络
					if(refreshView.getCurrentMode() == com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.PULL_FROM_END){
						currentPagingJokePage++;
						ApiRequests.getJokes(mainHandler, jokeList, UID, currentPagingJokePage, false);
						
					} else {
						currentPagingJokePage = 1;
						ApiRequests.getJokes(mainHandler, jokeList, UID, currentPagingJokePage, true);
					}
				}
			}else{
				if(offlineJokeList != null && offlineJokeList.size() != 0){
					//用户下载了离线数据，应该先播放离线 的数据
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
			relativeLayout_share = (RelativeLayout)findViewById(R.id.homepage_dialog_timeout);//选择分享方式的linearlayout
			
			//判断是否支持分享到朋友圈
			if(!WeChatShare.checkIsShareToFriendsCircle(weChatShareApi)){
				Button button_shareToFriendsCircle = (Button)findViewById(R.id.homepage_button_friendscircle);
				button_shareToFriendsCircle.setVisibility(View.GONE);
			}
			int yOffset  = (int)(relativeLayout_share.getHeight() * HomepageActivity.this.getResources().getDisplayMetrics().density);

			Animation animation = new TranslateAnimation(0F,0F, yOffset,0);
			animation.setDuration(2000);               //设置动画持续时间              
			animation.setRepeatCount(0);    
			animation.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationEnd(Animation arg0) {
					relativeLayout_share.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
				}

				@Override
				public void onAnimationStart(Animation arg0) {
				}
				
			});
			relativeLayout_share.setVisibility(View.VISIBLE);
			relativeLayout_share.startAnimation(animation);
			
		}
	
	/**
	 * 分享到朋友圈
	 */
	public void onShareToFriendsCircleButtonClick(View view){
		relativeLayout_share.setVisibility(View.GONE);
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
		
		int yOffset  = (int)(relativeLayout_share.getHeight() * HomepageActivity.this.getResources().getDisplayMetrics().density);//偏移

		Animation animation = new TranslateAnimation(0F,0F, 0,yOffset);
		animation.setDuration(2000);               //设置动画持续时间              
		animation.setRepeatCount(0);    
		animation.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation arg0) {
				relativeLayout_share.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
			}
			
		});
		relativeLayout_share.startAnimation(animation);
		
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
			int index_createat = cursor.getColumnIndexOrThrow(DataBase.OFFLINE_CREATEAT);
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
				tempJoke.setCreatedAt(cursor.getString(index_createat));
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
}



