package com.example.maritaholm.myairhockitygame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* The Game activity controls the game itself and handles user inputs
 */

public class Game extends Activity implements View.OnTouchListener {

    private ViewGroup mFrame;
    private Bitmap player1Bitmap;
    private Bitmap player2Bitmap;
    private Bitmap puckBitmap;
    private Player player1;
    private Player player2;
    private Field mField;
    private Puck puck;
    private static final int REFRESH_RATE = 40;
    private int playerRadius;
    private int puckRadius;
    //Game settings
    private int pointsToWin;
    private String friction;
    private int round = 1;
    // Creates a WorkerThread
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    public boolean isSoundEnabled;
    //True for best out of 3, otherwise false
    private Boolean bestOutOf3;
    private Player[] players;
    private SharedPreferences prefs = null;
    int width;
    int height;
    private boolean pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up view
        setContentView(R.layout.activity_game);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        mFrame = (ViewGroup) findViewById(R.id.frame);
        mFrame.setOnTouchListener(this);

        //Get settings
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Uses default if key is null
        pointsToWin = prefs.getInt("points", 3);
        friction = prefs.getString("friction", "some");
        bestOutOf3 = prefs.getBoolean("bestOutOf3", false);
        isSoundEnabled = prefs.getBoolean("sound",false);

        //Set up bitmaps to display players
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        player1Bitmap = BitmapFactory.decodeResource(getResources(), prefs.getInt("player1",R.drawable.orange_player), opts);
        player2Bitmap = BitmapFactory.decodeResource(getResources(), prefs.getInt("player2",R.drawable.blue_player), opts);

        mField = new Field(getApplicationContext(),mFrame, player1Bitmap, player2Bitmap);
        mFrame.addView(mField);

        players = new Player[2];

        playerRadius = width/10;
        puckRadius = width/30;

        //Player 1
        player1 = new Player("player1",getApplicationContext(), width/2 - playerRadius, playerRadius, player1Bitmap, playerRadius);
        players[0]=player1;
        mFrame.addView(player1);

        //Player 2
        player2 = new Player("player2", getApplicationContext(), width/2 - playerRadius,height - 3 * playerRadius, player2Bitmap, playerRadius);
        players[1]=player2;
        mFrame.addView(player2);

        //The puck
        puckBitmap = BitmapFactory.decodeResource(getResources(), prefs.getInt("puck",R.drawable.grey_puck));
        puck = new Puck(getBaseContext(), (float) width / 2 - puckRadius /2,
                (float) height / 2 - 2* puckRadius, puckBitmap, mFrame,this.friction, puckRadius);
        mFrame.addView(puck);
        start();

    }
    @Override
    protected void onResume() {
        super.onResume();
        //If game is paused show the pause dialog
        if (pause) {
            createPauseDialog().show();
        }
    }

    public void start() {
        //Set up audio
        final MediaPlayer playSoundOnGoal = MediaPlayer.create(getApplicationContext(),R.raw.ongoal);
        final MediaPlayer playSoundOnWin = MediaPlayer.create(getApplicationContext(),R.raw.cheer);


        executor.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                //if game is paused return
                if (pause) {
                    return;
                }
                //Moving the puck
                puck.move(REFRESH_RATE);
                puck.decelerate();
                puck.postInvalidate();


                //Logic for incrementing goal score and determining game winner
                if (puck.topGoal()) {
                    if (isSoundEnabled) {
                        vibrateOnGoal();
                        playSoundOnGoal.start();
                    }
                    mField.setScoreBot(mField.getScoreBot() + 1);
                    resetPuck();
                }
                if (puck.botGoal()) {
                    if (isSoundEnabled) {
                        vibrateOnGoal();
                        playSoundOnGoal.start();
                    }
                    mField.setScoreTop(mField.getScoreTop() + 1);
                    resetPuck();
                }
                if (mField.getScoreBot() == pointsToWin) {
                    if (isSoundEnabled) {
                        playSoundOnWin.start();
                    }

                    mField.setBotWins(mField.getBotWins() + 1);
                    if (bestOutOf3 && !(mField.getBotWins() >= 2)) {
                        mField.drawRoundWinner("bot", round);
                        round++;
                        mField.resetScore();
                        resetPuck();
                        /*if (mField.getBotWins() == 2) {
                            showWinnerDialog("Bottom");
                            executor.shutdown();

                        }*/
                    } else {
                        showWinnerDialog("Bottom");
                        executor.shutdown();
                    }
                }
                if (mField.getScoreTop() == pointsToWin) {
                    if (isSoundEnabled) {
                        playSoundOnWin.start();
                    }
                    mField.setTopWins(mField.getTopWins() + 1);
                    if (bestOutOf3 && !(mField.getTopWins() == 2)) {
                        mField.drawRoundWinner("top", round);
                        round++;
                        mField.resetScore();
                        resetPuck();
                        /*if (mField.getTopWins() == 2) {
                            showWinnerDialog("Top");
                            executor.shutdown();
                        }*/
                    } else {
                        showWinnerDialog("Top");
                        executor.shutdown();
                    }
                }
            }
        }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
    }
    private void vibrateOnGoal(){
        Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    //Check if any players intersect a given (x,y) position
    private boolean intersects(float x, float y) {
        for (Player p : players) {
            if (p.intersects(x, y)) {
                return true;
            }
        }
        return false;
    }

    //Gets the player that is that the given (x,y) coordinate (if any)
    private Player getPlayerAt(float x, float y) {
        for (Player p : players) {
            if (p.intersects(x, y)) {
                return p;
            }
        }
        return null;
    }

    //Resets puck position
    private void resetPuck() {
        puck.resetVelocity();
        puck.setX(width / 2 - puckRadius / 2);
        puck.setY(height / 2 - 2 * puckRadius);
        mFrame.postInvalidate();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Cycles through all players
        for (int i = 0; i < event.getPointerCount(); i++) {
            Player p = getPlayerAt(event.getX(i), event.getY(i));

            if (p != null) {
                float x = event.getX(i);
                float y = event.getY(i);
                //The switch statement makes sure that the players can't move across the center line
                switch (p.getName()) {
                    case "player1":
                        if (y >= (mFrame.getHeight() / 2) - p.getRadius() ) {

                            p.moveTo(x, (float) ((mFrame.getHeight() / 2)-p.getRadius()));
                        }
                        else {
                            p.moveTo(x, y);
                        }
                        break;
                    case "player2":
                        if (y <= (mFrame.getHeight() / 2)+p.getRadius()) {

                            p.moveTo(x, (float) ((mFrame.getHeight() / 2)+p.getRadius()));
                        }
                        else {
                            p.moveTo(x, y);
                        }
                        break;
                    default:
                        break;
                }

                //Increases the velocity of the puck by the velocity of the player
                //when it touches the puck
                VelocityTracker tracker = VelocityTracker.obtain();
                tracker.addMovement(event);
                tracker.computeCurrentVelocity(500);
                if (p.intersects(puck)) {
                    float xVel = VelocityTrackerCompat.getXVelocity(tracker, event.getPointerId(i));
                    float yVel = VelocityTrackerCompat.getYVelocity(tracker, event.getPointerId(i));

                    puck.IncreaseVelocity(xVel, yVel);
                    return true;
                }
            }
        }
        return true;
    }

    //Creates a dialog if the back button is pressed
    @Override
    public void onBackPressed() {
        createPauseDialog().show();
    }

    protected Dialog createPauseDialog() {
        //Sets up the two button titles
        CharSequence[] choices = new CharSequence[2];
        choices[0] = "Resume";
        choices[1] = "Main Menu";

        //Pauses the game
        pause = true;

        //Set up dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Game.this);
        builder.setTitle("PAUSED")
                .setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {
                            finish();
                        } else if (which == 0) {
                            pause = false;
                        }
                    }
                });
        //Resumes the game if the user presses outside of dialog box
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                pause = false;
            }
        });
        return builder.create();
    }

    @Override
    public void onPause() {
        super.onPause();
        pause = true;
    }

    //Show winner dialog
    private void showWinnerDialog(String winner){
        DialogFragment mWinnerDialog = WinnerDialog.newInstance(winner);
        mWinnerDialog.show(getFragmentManager(), "dialog");
    }
}
