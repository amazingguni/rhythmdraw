package com.codecamp.rhythmdraw;





import android.app.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.os.Bundle;

import android.view.View;


public class MainActivity extends Activity {
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        setContentView(R.layout.main);
	        
	    }
	    
	    public void mOnClick(View v){
	    	Intent in=null;
	    	switch (v.getId())
	    	{
	    		case R.id.play:
	    		in = new Intent(this, RhythmGameActivity.class);
	        	startActivity(in);
	    			break;
	    		
	    		case R.id.register:	    		
	    		in = new Intent(this, SelectMusicActivity.class);
	    		startActivity(in);
	    		break;    		
	    	}   	    	
	    	
	    }
	
}