package com.jokes.handlers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jokes.objects.Joke;

public class LikesHandler extends AbsHandler {

	@Override
	public Object parseResponse(String responseStr) throws JSONException {
		List<Joke> jokes = new ArrayList<Joke>();
		JSONObject object = new JSONObject(responseStr);
		JSONArray array = object.getJSONArray("likes");

		// 得到最最近一次会话
		for (int ii = 0; ii < array.length(); ii++) {
			JSONObject oJsonObject = array.getJSONObject(ii);
			Joke joke = new Joke(oJsonObject);
			jokes.add(joke);
		}
		return jokes;
	}

}
