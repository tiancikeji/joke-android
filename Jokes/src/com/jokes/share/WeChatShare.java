package com.jokes.share;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.jokes.core.R;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXMusicObject;
import com.tencent.mm.sdk.platformtools.Util;

public class WeChatShare {
	
	private static final String APP_ID = "wxc15df4cc42ae252b";
	private static final String APP_KEY= "658010a294485a9e81df8788bd025b14";
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	
	public static IWXAPI regToWx(Context context){
		IWXAPI api;
		api = WXAPIFactory.createWXAPI(context, APP_ID, true);
		api.registerApp(APP_ID);
		return api;
	}
	
//	public static void sendAppInfoToFriendsFriendsCircle(IWXAPI api, Resources res, Context context){
//		final WXAppExtendObject appdata = new WXAppExtendObject();
//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		Bitmap image = BitmapFactory.decodeResource(res, R.drawable.btn_favorite_big);
//		if(!image.compress(Bitmap.CompressFormat.PNG, 100, stream)){
//			Log.e("JOKE", "decode failure");
//			return;
//		}
//		
//		appdata.fileData = stream.toByteArray();
//		appdata.extInfo = "说点什么";
//
//		final WXMediaMessage msg = new WXMediaMessage();
//		msg.setThumbImage(image);
//		msg.title = "一听到底";
//		msg.description = "呐，做人呢开心最重要了，来，我请你听笑话好不好？";
//		msg.mediaObject = appdata;
//		
//		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = buildTransaction("appdata");
//		req.message = msg;
//		req.scene = SendMessageToWX.Req.WXSceneTimeline;
//		Log.d("JOKE", "WeChat API Message Sent: " + api.sendReq(req));
//	}
//	
//	public static void sendAppInfo(IWXAPI api, Resources res, Context context){
//		final WXAppExtendObject appdata = new WXAppExtendObject();
//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		Bitmap image = BitmapFactory.decodeResource(res, R.drawable.btn_favorite_big);
//		if(!image.compress(Bitmap.CompressFormat.PNG, 100, stream)){
//			Log.e("JOKE", "decode failure");
//			return;
//		}
//		
//		appdata.fileData = stream.toByteArray();
//		appdata.extInfo = "this is ext info";
//
//		final WXMediaMessage msg = new WXMediaMessage();
//		msg.setThumbImage(image);
//		msg.title = "一听到底";
//		msg.description = "呐，做人呢开心最重要了，来，我请你听笑话好不好？";
//		msg.mediaObject = appdata;
//		
//		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = buildTransaction("appdata");
//		req.message = msg;
//		req.scene =SendMessageToWX.Req.WXSceneSession;
//		Log.d("JOKE", "WeChat API Message Sent: " + api.sendReq(req));
//	}
	
	/**
	 * 分享音乐到微信
	 * @param api
	 * @param res
	 * @param context
	 * @param url
	 * @param joke_id
	 * @param share_type：1为分享到好友，2为分享到朋友圈
	 */
	public static void sendMusic(IWXAPI api, Resources res, Context context,String url, String joke_id,int share_type){
		WXMusicObject music = new WXMusicObject();
//		music.musicUrl="http://staff2.ustc.edu.cn/~wdw/softdown/index.asp/0042515_05.ANDY.mp3"; 
		music.musicUrl = url;
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = music;
		msg.title = "一听到底";
		msg.description = "呐，做人呢开心最重要了，来，我请你听笑话好不好？";
		
//		InputStream ism;
//		Bitmap thumb = null;
//		//获取图片
//		try {
//			ism = context.openFileInput(joke_id);
//			thumb = BitmapFactory.decodeStream(ism);
//			if(ism != null){
//				ism.close();
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(thumb == null){
//			thumb = BitmapFactory.decodeResource(res, R.drawable.btn_favorite_big);
//		}
		Bitmap thumb = BitmapFactory.decodeResource(res, R.drawable.btn_favorite_big);
		msg.thumbData = Util.bmpToByteArray(thumb, true);
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("music");
		req.message = msg;
		if(share_type == 1){
			req.scene = SendMessageToWX.Req.WXSceneSession;
		}else{
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		}
		Log.d("JOKE", "WeChat API Message Sent: " + api.sendReq(req));
	}
	
	static String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	/**
	 * 检查是否可以分享到平有权
	 */
	public static boolean checkIsShareToFriendsCircle(IWXAPI api){
		int wxSdkVersion = api.getWXAppSupportAPI();
		if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	WXTextObject textObject = new WXTextObject();
	textObject.text = "Test";
	
	WXMediaMessage msg = new WXMediaMessage(textObject);
	msg.description = "Test of WeChat API";
	msg.mediaObject = textObject;
	
	SendMessageToWX.Req req = new SendMessageToWX.Req();
	req.scene = SendMessageToWX.Req.WXSceneTimeline;
	req.transaction = String.valueOf(System.currentTimeMillis());
	req.message = msg;
	
	api.sendReq(req);*/

}
