package com.codecamp.rhythmdraw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.view.SurfaceHolder;

public class RhythmItem {
	int x,y;
	private Bitmap mBitmap;
	private ByteArrayOutputStream baos;
	private Paint mPaint; 
	int alpha;
	private int img_idx;
	
	public RhythmItem(int pX, int pY, Bitmap pBitmap, int img_idx){
		this.x = pX;
		this.y = pY;
		this.mBitmap = pBitmap;
		this.alpha = 100;		
		this.img_idx = img_idx;
	}
	
	public byte[] getByteArray() {  
        baos = new ByteArrayOutputStream();  
        mBitmap.compress( CompressFormat.JPEG, 100, baos) ;  
        byte[] byteArray = baos.toByteArray() ;  
        return byteArray ;  
    }
	
	public Bitmap getBitmap(){
		return this.mBitmap;
	}
	
	public int getXpos(){
		return this.x;
	}
	
	public int getYpos(){
		return this.y;
	}

	public int getimg_idx(){
		return this.img_idx;
	}
	
	public void Destroy(){
		if(mBitmap != null){
			mBitmap.recycle();
			mBitmap = null;
		}
		if(baos != null){			
			try {
				baos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			baos = null;
		}
	}
	void Draw(Canvas canvas) {
		this.mPaint = new Paint();
		mPaint.setAntiAlias(true);
		//mPaint.setColor(Color.RED);
		//mPaint.setAlpha(alpha);	
		canvas.drawBitmap(mBitmap, x, y, mPaint);		
	}
	
}
