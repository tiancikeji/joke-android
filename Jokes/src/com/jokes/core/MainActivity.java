package com.jokes.core;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;

import com.jokes.objects.Joke;
import com.jokes.objects.Like;
import com.jokes.utils.ApiRequests;
import com.jokes.utils.HandlerCodes;

public class MainActivity extends Activity {
	
	private static final String DEBUG_TAG = "JOKE";
	
	private List<Joke> jokeList;
	private Like like;
	
	private Handler mainHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
		
			switch(msg.what){
			case HandlerCodes.GET_JOKES_SUCCESS:
				Log.d(DEBUG_TAG, "Jokes success message received, printing... size = "+jokeList.size());
				if(jokeList.size() > 0){
					Joke joke = jokeList.get(0);
					ApiRequests.likeJoke(mainHandler, joke.getId(), joke.getUserId(), like);
				}
				break;
			case HandlerCodes.GET_JOKES_FAILURE:
				break;
			case HandlerCodes.LIKE_SUCCESS:
				Log.d(DEBUG_TAG, "Like Succes " + like);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		jokeList = new ArrayList<Joke>();
		like = new Like();
		//ApiRequests.getJokes(mainHandler, jokeList);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
