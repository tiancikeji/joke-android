package com.jokes.share;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.jokes.core.R;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXMusicObject;
import com.tencent.mm.sdk.platformtools.Util;

public class WeChatShare {
	
//	private static final String APP_ID = "wxc15df4cc42ae252b";
	private static final String APP_ID = "wxb3b0db608a4925ee";
	private static final String APP_KEY= "658010a294485a9e81df8788bd025b14";
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	
	public static IWXAPI regToWx(Context context){
		IWXAPI api;
		api = WXAPIFactory.createWXAPI(context, APP_ID, true);
		api.registerApp(APP_ID);
		return api;
	}
	
//	public static void sendAppInfoToFriendsCircle(IWXAPI api, Resources res, Context context){
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
	 * 分享笑话到朋友圈
	 * @param api
	 * @param res
	 * @param context
	 * @param url
	 * @param joke_id
	 */
	public static void sendMusicToFriendsCircle(IWXAPI api, Resources res, Context context,String url, String joke_id){
		
		WXMusicObject music = new WXMusicObject();
		music.musicUrl = url;
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = music;
		msg.title = "一听到底";
		msg.description = "呐，做人呢开心最重要了，来，我请你听笑话好不好？";
		
		InputStream ism = null;
		Bitmap thumb = null;
		//获取图片
		try {
			ism = context.openFileInput(joke_id);
			thumb = BitmapFactory.decodeStream(ism);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		if(thumb == null){
			thumb = BitmapFactory.decodeResource(res, R.drawable.btn_favorite_big);
//		}
		
		if(ism != null){
			try {
				ism.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		msg.thumbData = getBitmapBytes(thumb,true);
		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = buildTransaction("music");
		req.transaction = buildTransaction("appdata");
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;
		Log.d("JOKE", "WeChat API Message Sent: " + api.sendReq(req));
	}
	
	/**
	 * 分享音乐到微信
	 * @param api
	 * @param res
	 * @param context
	 * @param url
	 * @param joke_id
	 * @param share_type：1为分享到好友，2为分享到朋友圈
	 */
	@SuppressLint("NewApi")
	public static void sendMusicToFriend(IWXAPI api, Resources res, Context context,String url, String joke_id){
		WXMusicObject music = new WXMusicObject();
		music.musicUrl = url;
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = music;
		msg.title = "一听到底";
		msg.description = "呐，做人呢开心最重要了，来，我请你听笑话好不好？";
		
		InputStream ism = null;
		Bitmap thumb = null;
		//获取图片
		try {
			ism = context.openFileInput(joke_id);
			thumb = BitmapFactory.decodeStream(ism);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		if(thumb == null){
			thumb = BitmapFactory.decodeResource(res, R.drawable.btn_favorite_big);
//		}
		
		if(ism != null){
			try {
				ism.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		msg.thumbData = getBitmapBytes(thumb,true);
		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = buildTransaction("music");
		req.transaction = buildTransaction("appdata");
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneSession;
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
	
	/**
	 * 对图片进行处理
	 * @param bitmap
	 * @param paramBoolean
	 * @return
	 */
	private static byte[] getBitmapBytes(Bitmap bitmap, boolean paramBoolean) {
        Bitmap localBitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);
        int i;
        int j;
        if (bitmap.getHeight() > bitmap.getWidth()) {
            i = bitmap.getWidth();
            j = bitmap.getWidth();
        } else {
            i = bitmap.getHeight();
            j = bitmap.getHeight();
        }
        while (true) {
            localCanvas.drawBitmap(bitmap, new Rect(0, 0, i, j), new Rect(0, 0,
                    150, 150), null);
            if (paramBoolean)
                bitmap.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
//                F.out(e);
            }
            i = bitmap.getHeight();
            j = bitmap.getHeight();
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
