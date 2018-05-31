package com.example.katrin.testsnepet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.min;

public class LogIn extends AppCompatActivity implements View.OnClickListener {

    Button sing_in;
    EditText usr_password, username;
    RequestQueue queue;
    String usrnm;
    float elo1;
    float elo2;
    float elo3;
    ArrayList<Double> coordarrayday1;
    ArrayList<Double> coordarrayday2;
    ArrayList<Double> coordarrayday3;
    ArrayList<String> loids;
    ArrayList<String> dates;
    boolean day_1_ready = false;
    boolean day_2_ready = false;
    boolean day_3_ready = false;
    int width;
    int USAGEDAY;
    FlagPositions flagPositions;
    EloScores eloScores;
    CoordDatas coordDatas;
    private String User_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        getSupportActionBar().hide();
        queue = Volley.newRequestQueue(this);

        loids = new ArrayList<>(Arrays.asList("8025", "7789", "7771"));
        dates = new ArrayList<>(Arrays.asList("2017-11-28", "2017-11-29", "2017-11-30", "2017-12-01"));
        username = (EditText) findViewById(R.id.username);
        usr_password = (EditText) findViewById(R.id.usr_password);
        sing_in = (Button) findViewById(R.id.sing_in);
        sing_in.setOnClickListener(this);
        Calendar cal = Calendar.getInstance();
        cal.setTime(Calendar.getInstance().getTime());
        USAGEDAY = 4;//cal.get((Calendar.DAY_OF_WEEK)); // Change day for testing
        Log.d("date today", String.valueOf(cal.get(Calendar.DAY_OF_WEEK)));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sing_in) {

            usrnm = username.getText().toString();
            String psswrd = usr_password.getText().toString();
            sing_in.setText("Inloggen...");
            jsonParse(usrnm, psswrd);
        }
    }

    private void jsonParse(final String user, final String pass){

        final String url = "http://applab.ai.ru.nl:5000/login_users/username="+user;
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
                Log.d("Received", url);
                try
                {
                    JSONArray jsonArray = response;
                    JSONObject users = jsonArray.getJSONObject(0);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        String Passw = users.getString("Password");
                        if (pass.equals(Passw)) {
                            User_id = users.getString("UserId");
                            getFlags(User_id);
                        } else {
                            Toast.makeText(getApplicationContext(), "Username or Password is wrong please, be sure you wrote correct data! ", Toast.LENGTH_SHORT).show();
                            sing_in.setText("Log in");
                        }
                        Log.d("====>", "User_ID ="+User_id+"; Pass ="+Passw+"; Should be "+pass+";");
                    }
                } catch (JSONException e)
                {
                    if (e instanceof JSONException){
                        Toast.makeText(getApplicationContext(), "Username or Password is wrong please, be sure you wrote correct data! ", Toast.LENGTH_SHORT).show();
                        sing_in.setText("Log in");
                    }
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("Failed", url);
                if (error instanceof TimeoutError || error instanceof ServerError){
                    Toast.makeText(LogIn.this,"Er ging iets fout tijdens het inlogggen. probeer het nog een keer", Toast.LENGTH_SHORT).show();
                    sing_in.setText("Inloggen");
                }

                error.printStackTrace();
            }
        });
        queue.add(arrReq);


    }

    private void getFlags(final String user_id){
        flagPositions = new FlagPositions();
        final String flagurl = "http://applab.ai.ru.nl:5000/list_flag_positions";
        JsonArrayRequest flagrequest = new JsonArrayRequest(Request.Method.GET, flagurl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Received", flagurl);
                for(int i =0; i<response.length();i++) {
                    try {
                        JSONObject flag_block = response.getJSONObject(i);
//                        Log.d("IS this equal?"+flag_block.getString("UserId"), user_id);
                        if (flag_block.getString("UserId").equals(user_id)) {
                            Log.d("Answer", "yes");
                            for (int j = 1; j < 7; j++) {
                                int coord = flag_block.getInt("Flag" + String.valueOf(j));
                                flagPositions.setCoord(coord, j);
                            }
                            flagPositions.setFlagsreceived(true);
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getEloAndCoordinates(user_id);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Failed", flagurl);
                if (error instanceof TimeoutError || error instanceof ServerError){
                    Toast.makeText(LogIn.this,"Er ging iets fout tijdens het inlogggen. probeer het nog een keer", Toast.LENGTH_SHORT).show();
                    sing_in.setText("Inloggen");
                }
                error.printStackTrace();
            }
        });
        queue.add(flagrequest);
    }
    private void getEloAndCoordinates(final String userid) {
        eloScores = new EloScores();
        coordDatas = new CoordDatas();
        String curlbase = "http://applab.ai.ru.nl:5000/calculate_m2m_coordinates/user_id=";
        final String eurlbase = "http://applab.ai.ru.nl:5000/scores/day=";
        for (int day=0; day<4; day++){
            final int d = day;
            int reps = day==3? 3:1;
            for (int rep = 0; rep < reps; rep++) {
                final int r = rep;
                final String coordurl1 = curlbase + userid + "/loid=" + loids.get((day+rep)%3)+"/date="+dates.get(day);
                JsonArrayRequest arrReq1 = new JsonArrayRequest(Request.Method.GET, coordurl1, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(final JSONArray response) {
                                Log.d("Received", coordurl1);
                                final String elourlday1 = eurlbase + String.valueOf(d+1) + "&user_id=" + userid + "&learning_obj_id=" + loids.get((d+r)%3);
                                final JSONArray jsonArray = response;
                                JsonArrayRequest eloScore1 = new JsonArrayRequest(Request.Method.GET, elourlday1, null,
                                        new Response.Listener<JSONArray>() {
                                            @Override
                                            public void onResponse(JSONArray responseelo) {
                                                Log.d("Received", elourlday1);
                                                double elodouble = 0;
                                                try {
                                                    elodouble = min(responseelo.getDouble(0) / 600., 1.);
                                                    Log.d("Real Elo", String.valueOf(elodouble));
                                                    eloScores.setElo_scores((float) elodouble, d+r);
                                                    Log.d("Elo_ready", String.valueOf(flagPositions.isFlagsreceived()));
                                                    if (isReady()) {
                                                        allReady();
                                                    }

                                                } catch (JSONException e) {
                                                    eloScores.setElo_scores((float) 0.5, d+r);
                                                    if (isReady()) allReady();
                                                    e.printStackTrace();
                                                }

                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("Failed", elourlday1);
                                        if (error instanceof TimeoutError || error instanceof ServerError){
                                            Toast.makeText(LogIn.this,"Er ging iets fout tijdens het inlogggen. probeer het nog een keer", Toast.LENGTH_SHORT).show();
                                            sing_in.setText("Inloggen");
                                        }
                                        error.printStackTrace();
                                    }
                                });
                                queue.add(eloScore1);
                                try {
                                    if (jsonArray != null && jsonArray.length() > 0) {
                                        float[] coordArray = new float[jsonArray.length()];
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            coordArray[i] = (float) jsonArray.getDouble(i);
                                        }
                                        coordDatas.setCoord_data(new CoordData(coordArray), d+r);
                                        if (isReady()) allReady();
                                    } else { //should be a different error message?
                                        coordDatas.setCoord_data(new CoordData(new float[]{0}), d+r);
                                        if (isReady()) allReady();
                                    }
                                } catch (JSONException e) {
                                    coordDatas.setCoord_data(new CoordData(new float[]{0}), d+r);
                                    if (isReady()) allReady();
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Failed", coordurl1);
                        if (error instanceof TimeoutError || error instanceof ServerError){
                            Toast.makeText(LogIn.this,"Er ging iets fout tijdens het inlogggen. probeer het nog een keer", Toast.LENGTH_SHORT).show();
                            sing_in.setText("Inloggen");
                        }
                        error.printStackTrace();
                    }
                });
                queue.add(arrReq1);
            }
        }
    }

    private boolean isReady(){
        if (!flagPositions.isFlagsreceived()) return false;
        if (!eloScores.isElo_scores_received()) return false;
        if (!coordDatas.isCoord_data_received()) return false;
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void allReady(){
        Bundle b = new Bundle();
        b.putInt("USAGEDAY", USAGEDAY);
        Intent intent = new Intent(LogIn.this, Math_1.class);
        intent.putExtra("userID", User_id);
        intent.putExtra("username", usrnm);
        intent.putExtra("flagpositions", flagPositions);
        intent.putExtra("coorddatalist", coordDatas);
        intent.putExtra("eloscores", eloScores);
        intent.putExtras(b);
        startActivity(intent);
    }
}
