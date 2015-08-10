package com.example.calin.speedcuber;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    TextView clockTextView, countDownTextView;
    RelativeLayout mainLayout;
    boolean running, countingDown;
    int inspectionTime;
    String cubeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = this.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        inspectionTime = preferences.getInt("InspectionTime", 0);
        cubeType = preferences.getString("CubeType", "3x3");

        clockTextView = (TextView) findViewById(R.id.clock_text_view);
        clockTextView.setText("00:00:00");

        countDownTextView = (TextView) findViewById(R.id.count_down_text_view);

        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        running = countingDown = false;
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

    public void ScreenHit(View view) {

        if (!running) {

            running = true;

            if (inspectionTime > 0) {
                startCountDown();
            }else {
                startStopWatch();
            }

        } else if (countingDown) {

            countingDown = false;

        } else if (running){

            running = false;

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
