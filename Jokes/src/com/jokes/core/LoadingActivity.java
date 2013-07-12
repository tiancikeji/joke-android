package com.jokes.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.Window;

public class LoadingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_loading);
		initTransitionAnimation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.loading, menu);
		return true;
	}
	
	private void initTransitionAnimation(){
		new Handler().postDelayed(new Runnable() {
			  @Override
			  public void run() {

			    //Create an intent that will start the main activity.
			    Intent mainIntent = new Intent(LoadingActivity.this, HomepageActivity.class);
			    LoadingActivity.this.startActivity(mainIntent);

			    //Finish splash activity so user cant go back to it.
			    LoadingActivity.this.finish();

			    //Apply splash exit (fade out) and main entry (fade in) animation transitions.
			    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
			  }
			}, 2000);
	}

}
