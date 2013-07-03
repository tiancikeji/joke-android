package com.jokes.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.jokes.objects.Joke;
import com.jokes.utils.ApiRequests;
import com.jokes.utils.AudioRecorder;
import com.jokes.utils.AudioUtils;
import com.jokes.utils.Constant;
import com.jokes.utils.HandlerCodes;
import com.jokes.utils.UmengAnaly;
import com.umeng.analytics.MobclickAgent;

public class RecordActivity extends Activity implements OnClickListener, OnInfoListener{
	private final static String DEBUG_TAG = "RecordActivity";
	private static final int CHANGEVOLUME = 100001;

	final int RESULT_LOAD_IMAGE = 0;//表示打开系统图库
	final int TAKE_PICTURE = 1;//为了表示返回方法中辨识你的程序打开的相机
	final int CUT_PHOTO_REQUEST_CODE = 2;//裁剪图片
	final String BUGTAG = "RecordActivity";
	CharSequence[] items = {"相册","相机"};
	String path = getSDPath() +"/jokes/";

	Button button_back;//返回按钮
	Button button_send;//发布按钮
	LinearLayout linearlayout_record;
	Button button_record;//录音按钮
	LinearLayout linearlayout_addpic;
	ImageView imageview_pic;//添加图片按钮
	Button button_play;//播放按钮
	LinearLayout linearlayout_bar;
	ImageView imageview_bar;//加载中动画

	ImageView imageview_point_1;
	ImageView imageview_point_2;
	ImageView imageview_point_3;
	ImageView imageview_point_4;
	ImageView imageview_point_5;
	ImageView imageview_point_6;
	ImageView imageview_point_7;
	ImageView imageview_point_8;

	Bitmap bipmpTemp ;//用户选择图片
	File imageFile;

	private AudioRecorder audioRecorder;
	private File mp3RecordedFile;
	private MediaPlayer mediaPlayer;
	//用来控制录音动画效果
	int count = 0;
	boolean isStartAnim = false;

	boolean isPlay = false;//判断是否播放音频
	
	Handler mainHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case HandlerCodes.CLOSE:
				RecordActivity.this.finish();
				break;
			case CHANGEVOLUME:
				changePointView(count);
				break;
			case HandlerCodes.CREATE_JOKE_SUCCESS:
				Toast.makeText(RecordActivity.this, "已经上传，请等待审核", Toast.LENGTH_LONG).show();
//				button_send.setEnabled(false);
				button_send.setTag(false);
				CountDownTimer timer = new CountDownTimer(3000, 3000) {
					@Override
					public void onTick(long arg0) {
					}
					
					@Override
					public void onFinish() {
						mainHandler.sendEmptyMessage(HandlerCodes.CLOSE);
					}
				}; 
				timer.start();
				break;
			case HandlerCodes.CREATE_JOKE_FAILURE:
				Toast.makeText(RecordActivity.this, "上传失败了", Toast.LENGTH_LONG).show();
//				button_send.setEnabled(false);
				button_send.setTag(false);
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.record_activity);
		init();
		//友盟统计：记录进入录音页面
		UmengAnaly.AnalyOnClickRecord(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.record_button_back:
			finish();
			break;
		case R.id.record_button_send:
			if((Boolean)button_send.getTag()){
				Joke joke = new Joke();
				joke.setName("笑话");
				joke.setDescription("笑话");
				ApiRequests.addJoke(mainHandler, joke, imageFile, mp3RecordedFile, Constant.uid);
				Toast.makeText(this, "正在发布...", Toast.LENGTH_SHORT).show();
//				button_send.setEnabled(false);
				button_send.setTag(false);
				//友盟统计：发布音频
				UmengAnaly.AnalyOnClickRecordSend(this);
			}
			
			break;
		case R.id.record_button_record:
			//点击开始录音，再次点击停止了录音
			if((Boolean)button_record.getTag()){
				isStartAnim = false;
				button_record.setTag(false);
				button_record.setBackgroundResource(R.drawable.btn_record_activity_record);
				//判断录音时间，跳转到添加图片页面
				linearlayout_record.setVisibility(View.GONE);
				linearlayout_addpic.setVisibility(View.VISIBLE);
				button_send.setVisibility(View.VISIBLE);
				AnimationDrawable animationDrawable = (AnimationDrawable) imageview_bar.getDrawable();
				animationDrawable.stop();
				
				mp3RecordedFile = audioRecorder.stopRecordingAudio(this);
				displayLengthOfAudioFile();
			
				//友盟统计：录制音频
				UmengAnaly.AnalyOnClickRecordRecord(this);
			}else{
				audioRecorder = new AudioRecorder();
				audioRecorder.startRecordingAudio(this);
				isStartAnim = true;
				startPlayAnim();
				button_record.setTag(true);
				linearlayout_bar.setVisibility(View.VISIBLE);
				
				button_record.setBackgroundResource(R.drawable.btn_record_activity_record_1);
			}
			break;
		case R.id.record_imageview_pic:
			new AlertDialog.Builder(RecordActivity.this)
			.setTitle("选择图片来源")
			.setItems(items, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					//打开相册
					if(which == RESULT_LOAD_IMAGE){
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);   
						intent.setType("image/*");
						startActivityForResult(Intent.createChooser(intent, "选择图片"),RESULT_LOAD_IMAGE);
					}else{
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(intent,TAKE_PICTURE);
					}

				}
			}).create().show();
			break;
		case R.id.record_button_play:
			if(mediaPlayer == null){
				mediaPlayer = new MediaPlayer();
			}
			if(!isPlay && mp3RecordedFile != null){
				isPlay = true;
				AudioUtils.startPlaying(mediaPlayer, mp3RecordedFile.getAbsolutePath());
			}else{
				isPlay = false;
				AudioUtils.stopPlaying(mediaPlayer);
				mediaPlayer.release();
				mediaPlayer = null;
			}
			break;
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		//处理返回图片
		if(data != null){
			Uri uri = data.getData();

			if(requestCode == TAKE_PICTURE){
				if(data.getExtras().get("data") != null){
					bipmpTemp = (Bitmap) data.getExtras().get("data");
					try {
						File tempImageFile = saveFile(bipmpTemp,"lijizhe"+".jpg");

						Intent intent1 = new Intent("com.android.camera.action.CROP"); 
						intent1.setDataAndType(Uri.fromFile(tempImageFile), "image/*");
						intent1.putExtra("crop", "true");
						intent1.putExtra("aspectX", 1);
						intent1.putExtra("aspectY", 1);
						intent1.putExtra("outputX", 132);
						intent1.putExtra("outputY", 132);
						intent1.putExtra("return-data", true);
						startActivityForResult(intent1,CUT_PHOTO_REQUEST_CODE);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}else if(requestCode == RESULT_LOAD_IMAGE){
				if (uri != null) {
					final Intent intent1 = new Intent("com.android.camera.action.CROP"); 
					intent1.setDataAndType(uri, "image/*");
					intent1.putExtra("crop", "true");
					intent1.putExtra("aspectX", 1);
					intent1.putExtra("aspectY", 1);
					intent1.putExtra("outputX", 290);
					intent1.putExtra("outputY", 290);
					intent1.putExtra("return-data", true);
					startActivityForResult(intent1,CUT_PHOTO_REQUEST_CODE);

				}
			}

			if(requestCode == CUT_PHOTO_REQUEST_CODE){

				bipmpTemp = (Bitmap) data.getExtras().get("data");
				//保存文件
				if(bipmpTemp != null){
					FileOutputStream output;
					try {
						imageFile = new File(getFilesDir().getAbsolutePath() + "/image.jpg");
						output = new FileOutputStream(imageFile);
						bipmpTemp.compress(CompressFormat.JPEG, 50, output);
						output.flush();
						output.close();
						//将图片显示到控件
						imageview_pic.setBackgroundColor(00000000);
						imageview_pic.setImageBitmap(bipmpTemp);
						
						//友盟统计：添加图片
						UmengAnaly.AnalyOnClickRecordAddPic(this);
						
					} catch(IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}

	private void init(){
		button_back = (Button)findViewById(R.id.record_button_back);
		button_send = (Button)findViewById(R.id.record_button_send);
//		button_send.setBackgroundResource(R.drawable.btn_current);
		button_send.setTag(true);
		button_send.setVisibility(View.GONE);
		linearlayout_record = (LinearLayout)findViewById(R.id.record_linearlayout_record);
		button_record = (Button)findViewById(R.id.record_button_record);
		button_record.setTag(false);//设置录音状态
		linearlayout_addpic = (LinearLayout)findViewById(R.id.record_linearlayout_addpic);
		linearlayout_addpic.setVisibility(View.GONE);
		imageview_pic = (ImageView)findViewById(R.id.record_imageview_pic);
		button_play = (Button)findViewById(R.id.record_button_play);
		linearlayout_bar = (LinearLayout)findViewById(R.id.record_linearlayout_bar);
		linearlayout_bar.setVisibility(View.GONE);
		imageview_bar = (ImageView)findViewById(R.id.record_imageview_bar);

		imageview_point_1 = (ImageView)findViewById(R.id.record_imageview_point_1);
		imageview_point_2 = (ImageView)findViewById(R.id.record_imageview_point_2);
		imageview_point_3 = (ImageView)findViewById(R.id.record_imageview_point_3);
		imageview_point_4 = (ImageView)findViewById(R.id.record_imageview_point_4);
		imageview_point_5 = (ImageView)findViewById(R.id.record_imageview_point_5);
		imageview_point_6 = (ImageView)findViewById(R.id.record_imageview_point_6);
		imageview_point_7 = (ImageView)findViewById(R.id.record_imageview_point_7);
		imageview_point_8 = (ImageView)findViewById(R.id.record_imageview_point_8);

		button_back.setOnClickListener(this);
		button_send.setOnClickListener(this);
		button_record.setOnClickListener(this);
		button_play.setOnClickListener(this);
		imageview_pic.setOnClickListener(this);
	}
	
	private void displayLengthOfAudioFile(){
		button_play.setText(AudioUtils.getAudioFileLength(mp3RecordedFile) + "\"");
	}

	/**
	 * 保存图片到sd卡
	 * @param bm
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public File saveFile(Bitmap bm, String fileName) throws IOException {    
		File dirFile = new File(path);  
		if(!dirFile.exists()){  
			dirFile.mkdir();  
		}  
		File myCaptureFile = new File(path + fileName);  
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));  
		bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);  
		bos.flush();  
		bos.close();  
		return myCaptureFile;
	} 

	/**
	 * 获取路径
	 * @return
	 */
	public static String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		return sdDir.toString();
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		Log.d("JOKE", "oninfo mediarecorder: " + what + ", " + extra);

	}

	/**
	 * 录音动画效果
	 */
	private void startPlayAnim(){

		new Thread(new Runnable(){

			@Override
			public void run() {
				while(isStartAnim){
					mainHandler.sendEmptyMessage(CHANGEVOLUME);
					if(count == 8){
						count = 0;
					}else{
						count++;
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}).start();
	}
	
	/**
	 * 根据count改变point状态
	 * @param count
	 */
	private void changePointView(int count){
		switch(count){
		case 0:{
			imageview_point_1.setBackgroundResource(R.drawable.point1);
			imageview_point_2.setBackgroundResource(R.drawable.point1);
			imageview_point_3.setBackgroundResource(R.drawable.point1);
			imageview_point_4.setBackgroundResource(R.drawable.point1);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 1:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point1);
			imageview_point_3.setBackgroundResource(R.drawable.point1);
			imageview_point_4.setBackgroundResource(R.drawable.point1);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 2:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point1);
			imageview_point_4.setBackgroundResource(R.drawable.point1);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 3:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point1);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 4:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point1);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 5:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point2);
			imageview_point_6.setBackgroundResource(R.drawable.point1);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 6:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point2);
			imageview_point_6.setBackgroundResource(R.drawable.point2);
			imageview_point_7.setBackgroundResource(R.drawable.point1);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 7:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point2);
			imageview_point_6.setBackgroundResource(R.drawable.point2);
			imageview_point_7.setBackgroundResource(R.drawable.point2);
			imageview_point_8.setBackgroundResource(R.drawable.point1);
		}
			break;
		case 8:{
			imageview_point_1.setBackgroundResource(R.drawable.point2);
			imageview_point_2.setBackgroundResource(R.drawable.point2);
			imageview_point_3.setBackgroundResource(R.drawable.point2);
			imageview_point_4.setBackgroundResource(R.drawable.point2);
			imageview_point_5.setBackgroundResource(R.drawable.point2);
			imageview_point_6.setBackgroundResource(R.drawable.point2);
			imageview_point_7.setBackgroundResource(R.drawable.point2);
			imageview_point_8.setBackgroundResource(R.drawable.point2);
		}
			break;
		}
	}

}
