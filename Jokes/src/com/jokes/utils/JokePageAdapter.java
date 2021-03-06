package com.jokes.utils;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.jokes.core.R;
import com.jokes.ext.PagerAdapter;
import com.jokes.ext.VerticalViewPager;
import com.jokes.objects.Joke;
import com.tencent.mm.sdk.openapi.IWXAPI;

public class JokePageAdapter extends PagerAdapter implements OnClickListener, AnimationListener {
	private static final String DEBUG_TAG = "JOKE";
	public static final int LIKE_BTN_ANI_LEN = 500;
	
	private Context context;
	private List<Joke> jokes;
	private MediaPlayer mp;
	private FragmentManager fm;
	private OnPreparedListener onPreparedListener;
	private Handler responseHandler;
	private Button likeButton;
	private TextView textview_duration;
	private String UID;
	private boolean isOnline;//判断是否处在离线状态，如果处在离线状态则播放本地音频
	
	private boolean isPlaying = false;
	private boolean isPaused  = false;
	
	private View currentView;
	private SeekBar seekBar;
	
	private Timer mTimer;
	private TimerTask mTimerTask;
	private Joke joke;
	private AnimationDrawable animationDrawable;
	private WakeLock wakeLock;
	
	public JokePageAdapter(android.support.v4.app.FragmentManager fm, Context context, List<Joke> jokes, MediaPlayer mp,
			OnPreparedListener onPreparedListener, Handler responseHandler, String UID,IWXAPI weChatShareApi,
			boolean isOnline, WakeLock wakeLock){
		this.context = context;
		this.jokes = jokes;
		this.mp = mp;
		this.fm = fm;
		this.onPreparedListener = onPreparedListener;
		this.responseHandler = responseHandler;
		this.UID = UID;
		this.isOnline = isOnline;
		this.wakeLock = wakeLock;
	}

	@Override
	public int getCount() {
		return jokes.size();
	}
	
	public void addToJokeListAndRefresh(List<Joke> moreJokes){
		jokes.addAll(moreJokes);
		this.notifyDataSetChanged();
	}
	
	private void setViewFromJoke(View view, Joke joke, int position){
		
		ImageView imageview_pic = (ImageView)view.findViewById(R.id.homepage_imageview_pic);
		if(joke.getFullPictureUrl() != null && !joke.getFullPictureUrl().equals("null")){
			new ImageDownLoadTask(joke.getId(),
					ApiRequests.buildAbsoluteUrl(joke.getFullPictureUrl()), context).execute(imageview_pic);
			imageview_pic.setTag(joke.getId());
			((ImageView) view.findViewById(R.id.homepage_imageview_pic)).setBackgroundColor(0);
		}
		
		((TextView)view.findViewById(R.id.homepage_textview_duration)).setText(joke.getLength() + "\"");
      
		Button button_favorite_small = (Button)view.findViewById(R.id.homepage_button_favorite_small);
		if(joke.getIsLike()){
			button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_1);
		} 
		else{
			button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_2);
		}
		((TextView)view.findViewById(R.id.homepage_textview_playcount)).setText(joke.getNumPlays()+"播放");
		((TextView)view.findViewById(R.id.homepage_textview_numlikes)).setText(joke.getNumLikes()+"");	
		
		TextView jokeIndexView = (TextView)view.findViewById(R.id.jokeIndexHack); //).setText(position);
		jokeIndexView.setText(String.valueOf(position));
		((FrameLayout)view.findViewById(R.id.homepage_framelayout_play)).setOnClickListener(this);
		//new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(originalString)
		
		if(!joke.getIsLike()){
			likeButton = (Button)view.findViewById(R.id.homepage_button_favorite_big);
		}
//		((Button)view.findViewById(R.id.homepage_button_favorite_small)).setOnClickListener(this);
//		((Button)view.findViewById(R.id.homepage_button_favorite_small)).setTag(joke.getIsLike());
		((Button)view.findViewById(R.id.homepage_button_share)).setOnClickListener(this);
		((LinearLayout)view.findViewById(R.id.homepage_linearlayout_favorite_small)).setOnClickListener(this);
		((LinearLayout)view.findViewById(R.id.homepage_linearlayout_favorite_small)).setTag(joke.getIsLike());
		((Button)view.findViewById(R.id.homepage_button_favorite_small)).setTag(joke.getId());
		((Button)view.findViewById(R.id.homepage_button_share)).setTag(""+ApiRequests.buildAbsoluteUrl(joke.getFullAudioUrl()));
	}
	
	public void resetPlayer(){
		if(wakeLock.isHeld()){
			wakeLock.release();
		}
		isPlaying = false;
		isPaused = false;
		if(null != mTimer){
			mTimer.cancel();
		}
		if(null != mTimerTask){
			mTimerTask.cancel();
		}
		if(null != seekBar){
			seekBar.setProgress(0);
		}
	}
	
	
	@Override
	public Object instantiateItem(View collection, int position) {
	    joke = jokes.get(position);
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View layout = inflater.inflate(R.layout.joke_panel, null);
	    setViewFromJoke(layout, joke, position); 
	    ((VerticalViewPager) collection).addView(layout);
	    return layout;
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
	     ((VerticalViewPager) collection).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
	    return view == object;
	}
	
    @Override
    public CharSequence getPageTitle(int position) {
        return "JOKE " + (position + 1);
    }

	@Override
	public void onClick(final View view) {
		switch(view.getId()){
		case R.id.homepage_framelayout_play:
			playJoke(view);
		break;
		case R.id.homepage_linearlayout_favorite_small:
			LinearLayout linearlayout_like_small = ((LinearLayout)view.findViewById(R.id.homepage_linearlayout_favorite_small));
			
			linearlayout_like_small.setClickable(false);
			boolean islike = (Boolean)(linearlayout_like_small.getTag());
			if(!islike){
				ApiRequests.likeJoke(responseHandler, Integer.parseInt(""+((Button)view.findViewById(R.id.homepage_button_favorite_small)).getTag()) , UID);
			}else{
				ApiRequests.unlikeJoke(responseHandler, Integer.parseInt(""+((Button)view.findViewById(R.id.homepage_button_favorite_small)).getTag()), UID);
			}
			break;
		case R.id.homepage_button_share:
//			WeChatShare.sendAppInfo(weChatShareApi, context.getResources(), context);
			
//			Message msg = new Message();
//			msg.what = HandlerCodes.MESSAGE_SHARE;
//			Bundle bundle = new Bundle();
//			bundle.putString("joke_id", ""+((Button)view.findViewById(R.id.homepage_button_share)).getTag());
//			msg.setData(bundle);
//			responseHandler.sendMessage(msg);
			
			responseHandler.sendEmptyMessage(HandlerCodes.MESSAGE_SHARE);
			break;
		}
		
	}
	
	
	private void playJoke(View view){
		AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView)view.findViewById(R.id.homepage_imageview_volume)).getDrawable();
		if(isPlaying && !isPaused){
			pauseJoke();
			animationDrawable = getAnimationDrawable(view);
			animationDrawable.stop();
		} else {
			joke = getJokeFromView(view);
			
			try {
				if(!isPlaying && !isPaused){
					//播放音频，去掉播放按钮的三角图片，替换为空白；隐藏播放次数；显示动画图片，开启动画；
					((FrameLayout)view.findViewById(R.id.homepage_framelayout_play)).setBackgroundResource(R.drawable.btn);
					TextView playCountTextView = (TextView)view.findViewById(R.id.homepage_textview_playcount);
					playCountTextView.setText(joke.getNumPlays()+"播放");
					joke.setNumPlays(joke.getNumPlays() + 1);
					playCountTextView.setVisibility(View.GONE);
					((ImageView)view.findViewById(R.id.homepage_imageview_volume)).setVisibility(View.VISIBLE);
					animationDrawable.start();
					
					isPlaying = true;
					
					//FIXME this is not actually working
					((ProgressBar)currentView.findViewById(R.id.bufferingAudioSpinner)).setVisibility(View.VISIBLE);
					
					//如果是离线状态则播放本地 音频
					if(isOnline){
						AudioUtils.prepareStreamAudio(mp, ApiRequests.buildAbsoluteUrl(joke.getFullAudioUrl()), onPreparedListener);
					}else{
						//获取下载文件名
						String[] array = joke.getFullAudioUrl().split("/");
						String fileName = array[array.length-1];
						
						AudioUtils.startPlayOffline(mp, AudioUtils.getAudioFilePath(context, fileName));
					}
					
					seekBar = (SeekBar)currentView.findViewById(R.id.homepage_seekbar_progress);
					mTimer = new Timer();
					mTimerTask = getTimerTask();
					mTimer.schedule(mTimerTask, 0, 1000);
					wakeLock.acquire();
				}else if(isPaused){
					mp.start();
					wakeLock.acquire();
					isPaused = false;
					animationDrawable = getAnimationDrawable(view);
					animationDrawable.start();
				} else{
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
		}
		//framelayout_play.setBackgroundResource(R.drawable.btn);
		//textview_playCount.setVisibility(View.GONE);
	}
	
	public void startPlayAnimation(){
		((ProgressBar)currentView.findViewById(R.id.bufferingAudioSpinner)).setVisibility(View.GONE);
		animationDrawable = getAnimationDrawable(currentView);
		animationDrawable.start();
	}
	/**
	 * 暂停
	 */
	private void pauseJoke(){
		//暂停笑话的同时暂停动画效果
		//if(null != countDownTimer){
		//	countDownTimer.cancel();
		//}
		//暂停，释放锁
		//releaseWakeLock();
		//暂停笑话
		AudioUtils.pausePlaying(mp);
		if(wakeLock.isHeld()){
			wakeLock.release();
		}
		isPaused = true;
	}
	
	public Joke getCurrentJoke(){
		return joke;
	}
	
	
	public Joke getJokeFromView(View view){
		TextView indexTextView = (TextView)view.findViewById(R.id.jokeIndexHack);
		if(null == indexTextView){
			indexTextView = (TextView)view.getRootView().findViewById(R.id.jokeIndexHack);	
		}
		return jokes.get(Integer.parseInt(indexTextView.getText().toString()));
	}
	
	public View getCurrentView(){
		return currentView;
	}
	
	@Override
	public void onAnimationEnd(Animation arg0) {
		likeButton.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private TimerTask getTimerTask(){
		return new TimerTask() {  
			@Override  
			public void run() {  
				if(mp==null)  
					return;  
				if (mp.isPlaying() && seekBar.isPressed() == false) {  
					handleProgress.sendEmptyMessage(0);  
				}  
			}  
		};  
	}

	Handler handleProgress = new Handler() {  
		public void handleMessage(Message msg) {  

			int position = mp.getCurrentPosition();  
			int duration = mp.getDuration();  

			if (duration > 0) {  
				long pos = seekBar.getMax() * position / duration;  
				seekBar.setProgress((int) pos); 
				//动态修改播放剩余时长,判断播放进度和剩余时长是否大于0
				textview_duration = (TextView)currentView.findViewById(R.id.homepage_textview_duration);
				if(pos > 0 && Integer.parseInt(textview_duration.getText().toString().split("\"")[0]) != 0){
					
					textview_duration.setText((Integer.parseInt(textview_duration.getText().toString().split("\"")[0])-1)+"\"");
				}
			}  
		};  
	};
	
	public void setPrimaryItem(android.view.ViewGroup container, int position, Object object) {
		View tempNewView = (View)object;
		if(null != currentView && tempNewView != currentView){
			((FrameLayout)currentView.findViewById(R.id.homepage_framelayout_play)).setBackgroundResource(R.drawable.playback_play);
			((TextView)currentView.findViewById(R.id.homepage_textview_playcount)).setVisibility(View.VISIBLE);
			((ImageView)currentView.findViewById(R.id.homepage_imageview_volume)).setVisibility(View.GONE);
			if(null != animationDrawable && animationDrawable.isRunning()){
				animationDrawable.stop();
			}
		}
		currentView = (View)object;
	}

	/*
	 * Trick PageAdapter into thinking height is less than it actually is
	 */
	@Override
	public float getPageHeight(int position) {
		return(0.9f);
	};
	
	private AnimationDrawable getAnimationDrawable(View view){
		return (AnimationDrawable) ((ImageView)view.findViewById(R.id.homepage_imageview_volume)).getDrawable();
	}
	
	/**
	 * 释放倒计时锁
	 */
	/*private void releaseWakeLock(){
		if(null != wakelock){
			wakelock.release();
			wakelock = null;
		}
	}*/
	
	
	//@Override public float getPageWidth(int position) { return(0.5f); }
}
