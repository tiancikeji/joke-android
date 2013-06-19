package com.jokes.handlers;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;

import android.util.Log;

/**
 * 抽象处理器
 * 
 * @author zhangkun
 * @date 2011-12-14
 */
public abstract class AbsHandler implements Ihandler {

	@Override
	public Object parseResponse(InputStream inputStream) throws JSONException {
		Object reponseResult = null;
		// String responseStr = streamToString(inputStream);

		String responseStr = inputStreamToString(inputStream,"UTF-8");
		try {
			responseStr = new String(responseStr.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Log.e("TIANTIAN_DACHE", e.toString() );
		}
		reponseResult = parseResponse(responseStr);
		return reponseResult;
	}

	abstract public Object parseResponse(String responseStr) throws JSONException;

	private String inputStreamToString(InputStream is, String encoding) {
		try {
			byte[] b = new byte[1024];
			String res = "";
			if (is == null) {
				return "";
			}

			int bytesRead = 0;
			while (true) {
				bytesRead = is.read(b, 0, 1024); // return final read bytes
													// counts
				if (bytesRead == -1) {// end of InputStream
					return res;
				}
				res += new String(b, 0, bytesRead, encoding); // convert to
																// string using
																// bytes
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("Exception: " + e);
			return "";
		}
	}

}
