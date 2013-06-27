package com.jokes.mywidget;

import com.jokes.core.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MyToast {

	private Toast temp_toast;
	private TextView title;

	public MyToast(Context context,String text){
		initMyToast(context,text);
	}

	public void initMyToast(Context context,String text){
		LayoutInflater inflater = LayoutInflater.from(context);
//		View layout = inflater.inflate(R.layout.mywidget_toast,null);
//		title = (TextView) layout.findViewById(R.id.mywidget_toast1_textview);
//		title.setText(text);
		temp_toast = new Toast(context);
		temp_toast.setGravity(Gravity.CENTER, 0, 0);
		temp_toast.setDuration(1000);
//		temp_toast.setView(layout);
	}

	public void startMyToast(){
		temp_toast.show();
	}
}
