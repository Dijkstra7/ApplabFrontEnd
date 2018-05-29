package com.example.katrin.testsnepet;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

public class Math_1 extends AppCompatActivity {
    ImageView carr;
    SeekBar pbar;
    SeekBar pbar2;
    SeekBar pbar3;
    SeekBar pbar4;
    String userid;
    RequestQueue queue;
    float elo1;
    float elo2;
    float elo3;
    ArrayList<Double> coordarrayday1;
    ArrayList<Double> coordarrayday2;
    ArrayList<Double> coordarrayday3;
    boolean day_1_ready = false;
    boolean day_2_ready = false;
    boolean day_3_ready = false;
    int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_1);

        carr = (ImageView) findViewById(R.id.carr);
        pbar = (SeekBar) findViewById(R.id.pbar);
        pbar2 = (SeekBar) findViewById(R.id.pbar2);
        pbar3 = (SeekBar) findViewById(R.id.pbar3);

        Intent intent = getIntent();
        userid = intent.getStringExtra("userID");

        queue = Volley.newRequestQueue(this);

        pbar.setMax(100);
        pbar.setProgress(0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x - 2*(int) carr.getX();
        Log.d("+++++>", String.valueOf(width));
        Bundle b = intent.getExtras();
        coordarrayday1 = (ArrayList<Double>) b.getSerializable("coordarrayday1");
        coordarrayday2 = (ArrayList<Double>) b.getSerializable("coordarrayday2");
        coordarrayday3 = (ArrayList<Double>) b.getSerializable("coordarrayday3");
        elo1 = (float) b.getFloat("elo1");
        elo2 = (float) b.getFloat("elo2");
        elo3 = (float) b.getFloat("elo3");
        allReady();
    }

    public void allReady(){
        float elowidth = width / coordarrayday1.size() * elo1;
        animateCar(elowidth, coordarrayday1);

        final Thread thread = new Thread(){

            public void run(){
                try {
                    Log.d("Elo1", String.valueOf(100.*elo1));
                    for (int i = 0; i < 100.*elo1; i++){
                        pbar.setProgress(i);
                        sleep(30);
                    }
                    Log.d("Elo2", String.valueOf(100.*elo2));
                    for (int i = 0; i < 100.*elo2; i++){
                        pbar2.setProgress(i);
                        sleep(30);
                    }
                    Log.d("Elo3", String.valueOf(100.*elo3));
                    for (int i = 0; i < 100.*elo3; i++){
                        pbar3.setProgress(i);
                        sleep(30);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void animateCar(final float x, final ArrayList<Double> jsonArray){
        Log.d("Elo", String.valueOf(x));
        AnimationSet s = new AnimationSet(false);
        s.setFillAfter(true);
        float old_y=0;
        for (int i = 0; i < jsonArray.size(); i++)
        {
            float y = jsonArray.get(i).floatValue() * 50;
            if (i>0){
                y = y - old_y;
            }
            old_y = jsonArray.get(i).floatValue()*50;
            Animation anm = new TranslateAnimation(0, x, 0, y);
            anm.setDuration(150);
            anm.setStartOffset(150*i);
            s.addAnimation(anm);
            Log.d("coord "+String.valueOf(i), String.valueOf(y));
        }
        carr.startAnimation(s);
    }
}
