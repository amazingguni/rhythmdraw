package com.codecamp.rhythmdraw;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;


/*
 *   MediaPlayer Class
 * 
 */
class Play extends Thread
{    	
	MediaPlayer mPlayer;
	static Play p;
	String path;
	//Play(String song){
	static Play Create(String song){
		p = new Play();
		p.mPlayer = new MediaPlayer();
		Log.d("Create MediaPlayer", "1!!!!");
		
		//mSdPath+"/media/Jackie Gleason - Love (Your spell is everywhere).mp3"
		p.path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/"+song;
		try {
			p.mPlayer.setDataSource(p.path);
			//p.mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			p.mPlayer.prepare();
			//p.mPlayer.setAudioStreamType(0);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;		
	}
	public void run()
	{	
		Log.d("PLAY", "1!!!!");
		
		if(mPlayer == null){
			Log.d("Create MediaPlayer", "1!!!!");
			mPlayer = new MediaPlayer();
			try {
				mPlayer.setDataSource(p.path);
				mPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		mPlayer.start();    		
	}    	
	public int getPosition()
	{    		
		return mPlayer.getCurrentPosition();
	}	
	public void stopSong()
	{
		Log.d("STOP", "!!!!!");
		if(mPlayer != null)
		mPlayer.stop();
	}    	
	public void setPath(String song)
	{
		path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/media/"+song;
	}
	public Play getInstance(){
		return p;
	}
}
