package com.example.katrin.testsnepet;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Math_2 extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    ImageView carr;
    ImageView flag_1;
    ImageView flag_2;
    SeekBar pbar;
    Button add_flag_button;
    String userid;
    RequestQueue queue;
    FlagPositions flagPositions;
    EloScores eloScores;
    CoordDatas coordDatas;
    int USAGEDAY;
    int width;
    boolean moveflag = false;


    @Override
    public void onBackPressed() {
        CharSequence options[] = new CharSequence[] {"Ja, uitloggen", "nee, blijven"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wil je uitloggen?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    Intent intent = new Intent(Math_2.this, LogIn.class);
                    startActivity(intent);
                }

            }
        });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_2);

        carr = (ImageView) findViewById(R.id.carr);
        pbar = (SeekBar) findViewById(R.id.pbar);
        add_flag_button = findViewById(R.id.button5);
        add_flag_button.setOnClickListener(this);
        flag_1 = findViewById(R.id.imageView5);
        flag_2 = findViewById(R.id.imageView4);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.Button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);

        Intent intent = getIntent();
        userid = intent.getStringExtra("userID");
        Bundle b = intent.getExtras();
        coordDatas = intent.getParcelableExtra("coorddatalist");
        eloScores = intent.getParcelableExtra("eloscores");
        flagPositions = intent.getParcelableExtra("flagpositions");
        USAGEDAY = b.getInt("USAGEDAY");

        queue = Volley.newRequestQueue(this);

        pbar.setMax(100);
        pbar.setProgress(0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x - carr.getMeasuredWidth();
        allReady();
    }

    public void allReady(){
        float coords[];
        if (USAGEDAY < 4){
            flag_1.setOnTouchListener(this);
            coords = coordDatas.getCoord_data(0).getCoord_data();
        } else{
            flag_1.setVisibility(View.VISIBLE);
            flag_2.setOnTouchListener(this);
            coords = concatenate(coordDatas.getCoord_data(0).getCoord_data(), coordDatas.getCoord_data(3).getCoord_data());
        }
        final int pos = USAGEDAY<4? 1: 4;
        float elowidth = width / coords.length * eloScores.getElo_scores(pos);
        animateCar(elowidth, coords);

        final Thread thread = new Thread(){

            public void run(){
                try {
                    Log.d("Elo1", String.valueOf(100.*eloScores.getElo_scores(pos)));
                    for (int i = 0; i < 100.*eloScores.getElo_scores(pos); i++){
                        pbar.setProgress(i);
                        sleep(30);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        if (flagPositions.getCoord(3)>=0){
            findViewById(R.id.imageView5).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.imageView5).setVisibility(View.INVISIBLE);
        }
        if (flagPositions.getCoord(4)>=0){
            findViewById(R.id.imageView4).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.imageView4).setVisibility(View.INVISIBLE);
        }
        thread.start();
    }

    private void animateCar(final float x, final float[] jsonArray){
        Log.d("Elo", String.valueOf(x));
        AnimationSet s = new AnimationSet(false);
        s.setFillAfter(true);
        float old_y=0;
        for (int i = 0; i < jsonArray.length; i++)
        {
            float y = jsonArray[i] * 50;
            if (i>0){
                y = y - old_y;
            }
            old_y = jsonArray[i]*50;

            Animation anm = new TranslateAnimation(0, x, 0, y);
            anm.setDuration(150);
            anm.setStartOffset(150*i);
            s.addAnimation(anm);
            Log.d("coord "+String.valueOf(i), String.valueOf(y));
        }
        carr.startAnimation(s);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button5:
                int ivid = (USAGEDAY < 5) ? R.id.imageView5: R.id.imageView4;
                int flag = (USAGEDAY < 5) ? 3: 4;
                findViewById(ivid).setVisibility(View.VISIBLE);
                if (moveflag){
                    add_flag_button.setText("Verplaats vlag");
                    save_flag_state(flag, findViewById(ivid).getX(), 1);
                    flagPositions.setCoord((int) findViewById(ivid).getX(), flag);
                } else {
                    add_flag_button.setText("Sla vlag-positie op");
                }
                moveflag = !moveflag;
                break;
            case R.id.button1:
                Bundle b = new Bundle();
                b.putInt("USAGEDAY", USAGEDAY);
                Intent intent2 = new Intent(this, Math_1.class);
                intent2.putExtra("userID", userid);
                intent2.putExtra("username", userid);
                intent2.putExtras(b);
                intent2.putExtra("flagpositions", flagPositions);
                intent2.putExtra("eloscores", eloScores);
                intent2.putExtra("coorddatalist", coordDatas);
                startActivity(intent2);
                break;
            case R.id.button3:
                Bundle b3 = new Bundle();
                b3.putInt("USAGEDAY", USAGEDAY);
                Intent intent3 = new Intent(this, Math_3.class);
                intent3.putExtra("userID", userid);
                intent3.putExtra("username", userid);
                intent3.putExtras(b3);
                intent3.putExtra("flagpositions", flagPositions);
                intent3.putExtra("eloscores", eloScores);
                intent3.putExtra("coorddatalist", coordDatas);
                startActivity(intent3);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.imageView5 && moveflag) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                findViewById(R.id.imageView5).setX(event.getRawX());
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE){
                findViewById(R.id.imageView5).setX(event.getRawX());
                return true;
            }
        }
    return false;
    }

    public void save_flag_state(final float flag, final float x, final int attempt) {
        String flagurl = "http://applab.ai.ru.nl:5000/save_flag_position/user_id="+userid+"&flag=Flag" + String.valueOf((int) flag) + "&flag_coord=" + String.valueOf((int) x);
        queue.add(new JsonObjectRequest(Request.Method.GET, flagurl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(Math_2.this, "Vlag opgeslagen.", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (attempt < 10) {
                    save_flag_state(flag, x, attempt + 1);
                } else {
                    Toast.makeText(Math_2.this, "Vlag kon niet worden opgeslagen. Probeer het nog eens.", Toast.LENGTH_SHORT).show();
                    moveflag = !moveflag;
                    add_flag_button.setText("Sla vlag-positie op");
                }
                error.printStackTrace();
            }
        }));
    }

    public float[] concatenate(float[] a, float[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        float[] c = (float[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
