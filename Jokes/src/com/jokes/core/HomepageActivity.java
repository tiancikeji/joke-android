package com.jokes.core;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class HomepageActivity extends Activity implements OnClickListener{

	Button button_setting;//���ð�ť
	Button button_record;//¼����ť
	Button button_favorite_big;//ͼƬ�м��ղذ�ť
	Button button_favorite_small;//�ղذ�ť
	FrameLayout framelayout_play;//���Ű�ť
	Button button_share;//����ť
	TextView textview_date;//����
	ImageView imageview_pic;//Ц��ͼƬ
	TextView textview_duration;//ʱ��
	TextView textview_playCount;//���Ŵ���
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.homepage_activity);
		
		init();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	private void init(){
		button_setting = (Button)findViewById(R.id.homepage_button_setting);
		button_record = (Button)findViewById(R.id.homepage_button_record);
		button_favorite_big = (Button)findViewById(R.id.homepage_button_favorite_big);
		button_favorite_big.setVisibility(View.GONE);
		button_favorite_small = (Button)findViewById(R.id.homepage_button_favorite_small);
		framelayout_play = (FrameLayout)findViewById(R.id.homepage_framelayout_play);
		button_share = (Button)findViewById(R.id.homepage_button_share);
		textview_date = (TextView)findViewById(R.id.homepage_textview_date);
		imageview_pic = (ImageView)findViewById(R.id.homepage_imageview_pic);
		textview_duration = (TextView)findViewById(R.id.homepage_textview_duration);
		textview_playCount = (TextView)findViewById(R.id.homepage_textview_playcount);
		button_setting.setOnClickListener(this);
		button_record.setOnClickListener(this);
		button_favorite_big.setOnClickListener(this);
		button_favorite_small.setOnClickListener(this);
		framelayout_play.setOnClickListener(this);
		button_share.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.homepage_button_setting:
			Intent intent1 = new Intent(HomepageActivity.this,SettingActivity.class);
			startActivity(intent1);
			break;
		case R.id.homepage_button_record:
			Intent intent2 = new Intent(HomepageActivity.this,RecordActivity.class);
			startActivity(intent2);
			break;
		case R.id.homepage_button_favorite_big:
			break;
		case R.id.homepage_button_favorite_small:
			break;
		case R.id.homepage_button_share:
			break;
		case R.id.homepage_framelayout_play:
			button_favorite_big.setVisibility(View.VISIBLE);
			break;
			
		}
		
	}

}
