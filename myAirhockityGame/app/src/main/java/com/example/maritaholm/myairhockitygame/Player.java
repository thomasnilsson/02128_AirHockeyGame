package com.example.maritaholm.myairhockitygame;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.graphics.Bitmap;


public class Player extends View {
    private final Paint mPainter = new Paint();
    private float xPos;
    private float yPos;
    private int radius;
    private String name;
    Bitmap mScaledBitmap;


    public Player(String name, Context context, float x, float y, Bitmap bitmap,int radius) {
        super(context);
        this.name = name;
        this.xPos = x;
        this.yPos = y;
        this.radius = radius;
        this.mScaledBitmap = Bitmap.createScaledBitmap(bitmap,  2 * radius, 2 * radius, false);
    }

    //Draws the player with the given bitmap
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.drawBitmap(mScaledBitmap, xPos, yPos, mPainter);

    }

    //Checks if the player is at the (x,y) position
    protected boolean intersects(float x, float y) {
        return (Math.abs(x - (xPos + radius)) <= radius && Math.abs(y - (yPos + radius)) <= radius);
    }

    //Checks if the player intersects a given puck
    protected boolean intersects(Puck puck) {
        return (distanceTo(puck) <= radius+puck.getRadius() );

    }

    //Moves the pucks centre to the position (x,y)
    protected void moveTo(float x, float y) {
        xPos = x - radius;
        yPos = y - radius;
        postInvalidate();
    }


    public double getRadius() {
        return radius;
    }

    //Determines distance to given puck
    private double distanceTo(Puck puck) {
        return (Math.sqrt(Math.pow(Math.abs((puck.getX()+puck.getRadius())- (xPos + radius)), 2)+
                Math.pow(Math.abs((puck.getY()+puck.getRadius())-(yPos+radius)),2)));
    }

    public String getName() {
        return this.name;
    }


}
