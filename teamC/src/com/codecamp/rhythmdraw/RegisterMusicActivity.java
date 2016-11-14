package com.codecamp.rhythmdraw;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


/*
 * 
 * 나중에 수정해야함 이건신경쓰지마시오
 * 
 */
public class RegisterMusicActivity extends Activity {
    /** Called when the activity is first created. */
	
	 ContentResolver mCr;
	 Cursor cursor;
	 ProgressDialog pd;
	 DecodeThread dt;
	 String decodePath,decodeName,decodeTitle;	 
	 int decodeDuration;
	 DBHelper mHelper;    
	 ListView MP3List, DeviceMP3List;	 
	 MP3ListAdapter addMP3Adapter;	 DeviceMP3ListAdapter deviceMP3Adapter;	 	
	 ArrayList<String> playPathlist = new ArrayList<String>();
	 ArrayList<String> playTitlelist = new ArrayList<String>();
	 ArrayList<Integer> playTimelist = new ArrayList<Integer>();	 
	 ArrayList<String> list,pathlist,deviceList;	 int mp3num;   
	 static final int PROGRESS_DIALOG = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //list layout 나중에 수정
        setContentView(R.layout.parentmusic);
        
        MP3List = (ListView)findViewById(R.id.parent_rhythm_addedlist);
        MP3List.setVerticalScrollBarEnabled(false);        
        DeviceMP3List = (ListView)findViewById(R.id.parent_rhythm_addlist);
		DeviceMP3List.setVerticalScrollBarEnabled(false);				
		
        initMusicListView();      
       
		MP3List.setAdapter(addMP3Adapter);   
		DeviceMP3List.setAdapter(deviceMP3Adapter);
		
		
	}
public void initMusicListView()
{
	 mCr = getContentResolver();
     mHelper = new DBHelper(this);	      
        
     //등록된 MP3 리스트 뷰
     SQLiteDatabase db = mHelper.getWritableDatabase();
     Cursor cs;
     cs = db.rawQuery("SELECT * FROM mp3list", null);       
     //리스트뷰에 추가
     list = new ArrayList<String>();
     pathlist = new ArrayList<String>();
     
     if(cs != null){	     
        while(cs.moveToNext()){
        	list.add(cs.getString(0)); //mp3name
        	pathlist.add(cs.getString(2)); //title
        }       	     	
       }   
     cs.close();  
     mHelper.close();	                
    addMP3Adapter = new MP3ListAdapter(this, R.layout.mp3list, list, pathlist);	        
    //디바이스의 MP3
    Uri uri;
	uri = Audio.Media.EXTERNAL_CONTENT_URI;
	cursor = mCr.query(uri, null, null, null, null);
	deviceMP3Adapter = new DeviceMP3ListAdapter(this, R.layout.devicemp3list, cursor);
}
 DialogInterface.OnClickListener mRegister = new DialogInterface.OnClickListener() {   			
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(which == DialogInterface.BUTTON1){ //디코딩진행=>노트저장=>노트,목록저장 (파일로)
				//Log.d("MP3NUM", Integer.toString(mp3num));
				 Uri uri;
					uri = Audio.Media.EXTERNAL_CONTENT_URI;
					cursor = mCr.query(uri, null, null, null, null);
					deviceMP3Adapter = new DeviceMP3ListAdapter(RegisterMusicActivity.this, R.layout.devicemp3list, cursor);
				cursor.moveToPosition(mp3num);				
				String mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();				
				if(cursor.getString(cursor.getColumnIndex(MediaColumns.MIME_TYPE)).contains("mpeg")){//MP3
					Log.d("MP3?", "YES");							
					decodeDuration = cursor.getInt(cursor.getColumnIndex(Audio.AudioColumns.DURATION));
					decodeName = cursor.getString(cursor.getColumnIndex(MediaColumns.DISPLAY_NAME));
					decodeTitle = cursor.getString(cursor.getColumnIndex(MediaColumns.TITLE));
					decodePath  = mSdPath+"/Music/"+decodeName;
					//short[] sizedSamples = new short[(int)44.1*decodeDuration];
					
					//Log.d("Sample Array Sized", Integer.toString(sizedSamples.length));
					ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					//WIFI접속확인
					if(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting())
					{
						
					
					if(list.contains(decodeTitle)){
						new AlertDialog.Builder(RegisterMusicActivity.this).setTitle("문제 발생").setMessage("이미 등록되어 있는 MP3 파일입니다.").setPositiveButton("확인", null).show();
					}					
					else{					
					//프로그레스바 생성		//	
					showDialog(PROGRESS_DIALOG);
					}
					}
					else{
						new AlertDialog.Builder(RegisterMusicActivity.this).setTitle("문제 발생").setMessage("WIFI가 연결된 상태이어야합니다.").setPositiveButton("확인", null).show();
					}
				}else{
					new AlertDialog.Builder(RegisterMusicActivity.this).setTitle("문제 발생").setMessage("MP3 파일만 등록이 가능합니다.").setPositiveButton("확인", null).show();
				}
				//cursor.close();			
			}
			else
				return;			
		}
	};	
	protected Dialog onCreateDialog(int id)
	{
		Log.d("Dialog", "onCreateDialog");
		switch(id)
		{			
			case PROGRESS_DIALOG:
				pd = new ProgressDialog(RegisterMusicActivity.this);
				pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				//pd.setMax(100);
				pd.setMessage("MP3 Decoding..");	
				
				
				dt = new DecodeThread(handler, decodePath);
				dt.start();
				return pd;
			default:
			return null;
		}			
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			//int total = msg.arg1;
			Log.d("handler msg", Integer.toString(msg.arg1));			
			
			if(msg.arg1 == -1){
				pd.setMessage("완료");
				list.add(decodeName);
				pathlist.add(decodeTitle);					
				addMP3Adapter.notifyDataSetChanged();
				removeDialog(PROGRESS_DIALOG);				
				new AlertDialog.Builder(RegisterMusicActivity.this).setTitle("추가 완료").setMessage("등록완료되었습니다.").setPositiveButton("확인", null).show();
				//Toast.makeText(P_Rhythm.this, "추가완료.", 1);
			}
			else if(msg.arg1 == 1)
			{
				pd.setMessage("선택한 MP3를 서버에 업로드 중..");
			}
			else if(msg.arg1 == 2)
			{
				pd.setMessage("서버를 기다리는 중..");
			}
			else if(msg.arg1 == 3)
			{
				pd.setMessage("서버에서 MP3를 Decoding 하는 중..");
			}
			else if(msg.arg1 == 4)
			{
				pd.setMessage("서버에서 MP3에서 박자를 검출 하는 중..");
			}
			else if(msg.arg1 == 5)
			{
				pd.setMessage("서버로부터 리듬 데이터를 받는 중..");
			}
			else if(msg.arg1 == 6)
			{
				pd.setMessage("DB에 저장 중");
			}
			else if(msg.arg1 == -99)
			{
				removeDialog(PROGRESS_DIALOG);
				new AlertDialog.Builder(RegisterMusicActivity.this).setTitle("문제 발생").setMessage("업로드에 실패하였습니다. WI-FI 연결을 확인해보세요.").setPositiveButton("확인", null).show();
			}
		}		
	};		

	private class DecodeThread extends Thread{
		Vector<Beat> note;
		Handler mHandler;
		//int 	mState;
		int		total;
		//int 	duration;
		String  path;//	String fileName;		
		short[] songData;
		private FileInputStream mFileInputStream = null;
		private URL connectUrl = null;
		private static final String HOST = "211.189.19.244"; ///서버주소
		private static final int PORT = 9090;	
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";	
		SocketChannel toServerSoc = null;	//채팅 서버에 연결할 소켓
		Selector selector = null;			//채팅 서버 연결할 셀렉터
		Charset charset = null;				//메시지 디코더
		CharsetDecoder decoder = null;		
		CharsetEncoder encoder = null;	
		ByteBuffer msgbuf;
		String noteresult;
		
		boolean connected=true;
		DecodeThread(Handler h, String path)
		{			
			charset = Charset.forName("EUC-KR");		
			encoder = charset.newEncoder();
			mHandler = h;		
			this.path = path;			
		}
		public void run()
		{			
						//114112
				//MP3파일 서버에 업로드 중
				Message msg = Message.obtain(mHandler, 0, 1, 0);
				mHandler.sendMessage(msg);
				Log.d("decodepath", decodePath);
				String result=HttpFileUpload("http://"+HOST+"/decodeupload.php",decodePath, decodeName);
				/*if(result.equals("파싱 실패")){
					Log.d("웹서버 연결실패", "웹서버연결실패");
				}*/
				if(result.equals("fail"))
				{						
					//웹서버 연결실패
					msg = Message.obtain(mHandler, 0, -99, 0);
					mHandler.sendMessage(msg);
					return;
				}
				//자바 서버 연결
				try
				{						
					selector = Selector.open();		// 셀렉터를 연다.						
					toServerSoc = SocketChannel.open(new InetSocketAddress(HOST, PORT)); 	// 소켓채널 생성.
						
					toServerSoc.configureBlocking(false);		// 비블록킹 모드로 설정. 
					toServerSoc.register(selector, SelectionKey.OP_READ);	// 서버소켓채널을 셀렉터에 등록.
						
					charset = Charset.forName("EUC-KR");	//통신 문자 디코더 정의 
					decoder = charset.newDecoder();
					
					Log.d("NIO서버접속", "ㅇㅇㅇ");
					AcceptListen(); //서버로부터 메시지 대기
				}catch (Exception e) { 
					msg = Message.obtain(mHandler, 0, -99, 0);
					mHandler.sendMessage(msg);
					return;
				} 
				
				msg = Message.obtain(mHandler, 0, 6, 0);
				mHandler.sendMessage(msg);	
				noteParsing(noteresult);				
				makeNote();
				
				//종료
				msg = Message.obtain(mHandler, 0, -1, 0);
				mHandler.sendMessage(msg);							
		}		
		public void AcceptListen(){  
			msgbuf = ByteBuffer.allocateDirect(1024*2); //버퍼메모리할당
			msgbuf.order(ByteOrder.LITTLE_ENDIAN);
			Log.d("Connect", "자바 서버 연결 메시지 기다리는 중");	
			try{
				while (connected) {
					Message msg = Message.obtain(mHandler, 0, 2, 0);
					mHandler.sendMessage(msg);	
					System.out.println("요청을 기다리는 중..");	
					selector.select(); //셀렉터 대기
					
					// 실렉터의 SelectedSet 에 저장된 준비된 이벤트들(SelectionKey들)을 하나씩 처리.
					Iterator it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						if (key.isReadable()) {
							read(key);
						} 
						// 이미 처리한 이벤트이므로 삭제.
						it.remove();				
					}								
				}			
			}catch (Exception e) {
					//System.out.println("클라이언트 READ 실패");
					Message msg = Message.obtain(mHandler, 0, -99, 0);
					mHandler.sendMessage(msg);						
					return;
			 } 				
		}	
		private void read(SelectionKey key) {
			// SelectionKey 로부터 소켓채널을 얻어옴.
			SocketChannel sc = (SocketChannel) key.channel();			
			
			String msg;// 메시지를 담을 스트링
			msgbuf.clear();
			
			try {
				// 요청한 클라이언트의 소켓채널로부터 데이터를 읽어들임.				
				int read = sc.read(msgbuf);				
				if(read == -1)
				{
					Log.d("SERVER", "메시지 받기 실패");
					Message m = Message.obtain(mHandler, 0, -99, 0);
					mHandler.sendMessage(m);
					key.cancel();
					sc.close();
					sc = null;
					connected=false;
				}
				
				msgbuf.flip();			
				msg = bytetostr(msgbuf); //바이트 디코딩하여 스트링으로 바꿈.
				String r=msgParsing(msg);
				Log.d("nioserverresult", r);
				
			} catch (IOException e) {								
				System.out.println(sc.toString() + "서버 접속 오류");
				Message m = Message.obtain(mHandler, 0, -99, 0);
				mHandler.sendMessage(m);
				}			
		}	
		private void sendMsg(String msg)
		{				
			System.out.println("sendmsg: "+msg);
			ByteBuffer bb; 
			try {
				bb = encoder.encode(CharBuffer.wrap(msg));
				bb.clear();				
				
				try {
					toServerSoc.write(bb);
				} catch (IOException e) {
					Message m = Message.obtain(mHandler, 0, -99, 0);
					mHandler.sendMessage(m);
					e.printStackTrace();
				}			
			} catch (CharacterCodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		private void clearBuffer(ByteBuffer buffer) { //버퍼 비우기
			if (buffer != null) {
				buffer.clear();
				buffer = null;
			}
		}	
		private String bytetostr(ByteBuffer buf) //바이트를 스트링으로 바꾸어주는 함수
		{
			String data = "";
			try{
				data = decoder.decode(buf).toString();
			}catch (CharacterCodingException e){}		
			return data;		
		}	
		private void makeNote()
		{
			if(note == null) return;
			SQLiteDatabase db;
			db = mHelper.getWritableDatabase();
			
			Log.d("Make Note!", "Note Size:" +Integer.toString(note.size()));
			db.execSQL("INSERT INTO mp3list VALUES ('"+decodeName+"', "+Integer.toString(decodeDuration)+", '"+decodeTitle+"');");
			//비트 추가
			for(int i=0; i<note.size();i++)
			{
				Beat bt = (Beat)note.get(i);
			
				db.execSQL("INSERT INTO beat VALUES ('"+decodeName+"', "+Integer.toString(bt.getTime())+");");
				//Log.d(decodeName, "INSERT INTO beat VALUES ('"+decodeName+"', "+Integer.toString(bt.getTime())+", "+Float.toString(bt.getStr())+");");
			}
			mHelper.close();
		}				
		public void noteParsing(String str)
		{
			if(str == null){
				Message m = Message.obtain(mHandler, 0, -99, 0);
				mHandler.sendMessage(m);
				return;
			}
			String parse[] = str.split("/");
			note = new Vector<Beat>();
			for(int i=2; i<parse.length; i++)
			{
				if(parse[i].equals("-1"))
					break;
				Beat b = new Beat();
				Log.d("BeatData", parse[i]);
				b.setStr(0);
				b.setMSTime(Integer.parseInt(parse[i]));//Time						
				note.add(b);
			}			
			
		}
		public String msgParsing(String str)
		{
			if(str == null){
				Message m = Message.obtain(mHandler, 0, -99, 0);
				mHandler.sendMessage(m);
				return "";
			}
			String parse[] = str.split("/");
			if(parse[0].contains("msg"))
			{
				if(parse[1].equals("upload"))
				{
					return "업로드 성공";
				}
				else if(parse[1].equals("fail"))
				{
					return "업로드 실패";
				}				
				else if(parse[1].equals("connect"))
				{					
					String s="uploaded/"+decodeName+"/"+Integer.toString(decodeDuration)+"/";
					sendMsg(s);
					return "접속 성공, 업로드요청보냄";
				}
				else if(parse[1].equals("beat"))
				{
					Message msg = Message.obtain(mHandler, 0, 4, 0);
					mHandler.sendMessage(msg);	
					return "박자 검출 시작";					
				}
				else if(parse[1].equals("decode"))
				{
					Message msg = Message.obtain(mHandler, 0, 3, 0);
					mHandler.sendMessage(msg);	
					return "서버에서 MP3 디코딩 시작";
				}
				else if(parse[1].equals("result"))
				{
					
					noteresult = str;
					Log.d("BeatData", str);
					Message msg = Message.obtain(mHandler, 0, 5, 0);
					mHandler.sendMessage(msg);	
					
					//접속종료
					connected=false;
					msgbuf.clear();
					msgbuf=null;
					try {
						toServerSoc.close();
						toServerSoc = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				else
				{
					return "파싱 실패";
				}				
			}	
			return "파싱 실패";
		}
		
		private String HttpFileUpload(String urlString, String path, String fileName) {
			try {			
				mFileInputStream = new FileInputStream(path);
				Log.d("url", urlString);
				connectUrl = new URL(urlString);				
				Log.d("Test", "mFileInputStream  is " + mFileInputStream);
				
				fileName=java.net.URLEncoder.encode(fileName, "UTF-8");
				Log.d("fileName UTF-8", fileName);
				// open connection 
				HttpURLConnection conn = (HttpURLConnection)connectUrl.openConnection();
				
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);		
				
				
				// write data
				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			
				
				dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
				Log.d("111", "123");
				
				dos.writeBytes("Content-Disposition: form-dat" +
						"a; name=\"uploadedfile\";filename=\"" + fileName+"\"" + lineEnd);
				
				dos.writeBytes(lineEnd);
				Log.d("111", "123");
				int bytesAvailable = mFileInputStream.available();
				int maxBufferSize = 1024;
				int bufferSize = Math.min(bytesAvailable, maxBufferSize);
				
				byte[] buffer = new byte[bufferSize];
				int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
				
				Log.d("Test", "MP3File byte is " + bytesRead);
				
				
				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = mFileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
				}	
				
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
				
				// close streams
				Log.e("Test" , "File is written");
				mFileInputStream.close();
				dos.flush(); // finish upload...			
				
				// get response
				int ch;
				InputStream is = conn.getInputStream();
				StringBuffer b =new StringBuffer();
				while( ( ch = is.read() ) != -1 ){
					b.append( (char)ch );
				}
				String s=b.toString();
			
				Log.e("Test", "result = " + s);
				String result;
				result = msgParsing(s);
				dos.close();			
				return result;
				
			} catch (Exception e) {
				Log.d("Test", "exception " + e.getMessage());
				return "fail";
				// TODO: handle exception
			}		
			
		}
	}
	//디바이스 내 저장된 mp3 리스트
	class DeviceMP3ListAdapter extends BaseAdapter {
		Context maincon;
		LayoutInflater Inflater;
		Cursor curSrc;
		int layout;

		public DeviceMP3ListAdapter(Context context, int alayout, Cursor
				cs) {
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			curSrc = cs;
			layout = alayout;
		}

		public int getCount() {
			return curSrc.getCount();
		}

		public String getItem(int position) {
			curSrc.moveToPosition(position);
			return curSrc.getString(cursor.getColumnIndex(MediaColumns.TITLE));			
		}

		public long getItemId(int position) {
			return curSrc.getPosition();
		}

		// 각 항목의 뷰 생성
		public View getView(final int position, View convertView, ViewGroup parent) {
			final int pos = position;
			if (convertView == null) {
				convertView = Inflater.inflate(layout, parent, false);
			}			
			
			TextView txt = (TextView)convertView.findViewById(R.id.MP3text);
			
			curSrc.moveToPosition(pos);
			txt.setText(curSrc.getString(curSrc.getColumnIndex(MediaColumns.TITLE)));
			txt.setSelected(true);

			return convertView;
		}
	}

	//등록된 mp3 리스트
	class MP3ListAdapter extends BaseAdapter {
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<String> arSrc;
		ArrayList<String> arPath;
		int layout;

		public MP3ListAdapter(Context context, int alayout, ArrayList<String
				> aarSrc, ArrayList<String> aarPath) {
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			arPath = aarPath;
			layout = alayout;
		}

		public int getCount() {
			return arPath.size();
		}

		public String getItem(int position) {
			return arPath.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		// 각 항목의 뷰 생성
		public View getView(final int position, View convertView, ViewGroup parent) {
			final int pos = position;
			if (convertView == null) {
				convertView = Inflater.inflate(layout, parent, false);
			}			

			TextView txt = (TextView)convertView.findViewById(R.id.MP3text);
			
			txt.setText(arPath.get(position));
			txt.setSelected(true);
				final Button btn = (Button)convertView.findViewById(R.id.MP3Deletebtn);
				btn.setOnTouchListener(new OnTouchListener(){
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						
					}
					else if(event.getAction() == MotionEvent.ACTION_UP){
						
						//DB불러오기	
						/*
						SQLiteDatabase db = mHelper.getWritableDatabase();      
						Log.d("TAG", arPath.get(position));						
						cursor = db.rawQuery("SELECT * FROM mp3list where title ='"+arPath.get(position)+"';", null);   
						Intent in = new Intent(RegisterMusicActivity.this, BeatDisplay.class);
						if(cursor != null)
						{
							while(cursor.moveToNext()){
								in.putExtra("path", cursor.getString(0));
								Log.d("TAG",cursor.getString(0));
				        	}
				        }						
						cursor.close();
						mHelper.close();
						startActivity(in);*/
					}
					return true;
					}
					
				});
				/*
				btn.setOnClickListener(new Button.OnClickListener() {
					//삭제버튼
					
				public void onClick(View v) {
					SQLiteDatabase db = mHelper.getWritableDatabase();
					//DB
					//리스트에서삭제
					db.execSQL("DELETE FROM mp3list where mp3name = '"+arSrc.get(pos)+"';");					
					//비트 노트 삭제
					db.execSQL("DELETE FROM beat where mp3name = '"+arSrc.get(pos)+"';");
					mHelper.close();
					//리스트뷰에서 삭제		
					arSrc.remove(position);
					arPath.remove(position);
					la.notifyDataSetChanged();
		
				}
			});*/

			return convertView;
		}
	}
}