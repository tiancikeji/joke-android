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
	private String uid;
	private int id;
	private String name;
	private String description;
	private boolean isLike = false;
	private int length;

//	{"id":104,"name":"iOSTest","picture_url":null,"audio_url":"audio-3f144c40c9132f9ad715b7203e80f0fa789a349e-1372306598.mp3",
//		"approved":1,"created_at":null,"updated_at":null,"uid":null,"description":"123","length":0,
//		"num_plays":0,"myjoke_id":null,"num_likes":0}
	public Joke(JSONObject json){
		try{
			this.pictureUrl = json.getString("picture_url");
			this.approved 	= json.getInt("approved") == APPROVED;
			this.audioUrl 	= json.getString("audio_url");
			this.createdAt 	= json.getString("created_at");
			this.updatedAt 	= json.getString("updated_at");
			this.uid 		= json.getString("uid");
			this.id			= json.getInt("id");
			this.name		= json.getString("name");
			this.description = json.getString("description");
			this.length = json.getInt("length");
		} catch(JSONException e){
			Log.e("JOKE", "JSON parsing exception in Joke Constructor " + e);
		}
		
	}
	
	public Joke() {
		this.description = "";
		this.name = "";
		this.length = 0;
	}

	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
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
	public String getUserId() {
		return uid;
	}
	public void setUserId(String userId) {
		this.uid = userId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean getIsLike(){
		return isLike;
	}
	public void setIsLike(boolean islike){
		this.isLike = islike;
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
				+ ", updatedAt=" + updatedAt + ", userId=" + uid + ", id="
				+ id + ", name=" + name + "]";
	}
	
	

}
