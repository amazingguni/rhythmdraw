package com.codecamp.rhythmdraw;

public class Beat {
	private float str;
	private int time;
	public Beat()
	{
		str = 0;
		time = 0; //ms
	}	
	public void setTime(float t)
	{
		float tmp = ((t-1)*256+1024)/44100*1000-200;
		time = (int)Math.floor(tmp);
	}
	public void setStr(float s)
	{
		str = s;
	}
	public void setMSTime(int t)
	{
		time = t;
	}	
	public int getTime()
	{
		return time;
	}
	public float getStr()
	{
		return str;
	}
}
