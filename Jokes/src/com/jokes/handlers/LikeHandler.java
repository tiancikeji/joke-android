package com.jokes.handlers;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.jokes.objects.Like;

public class LikeHandler extends AbsHandler {

	@Override
	public Object parseResponse(String responseStr) throws JSONException {
		Log.d("JOKE", responseStr);
		JSONObject object = new JSONObject(responseStr);
		return new Like(object.getJSONObject("like"));
	}

}
