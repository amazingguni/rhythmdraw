package com.example.shapetest;

//import java.util.HashMap;

import android.util.SparseArray;

public class Score {

	private static final int SCORE_EXCELLENT = 4;
	private static final int SCORE_COOL = 3;
	private static final int SCORE_GOOD = 2;
	private static final int SCORE_BAD = 1;
	private static final int SCORE_OHMYGOD = 0;

	private static final int COMBO = 5;
	private static final int SCORE = 6;

	private int mLevel;
	private int mCombo;

	private SparseArray<Integer> mScoreBoard;

	public Score() {
		mScoreBoard = new SparseArray<Integer>();

		mLevel = 0;
		mCombo = 0;

		// 점수 기록 초기화
		mScoreBoard.put(SCORE_EXCELLENT, 0);
		mScoreBoard.put(SCORE_COOL, 0);
		mScoreBoard.put(SCORE_GOOD, 0);
		mScoreBoard.put(SCORE_BAD, 0);
		mScoreBoard.put(SCORE_OHMYGOD, 0);
		mScoreBoard.put(COMBO, 0);
		mScoreBoard.put(SCORE, 0);

	}

	// 점수 판단 자료 입력
	public void setSyncro(int syncro) {
		setScoreBoard(syncro);
	}

	// 점수 단계 표시
	private void setScoreBoard(int syncro) {

		if (syncro <= 100 && syncro > 80) {
			mLevel = SCORE_EXCELLENT;
			// EXCELLENT 기록 증가.
			mScoreBoard.put(SCORE_EXCELLENT,
					mScoreBoard.get(SCORE_EXCELLENT) + 1);
			mScoreBoard.put(SCORE, mScoreBoard.get(SCORE) + SCORE_EXCELLENT);
			
		} else if (syncro <= 80 && syncro > 60) {
			mLevel = SCORE_COOL;
			// COOL 기록 증가.
			mScoreBoard.put(SCORE_COOL, mScoreBoard.get(SCORE_COOL) + 1);
			mScoreBoard.put(SCORE, mScoreBoard.get(SCORE) + SCORE_COOL);
			
		} else if (syncro <= 60 && syncro > 40) {
			mLevel = SCORE_GOOD;
			// GOOD 기록 증가.
			mScoreBoard.put(SCORE_GOOD, mScoreBoard.get(SCORE_GOOD) + 1);
			mScoreBoard.put(SCORE, mScoreBoard.get(SCORE) + SCORE_GOOD);
			
		} else if (syncro <= 40 && syncro > 20) {
			mLevel = SCORE_BAD;
			// BAD 기록 증가.
			mScoreBoard.put(SCORE_BAD, mScoreBoard.get(SCORE_BAD) + 1);
			mScoreBoard.put(SCORE, mScoreBoard.get(SCORE) + SCORE_BAD);
			
		} else if (syncro <= 20 && syncro >= 0) {
			mLevel = SCORE_OHMYGOD;
			// OHMYGOD 기록 증가.
			mScoreBoard.put(SCORE_OHMYGOD, mScoreBoard.get(SCORE_OHMYGOD) + 1);
			mScoreBoard.put(SCORE, mScoreBoard.get(SCORE) + SCORE_OHMYGOD);
			
		}

		// 콤보 판단
		if (isCorrect()) {
			++mCombo; // 정답 처리, 콤보 증가
			if(mScoreBoard.get(COMBO) <= mCombo) {
				mScoreBoard.put(COMBO, mCombo); // 최대 콤보수를 넘었다면, 최대 콤보수 기록
			}
		} else {
			mCombo = 0; // 정답이 아니라면 현재 콤보 초기화.
		}

	}

	// 정답 판단
	private boolean isCorrect() {
		if (mLevel <= SCORE_EXCELLENT && mLevel >= SCORE_GOOD) {
			return true;
		} else {
			return false;
		}

	}

	// 점수 출력
	public int getScore() {
		return mScoreBoard.get(SCORE);
	}

	// 콤보 출력.
	public int getCombo() {
//		return mScoreBoard.get(COMBO);
		return mCombo;
	}

	// 판정 출력
	public int getVerdict() {
		return mLevel;
	}

	// 최종 결과 반환
	public SparseArray<Integer> getFinalResult() {
		return mScoreBoard;
	}
	
	public void clear() {
		mLevel = 0;
		mCombo = 0;
		mScoreBoard.get(SCORE_OHMYGOD);
		mScoreBoard.get(SCORE_BAD);
		mScoreBoard.get(SCORE_GOOD);
		mScoreBoard.get(SCORE_COOL);
		mScoreBoard.get(SCORE_EXCELLENT);
		mScoreBoard.get(COMBO);
		mScoreBoard.get(SCORE);
	}
}
