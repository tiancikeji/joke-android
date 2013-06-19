package com.jokes.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecordActivity extends Activity implements OnClickListener{
	final int RESULT_LOAD_IMAGE = 0;//��ʾ��ϵͳͼ��
	final int TAKE_PICTURE = 1;//Ϊ�˱�ʾ���ط����б�ʶ��ĳ���򿪵����
	final int CUT_PHOTO_REQUEST_CODE = 2;//�ü�ͼƬ
	final String BUGTAG = "RecordActivity";
	CharSequence[] items = {"���","���"};
	String path = getSDPath() +"/jokes/";
	
	Button button_back;//���ذ�ť
	Button button_send;//������ť
	LinearLayout linearlayout_record;
	Button button_record;//¼����ť
	LinearLayout linearlayout_addpic;
	ImageView imageview_pic;//���ͼƬ��ť
	Button button_play;//���Ű�ť
	LinearLayout linearlayout_bar;
	ImageView imageview_bar;//�����ж���
	
	Bitmap bipmpTemp ;//�û�ѡ��ͼƬ
	File imageFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.record_activity);
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

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.record_button_back:
			finish();
			break;
		case R.id.record_button_send:
			break;
		case R.id.record_button_record:
			//�����ʼ¼�����ٴε��ֹͣ��¼��
			if((Boolean)button_record.getTag()){
				button_record.setTag(false);
				button_record.setBackgroundResource(R.drawable.btn_record_activity_record);
				//�ж�¼��ʱ�䣬��ת�����ͼƬҳ��
				linearlayout_record.setVisibility(View.GONE);
				linearlayout_addpic.setVisibility(View.VISIBLE);
				button_send.setVisibility(View.VISIBLE);
				AnimationDrawable animationDrawable = (AnimationDrawable) imageview_bar.getDrawable();
				animationDrawable.stop();
			}else{
				button_record.setTag(true);
				linearlayout_bar.setVisibility(View.VISIBLE);
				AnimationDrawable animationDrawable = (AnimationDrawable) imageview_bar.getDrawable();
				animationDrawable.start();
				button_record.setBackgroundResource(R.drawable.btn_record_activity_record_1);
			}
			break;
		case R.id.record_imageview_pic:
			new AlertDialog.Builder(RecordActivity.this)
				.setTitle("ѡ��ͼƬ��Դ")
				.setItems(items, new DialogInterface.OnClickListener(){
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//�����
						if(which == RESULT_LOAD_IMAGE){
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);   
							intent.setType("image/*");
							startActivityForResult(Intent.createChooser(intent, "ѡ��ͼƬ"),RESULT_LOAD_IMAGE);
						}else{
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							startActivityForResult(intent,TAKE_PICTURE);
						}
						
					}
				}).create().show();
			break;
		case R.id.record_button_play:
			break;
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		//������ͼƬ
		if(data != null){
			Uri uri = data.getData();

			if(requestCode == TAKE_PICTURE){
				Log.e("���", "���");
				if(data.getExtras().get("data") != null){
					Log.e("--�ü���--", "=");
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
				Log.e("���", "���");
				if (uri != null) {
					Log.e("--�ü���--", "=");
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
				Log.e("--�ü���--", "=");
				
				bipmpTemp = (Bitmap) data.getExtras().get("data");
				Log.e("--bipmpTemp--", bipmpTemp.getHeight()+":"+bipmpTemp.getWidth());
				//�����ļ�
				if(bipmpTemp != null){
					FileOutputStream output;
					try {
						output = this.openFileOutput("pic", Context.MODE_PRIVATE);
						bipmpTemp.compress(CompressFormat.JPEG, 50, output);
						output.flush();
						output.close();
						//��ͼƬ��ʾ���ؼ�
						imageview_pic.setBackgroundColor(00000000);
						imageview_pic.setImageBitmap(bipmpTemp);
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
		button_send.setVisibility(View.GONE);
		linearlayout_record = (LinearLayout)findViewById(R.id.record_linearlayout_record);
		button_record = (Button)findViewById(R.id.record_button_record);
		button_record.setTag(false);//����¼��״̬
		linearlayout_addpic = (LinearLayout)findViewById(R.id.record_linearlayout_addpic);
		linearlayout_addpic.setVisibility(View.GONE);
		imageview_pic = (ImageView)findViewById(R.id.record_imageview_pic);
		button_play = (Button)findViewById(R.id.record_button_play);
		linearlayout_bar = (LinearLayout)findViewById(R.id.record_linearlayout_bar);
		linearlayout_bar.setVisibility(View.GONE);
		imageview_bar = (ImageView)findViewById(R.id.record_imageview_bar);
		
		button_back.setOnClickListener(this);
		button_send.setOnClickListener(this);
		button_record.setOnClickListener(this);
		button_play.setOnClickListener(this);
		imageview_pic.setOnClickListener(this);
	}
	
	/**
	 * ����ͼƬ��sd��
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
	 * ��ȡ·��
	 * @return
	 */
	public static String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// ��ȡ��Ŀ¼
		}
		return sdDir.toString();
	}

}
