package com.example.game2048;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameView extends GridLayout {

	//记录卡片当期状态
	private Card[][] cardsMap = new Card[4][4];
	//记录卡片上次状态，以供撤销使用
	private int[][] lastCardsMap = new int[4][4];

	// 使用点类记录可以产生随机卡片的位置，实际上就是同时记录x,y
	private List<Point> emptyPoints = new ArrayList<Point>();
	//记录每张卡片宽度的值
	int cardWidth = -1;
	
	//记录难易程度
	int level = 0;
	
	//记录分数
	int score = 0;
	int backScore = 0;
	
	//记录滑动时是否有移动或合并，有才增加数字
	boolean haveMove; 
	
	//获取主面Activity引用
	MainActivity mainActivity = MainActivity.getInstance();

	//返回上一步的按钮
	Button btnBack;
	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGameView();
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGameView();
	}

	public GameView(Context context) {
		super(context);
		btnBack = (Button) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getContext(), "默认Toast样式",
						Toast.LENGTH_SHORT).show();
				Log.i("GameView","btn clicked");
				backLastView();

			}
		});
		initGameView();
	}

	private void initGameView() {
		setColumnCount(4);
		setBackgroundColor(0xffbbada0);
		setOnTouchListener(new View.OnTouchListener() {

			// 起始点、终止点、x上偏移量、y上偏移量
			private float startX, startY, offsetX, offsetY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				// 事件开始的时候存储游戏状态
				save();
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						startX = event.getX();
						startY = event.getY();
						break;
					case MotionEvent.ACTION_UP:
						offsetX = event.getX() - startX;
						offsetY = event.getY() - startY;

						if (Math.abs(offsetX) > Math.abs(offsetY)) {
							// 水平方向上
							if (offsetX < -5) {
								System.out.println("left");
								swipeLeft();
							} else if (offsetX > 5) {
								swipeRight();
							}
						} else {
							// 垂直方向
							if (offsetY < -5) {
								swipeUp();
							} else if (offsetY > 5) {
								swipeDown();
							}
						}

						break;

				}
				return true;
			}

		});
	}

	// 左右上下逻辑处理
	private void swipeLeft() {
		haveMove = false;
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {

				// 从当前位置向右遍历
				for (int x1 = x + 1; x1 < 4; x1++) {
					if (cardsMap[x1][y].getNum() > 0) {
						// 如果当前值为空，后面有值
						if (cardsMap[x][y].getNum() <= 0) {
							cardsMap[x][y].setNum(cardsMap[x1][y].getNum());
							cardsMap[x1][y].setNum(0);
							haveMove = true;
							// 如果当前为空，后面有两个以上相同的值
							x--;

						}// 当前不为空，并两张卡片值相同,则合并
						else if (cardsMap[x][y].equals(cardsMap[x1][y])) {
							countScore(cardsMap[x][y].getNum());
							cardsMap[x][y].setNum(cardsMap[x][y].getNum() * 2);
							cardsMap[x1][y].setNum(0);	
							haveMove = true;
							successGame(cardsMap[x][y].getNum());
						}
						break;
					}
				}
			}

		}
		if (haveMove){
			if (level == 0){
				addRandomNum();
			}else if(level == 1){
				addRandomNum();
				addRandomNum();
			}else if (level == 2){				
				addRandomNum();
				addRandomNum();
				addRandomNum();
			}

		}
		overGame();
	}

	private void swipeRight() {
		haveMove = false;
		for (int y = 0; y < 4; y++) {
			for (int x = 3; x >= 0; x--) {

				// 从当前位置向右遍历
				for (int x1 = x - 1; x1 >= 0; x1--) {
					if (cardsMap[x1][y].getNum() > 0) {
						// 如果当前值为空，后面有值
						if (cardsMap[x][y].getNum() <= 0) {
							cardsMap[x][y].setNum(cardsMap[x1][y].getNum());
							cardsMap[x1][y].setNum(0);
							haveMove = true;

							// 如果当前为空，后面有两个以上相同的值
							x++;

						}// 当前不为空，并两张卡片值相同,则合并
						else if (cardsMap[x][y].equals(cardsMap[x1][y])) {
							countScore(cardsMap[x][y].getNum());
							cardsMap[x][y].setNum(cardsMap[x][y].getNum() * 2);
							cardsMap[x1][y].setNum(0);
							haveMove = true;
							successGame(cardsMap[x][y].getNum());
						}
						break;
					}
				}
			}

		}
		if (haveMove){
			if (level == 0){
				addRandomNum();
			}else if(level == 1){
				addRandomNum();
				addRandomNum();
			}else if (level == 2){				
				addRandomNum();
				addRandomNum();
				addRandomNum();
			}
		}
		overGame();
	}

	private void swipeUp() {
		haveMove = false;
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {

				// 从当前位置向右遍历
				for (int y1 = y + 1; y1 < 4; y1++) {
					if (cardsMap[x][y1].getNum() > 0) {
						// 如果当前值为空，后面有值
						if (cardsMap[x][y].getNum() <= 0) {
							cardsMap[x][y].setNum(cardsMap[x][y1].getNum());
							cardsMap[x][y1].setNum(0);
							haveMove = true;

							// 如果当前为空，后面有两个以上相同的值
							y--;

						}// 当前不为空，并两张卡片值相同,则合并
						else if (cardsMap[x][y].equals(cardsMap[x][y1])) {
							countScore(cardsMap[x][y].getNum());
							cardsMap[x][y].setNum(cardsMap[x][y].getNum() * 2);
							cardsMap[x][y1].setNum(0);
							haveMove = true;
							successGame(cardsMap[x][y].getNum());
						}
						break;
					}
				}
			}

		}
		if (haveMove){
			if (level == 0){
				addRandomNum();
			}else if(level == 1){
				addRandomNum();
				addRandomNum();
			}else if (level == 2){				
				addRandomNum();
				addRandomNum();
				addRandomNum();
			}
		}		
		overGame();
	}

	private void swipeDown() {
		haveMove = false;
		for (int x = 0; x < 4; x++) {
			for (int y = 3; y > 0; y--) {

				// 从当前位置向上遍历
				for (int y1 = y - 1; y1 >= 0; y1--) {
					if (cardsMap[x][y1].getNum() > 0) {
						// 如果当前值为空，后面有值
						if (cardsMap[x][y].getNum() <= 0) {
							cardsMap[x][y].setNum(cardsMap[x][y1].getNum());
							cardsMap[x][y1].setNum(0);
							haveMove = true;

							// 如果当前为空，后面有两个以上相同的值
							y++;

						}// 当前不为空，并两张卡片值相同,则合并
						else if (cardsMap[x][y].equals(cardsMap[x][y1])) {
							countScore(cardsMap[x][y].getNum());
							cardsMap[x][y].setNum(cardsMap[x][y].getNum() * 2);
							cardsMap[x][y1].setNum(0);
							haveMove = true;
							successGame(cardsMap[x][y].getNum());
						}
						break;
					}
				}
			}

		}
		if (haveMove){
			if (level == 0){
				addRandomNum();
			}else if(level == 1){
				addRandomNum();
				addRandomNum();
			}else if (level == 2){				
				addRandomNum();
				addRandomNum();
				addRandomNum();
			}
		}
		overGame();
	}

	// 给没有数字的card上产生随机数并加入
	private void addRandomNum() {
		emptyPoints.clear();
		// 将可以产生随机数的区域用list存储
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				if (cardsMap[x][y].getNum() <= 0) {
					emptyPoints.add(new Point(x, y));
				}
			}
		}
		// 随机取出一个点
		if (!emptyPoints.isEmpty()){
			Point p = emptyPoints
					.remove((int) (Math.random() * emptyPoints.size()));
			cardsMap[p.x][p.y].setNum(Math.random() > 0.1 ? 2 : 4);
			cardsMap[p.x][p.y].animationCardBorn();
		}
	}
	
	// 存储游戏当前状态
	private void save() {
//		lastCardsMap = cardsMap.clone();这种方式只能实现数组的浅复制，存储的是指针，打印出来一样但会同时被更改
		for(int i = 0;i < 4;i ++) {
			for(int j = 0; j < 4 ; j ++) {
				lastCardsMap[i][j] = cardsMap[i][j].getNum();
			}
		}
	}
	//返回上次布局
	public void backLastView() {
		//判断是否为游戏刚开始
		boolean noBeginGame = false;
		for (int i = 0 ; i < 4 ; i++){
			for (int j = 0 ; j < 4 ; j++){
				if (lastCardsMap[i][j] > 0){
					noBeginGame = true;
				}
			}
		}
		
		if (noBeginGame){
			//判断是否已撤销过，只能撤销一次
			boolean backOnce = false;
			for (int i = 0 ; i < 4 ; i++){
				for (int j = 0 ; j < 4 ; j++){
					if (cardsMap[i][j].getNum() != lastCardsMap[i][j]){
						backOnce = true;
					}
				}
			}
			
			if (backOnce){
				Toast.makeText(getContext(), "撤销",
						Toast.LENGTH_SHORT).show();
				for (int i = 0;i < 4;i ++) {
					for(int j = 0;j < 4; j++) {
						cardsMap[i][j].setNum(lastCardsMap[i][j]);
					}
				}
				//设置分数
				mainActivity.tvScore.setText(backScore + "");
				score = backScore;
			}else{
				Toast.makeText(getContext(), "只能撤销一次",
						Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(getContext(), "游戏刚开始，无法撤销",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	//计分
	private void countScore(int cardNum) {
		backScore = score;
		score += cardNum * 10;
		mainActivity.tvScore.setText(score + "");
	}
	
	//判断是否已经结束游戏
	private void overGame(){
		//记录是否还有空白块
		boolean noEmptyPoint = true;
		//记录水平方向上是否还可以合并
		boolean horizontal = true;
		//记录垂直方向上是否还可以合并
		boolean vertical = true;
		
		//判断是否还有空白块
		for (int y = 0 ; y < 4 ; y++){
			for (int x = 0 ; x < 4 ; x++){
				if (cardsMap[x][y].getNum() <= 0){
					noEmptyPoint = false;
				}
			}
		}	
		
		//判断水平方向上是否还可以合并
		if (noEmptyPoint){
			for (int y = 0 ; y < 4 ; y++){
				for (int x = 0; x < 3 ; x++){
					if(cardsMap[x][y].equals(cardsMap[x+1][y])){
						horizontal = false;
					}
				}
			}
			
			if(horizontal){
				for ( int x = 0 ; x < 4 ; x++){
					for (int y = 0 ; y < 3 ; y++){
						if (cardsMap[x][y].equals(cardsMap[x][y+1])){
							vertical = false;
						}
					}
				}
			}
		}
		
		if (noEmptyPoint && horizontal && vertical){
			AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
			builder.setMessage("游戏结束");
			builder.setPositiveButton("取消", null);
			builder.setNegativeButton("再玩一局", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					againGame();
				}
			});
			builder.show();
		}
		
	}
	
	
	//判断是否成功玩到2048
	private void successGame(int cardNum){
		if (cardNum == 2048){
			Toast.makeText(getContext(), "恭喜过关，成功到达2048",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	//再玩一局
	public void againGame(){
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				cardsMap[x][y].setNum(0);
				lastCardsMap[x][y] = 0;
			}
		}
		score = 0;
		backScore = 0;
		mainActivity.tvScore.setText(0 + "");
		addRandomNum();
		addRandomNum();
	}
	
	//难易程度
	public void selectLevel(){
		final String[] items = new String[] { "简单 ", "普通", "困难"};
		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setTitle("请选择难易程度").setItems(items,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int which) {
						if (items[which] != "null"){
							level = which;
						}
					}
				});
		AlertDialog ad = builder.create();
		ad.show();
	}
	
	@Override
	// 当屏幕分辨率发生改变的时候执行
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 初始化每张卡片的宽
		cardWidth = (Math.min(w, h) - 10) / 4;
		Card c;
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				c = new Card(getContext());
				// 初始化全部卡片，并用数组记录
				c.setNum(0);
				addView(c, cardWidth, cardWidth);

				cardsMap[x][y] = c;
			}

		}

		addRandomNum();
		addRandomNum();
	}
	
}
