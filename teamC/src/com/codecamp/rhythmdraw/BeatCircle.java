package com.codecamp.rhythmdraw;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/*
 * ��Ʈ ����� canvas���� �׸��� ��
 */

class BeatCircle {
	//int time;
	int x, y; int rad;	
	int color;	int alpha;	 
	    	
	int stype;
	
	// ���ο� �� ����
	static BeatCircle Create(float beatsize, int speed, int type) {
		int[] posX = new int[5]; //�ӽ� ��ġ
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
		Random Rnd = new Random(); //�����ѻ���
		newBeat.color = color[Rnd.nextInt(5)];
		return newBeat;
	}
	// �׸���
	void Draw(Canvas canvas) {
		Paint pnt = new Paint();
		pnt.setAntiAlias(true);
		pnt.setColor(color);
		pnt.setAlpha(alpha);	
		canvas.drawCircle(x, y, rad, pnt);		
	}
}