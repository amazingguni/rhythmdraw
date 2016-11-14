package com.codecamp.rhythmdraw;

public class RhythmGameData {
	
	private String[] musicName = {"gangnamstyle.mp3","deadmau5.mp3","primary.mp3"};
	private int[] albumImageId = {R.drawable.gangnamstyle_album_img,R.drawable.professional_album_img,R.drawable.primarymessengers_album_img,R.drawable.no_album_image};
	private double[] musicLevelArray = {3.5,4,3,0};
	private String[] musicInfoArray = {"가수 : 싸이(PSY)\n곡명 : 강남스타일\n장르 : 일렉힙합","가수 : Deadmau5\n곡명 : Professional Griefers\n장르 : 소울","가수 : 씨쓰루\n곡명 : Primary Messengers\n장르 : 힙합","곡명의 정보가 없습니다."};
	private int[][] backgroundImageId = 
		{
			{R.drawable.gangnamstyle_first_layer0,R.drawable.gangnamstyle_first_layer1,R.drawable.gangnamstyle_first_layer2,R.drawable.gangnamstyle_first_layer3,R.drawable.gangnamstyle_first_layer4,R.drawable.gangnamstyle_first_layer5},
			{R.drawable.professional_bg0,R.drawable.professional_bg1,R.drawable.professional_bg2,R.drawable.professional_bg3,R.drawable.professional_bg4},
			{R.drawable.messengers_bg0,R.drawable.messengers_bg1,R.drawable.messengers_bg2,R.drawable.messengers_bg3,R.drawable.messengers_bg4,R.drawable.messengers_bg5},
			{R.drawable.etc_first_layer}
		};
	private int[] resultImageId = {R.drawable.gangnamstyle_result_img,R.drawable.deadmau5_result_img,R.drawable.primary_result_img,R.drawable.result_no_img};
	private int nowBgId = 0;
	
	public RhythmGameData(){
		nowBgId = 0;
	}
	
	public int getAlbumImageId(String _musicName){
		int musicIndex = getMusicIndex(_musicName);
		return albumImageId[musicIndex];
	}
	
	public String getMusicInfo(String _musicName){
		int musicIndex = getMusicIndex(_musicName);
		return musicInfoArray[musicIndex];
	}
	
	public int getBgId(String _musicName){
		int musicIndex = getMusicIndex(_musicName);
		int nowMusicBgEa = backgroundImageId[musicIndex].length;
		if(nowBgId+1==nowMusicBgEa){
			nowBgId = 0;
		}else{
			nowBgId++;
		}
		return backgroundImageId[musicIndex][nowBgId];
	}
	
	public int getResultImageId(String _musicName){
		int musicIndex = getMusicIndex(_musicName);
		return resultImageId[musicIndex];
	}
	
	private int getMusicIndex(String _musicName){
		for(int i=0; i<musicName.length; i++){
			if(musicName[i]==_musicName){
				return i;
			}
		}
		return 3;
	}
}
