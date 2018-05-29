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
    boolean day_1_ready = false;
    boolean day_2_ready = false;
    boolean day_3_ready = false;
    int width;

    private String User_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        getSupportActionBar().hide();
        queue = Volley.newRequestQueue(this);

        username = (EditText) findViewById(R.id.username);
        usr_password = (EditText) findViewById(R.id.usr_password);
        sing_in = (Button) findViewById(R.id.sing_in);
        sing_in.setOnClickListener(this);

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

        String url = "http://applab.ai.ru.nl:5000/login_users/username="+user;
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    JSONArray jsonArray = response;
                    JSONObject users = jsonArray.getJSONObject(0);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        String Passw = users.getString("Password");
                        if (pass.equals(Passw)) {
                            User_id = users.getString("UserId");
                            getEloAndCoordinates(User_id);
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
                if (error instanceof TimeoutError){
                    Intent intent = new Intent(LogIn.this, Math_1.class);
                    intent.putExtra("username", "user2007");
                    intent.putExtra("userID", "2007");
                    intent.putExtra("coords", "1234");
                    startActivity(intent);
                }

                error.printStackTrace();
            }
        });
        queue.add(arrReq);


    }
    private void getEloAndCoordinates(final String userid){
        String coordurl1 = "http://applab.ai.ru.nl:5000/calculate_m2m_coordinates/user_id=" + userid + "/loid=8025";
        JsonArrayRequest arrReq1 = new JsonArrayRequest(Request.Method.GET, coordurl1, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(final JSONArray response)
                    {
                        final String elourlday1 = "http://applab.ai.ru.nl:5000/scores/day=1&user_id=" + userid + "&learning_obj_id=8025";
                        final JSONArray jsonArray = response;
                        JsonArrayRequest eloScore1 = new JsonArrayRequest(Request.Method.GET, elourlday1, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responseelo) {
                                        double elodouble = 0;
                                        try {
                                            Log.d("Real Elo", String.valueOf(responseelo.getDouble(0)));
                                            elodouble = responseelo.getDouble(0)/600.;
                                            if (elodouble == 0){
                                                elodouble = 0.5;
                                            }
                                            elo1 = (float) elodouble;
                                            day_1_ready = true;
                                            if (day_1_ready && day_2_ready && day_3_ready){
                                                allReady();
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("=ELO==>", "fu");
                                error.printStackTrace();
                            }
                        });
                        queue.add(eloScore1);
                        try {
                            if (jsonArray != null && jsonArray.length() > 0) {
                                ArrayList<Double> coordArray = new ArrayList<Double>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    coordArray.add(jsonArray.getDouble(i));
                                }
                                coordarrayday1 = coordArray;
                            }else { //should be a different error message?
                                coordarrayday1 = new ArrayList<>(Arrays.asList(0.0));
                            }
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                error.printStackTrace();
            }
        });
        queue.add(arrReq1);
        String coordurl2 = "http://applab.ai.ru.nl:5000/calculate_m2m_coordinates/user_id=" + userid + "/loid=7789";
        JsonArrayRequest arrReq2 = new JsonArrayRequest(Request.Method.GET, coordurl1, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(final JSONArray response)
                    {
                        final String elourlday2 = "http://applab.ai.ru.nl:5000/scores/day=2&user_id=" + userid + "&learning_obj_id=7789";
                        final JSONArray jsonArray = response;
                        JsonArrayRequest eloScore2 = new JsonArrayRequest(Request.Method.GET, elourlday2, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responseelo) {
                                        double elodouble = 0;
                                        try {
                                            elodouble = responseelo.getDouble(0)/600;
                                            if (elodouble == 0){
                                                elodouble = 0.5;
                                            }
                                            elo2 = (float) elodouble;
                                            day_2_ready = true;
                                            if (day_1_ready && day_2_ready && day_3_ready){
                                                allReady();
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("=ELO==>", "fu");
                                error.printStackTrace();
                            }
                        });
                        queue.add(eloScore2);
                        try {
                            if (jsonArray != null && jsonArray.length() > 0) {
                                ArrayList<Double> coordArray = new ArrayList<Double>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    coordArray.add(jsonArray.getDouble(i));
                                }
                                coordarrayday2 = coordArray;
                            }else { //should be a different error message?
                                coordarrayday2 = new ArrayList<>(Arrays.asList(0.0));
                            }
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                error.printStackTrace();
            }
        });
        queue.add(arrReq2);
        String coordurl3 = "http://applab.ai.ru.nl:5000/calculate_m2m_coordinates/user_id=" + userid + "/loid=7771";
        JsonArrayRequest arrReq3 = new JsonArrayRequest(Request.Method.GET, coordurl3, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(final JSONArray response)
                    {
                        final String elourlday3 = "http://applab.ai.ru.nl:5000/scores/day=3&user_id=" + userid + "&learning_obj_id=7771";
                        final JSONArray jsonArray = response;
                        JsonArrayRequest eloScore3 = new JsonArrayRequest(Request.Method.GET, elourlday3, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responseelo) {
                                        double elodouble = 0;
                                        try {
                                            elodouble = responseelo.getDouble(0)/600;
                                            if (elodouble == 0){
                                                elodouble = 0.5;
                                            }
                                            elo3 = (float) elodouble;
                                            day_3_ready = true;
                                            if (day_1_ready && day_2_ready && day_3_ready){
                                                allReady();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("=ELO==>", "fu");
                                error.printStackTrace();
                            }
                        });
                        queue.add(eloScore3);
                        try {
                            if (jsonArray != null && jsonArray.length() > 0) {
                                ArrayList<Double> coordArray = new ArrayList<Double>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    coordArray.add(jsonArray.getDouble(i));
                                }
                                coordarrayday3 = coordArray;
                            }else { //should be a different error message?
                                coordarrayday3 = new ArrayList<>(Arrays.asList(0.0));
                            }
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                error.printStackTrace();
            }
        });
        queue.add(arrReq3);
    }

    private void allReady(){
        Bundle b = new Bundle();
        b.putSerializable("coordarrayday1", (Serializable) coordarrayday1);
        b.putSerializable("coordarrayday2", (Serializable) coordarrayday2);
        b.putSerializable("coordarrayday3", (Serializable) coordarrayday3);
        b.putFloat("elo1",elo1);
        b.putFloat("elo2",elo2);
        b.putFloat("elo3",elo3);
        Intent intent = new Intent(LogIn.this, Math_1.class);
        intent.putExtra("userID", User_id);
        intent.putExtra("username", usrnm);
        intent.putExtras(b);
        startActivity(intent);
    }
}
