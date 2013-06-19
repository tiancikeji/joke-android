package com.jokes.utils;

import java.util.List;

import org.json.JSONException;

import android.os.Handler;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.jokes.handlers.JokeHandler;
import com.jokes.handlers.LikeHandler;
import com.jokes.objects.Joke;
import com.jokes.objects.Like;

@SuppressWarnings("unchecked")
public class ApiRequests {
	private static final String DEBUG_TAG = "JOKE";
	
	private static final String BASE_URL = "http://42.96.164.29:8888/api";
	private static final String LIST_URL = BASE_URL + "/myjokes";
	private static final String LIKE_URL = BASE_URL + "/likes";
	
	public static void getJokes(final Handler responseHandler, final List<Joke> jokes){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest response = HttpRequest.get(LIST_URL);
				JokeHandler handler = new JokeHandler();
				try {
					jokes.clear(); 
					jokes.addAll((List<Joke>)handler.parseResponse(response.body()));
					responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
				} catch (HttpRequestException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_FAILURE);
					Log.e(DEBUG_TAG, e.toString());
				} catch (JSONException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_FAILURE);
					Log.e(DEBUG_TAG, e.toString());
				}
			}
		}).start();
	}
	
	public static void addJoke(final Handler responseHandler){
		
	}
	
	public static void likeJoke(final Handler responseHandler, final int jokeId, final int userId, final Like like){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest response = HttpRequest.post(LIKE_URL, true, "like[myjoke_id]",
						jokeId, "like[user_id]", userId);
				LikeHandler handler = new LikeHandler();
				try {
					like.setFromLike((Like)handler.parseResponse(response.body()));
					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_SUCCESS);
				} catch (HttpRequestException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_FAILURE);
					Log.e(DEBUG_TAG, e.toString());
				} catch (JSONException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_FAILURE);
					Log.e(DEBUG_TAG, e.toString());
				}
						
			
			}
		}).start();
		
	}
	
	public static void unlikeJoke(final Handler responseHandler, final int jokeId, final int userId){
		
	}
	

}
