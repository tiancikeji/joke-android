package com.jokes.utils;

import android.content.Context;
import android.os.Bundle;

import com.tencent.mm.sdk.openapi.GetMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

public class WeChatShare {
	
	private static final String APP_ID = "wxb3b0db608a4925ee";
	private static final String APP_KEY= "037a31a483194fe7d86b10470269590";
	
	private static IWXAPI api;
	
	public static void regToWx(Context context){
		api = WXAPIFactory.createWXAPI(context, APP_ID, true);
		api.registerApp(APP_ID);
		
		WXTextObject textObject = new WXTextObject();
		textObject.text = "Test";
		
		WXMediaMessage msg = new WXMediaMessage(textObject);
		msg.description = "Test of WeChat API";
		
		GetMessageFromWX.Resp resp = new GetMessageFromWX.Resp();
		
		resp.transaction = new GetMessageFromWX.Req(new Bundle()).transaction;
		resp.message = msg;
		
		api.sendResp(resp);
	}

}
