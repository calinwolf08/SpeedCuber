package com.example.calin.speedcuber;

import android.app.AlertDialog;
import android.app.DialogFragment;
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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements SettingsDialog.SettingsCommunicator,
        UpdateScoreDialog.AlertCommunicator, DeleteScoresDialog.DeleteCommunicator{

    TextView clockTextView, countDownTextView, firstPlace, firstPlaceName,
            secondPlace, secondPlaceName, thirdPlace, thirdPlaceName, tableTitle;

    TableLayout highScoreTable;

    boolean running, countingDown;
    int index;

    int inspectionTime;
    String cubeType, highScores[][], playerName;

    final static String ZERO_TIME = "00:00:00";
    final static int DEFAULT_INSPECTION_TIME = 5;

    final static String INSPECTION_KEY = "InspectionTime";
    final static String CUBE_TYPE_KEY = "CubeType";
    final static String PLAYER_NAME_KEY = "PlayerName";

    final static String FILENAME = "HighScores";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        clockTextView = (TextView) findViewById(R.id.clock_text_view);
        clockTextView.setText(ZERO_TIME);

        highScoreTable = (TableLayout) findViewById(R.id.high_score_table);
        countDownTextView = (TextView) findViewById(R.id.count_down_text_view);
        firstPlace = (TextView) findViewById(R.id.first_place_time);
        firstPlaceName = (TextView) findViewById(R.id.first_player_name);
        secondPlace = (TextView) findViewById(R.id.second_place_time);
        secondPlaceName = (TextView) findViewById(R.id.second_player_name);
        thirdPlace = (TextView) findViewById(R.id.third_place_time);
        thirdPlaceName = (TextView) findViewById(R.id.third_player_name);
        tableTitle = (TextView) findViewById(R.id.table_title_text_view);

        running = countingDown = false;

        getPreferenceValues();
        getHighScoreValues();
        fillHighScoreTable();
    }

    @Override
    protected void onPause() {

        //if setting set, keep timer going other wise stop it
        updatePreferences();
        saveHighScores();
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

            if (running) {

                running = false;

            }

            DialogFragment settingsDialog = new SettingsDialog();

            settingsDialog.show(getFragmentManager(), "settingsDialog"); //tag

            return true;
        }

        if (id == R.id.action_delete) {

            if (running)
                running = false;

            DialogFragment deleteFragment = new DeleteScoresDialog();

            deleteFragment.show(getFragmentManager(), "deleteDialog");

            return true;

        }

        return super.onOptionsItemSelected(item);
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
        highScoreTable.setVisibility(View.INVISIBLE);
        tableTitle.setVisibility(View.INVISIBLE);
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
                        highScoreTable.setVisibility(View.VISIBLE);
                        tableTitle.setVisibility(View.VISIBLE);
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

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        checkHighScores();
                    }
                });

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

    private void updatePreferences() {

        SharedPreferences prefs = this.getSharedPreferences("settingsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(INSPECTION_KEY, inspectionTime);
        editor.putString(CUBE_TYPE_KEY, cubeType);
        editor.putString(PLAYER_NAME_KEY, playerName);

        editor.commit();

    }

    public void getPreferenceValues() {
        SharedPreferences prefs = this.getSharedPreferences("settingsPrefs", Context.MODE_PRIVATE);

        inspectionTime = prefs.getInt(INSPECTION_KEY, DEFAULT_INSPECTION_TIME);
        cubeType = prefs.getString(CUBE_TYPE_KEY, "3x3");
        playerName = prefs.getString(PLAYER_NAME_KEY, "PLAYER 1");

    }

    //get high score values from file
    public void getHighScoreValues() {

        highScores = new String[3][2];

        try {

            Scanner scan = new Scanner(openFileInput(FILENAME + cubeType + ".txt"));
            int index = 0;

            while (scan.hasNext() && index < 3) {

                highScores[index][0] = scan.next();
                highScores[index++][1] = scan.next();

            }

            scan.close();

        } catch (FileNotFoundException e) { //file not found so no high score set for this cube type
            //empty strings will fill table instead
            e.printStackTrace();
        }

    }
    //save high score values into file
    private void saveHighScores() { //save scores before new table is loaded

        try {
            PrintWriter writer = new PrintWriter(
                    openFileOutput(FILENAME + cubeType + ".txt", MODE_PRIVATE));

            for (int i = 0; i < 3; i++) {
                writer.print(highScores[i][0] == null ? " " : highScores[i][0]);
                writer.print(" ");
                writer.print(highScores[i][1] == null ? " " : highScores[i][1]);
                writer.print("\n");
            }

            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void deleteHighScoreFiles() {

        for (int i=0; i<9; i++) {

            try {
                PrintWriter writer = new PrintWriter(
                        openFileOutput(FILENAME + getCubeType(i) + ".txt", MODE_PRIVATE));

                writer.write("");
                writer.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    private void fillHighScoreTable() {

        tableTitle.setText("High Score's for " + cubeType);

        firstPlaceName.setText(highScores[0][0]);
        firstPlace.setText(highScores[0][1]);

        secondPlaceName.setText(highScores[1][0]);
        secondPlace.setText(highScores[1][1]);

        thirdPlaceName.setText(highScores[2][0]);
        thirdPlace.setText(highScores[2][1]);
    }

    public void checkHighScores() {
        String cur = clockTextView.getText().toString(), curName = playerName;

        for(int i = 0; i < 3; i++) {
            //if high score could go here open alert dialog to ask permission to replace
            if(highScores[i][1] == null || cur.compareTo(highScores[i][1]) < 0) {
                index = i;
                askPlayerToSaveScore();
                return;
            }
        }

    }

    private void askPlayerToSaveScore() {
        DialogFragment updateDialog = new UpdateScoreDialog();

        updateDialog.show(getFragmentManager(), "updateDialog"); //tag
    }

    private void updateHighScores() { //update high score table on screen

        String cur = playerName, cur2 = clockTextView.getText().toString(), temp, temp2;

        for(int i=index; i<3; i++) {

            temp = highScores[i][0];
            temp2 = highScores[i][1];

            highScores[i][0] = cur;
            highScores[i][1] = cur2;

            cur = temp;
            cur2 = temp2;

        }

        fillHighScoreTable();
    }

    public String getCubeType(int index) {

        String ret;

        switch (index) {

            case 0:
                ret = "2x2";
                break;
            case 1:
                ret = "3x3";
                break;
            case 2:
                ret = "4x4";
                break;
            case 3:
                ret = "5x5";
                break;
            case 4:
                ret = "6x6";
                break;
            case 5:
                ret = "7x7";
                break;
            case 6:
                ret = "MegaMinx";
                break;
            case 7:
                ret = "Square-1";
                break;
            case 8:
                ret = "Pyraminx";
                break;
            default:
                ret = "";
        }

        return ret;
    }

    @Override
    public void dialogMessage(int buttonClicked, String playerName,
                              String inspectionTime, int cubeType) {

        boolean needToUpdate = false;
        String temp;

        if(buttonClicked == 1) {

            if (playerName != null && !playerName.equals("")
                    && !playerName.equals(this.playerName)) {
                needToUpdate = true;
                this.playerName = playerName;
            }
            if (inspectionTime != null && !inspectionTime.equals("")
                    && !inspectionTime.equals(this.inspectionTime)) {
                needToUpdate = true;
                this.inspectionTime = Integer.parseInt(inspectionTime.split(" ")[0]);
            }
            if (cubeType >= 0) {

                temp = getCubeType(cubeType);

                if(!temp.equals(this.cubeType)) {
                    needToUpdate = true;
                    saveHighScores();
                    this.cubeType = temp;
                    getHighScoreValues();
                    fillHighScoreTable();
                }

            }

            if (needToUpdate) {
                Toast.makeText(this, "New Settings Applied", Toast.LENGTH_SHORT).show();
                updatePreferences();
            }

        }

    }

    @Override
    public void alertAnswer(boolean response) {
        if (response) {
            updateHighScores();
        }
    }

    @Override
    public void DeleteAnswer(boolean response) {

        if (response) {
            deleteHighScoreFiles();
            getHighScoreValues();
            fillHighScoreTable();
        }

    }
}
