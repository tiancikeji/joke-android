package com.jokes.utils;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.jokes.handlers.JokeHandler;
import com.jokes.handlers.LikeHandler;
import com.jokes.objects.GeneralResponse;
import com.jokes.objects.Joke;
import com.jokes.objects.Like;

@SuppressWarnings("unchecked")
public class ApiRequests {
	private static final String DEBUG_TAG = "JOKE";
	
	private static final String BASE_URL = "http://42.96.164.29:8888/api";
	private static final String JOKE_URL = BASE_URL + "/myjokes";
	private static final String LIKE_URL = BASE_URL + "/likes";
	//private static final String IMG_UPLOAD_URL 		= JOKE_URL + "/photo";
	//private static final String AUDIO_UPLOAD_URL 	= JOKE_URL + "/audio";
	
	public static void getJokes(final Handler responseHandler, final List<Joke> jokes, final String uid){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest response = HttpRequest.get(JOKE_URL, true, "page", 1, "uid", uid);
				JokeHandler handler = new JokeHandler();
				final String responseStr = response.body();
				try {
					jokes.clear(); 
					jokes.addAll((List<Joke>)handler.parseResponse(responseStr));
					responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
				} catch (HttpRequestException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_FAILURE);
					Log.e(DEBUG_TAG, "GetJokes " + e.toString() + " " + responseStr);
				} catch (JSONException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_FAILURE);
					Log.e(DEBUG_TAG, "GetJokes " + e.toString() + " " + responseStr);
				}
			}
		}).start();
	}
	
	
	public static void addJoke(final Handler responseHandler, final Joke joke, final File image,
			final File audio, final String uid){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				
				HttpRequest request = HttpRequest.post(JOKE_URL);
				request.part("myjoke[name]", joke.getName());
				request.part("imageFileData", image);
				request.part("audioFileData", audio);
				request.part("myjoke[uid]", uid);
		
				
				responseHandler.sendEmptyMessage(HandlerCodes.CREATE_JOKE_SUCCESS);
					/*responseHandler.sendEmptyMessage(HandlerCodes.CREATE_JOKE_FAILURE);
					Log.e(DEBUG_TAG, "Create Joke " + e.toString());*/
			}
		}).start();
	}
	
	public static void likeJoke(final Handler responseHandler, final int jokeId, final String userId, final Like like){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest response = HttpRequest.post(LIKE_URL, true, "myjoke_id",
						jokeId, "uid", userId, "isLike", 1);
				LikeHandler handler = new LikeHandler();
				final String responseStr = response.body();
				try {
					like.setFromLike((Like)handler.parseResponse(responseStr));
					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_SUCCESS);
				} catch (HttpRequestException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_FAILURE);
					Log.e(DEBUG_TAG, "Like " + e.toString());
				} catch (JSONException e) {
					try {
						JSONObject respJsonObject = new JSONObject(responseStr);
						if(respJsonObject.getBoolean("success")){
							//TODO
						}
					} catch (JSONException e1) {
						Log.e(DEBUG_TAG, "Like " + e.toString());
					}
					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_FAILURE);
				}
			}
		}).start();
		
	}
	
	public static void unlikeJoke(final Handler responseHandler, final int jokeId, final String userId){
		
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest response = HttpRequest.post(LIKE_URL, true, "uid", userId, "myjoke_id", jokeId, "isLike", 0);
				final String responseString = response.body();
				try {
					GeneralResponse generalResponse = new GeneralResponse(new JSONObject(responseString));
					if(generalResponse.isSuccess()){
						responseHandler.sendEmptyMessage(HandlerCodes.UNLIKE_SUCCESS);
					} else {
						Log.d(DEBUG_TAG, "Unlike "  + responseString);
						responseHandler.sendEmptyMessage(HandlerCodes.UNLIKE_FAILURE);
					}
				} catch (HttpRequestException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.UNLIKE_FAILURE);
					Log.e(DEBUG_TAG, "Unlike " + e.toString() + " " + responseString);
				} catch (JSONException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.UNLIKE_FAILURE);
					Log.e(DEBUG_TAG, "Unlike " + e.toString() + " " + responseString);
				}
						
			
			}
		}).start();
		
	}
	
	/*private static byte[] convertBitmapToByteArray(Bitmap image){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 10, stream);
		return Base64.encode(stream.toByteArray(),Base64.DEFAULT);
	}*/
	

}
