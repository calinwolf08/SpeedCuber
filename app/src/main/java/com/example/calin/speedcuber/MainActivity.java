package com.example.calin.speedcuber;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    TextView clockTextView, countDownTextView, firstPlace, firstPlaceName,
            secondPlace, secondPlaceName, thirdPlace, thirdPlaceName;
    RelativeLayout mainLayout;
    boolean running, countingDown;
    int inspectionTime;
    String cubeType, highScores[], highScoreNames[], playerName;

    final static String ZERO_TIME = "00:00:00";
    final static int DEFAULT_INSPECTION_TIME = 2;

    final static String INSPECTION_KEY = "InspectionTime";
    final static String CUBE_TYPE_KEY = "CubeType";
    final static String PLAYER_NAME_KEY = "PlayerName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        clockTextView = (TextView) findViewById(R.id.clock_text_view);
        clockTextView.setText(ZERO_TIME);

        countDownTextView = (TextView) findViewById(R.id.count_down_text_view);
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        firstPlace = (TextView) findViewById(R.id.first_place_time);
        firstPlaceName = (TextView) findViewById(R.id.first_player_name);
        secondPlace = (TextView) findViewById(R.id.second_place_time);
        secondPlaceName = (TextView) findViewById(R.id.second_player_name);
        thirdPlace = (TextView) findViewById(R.id.third_place_time);
        thirdPlaceName = (TextView) findViewById(R.id.third_player_name);

        running = countingDown = false;

        getPreferenceValues();
        fillHighScoreTable();
    }

    @Override
    protected void onPause() {

        //if setting set, keep timer going other wise stop it
        updatePreferences();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updatePreferences() {

        SharedPreferences prefs = this.getSharedPreferences("settingsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(INSPECTION_KEY, inspectionTime);
        editor.putString(CUBE_TYPE_KEY, cubeType);
        editor.putString(PLAYER_NAME_KEY, playerName);
        editor.putStringSet(cubeType + "Scores", new HashSet<String>(Arrays.asList(highScores)));
        editor.putStringSet(cubeType + "Names", new HashSet<String>(Arrays.asList(highScoreNames)));

        editor.commit();

    }

    private void fillHighScoreTable() {

        if (highScores[0] != null) {
            firstPlace.setText(highScores[0]);
            firstPlaceName.setText(highScoreNames[0]);
        }
        if (highScores[1] != null) {
            secondPlace.setText(highScores[1]);
            secondPlaceName.setText(highScoreNames[1]);
        }
        if (highScores[2] != null) {
            thirdPlace.setText(highScores[2]);
            thirdPlaceName.setText(highScoreNames[2]);
        }
    }

    public void getPreferenceValues() {
        SharedPreferences prefs = this.getSharedPreferences("settingsPrefs", Context.MODE_PRIVATE);

        inspectionTime = prefs.getInt(INSPECTION_KEY, DEFAULT_INSPECTION_TIME);
        cubeType = prefs.getString(CUBE_TYPE_KEY, "3x3");
        playerName = prefs.getString(PLAYER_NAME_KEY, "PLAYER 1");
        highScores = prefs.getStringSet(cubeType + "Scores",
                new HashSet<String>()).toArray(new String[3]);
        highScoreNames = prefs.getStringSet(cubeType + "Names",
                new HashSet<String>()).toArray(new String[3]);
    }

    public void ScreenHit(View view) {

        if (!running) {

            running = true;

            int orientation = getResources().getConfiguration().orientation;

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            if (inspectionTime > 0) {
                startCountDown();
            }else {
                startStopWatch();
            }

        } else if (countingDown) {

            countingDown = false;

        } else if (running){

            running = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

        }

    }

    private void startCountDown() {

        clockTextView.setVisibility(View.INVISIBLE);
        countDownTextView.setVisibility(View.VISIBLE);
        countingDown = true;

        new Thread() {
            @Override
            public void run() {

                int timeLeft = inspectionTime;

                while(countingDown) {

                    final String cur = String.valueOf(timeLeft);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //update time
                            countDownTextView.setText(cur);
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (--timeLeft <= 0) {
                        countingDown = false;
                    }

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownTextView.setVisibility(View.GONE);
                        clockTextView.setVisibility(View.VISIBLE);
                        startStopWatch();
                    }
                });

            }
        }.start();
    }

    private void startStopWatch() {

        new Thread() {

            long startTime, curTime;

            @Override
            public void run() {

                startTime = System.currentTimeMillis();

                while(running) {

                    curTime = System.currentTimeMillis() - startTime;

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            updateTime(curTime);
                        }
                    });
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    public void updateTime(Long curTime) {

        String vals[] = {String.valueOf(curTime / 60000), //min
                String.valueOf((curTime / 1000) % 60), //sec
                String.valueOf((curTime / 10) % 100)}; //milli

        for(int i=0; i<3; i++) {
            if(Integer.parseInt(vals[i]) < 10)
                vals[i] = "0" + vals[i];
        }

        clockTextView.setText(vals[0] + ":" + vals[1] + ":" + vals[2]);
    }
}
