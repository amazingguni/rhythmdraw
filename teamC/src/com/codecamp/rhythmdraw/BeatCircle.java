package com.codecamp.rhythmdraw;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/*
 * 비트 검출시 canvas에서 그리는 원
 */

class BeatCircle {
	//int time;
	int x, y; int rad;	
	int color;	int alpha;	 
	    	
	int stype;
	
	// 새로운 볼 생성
	static BeatCircle Create(float beatsize, int speed, int type) {
		int[] posX = new int[5]; //임시 위치
		posX[0]=100;
		posX[1]=posX[0]+160+50;
		posX[2]=posX[1]+160+50;
		posX[3]=posX[2]+160+50;
		posX[4]=posX[3]+160+50;		
		BeatCircle newBeat = new BeatCircle();
		newBeat.x = posX[type];
		newBeat.rad = 80;
		newBeat.y = 600/2;		
		newBeat.alpha = 100;
		newBeat.stype = type;
		int[] color = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA};
		Random Rnd = new Random(); //랜덤한색상
		newBeat.color = color[Rnd.nextInt(5)];
		return newBeat;
	}
	// 그리기
	void Draw(Canvas canvas) {
		Paint pnt = new Paint();
		pnt.setAntiAlias(true);
		pnt.setColor(color);
		pnt.setAlpha(alpha);	
		canvas.drawCircle(x, y, rad, pnt);		
	}
}