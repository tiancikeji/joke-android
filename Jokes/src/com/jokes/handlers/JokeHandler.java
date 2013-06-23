package com.jokes.handlers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jokes.objects.Joke;

public class JokeHandler extends AbsHandler {

	@Override
	public Object parseResponse(String responseStr) throws JSONException {
		List<Joke> jokes = new ArrayList<Joke>();
		
		JSONObject object = new JSONObject(responseStr);
		JSONArray array = object.getJSONArray("myjokes");

		// 得到最最近一次会话
		for (int ii = 0; ii < array.length(); ii++) {
			JSONObject oJsonObject = array.getJSONObject(ii);
			Joke joke = new Joke(oJsonObject);
			
			/*final int status = oJsonObject.getInt("status");
						if (status == Constant.STATUS_NEW || status == Constant.STATUS_CANCEL ||
					status == Constant.STATUS_ACCEPT || status == Constant.STATUS_FINISH) {
				list.add(info);
			}*/
			jokes.add(joke);
		}
		return jokes;
	}

}
