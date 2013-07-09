package com.jokes.objects;

import org.json.JSONException;
import org.json.JSONObject;

public class Update {
//	{"current_version":"1.0","url":"/versions/Joke-v0.1.1.apk"}
	
	private String current_version;
	private String url;
	
	public Update(JSONObject json){
		try {
			this.current_version = json.getString("current_version");
			this.url = json.getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public String getCurrentVersion(){
		return current_version;
	}
	
	public String getUrl(){
		return url;
	}
}
