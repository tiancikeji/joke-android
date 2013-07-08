package com.jokes.objects;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GeneralResponse {
	
	private boolean success;
	private String reason;
	
	
	public GeneralResponse(JSONObject json){
		try {
			this.success = json.getBoolean("success");
			if(!success)
			this.reason = json.getString("reason");
			
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
