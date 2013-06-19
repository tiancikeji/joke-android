package com.jokes.handlers;

import java.io.InputStream;

import org.json.JSONException;
/**
 * 
 * @author zhangkun
 * @date 2011-12-1
 */
public interface Ihandler {
	public Object parseResponse(InputStream inputStream) throws JSONException;

//	void onBookLoad(BookInfo bookInfo);
}
