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
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SelectMusicActivity extends Activity {
	 ContentResolver mCr;
	 Cursor cursor;
	 ProgressDialog pd;
	 DecodeThread dt;
	 String decodePath,decodeName,decodeTitle;	 
	 int decodeDuration;
	 boolean isStartable;
	 DBHelper mHelper;    
	 ListView MP3List, DeviceMP3List;	 
	 ImageView StartBtn; TextView isRegisteredText;
	 DeviceMP3ListAdapter deviceMP3Adapter;	 	
	 ArrayList<String> playPathlist = new ArrayList<String>();
	 ArrayList<String> playTitlelist = new ArrayList<String>();
	 ArrayList<Integer> playTimelist = new ArrayList<Integer>();	 
	 ArrayList<String> list,pathlist,deviceList;	 int mp3num;  
	 String cur_title;
	 static final int PROGRESS_DIALOG = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectmusic);                
        StartBtn = (ImageView)findViewById(R.id.playBtn);
        StartBtn.setOnTouchListener(startBtnListener);
		isRegisteredText = (TextView)findViewById(R.id.isRegisterdText);
        
        DeviceMP3List = (ListView)findViewById(R.id.device_musiclist);
		DeviceMP3List.setVerticalScrollBarEnabled(false);
		initMusicListView();
		DeviceMP3List.setAdapter(deviceMP3Adapter);
		
	}
public void initMusicListView()
{
	 mCr = getContentResolver();
     mHelper = new DBHelper(this);	      
        
     //��ϵ� MP3 ����Ʈ ��
     SQLiteDatabase db = mHelper.getWritableDatabase();
     Cursor cs;
     cs = db.rawQuery("SELECT * FROM mp3list", null);       
     //����Ʈ�信 �߰�
     list = new ArrayList<String>();
     pathlist = new ArrayList<String>();
     
     if(cs != null){	     
        while(cs.moveToNext()){
        	Log.e("TAG", cs.getString(0));
        	list.add(cs.getString(0)); //mp3name
        	pathlist.add(cs.getString(2)); //title
        }       	     	
       }   
     cs.close();  
     mHelper.close();
    //����̽��� MP3
    Uri uri;
	uri = Audio.Media.EXTERNAL_CONTENT_URI;
	cursor = mCr.query(uri, null, null, null, null);
	deviceMP3Adapter = new DeviceMP3ListAdapter(this, R.layout.devicemp3list, cursor);
}

	protected Dialog onCreateDialog(int id)
	{	
		switch(id)
		{			
			case PROGRESS_DIALOG:
				pd = new ProgressDialog(SelectMusicActivity.this);
				pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				pd.setMessage("MP3 Decoding..");	
				dt = new DecodeThread(handler, decodePath);
				dt.start();
				return pd;
			default:
			return null;
		}			
	}
	//�������� ��� ����� �˸��� �ڵ鷯
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			if(msg.arg1 == -1){ //���ڵ��Ϸ�
				pd.setMessage("�Ϸ�");
				list.add(decodeName);
				pathlist.add(decodeTitle);					
				//addMP3Adapter.notifyDataSetChanged();
				removeDialog(PROGRESS_DIALOG);				
				new AlertDialog.Builder(SelectMusicActivity.this).setTitle("�߰� �Ϸ�").setMessage("��ϿϷ�Ǿ����ϴ�.").setPositiveButton("Ȯ��", null).show();
				StartBtn.setBackgroundResource(R.drawable.play_btn);
				isRegisteredText.setText("");
				isStartable = true;
				//Toast.makeText(P_Rhythm.this, "�߰��Ϸ�.", 1);
			}
			else if(msg.arg1 == 1)
			{
				pd.setMessage("������ MP3�� ������ ���ε� ��..");
			}
			else if(msg.arg1 == 2)
			{
				pd.setMessage("������ ��ٸ��� ��..");
			}
			else if(msg.arg1 == 3)
			{
				pd.setMessage("�������� MP3�� Decoding �ϴ� ��..");
			}
			else if(msg.arg1 == 4)
			{
				pd.setMessage("�������� MP3���� ���ڸ� ���� �ϴ� ��..");
			}
			else if(msg.arg1 == 5)
			{
				pd.setMessage("�����κ��� ���� �����͸� �޴� ��..");
			}
			else if(msg.arg1 == 6)
			{
				pd.setMessage("DB�� ���� ��");
			}
			else if(msg.arg1 == -99)
			{
				removeDialog(PROGRESS_DIALOG);
				new AlertDialog.Builder(SelectMusicActivity.this).setTitle("���� �߻�").setMessage("���ε忡 �����Ͽ����ϴ�. WI-FI ������ Ȯ���غ�����.").setPositiveButton("Ȯ��", null).show();
			}
		}		
	};		

	
	//����̽� �� ����� mp3 ����Ʈ
	class DeviceMP3ListAdapter extends BaseAdapter {
		Context maincon;
		LayoutInflater Inflater;
		Cursor curSrc;
		int layout;
		 
		RhythmGameData _RhythmGameData = new RhythmGameData();
		ImageView jacket_img = (ImageView)findViewById(R.id.jacket_img); 
		TextView album_info_txt = (TextView)findViewById(R.id.album_info_txt);
		String mp3_file_name;
		
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

		// �� �׸��� �� ����
		public View getView(final int position, View convertView, ViewGroup parent) {
			final int pos = position;
			if (convertView == null) {
				convertView = Inflater.inflate(layout, parent, false);
			}			
			
			final TextView txt = (TextView)convertView.findViewById(R.id.MP3text);
			
			curSrc.moveToPosition(pos);
			
			txt.setText(curSrc.getString(curSrc.getColumnIndex(MediaColumns.TITLE)));
			txt.setSelected(true);
			
				convertView.setOnTouchListener(new View.OnTouchListener(){

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						mp3num = position;
						cur_title = txt.getText().toString();
						if(pathlist.contains(txt.getText().toString())){
							StartBtn.setBackgroundResource(R.drawable.play_btn);
							isRegisteredText.setText("");
							isStartable=true;
						}else{
							StartBtn.setBackgroundResource(R.drawable.download_btn);
							isRegisteredText.setText("���� �м� ������\n�ٿ�ε� �� ��\n�÷��� �� �� �ֽ��ϴ�.");
							isStartable=false;
						}
						
						if(cur_title.equals("������Ÿ��")){
							mp3_file_name = "03 ������Ÿ��.mp3";
						}else if(cur_title.equals("������ (Feat. ����, Zion.T)")){
							mp3_file_name = "������ (Feat. ����, Zion.T)";
						}else if(cur_title.equals("Professional Grifers (Feat. Gerard Way)")){
							mp3_file_name = "Professional Grifers (Feat. Gerard Way)";
						}else{
							mp3_file_name = "etc";
						}
						
						jacket_img.setBackgroundResource(_RhythmGameData.getAlbumImageId(mp3_file_name));
						album_info_txt.setText(_RhythmGameData.getMusicInfo(mp3_file_name));
						
						return false;
					}
					
				});

			return convertView;
		}
	}
	OnTouchListener startBtnListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub				
			ImageView iv = (ImageView)v;
			if(event.getAction() == MotionEvent.ACTION_DOWN){					
				if(isStartable){ //��ϵ��ִ� MP3�� ����
					SQLiteDatabase db = mHelper.getWritableDatabase();
					cursor = db.rawQuery("SELECT * FROM mp3list where title ='"+cur_title+"';", null);   
					Intent in = new Intent(SelectMusicActivity.this, RhythmGameActivity.class);
					if(cursor != null)
					{
						while(cursor.moveToNext()){
							in.putExtra("fileName", cursor.getString(0));
			        	}
			        }						
					cursor.close();
					mHelper.close();
					startActivity(in);
				}else{ //��Ͼȵ��ִ� MP3�� ������ ���ε�
					Uri uri;
					uri = Audio.Media.EXTERNAL_CONTENT_URI;
					cursor = mCr.query(uri, null, null, null, null);
					deviceMP3Adapter = new DeviceMP3ListAdapter(SelectMusicActivity.this, R.layout.devicemp3list, cursor);
					cursor.moveToPosition(mp3num);				
					String mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();				
					if(cursor.getString(cursor.getColumnIndex(MediaColumns.MIME_TYPE)).contains("mpeg")){//MP3
						decodeDuration = cursor.getInt(cursor.getColumnIndex(Audio.AudioColumns.DURATION));
						decodeName = cursor.getString(cursor.getColumnIndex(MediaColumns.DISPLAY_NAME));
						decodeTitle = cursor.getString(cursor.getColumnIndex(MediaColumns.TITLE));
						decodePath  = mSdPath+"/Music/"+decodeName;					
						ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						//WIFI����Ȯ��
						if(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting())
						{
							if(pathlist.contains(decodeTitle)){
								new AlertDialog.Builder(SelectMusicActivity.this).setTitle("���� �߻�").setMessage("�̹� ��ϵǾ� �ִ� MP3 �����Դϴ�.").setPositiveButton("Ȯ��", null).show();
							}else{					
									//���α׷����� ����		//	
									showDialog(PROGRESS_DIALOG);
							}
						}else{
							new AlertDialog.Builder(SelectMusicActivity.this).setTitle("���� �߻�").setMessage("WIFI�� ����� �����̾���մϴ�.").setPositiveButton("Ȯ��", null).show();
						}
					}else{
						new AlertDialog.Builder(SelectMusicActivity.this).setTitle("���� �߻�").setMessage("MP3 ���ϸ� ����� �����մϴ�.").setPositiveButton("Ȯ��", null).show();
					}		
				}
			}					
			return true;			
		}
		
	};
	
	
	
	/*
	 * 
	 * 
	 *  ������ ��� MP3 ���ڵ�, ��ȣó�� ����
	 * 
	 * 
	 */
	
	
	private class DecodeThread extends Thread{
		Vector<Beat> note;
		Handler mHandler;
		int		total;
		String  path;		
		short[] songData;
		private FileInputStream mFileInputStream = null;
		private URL connectUrl = null;
		private static final String HOST = "211.189.19.244"; ///�����ּ�
		private static final int PORT = 9090;	
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";	
		SocketChannel toServerSoc = null;	//ä�� ������ ������ ����
		Selector selector = null;			//ä�� ���� ������ ������
		Charset charset = null;				//�޽��� ���ڴ�
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
			//MP3���� ������ ���ε� ��
			Message msg = Message.obtain(mHandler, 0, 1, 0);
			mHandler.sendMessage(msg);
			Log.d("decodepath", decodePath);
			String result=HttpFileUpload("http://"+HOST+"/decodeupload.php",decodePath, decodeName);
			
			if(result.equals("fail"))
			{						
				//������ �������
				msg = Message.obtain(mHandler, 0, -99, 0);
				mHandler.sendMessage(msg);
				return;
			}
			//�ڹ� ���� ����
			try
			{						
				selector = Selector.open();		// �����͸� ����.						
				toServerSoc = SocketChannel.open(new InetSocketAddress(HOST, PORT)); 	// ����ä�� ����.
					
				toServerSoc.configureBlocking(false);		// ����ŷ ���� ����. 
				toServerSoc.register(selector, SelectionKey.OP_READ);	// ��������ä���� �����Ϳ� ���.
					
				charset = Charset.forName("EUC-KR");	//��� ���� ���ڴ� ���� 
				decoder = charset.newDecoder();
				
				Log.d("NIO��������", "������");
				AcceptListen(); //�����κ��� �޽��� ���
			}catch (Exception e) { 
				msg = Message.obtain(mHandler, 0, -99, 0);
				mHandler.sendMessage(msg);
				return;
			} 
			
			msg = Message.obtain(mHandler, 0, 6, 0);
			mHandler.sendMessage(msg);	
			noteParsing(noteresult);				
			makeNote();
			
			//����
			msg = Message.obtain(mHandler, 0, -1, 0);
			mHandler.sendMessage(msg);							
		}	
		public void AcceptListen(){  
			msgbuf = ByteBuffer.allocateDirect(1024*2); //���۸޸��Ҵ�
			msgbuf.order(ByteOrder.LITTLE_ENDIAN);
			Log.d("Connect", "�ڹ� ���� ���� �޽��� ��ٸ��� ��");	
			try{
				while (connected) {
					Message msg = Message.obtain(mHandler, 0, 2, 0);
					mHandler.sendMessage(msg);	
					System.out.println("��û�� ��ٸ��� ��..");	
					selector.select(); //������ ���
					
					// �Ƿ����� SelectedSet �� ����� �غ�� �̺�Ʈ��(SelectionKey��)�� �ϳ��� ó��.
					Iterator it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						if (key.isReadable()) {
							read(key);
						} 
						// �̹� ó���� �̺�Ʈ�̹Ƿ� ����.
						it.remove();				
					}								
				}			
			}catch (Exception e) {
					//System.out.println("Ŭ���̾�Ʈ READ ����");
					Message msg = Message.obtain(mHandler, 0, -99, 0);
					mHandler.sendMessage(msg);						
					return;
			 } 				
		}	
		private void read(SelectionKey key) {
			// SelectionKey �κ��� ����ä���� ����.
			SocketChannel sc = (SocketChannel) key.channel();			
			
			String msg;// �޽����� ���� ��Ʈ��
			msgbuf.clear();
			
			try {
				// ��û�� Ŭ���̾�Ʈ�� ����ä�ηκ��� �����͸� �о����.				
				int read = sc.read(msgbuf);				
				if(read == -1)
				{
					Log.d("SERVER", "�޽��� �ޱ� ����");
					Message m = Message.obtain(mHandler, 0, -99, 0);
					mHandler.sendMessage(m);
					key.cancel();
					sc.close();
					sc = null;
					connected=false;
				}
				
				msgbuf.flip();			
				msg = bytetostr(msgbuf); //����Ʈ ���ڵ��Ͽ� ��Ʈ������ �ٲ�.
				String r=msgParsing(msg);
				Log.d("nioserverresult", r);
				
			} catch (IOException e) {								
				System.out.println(sc.toString() + "���� ���� ����");
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
		private void clearBuffer(ByteBuffer buffer) { //���� ����
			if (buffer != null) {
				buffer.clear();
				buffer = null;
			}
		}	
		private String bytetostr(ByteBuffer buf) //����Ʈ�� ��Ʈ������ �ٲپ��ִ� �Լ�
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
			//��Ʈ �߰�
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
					return "���ε� ����";
				}
				else if(parse[1].equals("fail"))
				{
					return "���ε� ����";
				}				
				else if(parse[1].equals("connect"))
				{					
					String s="uploaded/"+decodeName+"/"+Integer.toString(decodeDuration)+"/";
					sendMsg(s);
					return "���� ����, ���ε��û����";
				}
				else if(parse[1].equals("beat"))
				{
					Message msg = Message.obtain(mHandler, 0, 4, 0);
					mHandler.sendMessage(msg);	
					return "���� ���� ����";					
				}
				else if(parse[1].equals("decode"))
				{
					Message msg = Message.obtain(mHandler, 0, 3, 0);
					mHandler.sendMessage(msg);	
					return "�������� MP3 ���ڵ� ����";
				}
				else if(parse[1].equals("result"))
				{
					
					noteresult = str;
					Log.d("BeatData", str);
					Message msg = Message.obtain(mHandler, 0, 5, 0);
					mHandler.sendMessage(msg);	
					
					//��������
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
					return "�Ľ� ����";
				}				
			}	
			return "�Ľ� ����";
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
	
}
