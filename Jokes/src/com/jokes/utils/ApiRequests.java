package com.jokes.utils;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.jokes.handlers.JokeHandler;
import com.jokes.objects.GeneralResponse;
import com.jokes.objects.Joke;
import com.jokes.objects.Update;

@SuppressWarnings("unchecked")
public class ApiRequests {
	private static final String DEBUG_TAG = "JOKE";
	
	private static final String BASE_URL = "http://42.96.164.29:8888";
	private static final String API_URL = "/api";
	private static final String JOKE_URL = BASE_URL + API_URL + "/myjokes";
	private static final String LIKE_URL = BASE_URL + API_URL + "/likes";
	private static final String FEEDBACK_URL = BASE_URL + API_URL + "/feedbacks";
	private static final String PLAY_URL = BASE_URL + API_URL + "/myjokes/play";
	//private static final String IMG_UPLOAD_URL 		= JOKE_URL + "/photo";
	//private static final String AUDIO_UPLOAD_URL 	= JOKE_URL + "/audio";
	private static final String UPDATE_URL = BASE_URL+API_URL+"/version/checkVersion";
	private static final String DOWNLOAD_APK_URL = BASE_URL + API_URL;
	
	/**
	 * 通过page获取最新10条笑话
	 * @param responseHandler
	 * @param jokes
	 * @param uid
	 * @param page
	 * @param clearList
	 */
	public static void getJokes(final Handler responseHandler, final List<Joke> jokes, 
			final String uid, final int page, final boolean clearList){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpRequest response = HttpRequest.get(JOKE_URL, true, "page", page, "uid", uid);
				JokeHandler handler = new JokeHandler();
				
				String responseStr = "";
				try {
					responseStr = response.body();
					if(clearList){
						jokes.clear();
					}
					List<Joke> tempJokes = (List<Joke>)handler.parseResponse(responseStr);
					jokes.addAll(tempJokes);
					responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
					if(tempJokes.size() <= 0){
						responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_NULL);
					}
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
	
	/**
	 * 获取更多时，通过date来获取，direction：0向前  1向后
	 * 此方法逻辑判断有些复杂，还没有想到好的解决方法，待优化
	 * @param responseHandler
	 * @param jokes
	 * @param uid
	 * @param date
	 * @param direction
	 * @param clearList，（只有在刷新是要求清除记录，在加载更多时不会清除列表）
	 */
	public static void getJokes(final Handler responseHandler, final List<Joke> jokes, 
			final String uid, final String date,final int direction, final boolean clearList){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest response = HttpRequest.get(JOKE_URL, true, "date", date, "dir", direction, "uid", uid);
				JokeHandler handler = new JokeHandler();
				
				String responseStr = "";
				try {
					responseStr = response.body();
					List<Joke> tempJokes = (List<Joke>)handler.parseResponse(responseStr);
					
					//检查数据是否已经存在 ，如果存在则不保存
//					if(jokes.size() != 0){
					if(!clearList){
						if(clearList){
							jokes.clear();
						}
						boolean isequals = true;
						for(int i = 0; i < tempJokes.size(); i++){
							
							for(int j = 0  ; j < jokes.size() ; j++){
								if(date.equals(Tools.getDateFormat_(tempJokes.get(i).getApprovalTime()))){
									isequals = false;
									break;
								}
							}
							if(isequals){
								jokes.add(tempJokes.get(i));
								isequals = true;
							}
						}
						//用户下载更多成功
						responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
					}else{
						int count = 0;//记录获取的笑话中有多少是新笑话
						for(int i=0; i<tempJokes.size(); i++){
							//循环10次，是因为每天更新的笑话个数为10，jokes中前十条就是进入程序是获取最新10条笑话。这是规则。
							for(int j = 0  ; j < (jokes.size()>=10?10:jokes.size()); j++){
								if(date.equals(Tools.getDateFormat_(tempJokes.get(i).getApprovalTime()))){
									count++;
								}
							}
						}
						//如果需要清除记录，那就说明是刷新笑话列表。此事如果count大于0说明有新笑话则删除记录重新加载
						jokes.clear();
						jokes.addAll(tempJokes);
						if(count > 0){
							Message msg = new Message();
							msg.what = HandlerCodes.GET_JOKES_REFRESH_SUCCESS;
							Bundle bundle = new Bundle();
							bundle.putInt("update_count", count);
							responseHandler.sendMessage(msg);
						}else{
							responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_SUCCESS);
						}
						
						
					}
					
					if(tempJokes.size() <= 0){
						responseHandler.sendEmptyMessage(HandlerCodes.GET_JOKES_NULL);
					}
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
	
	public static void getLikeJokes(final Handler responseHandler, final List<Joke> jokes, final String uid){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest response = HttpRequest.get(LIKE_URL, true, "uid", uid);
				JokeHandler handler = new JokeHandler();
				final String responseStr = response.body();
				try {
					jokes.clear(); 
					jokes.addAll((List<Joke>)handler.parseResponse(responseStr));
					responseHandler.sendEmptyMessage(HandlerCodes.GET_LIKEJOKES_SUCCESS);
				} catch (HttpRequestException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.GET_LIKEJOKES_FAILURE);
					Log.e(DEBUG_TAG, "GetJokes " + e.toString() + " " + responseStr);
				} catch (JSONException e) {
					responseHandler.sendEmptyMessage(HandlerCodes.GET_LIKEJOKES_FAILURE);
					Log.e(DEBUG_TAG, "GetJokes " + e.toString() + " " + responseStr);
				}
			}
		}).start();
	}

	public static void addPlay(final Handler responseHandler, final Joke joke, final String uid){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				
				HttpRequest request = HttpRequest.post(PLAY_URL);
				request.part("uid", uid);
				request.part("id", joke.getId());
				
				final String responseStr = request.body();
				try {
					if(new JSONObject(responseStr).getBoolean("success")){
						responseHandler.sendEmptyMessage(HandlerCodes.ADD_PLAY_SUCCESS);
					} else {
					
					}
				} catch (JSONException e) {
					Log.e(DEBUG_TAG, "Error parsing response = " + e + " | " + responseStr);
					responseHandler.sendEmptyMessage(HandlerCodes.ADD_PLAY_FAILURE);
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
				if(null != image){
					request.part("imageFileData", image);
				}
				request.part("audioFileData", audio);
				request.part("myjoke[uid]", uid);
				request.part("myjoke[description]", joke.getDescription());
				if(0 == joke.getLength()){
					request.part("myjoke[length]", AudioUtils.getAudioFileLength(audio));
				}
				
				final String responseStr = request.body();
				try {
					if(new JSONObject(responseStr).getBoolean("success")){
						responseHandler.sendEmptyMessage(HandlerCodes.CREATE_JOKE_SUCCESS);
					} else {
					
					}
				} catch (JSONException e) {
					Log.e(DEBUG_TAG, "Error parsing response = " + e + " | " + responseStr);
					responseHandler.sendEmptyMessage(HandlerCodes.CREATE_JOKE_FAILURE);
				}
			}
		}).start();
	}
	
	public static void likeJoke(final Handler responseHandler, final int jokeId, final String userId){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest response = HttpRequest.post(LIKE_URL, true, "myjoke_id",
						jokeId, "uid", userId, "isLike", 1);
				final String responseStr = response.body();
				try {
					JSONObject resp = new JSONObject(responseStr);
					if(resp.getBoolean("success")){
						responseHandler.sendEmptyMessage(HandlerCodes.LIKE_SUCCESS);
					} else {
						responseHandler.sendEmptyMessage(HandlerCodes.LIKE_FAILURE);	
					}
				} catch (JSONException e1) {
					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_FAILURE);
					Log.e(DEBUG_TAG, "Like " + e1.toString());
				}
//				try {
//					//like.setFromLike((Like)handler.parseResponse(responseStr));
//					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_SUCCESS);
//				} catch (HttpRequestException e) {
//					responseHandler.sendEmptyMessage(HandlerCodes.LIKE_FAILURE);
//					Log.e(DEBUG_TAG, "Like " + e.toString());
//				}
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
	
	public static void addFeedback(final Handler responseHandler, final File audio, final String uid){
		new Thread(new Runnable() {	
			@Override
			public void run() {
				HttpRequest request = HttpRequest.post(FEEDBACK_URL);
				request.part("feedbackFileData", audio);
				request.part("feedback[uid]", uid);
				request.part("feedback[length]", AudioUtils.getAudioFileLength(audio));
				
				final String responseStr = request.body();
				try {
					if(new JSONObject(responseStr).getBoolean("success")){
						responseHandler.sendEmptyMessage(HandlerCodes.CREATE_FEEDBACK_SUCCESS);
					} else {
					}
				} catch (JSONException e) {
					Log.e(DEBUG_TAG, "Error parsing response = " + e + " | " + responseStr);
					responseHandler.sendEmptyMessage(HandlerCodes.CREATE_FEEDBACK_FAILURE);
				}
			}
		}).start();
	}
	
	public static void checkAppUpdate(final Handler responsehandler){
		//"42.96.164.29:8888/api/version/checkVersion.json"
		new Thread(new Runnable(){

			@Override
			public void run() {
				HttpRequest response = HttpRequest.get(UPDATE_URL);
				final String responseStr = response.body();
				try {
					Update update = new Update(new JSONObject(responseStr));
					if(update.getCurrentVersion() != null){
						Message msg = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("current_version", update.getCurrentVersion());
						bundle.putString("url", update.getUrl());
						msg.setData(bundle);
						msg.what = HandlerCodes.CHECK_UPDATE_SUCCESS;
						responsehandler.sendMessage(msg);
					}else{
						responsehandler.sendEmptyMessage(HandlerCodes.CHECK_UPDATE_FAILURE);
					}
				} catch (JSONException e) {
					responsehandler.sendEmptyMessage(HandlerCodes.CHECK_UPDATE_FAILURE);
					Log.d(DEBUG_TAG, "check update error:" + e.toString() + " | " + response);
				}
			}
			
		}).start();

	}
	

	/*private static byte[] convertBitmapToByteArray(Bitmap image){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 10, stream);
		return Base64.encode(stream.toByteArray(),Base64.DEFAULT);
	}*/
	
	/**
	 * @param relativeUrl
	 * @return Absolute Url
	 */
	public static String buildAbsoluteUrl(final String relativeUrl){
		return "http://42.96.164.29:8888" + relativeUrl;
	}
}
