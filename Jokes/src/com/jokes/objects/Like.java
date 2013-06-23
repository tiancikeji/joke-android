package com.jokes.objects;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Like {
	private String createdAt;
	private int id;
	private int jokeId;
	private String updatedAt;
	private String uid;
	
	public Like(){
		
	}
	
	public Like(JSONObject json){
		setFromJsonHelper(json);
	}

	public void setFromJson(JSONObject json){
		setFromJsonHelper(json);
	}
	
	private void setFromJsonHelper(JSONObject json){
		try {
			createdAt 	= json.getString("created_at");
			id 			= json.getInt("id");
			jokeId 		= json.getInt("myjoke_id");
			updatedAt 	= json.getString("updated_at");
			uid 		= json.getString("uid");
			
		} catch (JSONException e) {
			Log.e("JOKE", "JSON parsing exception in Like Constructor " + e);
		}
	}
	
	public void setFromLike(Like like){
		createdAt 	= like.getCreatedAt();
		id 			= like.getId();
		jokeId		= like.getJokeId();
		updatedAt	= like.getUpdatedAt();
		uid		= like.getUserId();
	}
	
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getJokeId() {
		return jokeId;
	}
	public void setJokeId(int jokeId) {
		this.jokeId = jokeId;
	}
	public String getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	public String getUserId() {
		return uid;
	}
	public void setUserId(String userId) {
		this.uid = userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Like other = (Like) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String
				.format("Like [createdAt=%s, id=%s, jokeId=%s, updatedAt=%s, userId=%s]",
						createdAt, id, jokeId, updatedAt, uid);
	}

}
