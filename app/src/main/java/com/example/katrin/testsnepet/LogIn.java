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
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class LogIn extends AppCompatActivity implements View.OnClickListener {

    Button sing_in;
    EditText usr_password, username;
    RequestQueue queue;

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

            String usrnm = username.getText().toString();
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
                            Intent intent = new Intent(LogIn.this, Home.class);
                            intent.putExtra("userID", User_id);
                            intent.putExtra("username", user);
                            intent.putExtra("coords", "");
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Username or Password is wrong please, be sure you wrote correct data! ", Toast.LENGTH_SHORT).show();
                            sing_in.setText("Log in");
                        }
                        Log.d("====>", "User_ID ="+User_id+"; Pass ="+Passw+"; Should be "+pass+";");
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
                    Intent intent = new Intent(LogIn.this, Home.class);
                    intent.putExtra("userID", "user2007");
                    intent.putExtra("username", "2007");
                    intent.putExtra("coords", "1234");
                    startActivity(intent);
                }

                error.printStackTrace();
            }
        });
        queue.add(arrReq);


    }
}
