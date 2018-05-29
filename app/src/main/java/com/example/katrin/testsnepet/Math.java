package com.example.katrin.testsnepet;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Math extends AppCompatActivity {
    ImageView carr;
    SeekBar pbar;
    SeekBar pbar2;
    SeekBar pbar3;
    SeekBar pbar4;
    String userid;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math);

        carr = (ImageView) findViewById(R.id.carr);
        pbar = (SeekBar) findViewById(R.id.pbar);
        pbar2 = (SeekBar) findViewById(R.id.pbar2);
        pbar3 = (SeekBar) findViewById(R.id.pbar3);
        pbar4 = (SeekBar) findViewById(R.id.pbar4);

        Intent intent = getIntent();
        userid = intent.getStringExtra("userID");

        queue = Volley.newRequestQueue(this);

        pbar.setMax(100);
        pbar.setProgress(0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x - 2*(int) carr.getX();
        Log.d("+++++>", String.valueOf(width));

        createAnimation(userid, width);

        final Thread thread = new Thread(){

            public void run(){
                try {
                    for (int i = 0; i <79; i++){
                        pbar.setProgress(i);
                        sleep(30);
                    }
                    for (int i = 0; i <65; i++){
                        pbar2.setProgress(i);
                        sleep(30);
                    }
                    for (int i = 0; i <90; i++){
                        pbar3.setProgress(i);
                        sleep(30);
                    }
                    for (int i = 0; i <27; i++){
                        pbar4.setProgress(i);
                        sleep(30);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();

    }

    private void createAnimation(final String userid, final int road_width){
        String coordurl = "http://applab.ai.ru.nl:5000/calculate_m2m_coordinates/user_id=" + userid + "/loid=7771";
        Log.d("+++++>", "huh");
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, coordurl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        try
                        {
                            JSONArray jsonArray = response;
                            if (jsonArray != null && jsonArray.length() > 0) {
                                Log.d("++++>", String.valueOf(jsonArray.length()));
                                int x = road_width / jsonArray.length();
                                ArrayList<Double> coordArray = new ArrayList<Double>();
                                for (int i=0;i<jsonArray.length();i++) {
                                    coordArray.add(jsonArray.getDouble(i));
                                }
                                animateCar(x, coordArray);
                            }
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (error instanceof TimeoutError){
                    Double[] array = {1.0, 0.0, -1.0, 0., 0.8, 0.0, -0.8, 0.0, 0.6, 0.3, 0.0};
                    ArrayList<Double> testarray = new ArrayList<>(Arrays.asList(array));
                    int x = road_width / testarray.size();
                    animateCar(x, testarray);
                    }
                error.printStackTrace();
            }
        });
        queue.add(arrReq);


    }

    private void animateCar(final float x, final ArrayList<Double> jsonArray){
        AnimationSet s = new AnimationSet(false);
        for (int i = 0; i < jsonArray.size(); i++)
        {
            float y = jsonArray.get(i).floatValue() * 100;
            //                                    Float floatcoord = Float.parseFloat(coords.toString())*100;
            Animation anm = new TranslateAnimation(0, x, 0, y);
            anm.setDuration(1000);
            anm.setFillAfter(true);
            s.addAnimation(anm);
            Log.d("====>", "coord=" + String.valueOf(y));
        }
        carr.startAnimation(s);
    }
}
