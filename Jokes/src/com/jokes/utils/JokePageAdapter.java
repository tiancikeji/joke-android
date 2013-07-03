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
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jokes.core.HomepageActivity;
import com.jokes.core.R;
import com.jokes.ext.PagerAdapter;
import com.jokes.ext.VerticalViewPager;
import com.jokes.objects.Joke;

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
//	private ImageView imageview_volume;
//	private TextView textview_playcount;
//	private TextView textview_numlikes;
	private String UID;
	
	private boolean isPlaying = false;
	private boolean isPaused  = false;
	
	private View currentView;
	private SeekBar seekBar;
	
	private Timer mTimer;
	private TimerTask mTimerTask;
	
	public JokePageAdapter(android.support.v4.app.FragmentManager fm, Context context, List<Joke> jokes, MediaPlayer mp,
			OnPreparedListener onPreparedListener, Handler responseHandler, String UID){
		this.context = context;
		this.jokes = jokes;
		this.mp = mp;
		this.fm = fm;
		this.onPreparedListener = onPreparedListener;
		this.responseHandler = responseHandler;
		this.UID = UID;
	}

	@Override
	public int getCount() {
		return jokes.size();
	}
	
	private void setViewFromJoke(View view, Joke joke, int position){
		
		ImageView imageview_pic = (ImageView)view.findViewById(R.id.homepage_imageview_pic);
		if(joke.getFullPictureUrl() != null && !joke.getFullPictureUrl().equals("null")){
			new ImageDownLoadTask(joke.getId(),
					ApiRequests.buildAbsoluteUrl(joke.getFullPictureUrl()), context).execute(imageview_pic);
		}
		
		((TextView)view.findViewById(R.id.homepage_textview_duration)).setText(joke.getLength() + "\"");
      
		Button button_favorite_small = (Button)view.findViewById(R.id.homepage_button_favorite_small);
		if(joke.getIsLike()){
			button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_1);
		} 
		else{
			button_favorite_small.setBackgroundResource(R.drawable.btn_favorite_2);
		}
		((TextView)view.findViewById(R.id.homepage_textview_playcount)).setText(joke.getNumPlays()+"");
		((TextView)view.findViewById(R.id.homepage_textview_numlikes)).setText(joke.getNumLikes()+"");	
		
		TextView jokeIndexView = (TextView)view.findViewById(R.id.jokeIndexHack); //).setText(position);
		jokeIndexView.setText(String.valueOf(position));
		((FrameLayout)view.findViewById(R.id.homepage_framelayout_play)).setOnClickListener(this);
		TextView dateTextView = (TextView)view.findViewById(R.id.homepage_textview_date);
		dateTextView.setText(joke.getCreatedAt().substring(0, 11));
		//new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(originalString)
		
		if(!joke.getIsLike()){
			likeButton = (Button)view.findViewById(R.id.homepage_button_favorite_big);
//			likeButton.setOnClickListener(this);
		}
		((Button)view.findViewById(R.id.homepage_button_favorite_small)).setOnClickListener(this);
		((Button)view.findViewById(R.id.homepage_button_favorite_small)).setTag(joke.getIsLike());
		
	}
	
	public void resetPlayer(){
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
		Log.e("JokepageAdapter", "instantiateItem"+position);
	    Joke joke = jokes.get(position);
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
/*
	@Override
	public Fragment getItem(int index) {
		Joke joke = jokes.get(index);
        Fragment fragment = new JokeFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        //args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
        //fragment.setArguments(args);
        return fragment;
	}*/
	
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
		case R.id.homepage_button_favorite_big:
			//Button likeButton = (Button)view.findViewById(R.id.homepage_button_favorite_big);
			//likeButton.setVisibility(View.GONE);
			
//			Joke joke = getJokeFromView(view);
//			Log.d(DEBUG_TAG, "home button = " + joke.getCreatedAt());
//			ApiRequests.likeJoke(responseHandler, joke.getId() , UID);
		break;
		case R.id.homepage_button_favorite_small:
			Button likeButton_small = (Button)view.findViewById(R.id.homepage_button_favorite_small);
			Joke temp_joke = getJokeFromView(view);
			boolean islike = (Boolean)((Button)view.findViewById(R.id.homepage_button_favorite_small)).getTag();
			if(!islike){
				Log.e("JokePageAdapter","false");
				ApiRequests.likeJoke(responseHandler, temp_joke.getId() , UID);
			}else{
				Log.e("JokePageAdapter","true");
				ApiRequests.unlikeJoke(responseHandler, temp_joke.getId(), temp_joke.getUserId());
			}
			break;
		}
		
	}
	
	private void playJoke(View view){
		if(isPlaying && !isPaused){
			pauseJoke();
			AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView)view.findViewById(R.id.homepage_imageview_volume)).getDrawable();
			animationDrawable.stop();
		} else {
			Joke joke = getJokeFromView(view);
			
			try {
				//((LinearLayout)view.findViewById(R.id.homepage_linearlayout_volume)).setVisibility(View.VISIBLE);
				/*
				if(countDownTime <=0){
					startPlayCounttimer(jokeCurrent.getLength());
				}else{
					startPlayCounttimer((int)countDownTime);
				}*/
				
				if(!isPlaying && !isPaused){
					//播放音频，去掉播放按钮的三角图片，替换为空白；隐藏播放次数；显示动画图片，开启动画；
					((FrameLayout)view.findViewById(R.id.homepage_framelayout_play)).setBackgroundResource(R.drawable.btn);
					((TextView)view.findViewById(R.id.homepage_textview_playcount)).setVisibility(View.GONE);
					((ImageView)view.findViewById(R.id.homepage_imageview_volume)).setVisibility(View.VISIBLE);
					AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView)view.findViewById(R.id.homepage_imageview_volume)).getDrawable();
					animationDrawable.start();
					
					isPlaying = true;
					AudioUtils.prepareStreamAudio(mp, ApiRequests.buildAbsoluteUrl(joke.getFullAudioUrl()), onPreparedListener);
					seekBar = (SeekBar)currentView.findViewById(R.id.homepage_seekbar_progress);
					mTimer = new Timer();
					mTimerTask = getTimerTask();
					mTimer.schedule(mTimerTask, 0, 1000);
//					if(!joke.getIsLike()){					
//						likeButton = (Button)currentView.findViewById(R.id.homepage_button_favorite_big);
//						Log.d(DEBUG_TAG, "like button " + likeButton);
//						likeButton.setVisibility(View.VISIBLE);
//						AlphaAnimation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
//						fadeInAnimation.setAnimationListener(this);
//						fadeInAnimation.setDuration(LIKE_BTN_ANI_LEN);
//						likeButton.setAnimation(fadeInAnimation);
//						likeButton.startAnimation(fadeInAnimation);
//					}
					//mTimer = new Timer();
					//mTimerTask = getTimerTask();
					//mTimer.schedule(mTimerTask, 0, 1000);
				}else if(isPaused){
					mp.start();
					isPaused = false;
					AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView)view.findViewById(R.id.homepage_imageview_volume)).getDrawable();
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
		isPaused = true;
	}
	
	private Joke getJokeFromView(View view){
		TextView indexTextView = (TextView)view.findViewById(R.id.jokeIndexHack);
		if(null == indexTextView){
			indexTextView = (TextView)view.getRootView().findViewById(R.id.jokeIndexHack);	
		}
		return jokes.get(Integer.parseInt(indexTextView.getText().toString()));
	}
	
	//Super Hack
	public void setCurrentView(View view){
		currentView = view;
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
			}  
		};  
	};

}
