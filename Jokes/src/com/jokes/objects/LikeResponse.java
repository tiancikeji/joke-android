package com.jokes.objects;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LikeResponse {
	
	private boolean success;
	private String reason;
	private int jokeId, numLikes;
	
	
	public LikeResponse(JSONObject json){
		try {
			this.jokeId = json.getInt("myjoke_id");
			this.success = json.getBoolean("success");
		} catch (JSONException e) {
			Log.e("JOKE", "Failure to parse Upload Response");
		}
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		return String.format("UploadResponse [success=%s, reason=%s]", success,
				reason);
	}
}
