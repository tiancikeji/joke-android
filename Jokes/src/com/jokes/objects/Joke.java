package com.jokes.objects;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Joke {
	
	private static final int APPROVED = 1;
	
	private String pictureUrl;
	private String audioUrl;
	private boolean approved;
	private String createdAt;
	private String updatedAt;
	private int userId;
	private int id;
	private String name;
	
	public Joke(JSONObject json){
		try{
			this.pictureUrl = json.getJSONObject("picture_url").getString("url");
			this.approved 	= json.getInt("approved") == APPROVED;
			this.audioUrl 	= json.getJSONObject("audio_url").getString("url");
			this.createdAt 	= json.getString("created_at");
			this.updatedAt 	= json.getString("updated_at");
			this.userId 	= json.getInt("user_id");
			this.id			= json.getInt("id");
			this.name		= json.getString("name");
		} catch(JSONException e){
			Log.e("JOKE", "JSON parsing exception in Joke Constructor " + e);
		}
		
	}
	
	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public String getAudioUrl() {
		return audioUrl;
	}
	public void setAudioUrl(String audioUrl) {
		this.audioUrl = audioUrl;
	}
	public boolean isApproved() {
		return approved;
	}
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
		Joke other = (Joke) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Joke [pictureUrl=" + pictureUrl + ", audioUrl=" + audioUrl
				+ ", approved=" + approved + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", userId=" + userId + ", id="
				+ id + ", name=" + name + "]";
	}
	
	

}