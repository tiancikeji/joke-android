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
	private int num_plays;
	private int num_likes;
	private int isLike;
	private int length;
	private String fullAudioUrl;
	private String fullPictureUrl;
	private int picture_size_in_b;
	private int audio_size_in_b;

	public Joke(JSONObject json){
		try{
			//this.pictureUrl = json.getString("picture_url");
			this.approved 	= json.getInt("approved") == APPROVED;
			//this.audioUrl 	= json.getString("audio_url");
			this.createdAt 	= json.getString("created_at");
			this.updatedAt 	= json.getString("updated_at");
			this.uid 		= json.getString("uid");
			this.id			= json.getInt("id");
			this.name		= json.getString("name");
			this.description = json.getString("description");
			this.length = json.getInt("length");
			this.fullAudioUrl = json.getString("full_audio_url");
			this.fullPictureUrl = json.getString("full_picture_url");
			this.num_plays = json.getInt("num_plays");
			this.num_likes = json.getInt("num_likes");
			this.isLike = json.getInt("is_like");
			this.picture_size_in_b = json.getInt("picture_size_in_b");
			this.audio_size_in_b = json.getInt("audio_size_in_b");
		} catch(JSONException e){
			Log.e("JOKE", "JSON parsing exception in Joke Constructor " + e);
		}
	}
	
	public Joke() {
		this.description = "";
		this.name = "";
		this.length = 0;
	}

	public int getPictureSizeInB(){
		return picture_size_in_b;
	}
	
	public void setPictureSizeInB(int picture_size_in_b){
		this.picture_size_in_b = picture_size_in_b;
	}
	
	public int getAudioSizeInB(){
		return audio_size_in_b;
	}
	
	public void setAudioSizeInB(int audio_size_in_b){
		this.audio_size_in_b = audio_size_in_b;
	}
	
	public int getNumPlays(){
		return num_plays;
	}
	public void setNumPlays(int num_plays){
		this.num_plays = num_plays;
	}
	public int getNumLikes(){
		return num_likes;
	}
	public void setNumLikes(int num_likes){
		this.num_likes = num_likes;
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
	public String getFullPictureUrl() {
		return fullPictureUrl;
	}
	public void setFullPictureUrl(String fullPictureUrl) {
		this.fullPictureUrl = fullPictureUrl;
	}
	public String getAudioUrl() {
		return audioUrl;
	}
	public void setAudioUrl(String audioUrl) {
		this.audioUrl = audioUrl;
	}
	public String getFullAudioUrl() {
		return fullAudioUrl;
	}
	public void setFullAudioUrl(String fullAudioUrl) {
		this.fullAudioUrl = fullAudioUrl;
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
		if(isLike == 0)
			return false;
		else
			return true;
	}
	public void setIsLike(int isLike) {
		this.isLike = isLike;
	}

	public void setIsLike(boolean islike){
//		this.isLike = islike;
		if(islike){
			isLike = 1;
		}else{
			isLike = 0;
		}
		
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
				+ id + ", name=" + name + ", length=" + length +"]";
	}
	
	

}
