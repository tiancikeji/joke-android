package com.jokes.utils;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.jokes.core.R;
import com.jokes.objects.Joke;

//NOTE: 注意 NOT in Use / 不在用
public class JokeListAdapter extends ArrayAdapter<Joke> {
	
	private Context context;
	private List<Joke> jokes;
	
	public JokeListAdapter(Context context, List<Joke> jokes){
		super(context, 0, jokes);
		this.context = context;
		this.jokes = jokes;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(null == view){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.joke_panel, null);
		}
		
		return view;
	}

}
