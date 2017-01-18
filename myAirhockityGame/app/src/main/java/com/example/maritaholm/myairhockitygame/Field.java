package com.example.maritaholm.myairhockitygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Responsible for drawing the edges of the field, the goal, the line in the middle, the score of
 * the current game and the number of wins in a best out of 3 game
 */
public class Field extends View {
    private View mFrame;
    private Paint mPaint;
    private int scoreTop = 0;
    private int scoreBot = 0;
    private int topWins = 0;
    private int botWins = 0;
    private Bitmap player1;
    private Bitmap player2;
    private Bitmap[] winners = new Bitmap[3];

    public Field(Context context, View mFrame, Bitmap player1, Bitmap player2) {
        super(context);
        mPaint = new Paint();
        this.mFrame = mFrame;
        //Used to draw the amount of game wins in a best out of 3
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        mPaint.setColor(Color.DKGRAY);
        //Right side of field
        canvas.drawRect(mFrame.getLeft(), mFrame.getTop(), mFrame.getLeft() + 10, mFrame.getBottom(), mPaint);

        //Left side of field
        canvas.drawRect(mFrame.getRight() - 10, mFrame.getTop(), mFrame.getRight(), mFrame.getBottom(), mPaint);

        //Top part of field
        canvas.drawRect(mFrame.getLeft(), mFrame.getTop(), (mFrame.getRight()/2)-100, mFrame.getTop()+10, mPaint);
        canvas.drawRect((mFrame.getRight() / 2) + 100, mFrame.getTop(), mFrame.getRight(), mFrame.getTop() + 10, mPaint);

        //Bottom side of field
        canvas.drawRect(mFrame.getLeft(), mFrame.getBottom() - 10, (mFrame.getRight() / 2) - 100, mFrame.getBottom(), mPaint);
        canvas.drawRect((mFrame.getRight() / 2) + 100, mFrame.getBottom() - 10, mFrame.getRight(), mFrame.getBottom(), mPaint);

        //Center line
        mPaint.setColor(Color.GRAY);
        canvas.drawRect(mFrame.getLeft() + 10, (mFrame.getBottom() / 2) - 2, (mFrame.getRight() / 2) - 150, (mFrame.getBottom() / 2) + 2, mPaint);
        canvas.drawRect((mFrame.getRight() / 2) + 150, (mFrame.getBottom() / 2) - 2, (mFrame.getRight() - 10), (mFrame.getBottom() / 2) + 2, mPaint);

        //Score for bottom side
        mPaint.setTextSize(50);
        canvas.drawText(Integer.toString(scoreBot), mFrame.getRight() / 2, (mFrame.getBottom() / 2) + 100, mPaint);

        //Score for top side
        canvas.rotate(180,mFrame.getRight() / 2, (mFrame.getBottom() / 2)-100);
        canvas.drawText(Integer.toString(scoreTop), ((mFrame.getRight() / 2) - 25), (mFrame.getBottom() / 2) - 100, mPaint);

        //Undoing previous rotation
        canvas.rotate(180, mFrame.getRight() / 2, (mFrame.getBottom() / 2) - 100);

        //Draw amount of wins between each player in best out of 3
        for(int i = 0; i < 3;i++){
            if(winners[i]!=null){
                if(i == 0){
                    canvas.drawBitmap(winners[0], (mFrame.getRight()/2) - 50, (mFrame.getBottom()/2)- 25/2, mPaint);
                } else if (i == 1) {
                    canvas.drawBitmap(winners[1],(mFrame.getRight()/2),(mFrame.getBottom()/2)- 25/2,mPaint);

                } else if (i == 2) {
                    canvas.drawBitmap(winners[2],(mFrame.getRight()/2) +  50,(mFrame.getBottom()/2)- 25/2,mPaint);

                }
            }
        }

    }

    protected void setScoreTop(int s){
        this.scoreTop=s;
        this.postInvalidate();
    }

    protected void setScoreBot(int s){
        this.scoreBot=s;
        this.postInvalidate();
    }

    protected int getScoreTop(){
        return this.scoreTop;
    }

    protected int getScoreBot(){
        return this.scoreBot;
    }

    //Sets the bitmap corresponding to the given winner
    protected void drawRoundWinner(String winner, int round){
        if(winner.equals("top")){
            winners[round-1] = Bitmap.createScaledBitmap(player1,25,25,false);
        } else {
            winners[round-1] = Bitmap.createScaledBitmap(player2,25,25,false);
        }
        postInvalidate();

    }

    protected void resetScore(){
        this.scoreBot = 0;
        this.scoreTop = 0;

    }

    protected int getTopWins() {
        return topWins;
    }

    protected void setTopWins(int topWins) {
        this.topWins = topWins;
    }

    protected int getBotWins() {
        return botWins;
    }

    protected void setBotWins(int botWins) {
        this.botWins = botWins;
    }
}
