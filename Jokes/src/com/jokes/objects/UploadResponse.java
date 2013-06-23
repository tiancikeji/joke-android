package com.jokes.objects;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UploadResponse {
	
	private boolean success;
	private String url;
	
	
	public UploadResponse(JSONObject json){
		try {
			this.url = json.getString("url");
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return String.format("UploadResponse [success=%s, url=%s]", success,
				url);
	}
}
