package com.jokes.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jokes.objects.Joke;
import com.jokes.objects.Like;
import com.jokes.utils.ApiRequests;
import com.jokes.utils.HandlerCodes;
import com.jokes.utils.Installation;

public class HomepageActivity extends Activity implements OnClickListener{

	Button button_setting;//设置按钮
	Button button_record;//录音按钮
	Button button_favorite_big;//图片中间收藏按钮
	Button button_favorite_small;//收藏按钮
	FrameLayout framelayout_play;//播放按钮
	Button button_share;//分享按钮
	TextView textview_date;//日期
	ImageView imageview_pic;//笑话图片
	TextView textview_duration;//时长
	TextView textview_playCount;//播放次数
	
	private static final String DEBUG_TAG = "JOKE";
	
	private List<Joke> jokeList;
	private Like like;
	private Joke joke;
	
	private Handler mainHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
		
			switch(msg.what){
			case HandlerCodes.GET_JOKES_SUCCESS:
				Log.d(DEBUG_TAG, "Jokes success message received, printing... size = "+jokeList.size());
				if(jokeList.size() > 0){
					joke = jokeList.get(0);
					ApiRequests.unlikeJoke(mainHandler, joke.getId(), joke.getUserId());
				}
				break;
			case HandlerCodes.GET_JOKES_FAILURE:
				break;
			case HandlerCodes.LIKE_SUCCESS:
				Log.d(DEBUG_TAG, "Like Succes " + like);
				break;
			case HandlerCodes.LIKE_FAILURE:
				Log.d(DEBUG_TAG, "Like Failure " + like);
				break;
			case HandlerCodes.UNLIKE_SUCCESS:
				ApiRequests.likeJoke(mainHandler, joke.getId(), joke.getUserId(), like);
				Log.d(DEBUG_TAG, "Unlike Succes " + like);
				break;
			case HandlerCodes.UNLIKE_FAILURE:
				ApiRequests.likeJoke(mainHandler, joke.getId(), joke.getUserId(), like);
				Log.d(DEBUG_TAG, "UnLike Failure " + like);
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
		}
		
		
		
		
		init();
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
	}
	
	private void init(){
		button_setting = (Button)findViewById(R.id.homepage_button_setting);
		button_record = (Button)findViewById(R.id.homepage_button_record);
		button_favorite_big = (Button)findViewById(R.id.homepage_button_favorite_big);
		button_favorite_big.setVisibility(View.GONE);
		button_favorite_small = (Button)findViewById(R.id.homepage_button_favorite_small);
		framelayout_play = (FrameLayout)findViewById(R.id.homepage_framelayout_play);
		button_share = (Button)findViewById(R.id.homepage_button_share);
		textview_date = (TextView)findViewById(R.id.homepage_textview_date);
		imageview_pic = (ImageView)findViewById(R.id.homepage_imageview_pic);
		textview_duration = (TextView)findViewById(R.id.homepage_textview_duration);
		textview_playCount = (TextView)findViewById(R.id.homepage_textview_playcount);
		button_setting.setOnClickListener(this);
		button_record.setOnClickListener(this);
		button_favorite_big.setOnClickListener(this);
		button_favorite_small.setOnClickListener(this);
		framelayout_play.setOnClickListener(this);
		button_share.setOnClickListener(this);
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
		case R.id.homepage_button_favorite_big:
			break;
		case R.id.homepage_button_favorite_small:
			break;
		case R.id.homepage_button_share:
			break;
		case R.id.homepage_framelayout_play:
			button_favorite_big.setVisibility(View.VISIBLE);
			break;
			
		}
		
	}

}







	