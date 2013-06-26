package com.jokes.core;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class FeedbackActivity extends Activity implements OnClickListener{

	Button button_back;
	Button button_send;
	Button button_record;
	Button button_play;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.feedback_activity);
		init();
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.feedback_button_back:
			finish();
			break;
		case R.id.feedback_button_send:
			break;
		case R.id.feedback_button_record:
			
			//点击开始录音，再次点击停止了录音
			if((Boolean)button_record.getTag()){
				button_record.setTag(false);
				button_record.setBackgroundResource(R.drawable.btn_record_activity_record);
				button_send.setVisibility(View.VISIBLE);
			}else{
				button_record.setTag(true);
				button_record.setBackgroundResource(R.drawable.btn_record_activity_record_1);
			}
			break;
		case R.id.feedback_button_play:
			break;
		}
		
	}
	
	private void init(){
		button_back = (Button)findViewById(R.id.feedback_button_back);
		button_send = (Button)findViewById(R.id.feedback_button_send);
		button_send.setVisibility(View.GONE);
		button_record = (Button)findViewById(R.id.feedback_button_record);
		button_record.setTag(false);//设置按钮是否在录音状态
		button_play = (Button)findViewById(R.id.feedback_button_play);
		button_back.setOnClickListener(this);
		button_send.setOnClickListener(this);
		button_record.setOnClickListener(this);
		button_play.setOnClickListener(this);
	}

}