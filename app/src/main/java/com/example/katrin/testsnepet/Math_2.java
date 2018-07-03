package com.example.katrin.testsnepet;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is equivalent to the Math_1 class except for some hardcoded values. Will in the future
 * be depricated.
 *
 * @see Math_1
 * @since 1
 */
public class Math_2 extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    ImageView carr;
    ImageView carr2;
    ImageView flag_1;
    ImageView flag_2;
    ImageView baseballmirror;
    SeekBar pbar;
    Button add_flag_button;
    String userid;
    RequestQueue queue;
    FlagPositions flagPositions;
    float[] eloScores2;
    float[] coordarrayday1;
    float[] coordarrayday2;
    float[] coordarrayday3;
    float[] coordarrayday4;
    float[] coordarrayday5;
    float[] coordarrayday6;
    float[] coords1;
    float[] coords2;
    boolean two_days;
    int USAGEDAY;
    int width;
    int flag_id;
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

        carr = findViewById(R.id.carr);
        carr2 = findViewById(R.id.carr2);
        baseballmirror = findViewById(R.id.thumb_mirror);
        pbar = findViewById(R.id.pbar);
        add_flag_button = findViewById(R.id.button5);
        add_flag_button.setOnClickListener(this);
        flag_1 = findViewById(R.id.imageView5);
        flag_2 = findViewById(R.id.imageView4);
        flag_1.setOnTouchListener(this);
        flag_2.setOnTouchListener(this);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.Button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);

        Intent intent = getIntent();
        userid = intent.getStringExtra("userID");
        Bundle b = intent.getExtras();
        flagPositions = intent.getParcelableExtra("flagpositions");
        coordarrayday1 = intent.getFloatArrayExtra("coord1");
        coordarrayday2 = intent.getFloatArrayExtra("coord2");
        coordarrayday3 = intent.getFloatArrayExtra("coord3");
        coordarrayday4 = intent.getFloatArrayExtra("coord4");
        coordarrayday5 = intent.getFloatArrayExtra("coord5");
        coordarrayday6 = intent.getFloatArrayExtra("coord6");
        coords1 = coordarrayday3;
        coords2 = coordarrayday4;
        eloScores2 = intent.getFloatArrayExtra("eloscores");
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
        width = size.x - 20;
        allReady();
    }

    public void allReady(){
        if (USAGEDAY < 4){
            flag_id = R.id.imageView5;
            two_days = false;
            carr2.setVisibility(View.INVISIBLE);
        } else{
            flag_id = R.id.imageView4;
            flag_1.setVisibility(View.VISIBLE);
            add_flag_button.setVisibility(View.INVISIBLE);
            two_days = true;
        }
        if (USAGEDAY==3) flag_id = R.id.imageView4;
        float elowidthday1 = coords1.length>1? width / coords1.length * eloScores2[1]:0;
        float elowidthday2 = coords2.length>1?width / coords2.length * (eloScores2[4] - eloScores2[1]):0;
        animateCar(elowidthday1, elowidthday2, coords1, coords2, two_days);

        final Thread thread = new Thread(){

            public void run(){
                try {
                    Log.d("Elo1", String.valueOf(100.* eloScores2[1]));
                    for (int i = 0; i < 100.* eloScores2[1]; i++){
                        pbar.setProgress(i);
                        sleep(30);
                    }
                    //TODO: change hardcoded offsets
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int[] baseball_location = new int[2];
                            pbar.getLocationOnScreen(baseball_location);
                            baseballmirror.setX(baseball_location[0]+pbar.getThumb().getBounds().centerX()-pbar.getThumbOffset()-7);
                            baseballmirror.setY(baseball_location[1]+pbar.getThumb().getBounds().centerY()-pbar.getBottom()-15);
                            baseballmirror.setVisibility(View.VISIBLE);
                        }
                    });
                    for (int i = pbar.getProgress(); i < (100. * eloScores2[4]); i++) {
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
            findViewById(R.id.imageView5).setX(flagPositions.getCoord(3));
        } else {
            findViewById(R.id.imageView5).setVisibility(View.INVISIBLE);
        }
        if (flagPositions.getCoord(4)>=0){
            findViewById(R.id.imageView4).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView4).setX(flagPositions.getCoord(4));
        } else {
            findViewById(R.id.imageView4).setVisibility(View.INVISIBLE);
        }

        thread.start();
    }

    private void animateCar(final float x1, final float x2, final float[] coordarray1, final float[] coordarray2, final boolean move_twice){
        Log.d("Elo-width day 2", String.valueOf(x2));
        AnimationSet s = new AnimationSet(false);
        AnimationSet s2 = new AnimationSet(false);
        s.setFillAfter(true);
        s2.setFillAfter(true);
        float old_y=0;
        for (int i = 0; i < coordarray1.length; i++)
        { //move car first time
            float y = coordarray1[i] * 50;
            if (i>0){
                y = y - old_y;
            }
            old_y = coordarray1[i]*50;

            Animation anm = new TranslateAnimation(0, x1, 0, y);
            Animation anm2 = new TranslateAnimation(0, x1, 0, y);
            anm.setDuration(150);
            anm2.setDuration(150);
            anm.setStartOffset(150*i);
            anm2.setStartOffset(150*i);
            s2.addAnimation(anm2);
            s.addAnimation(anm);
//            Log.d("animation", String.valueOf(s2.getAnimations().size()));
            Log.d("coord "+String.valueOf(i), String.valueOf(y));
        }
        if (move_twice) {
            int new_car_delay = 500; //ms waiting before moving again
            Animation anm_vis = new AlphaAnimation(0,1);
            anm_vis.setDuration(new_car_delay);
            anm_vis.setStartOffset(coordarray1.length*150);
            s2.addAnimation(anm_vis);

            for (int i = 0; i < coordarray2.length; i++)
            { // move car second time
                float y = coordarray2[i] * 50;
                if (i>0){
                    y = y - old_y;
                }
                old_y = coordarray2[i]*50;

                Animation anm = new TranslateAnimation(0, x2, 0, y);
                anm.setDuration(150);
                anm.setStartOffset(150*(i+coordarray1.length)+new_car_delay);
                s.addAnimation(anm);
//            Log.d("coord "+String.valueOf(i), String.valueOf(y));
            }
        }
        if ((coordarray1.length+coordarray2.length)>0) carr.startAnimation(s);
        Log.d("Animation", String.valueOf(s2.getAnimations().size()));
        if (move_twice && coordarray1.length>0) carr2.startAnimation(s2);
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
                    int[] location = new int[2];
                    findViewById(ivid).getLocationOnScreen(location);
                    save_flag_state(flag, location[0], 1);
                    flagPositions.setCoord((int) findViewById(ivid).getX(),flag);
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
                intent2.putExtra("coord1", coordarrayday1);
                intent2.putExtra("coord2", coordarrayday2);
                intent2.putExtra("coord3", coordarrayday3);
                intent2.putExtra("coord4", coordarrayday4);
                intent2.putExtra("coord5", coordarrayday5);
                intent2.putExtra("coord6", coordarrayday6);
                intent2.putExtra("eloscores", eloScores2);
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
                intent3.putExtra("coord1", coordarrayday1);
                intent3.putExtra("coord2", coordarrayday2);
                intent3.putExtra("coord3", coordarrayday3);
                intent3.putExtra("coord4", coordarrayday4);
                intent3.putExtra("coord5", coordarrayday5);
                intent3.putExtra("coord6", coordarrayday6);
                intent3.putExtra("eloscores", eloScores2);
                startActivity(intent3);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == flag_id && moveflag) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                findViewById(flag_id).setX(event.getRawX());
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE){
                findViewById(flag_id).setX(event.getRawX());
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
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = "Group2:Group2-1234";
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        });
    }
}
