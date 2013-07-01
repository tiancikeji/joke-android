package com.jokes.share;

import java.io.ByteArrayOutputStream;

import android.R.bool;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.CheckBox;

import com.jokes.core.R;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Util;

public class WeChatShare {
	
	private static final String APP_ID = "wxb3b0db608a4925ee";
	//private static final String APP_ID = "wxd930ea5d5a258f4f";
	private static final String APP_KEY= "037a31a483194fe7d86b10470269590";
	
	public static IWXAPI regToWx(Context context){
		IWXAPI api;
		api = WXAPIFactory.createWXAPI(context, APP_ID, true);
		api.registerApp(APP_ID);
		return api;
	}
	
	public static void sendAppInfo(IWXAPI api, Resources res, Context context){
		final WXAppExtendObject appdata = new WXAppExtendObject();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Bitmap image = BitmapFactory.decodeResource(res, R.drawable.btn_favorite_big);
		if(!image.compress(Bitmap.CompressFormat.PNG, 100, stream)){
			Log.e("JOKE", "decode failure");
			return;
		}
		
		appdata.fileData = stream.toByteArray();
		appdata.extInfo = "this is ext info";

		final WXMediaMessage msg = new WXMediaMessage();
		msg.setThumbImage(image);
		msg.title = "this is the title";
		msg.description = "this is description sjgks Long Very Long Very Long Very Longgj skjgks kgsk lgskg jslgj";
		msg.mediaObject = appdata;
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("appdata");
		req.message = msg;
		CheckBox isTimelineCb = new CheckBox(context);
		isTimelineCb.setChecked(false);
		req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		Log.d("JOKE", "WeChat API Message Sent: " + api.sendReq(req));
	}
	
	private static String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
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
