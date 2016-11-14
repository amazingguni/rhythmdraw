package com.codecamp.rhythmdraw;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.samsung.samm.common.SObjectStroke;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SPenTouchListener;


/*
 *  리듬 게임
 */
public class RhythmGameActivity extends Activity {
	/*
	 * SCanvas & Surface View  
	 */
	private Context 		mContext;
	private FrameLayout		mLayoutContainer;	
	private RelativeLayout	mCanvasContainer;
	private SCanvasView		mSCanvas;
	private drawView 		dv; //SCanvas 아래 서피스뷰
	
	/*
	 *  MP3 재생 및 DB에서 비트 검출 데이터 불러오기 관련
	 */
	private Play 			p;   //MediaPlayer Class	 
	private Cursor 			cursor; 
	private DBHelper 		mHelper;	
	private String		 	songTitle;	
	
	private Vector<RhythmItem> RhythmItemVector = new Vector<RhythmItem>();//아이템을 저장할 벡터;
	private RhythmItem rhy_item = null;
	private ArrayList<SPenTraker> list = new ArrayList<SPenTraker>();
	RhythmItem			curRhythmItem;	 //현재 그려질 리듬 아이템
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mContext = this;
        mHelper = new DBHelper(this);
    	/*
         * 
         * SCanvas & SurfaceView Init 
         * 
         */
		setContentView(R.layout.rhythmgame);							       
		mLayoutContainer = (FrameLayout) findViewById(R.id.layout_container);
		mCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);
		mSCanvas = new SCanvasView(mContext);     
		//mSCanvas.setSCanvasHoverPointerStyle(SCanvasConstants.SCANVAS_HOVERPOINTER_STYLE_SPENSDK);
		//mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);					        
		mCanvasContainer.addView(mSCanvas);
		//mSCanvas.setBGColor(Color.argb(0, 255,255,255));
		
		mSCanvas.setSCanvasInitializeListener(new SCanvasInitializeListener(){
			public void onInitialized() { 
				mSCanvas.getCanvasMode();
				mSCanvas.setSettingStrokeInfo(SObjectStroke.SAMM_STROKE_STYLE_PENCIL, 15, getResources().getColor(R.color.pen_color));
			}
		});
		
		mSCanvas.setSPenTouchListener(new SPenTouchListener() {
			public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {return false;}
			public boolean onTouchFinger(View arg0, MotionEvent arg1) {return false;}
			public void onTouchButtonUp(View arg0, MotionEvent arg1) {}
			public void onTouchButtonDown(View arg0, MotionEvent arg1) {}
			public boolean onTouchPen(View arg0, MotionEvent event) {
				if(!RhythmItemVector.isEmpty()){
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						list.clear();
						
						int x = (int) event.getX();
						int y = (int) event.getY();
						for(int i = 0 ; i < RhythmItemVector.size(); i++){
							checkType(RhythmItemVector.get(i), x, y);
						}
						
					}else if(event.getAction() == MotionEvent.ACTION_MOVE){
						int x = (int) event.getX();
						int y = (int) event.getY();
						
						if(rhy_item != null){
							checkDrawing(rhy_item, x, y);
						}
					}else if(event.getAction() == MotionEvent.ACTION_UP){
						int result = checkResult();
						mSCanvas.clearSCanvasView();
					}
				}
				return false;
			}
		});
		
		SurfaceView sv = (SurfaceView)findViewById(R.id.surface);
		dv = new drawView(RhythmGameActivity.this, sv);
		
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		songTitle = extras.getString("fileName");
		
		//임시 파일명 Intent로 넘겨받아야함
		//songTitle = "03 강남스타일.mp3";
		p = Play.Create(songTitle);
    }
    
    public int checkResult(){
    	int per = 0;
    	int img_x = rhy_item.getXpos(), img_y = rhy_item.getYpos();
    	int imgscale = 100;
    	int scalexy, scalexy_in;
    	int center_x = (img_x + imgscale/2);
        int center_y = (img_y + imgscale/2);
    	
    	switch(rhy_item.getimg_idx()){
		case 0 :
		    scalexy = 80;
		    scalexy_in = 40;
		    
			for(int i = 0 ; i < list.size(); i+=(int)(list.size()/10)){
				if((Math.sqrt((list.get(i).getX()-center_x)*(list.get(i).getX()-center_x) + (list.get(i).getY()-center_y)*(list.get(i).getY()-center_y)) < scalexy/2) && (Math.sqrt((list.get(i).getX()-center_x)*(list.get(i).getX()-center_x) + (list.get(i).getY()-center_y)*(list.get(i).getY()-center_y)) > scalexy_in/2)){
	        		per+=10;
	        	}else{
	        		
	        	}
			}
			break;
		case 1 : 
			for(int i = 0 ; i < list.size(); i+=(int)(list.size()/10)){
				if(list.get(i).getX() >= img_x+45 && list.get(i).getX() <=img_x+55 && list.get(i).getY() >=img_y+12 && list.get(i).getY() <= img_y+22){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+10 && list.get(i).getX() <=img_x+20 && list.get(i).getY() >=img_y+70 && list.get(i).getY() <= img_y+80){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+79 && list.get(i).getX() <=img_x+89 && list.get(i).getY() >=img_y+70 && list.get(i).getY() <= img_y+80){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+65 && list.get(i).getX() <=img_x+75 && list.get(i).getY() >=img_y+40 && list.get(i).getY() <= img_y+50){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+25 && list.get(i).getX() <=img_x+35 && list.get(i).getY() >=img_y+40 && list.get(i).getY() <= img_y+50){
					per+=15;
				}
			}
			break;
		case 2 : 
		    for(int i = 0 ; i < list.size(); i+=(int)(list.size()/10)){
				if(list.get(i).getX() >= img_x+11 && list.get(i).getX() <=img_x+21 && list.get(i).getY() >=img_y+10 && list.get(i).getY() <= img_y+20){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+69 && list.get(i).getX() <=img_x+79 && list.get(i).getY() >=img_y+10 && list.get(i).getY() <= img_y+20){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+11 && list.get(i).getX() <=img_x+21 && list.get(i).getY() >=img_y+23 && list.get(i).getY() <= img_y+33){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+11 && list.get(i).getX() <=img_x+21 && list.get(i).getY() >=img_y+70 && list.get(i).getY() <= img_y+80){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+69 && list.get(i).getX() <=img_x+79 && list.get(i).getY() >=img_y+70 && list.get(i).getY() <= img_y+80){
					per+=15;
				}
			}
			break;
		case 3 : 
			
			break;
		case 4 : 
			for(int i = 0 ; i < list.size(); i+=(int)(list.size()/10)){
				if(list.get(i).getX() >= img_x+28 && list.get(i).getX() <=img_x+38 && list.get(i).getY() >=img_y+13 && list.get(i).getY() <= img_y+23){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+28 && list.get(i).getX() <=img_x+38 && list.get(i).getY() >=img_y+41 && list.get(i).getY() <= img_y+51){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+28 && list.get(i).getX() <=img_x+38 && list.get(i).getY() >=img_y+69 && list.get(i).getY() <= img_y+79){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+43 && list.get(i).getX() <=img_x+53 && list.get(i).getY() >=img_y+69 && list.get(i).getY() <= img_y+79){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+59 && list.get(i).getX() <=img_x+69 && list.get(i).getY() >=img_y+69 && list.get(i).getY() <= img_y+79){
					per+=15;
				}
			}
			break;
		case 5 : 
			for(int i = 0 ; i < list.size(); i+=(int)(list.size()/10)){
				if(list.get(i).getX() >= img_x+19 && list.get(i).getX() <=img_x+29 && list.get(i).getY() >=img_y+13 && list.get(i).getY() <= img_y+23){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+19 && list.get(i).getX() <=img_x+29 && list.get(i).getY() >=img_y+70 && list.get(i).getY() <= img_y+80){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+61 && list.get(i).getX() <=img_x+71 && list.get(i).getY() >=img_y+13 && list.get(i).getY() <= img_y+23){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+61 && list.get(i).getX() <=img_x+71 && list.get(i).getY() >=img_y+70 && list.get(i).getY() <= img_y+80){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+40 && list.get(i).getX() <=img_x+50 && list.get(i).getY() >=img_y+41 && list.get(i).getY() <= img_y+51){
					per+=15;
				}
			}
			break;
		case 6 : 
			for(int i = 0 ; i < list.size(); i+=(int)(list.size()/10)){
				if(list.get(i).getX() >= img_x+9 && list.get(i).getX() <=img_x+19 && list.get(i).getY() >=img_y+43 && list.get(i).getY() <= img_y+53){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+23 && list.get(i).getX() <=img_x+33 && list.get(i).getY() >=img_y+57 && list.get(i).getY() <= img_y+67){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+31 && list.get(i).getX() <=img_x+41 && list.get(i).getY() >=img_y+67 && list.get(i).getY() <= img_y+77){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+52 && list.get(i).getX() <=img_x+62 && list.get(i).getY() >=img_y+44 && list.get(i).getY() <= img_y+54){
					per+=15;
				}
				
				if(list.get(i).getX() >= img_x+72 && list.get(i).getX() <=img_x+82 && list.get(i).getY() >=img_y+23 && list.get(i).getY() <= img_y+33){
					per+=15;
				}
			}
			break;
		}
    	
    	return per;
    }
    
    public void checkDrawing(RhythmItem item, int x, int y){
    	if(x >= item.getXpos() && x <= item.getXpos()+100 && y >= item.getYpos() && y <= item.getYpos()+100){
    		list.add(new SPenTraker(x, y));
    	}
    }
    
    public void checkType(RhythmItem item, int x, int y){
    	if(x >= item.getXpos() && x <= item.getXpos()+100 && y >= item.getYpos() && y <= item.getYpos()+100){
    		switch(item.getimg_idx()){
    		case 0 :
    			rhy_item = item;
    			break;
    		case 1 : 
    			rhy_item = item;
    			break;
    		case 2 : 
    			rhy_item = item;
    			break;
    		case 3 : 
    			rhy_item = item;
    			break;
    		case 4 : 
    			rhy_item = item;
    			break;
    		case 5 : 
    			rhy_item = item;
    			break;
    		case 6 : 
    			rhy_item = item;
    			break;
    		}
    	}
    }
    
 /*
  * SurfaceView
  */
  class drawView extends SurfaceView implements SurfaceHolder.Callback{    	
    	SurfaceHolder 		mHolder;
    	DrawBeatThread 		mThread;
    			
    	int delay; 	  
    	int totalbeat;
    	
    	private final int preview_delay = 1000; //미리 보여주는 밀리세컨드 딜레이
    	private final int filter_delay = 1000; //비트를 걸러낼 딜레이(ms) 
    	private final int end_delay = 1500; //마지막 비트가 나오고 끝낼 딜레이(ms)
    	
    	Vector<Beat> Note = new Vector<Beat>();  //Beat들을 저장할 벡터
    	Timer tm = new Timer();
    	
		public drawView(Context context, SurfaceView sv) {
			super(context);		
			mHolder = sv.getHolder();
			mHolder.addCallback(this);		
			mThread = new DrawBeatThread(mHolder, handler);//그리는 스레드 생성
		}					
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			SQLiteDatabase db = mHelper.getWritableDatabase();
			cursor = db.rawQuery("SELECT time FROM beat where mp3name = '"+songTitle+"';", null);
			//이어폰 스피커 확인
			AudioManager am = (AudioManager) RhythmGameActivity.this.getSystemService(Context.AUDIO_SERVICE);
			if (am.isWiredHeadsetOn()) {
				delay =0;
			} else {
				delay = 185;
			}
			//중복된 비트 제거, 최대 딜레이에 따라서 필터링해야함
			//DB에서 비트 데이터들을 벡터로 저장 
			boolean dupulicate=false;
			Beat prevBeat=null;
			while (cursor.moveToNext()) {			
				if(cursor.getInt(0)-delay-preview_delay > 0){
					Beat b = new Beat();				
					b.setMSTime(cursor.getInt(0));				
					for(int i=0; i<Note.size();i++){
						if(b.getTime() == Note.get(i).getTime()){
							dupulicate=true;
						}
					}
					if(!dupulicate){
						if(prevBeat != null){
							if(b.getTime()> prevBeat.getTime() +filter_delay){ ////1초 딜레이로 걸러냄
								Note.add(b);
								prevBeat=b;
							}
						}else{
							Note.add(b);
							prevBeat=b; 
						}						
					}				
					dupulicate = false;
					
				}				
			}
			cursor.close();
			mHelper.close();	
			
			totalbeat = Note.size(); //검출된 비트의 개수
			for(int i=0; i<Note.size();i++){
				//Log.e("TAG", "BeatTime:"+Note.get(i).getTime());
			}
			/*
			 * 리듬 아이템의 타입
			 */
			final String[] type = new String[7];
			for(int i=0;i<type.length;i++){
				type[i] = "draw_item_"+i+".png";
			}
			/*
			 * 
			 * 타이머에 따른 이벤트 처리
			 * 
			 */
			
			for(int i=0; i<totalbeat;i++)
			{
				final Beat bt = (Beat)Note.get(i);
				final int idx = i;
				TimerTask sb = new  TimerTask() {    
			           @Override				           
			           public void run() {   
			                 final Runnable timerAction = new Runnable() {
			                @Override
			                    public void run() { //해당 타이밍에 도형 생성
			                	//Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.)
			        			Random rand = new Random();
			        			
			        			int posX = rand.nextInt(16)*50;
			        			int posY = rand.nextInt(8)*50+150; 
			                	
			                	int ranidx = rand.nextInt(7);
			                	Bitmap bitmap = readBitmap(type[ranidx]);
			                	//RhythmItem newRhythmItem = new RhythmItem(posX,posY,bitmap, ranidx);
			                	if(curRhythmItem != null){
			                		curRhythmItem.Destroy();
			                		curRhythmItem = null;
			                	}
			                	curRhythmItem = new RhythmItem(posX,posY,bitmap, ranidx);
			                	//위치수정
			                	//BeatCircle newBeatCircle = BeatCircle.Create(0, 10, idx%5);
			                	//RhythmItemVector.add(newRhythmItem);
			                	//Log.e("TAG", "ADD");
			                	/*
			                	if(RhythmItemVector.size()>7){
			                		//Log.e("TAG", "ASDFASD");
			                		for(int i=0; i<RhythmItemVector.size(); i++){
			                			RhythmItemVector.get(0).Destroy();
			                		}
			                		RhythmItemVector.clear(); //화면에 7개가 차면 지운다
			                	}*/
			                						                	
			                   }
			               };
			                 handler.post(timerAction);
			            }			           
			        };				
			        tm.schedule(sb, bt.getTime()-preview_delay); //1초 미리 보여줌
			        if(i == totalbeat-1){ //마지막 이벤트
			        	TimerTask fin = new  TimerTask() {    
					           @Override				           
					           public void run() {  
					                 final Runnable timerAction = new Runnable() {
					                @Override
					                    public void run() { //종료메시지
					                	mThread.handler.sendEmptyMessage(-1);
					                    }
					                 };
					                 handler.post(timerAction);
					            }			           
					        };				        	
					        tm.schedule(fin, bt.getTime()+end_delay); //마지막 비트가 끝나고 1500초후 종료
			        }	    
			}			
			p.start();//음악재생
			mThread.start();//그리는 스레드 시작	
		}
		//Assets에서 Bitmap 불러오기
		protected Bitmap readBitmap(String name){
			  AssetManager assetManager = (AssetManager)mContext.getResources().getAssets();
			  InputStream is = null;;
			  try {
			   is = assetManager.open(name,AssetManager.ACCESS_BUFFER);
			  } catch (IOException e) {			   
			   e.printStackTrace();
			  }
			  return BitmapFactory.decodeStream(is);
		}		
		
		Handler handler = new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
					case -1: //종료
						finish();
					break;
				}
			}		
		};		 
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {			
			
		}		
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if(mThread != null){
				mThread.stopThread();
				mThread =null;
			}
			if(tm != null){
				tm.cancel();
				tm=null;
			}
		}
		/*
		@Override
		public void onDraw(Canvas canvas){
			canvas.drawColor(Color.WHITE);
			super.onDraw(canvas);
			
		}*/
		 
	    class DrawBeatThread extends Thread{
	    	boolean stop;
	    	SurfaceHolder mHolder;
	    	Handler mHandler;
	    	int notouchframe;
	    	DrawBeatThread(SurfaceHolder Holder, Handler h){
	    		mHandler = h;
	    		mHolder = Holder;
	    		stop = false;	    		
	    	}	    
	    	public void run(){
	    		Canvas canvas;
	    		while(!stop){
	    			synchronized(mHolder){
	    				canvas = mHolder.lockCanvas();
	    				if(canvas == null) break;	    				
	    				canvas.drawColor(Color.WHITE);		    			
	    				if(curRhythmItem != null) curRhythmItem.Draw(canvas);
	    				mHolder.unlockCanvasAndPost(canvas);	
	    			}	    			
	    		}	    		
	    	}
	    	public void stopThread(){
				if(p.mPlayer.isPlaying()) p.stopSong();
				
	    	}
	    	Handler handler = new Handler(){
				public void handleMessage(Message msg){
					if(msg.what == -1){						
						stopThread();						
					}
				}		
			};		 
	    }
    } 
  @Override
	protected void onStop() {
	  super.onStop();
	  if(p != null){		
		if(p.mPlayer != null){
			if(p.mPlayer.isPlaying()){
				p.stopSong();
			}
			p.mPlayer.release();						
		}
	  }
		
	}
}

class SPenTraker {
	int x;
	int y;
	
	SPenTraker(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
}
