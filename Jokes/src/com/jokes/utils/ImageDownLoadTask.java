package com.jokes.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class ImageDownLoadTask extends AsyncTask<Object, Object, Object> {
	private ImageView imageView;
	private String iconurl;
	Bitmap bmp;
	Context context;
	int jokeId;
	
	public ImageDownLoadTask(int jokeId,String url, Context context) {
		this.iconurl = url;
//		this.iconurl = "http://image.baidu.com/i?ct=503316480&z=&tn=baiduimagedetail&ipn=d&word=%E9%87%91%E5%B8%8C%E6%BE%88&ie=utf-8&in=5261&cl=&lm=&st=&pn=31&rn=1&di=163468126150&ln=1998&fr=&&fmq=1372239520062_R&ic=&s=&se=&sme=0&tab=&width=&height=&face=&is=&istype=&ist=&jit=&objurl=http%3A%2F%2Fpic.baike.soso.com%2Fp%2F20120910%2F20120910130935-730414026.jpg#pn31&-1&di163468126150&objURLhttp%3A%2F%2Fpic.baike.soso.com%2Fp%2F20120910%2F20120910130935-730414026.jpg&fromURLippr_z2C%24qAzdH3FAzdH3Fkwthj_z%26e3Bf5f5_z%26e3Bv54AzdH3Filc0l9_z%26e3Bip4&W500&H379&T7712&S26&TPjpg";
		this.context = context;
		this.jokeId = jokeId;
	}
	
	@Override
	protected void onPostExecute(Object result) {
		if (result != null && imageView != null) {
//			if(item != null){
//				item.bitmap = (Bitmap) result;
//			}
			this.imageView.setImageBitmap((Bitmap) result);
			this.imageView = null;
		}
	}

	@Override
	protected Object doInBackground(Object... views) {
		bmp = downImage(iconurl,context);
		this.imageView = (ImageView) views[0];
		return bmp;
	}
	
	public Bitmap downImage(String ImageUrl, Context context){
		Log.e("图片地址", ImageUrl);
		InputStream ism = null;
		URLConnection conn = null;
		FileOutputStream output = null;
		Bitmap bitmap = null;
		
		//在本地读取文件，如果存在，则直接解析，否则从网络获取

		//ism = context.openFileInput(imageFile);
		String[] fileList = context.fileList();
		for(int i = 0;i<fileList.length;i++){
			if(fileList[i].equals(jokeId+"")){
				try {
					ism = context.openFileInput(jokeId+"");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				bitmap = BitmapFactory.decodeStream(ism);
				try {
					if(ism != null){
						ism.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		if(bitmap == null){
			try {
				//连接网络
				URL url = new URL(ImageUrl);
				conn = url.openConnection();
				conn.connect();
				ism = conn.getInputStream();

				bitmap = BitmapFactory.decodeStream(ism);

				try {
					if(ism != null){
						ism.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				//保存文件
				if(bitmap != null){
					output = context.openFileOutput(jokeId+"", Context.MODE_PRIVATE);
					bitmap.compress(CompressFormat.JPEG, 50, output);
					
					output.flush();
					output.close();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
			
		return bitmap;
	}
	
}
