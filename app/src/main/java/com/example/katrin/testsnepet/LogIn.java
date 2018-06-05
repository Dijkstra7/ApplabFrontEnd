package com.example.katrin.testsnepet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

public class LogIn extends AppCompatActivity implements View.OnClickListener {

    Button login;
    EditText usr_password, username;
    RequestQueue queue;
    String usrnm;
    float elo1;
    float elo2;
    float elo3;
    float[] coordarrayday1 = {0};
    float[] coordarrayday2 = {0};
    float[] coordarrayday3 = {0};
    float[] coordarrayday4 = {0};
    float[] coordarrayday5 = {0};
    float[] coordarrayday6 = {0};
    ArrayList<String> loids;
    ArrayList<String> dates;
    boolean day_1_ready = false;
    boolean day_2_ready = false;
    boolean day_3_ready = false;
    int width;
    int USAGEDAY;
    FlagPositions flagPositions;
    EloScores2 fake;
    float[] eloScores2 = {0, 0, 0, 0, 0, 0};
    boolean eloReady = false;
    int eloNumber = 0;
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
        login = (Button) findViewById(R.id.sing_in);
        login.setOnClickListener(this);
        Calendar cal = Calendar.getInstance();
        cal.setTime(Calendar.getInstance().getTime());
        USAGEDAY = cal.get((Calendar.DAY_OF_WEEK))-2; // Monday =0, Tuesday=1 etc.
        Log.d("date today", String.valueOf(cal.get(Calendar.DAY_OF_WEEK)));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sing_in) {

            usrnm = username.getText().toString();
            String psswrd = usr_password.getText().toString();
            login.setText("Inloggen...");
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
                            Toast.makeText(getApplicationContext(), "Gebruikersnaam of wachtwoord is fout. Probeer het nog een keer.", Toast.LENGTH_SHORT).show();
                            login.setText("Log in");
                        }
                        Log.d("====>", "User_ID ="+User_id+"; Pass ="+Passw+"; Should be "+pass+";");
                    }
                } catch (JSONException e)
                {
                    if (e instanceof JSONException){
                        Toast.makeText(getApplicationContext(), "Gebruikersnaam of wachtwoord is fout. Probeer het nog een keer.", Toast.LENGTH_SHORT).show();
                        login.setText("Log in");
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
                    login.setText("Inloggen");
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
        };
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (!flagPositions.isFlagsreceived()){
                    for (int j = 0; j < 6; j++) {
                        flagPositions.setCoord(-1, j+1);
                        flagPositions.setFlagsreceived(true);
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
                    login.setText("Inloggen");
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
        };
        queue.add(flagrequest);
    }
    private void getEloAndCoordinates(final String userid) {
        eloScores2 = new float[6];
        coordDatas = new CoordDatas();
        String curlbase = "http://applab.ai.ru.nl:5000/fast_m2m/user_id=";
        final String eurlbase = "http://applab.ai.ru.nl:5000/scores/day=";
        for (int day=0; day<4; day++){
            final int d = day;
            int reps = day==3? 3:1;
            for (int rep = 0; rep < reps; rep++) {
                final int r = rep;
                if (d>=USAGEDAY){
                    eloScores2[d+r] = (float) 0;
                    eloNumber+=2;
                    Log.d("elonumber", String.valueOf(eloNumber));
                    if (isReady()) allReady();
                } else {
                    final String coordurl1 = curlbase + userid + "/loid=" + loids.get((day+rep)%3)+"/day="+String.valueOf(day+1);
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
                                                        elodouble = max(min(responseelo.getDouble(0) / 600., 1.), 0.);
                                                        Log.d("Real Elo "+String.valueOf(d+r), String.valueOf(elodouble));
                                                        eloScores2[d+r] = (float) elodouble;
                                                        eloNumber++;
                                                        Log.d("elonumber", String.valueOf(eloNumber));
                                                        Log.d("Elo_ready", String.valueOf(flagPositions.isFlagsreceived()));
                                                        if (isReady()) {
                                                            allReady();
                                                        }

                                                    } catch (JSONException e) {
                                                        eloScores2[d+r] = (float) 0;
                                                        eloNumber++;
                                                        Log.d("elonumber", String.valueOf(eloNumber));
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
                                                login.setText("Inloggen");
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
                                    };
                                    queue.add(eloScore1);
                                    try {
                                        if (jsonArray != null && jsonArray.length() > 0) {
                                            switch (d+r) {
                                                case 0:
                                                    coordarrayday1 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday1[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 1:
                                                    coordarrayday2 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday2[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 2:
                                                    coordarrayday3 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday1[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 3:
                                                    coordarrayday4 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday4[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 4:
                                                    coordarrayday5 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday5[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 5:
                                                    coordarrayday6 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday6[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                default:
                                                    Log.d("error in cases case", String.valueOf(d+r));
                                            }
                                            if (isReady()) allReady();
                                        } else { //should be a different error message?
                                            switch (d+r) {
                                                case 0:
                                                    coordarrayday1 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday1[i] = (float) 0;
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 1:
                                                    coordarrayday2 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday2[i] = (float) 0;
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 2:
                                                    coordarrayday3 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday1[i] = (float) 0;
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 3:
                                                    coordarrayday4 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday4[i] = (float) 0;
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 4:
                                                    coordarrayday5 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday5[i] = (float) 0;
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                case 5:
                                                    coordarrayday6 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday6[i] = (float) 0;
                                                    }
                                                    eloNumber++;
                                                    Log.d("elonumber", String.valueOf(eloNumber));
                                                    break;
                                                default:
                                                    Log.d("Foutje in cases, case", String.valueOf(d+r));
                                            }
                                            if (isReady()) allReady();
                                        }
                                    } catch (JSONException e) {
                                        switch (d+r) {
                                            case 0:
                                                coordarrayday1 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday1[i] = (float) 0;
                                                }
                                                eloNumber++;
                                                Log.d("elonumber", String.valueOf(eloNumber));
                                                break;
                                            case 1:
                                                coordarrayday2 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday2[i] = (float) 0;
                                                }
                                                eloNumber++;
                                                Log.d("elonumber", String.valueOf(eloNumber));
                                                break;
                                            case 2:
                                                coordarrayday3 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday1[i] = (float) 0;
                                                }
                                                eloNumber++;
                                                Log.d("elonumber", String.valueOf(eloNumber));
                                                break;
                                            case 3:
                                                coordarrayday4 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday4[i] = (float) 0;
                                                }
                                                eloNumber++;
                                                Log.d("elonumber", String.valueOf(eloNumber));
                                                break;
                                            case 4:
                                                coordarrayday5 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday5[i] = (float) 0;
                                                }
                                                eloNumber++;
                                                Log.d("elonumber", String.valueOf(eloNumber));
                                                break;
                                            case 5:
                                                coordarrayday6 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday6[i] = (float) 0;
                                                }
                                                eloNumber++;
                                                Log.d("elonumber", String.valueOf(eloNumber));
                                                break;
                                            default:
                                                Log.d("Fout bij case", String.valueOf(d+r));
                                        }
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
                                login.setText("Inloggen");
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
                    };
                    queue.add(arrReq1);
                }
            }
        }
    }

    private boolean isReady(){
        if (!flagPositions.isFlagsreceived()) return false;
        if (eloNumber<12) return false;
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
        intent.putExtra("flagpositions", flagPositions);
        intent.putExtra("coord1", coordarrayday1);
        intent.putExtra("coord2", coordarrayday2);
        intent.putExtra("coord3", coordarrayday3);
        intent.putExtra("coord4", coordarrayday4);
        intent.putExtra("coord5", coordarrayday5);
        intent.putExtra("coord6", coordarrayday6);
        intent.putExtra("eloscores", eloScores2);
        intent.putExtras(b);
        startActivity(intent);
    }
}
