package com.example.katrin.testsnepet;

import android.annotation.SuppressLint;
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

/**
 * The LogIn class is responsible for the logic of the first screen. With this class the user can
 * enter its username and password and click on the login button. The class will then check whether
 * the login and password are correct. When this is the case try to load the other data for the
 * user. Upon success it will load the Math screen. Upon failure it will provide an error message
 * for the user.
 * Javadoc created by Rick Dijkstra
 *
 * @author Rick Dijkstra
 * @author Katrin Bujari
 * @author Alessandro Ardu
 * @version 1.0, 5 jun 2018
 */
public class LogIn extends AppCompatActivity implements View.OnClickListener {

    /** The button and EditText fields are referenced for easier manipulation. */
    Button login;
    EditText usr_password, username;

    /** The queue is used to send requests to the API*/
    RequestQueue queue;

    /** Store the username, as written by the user in the username EditText field/ */
    String usrnm;

    /** Store the coordinates as retrieved from the API. */
    float[] coordarrayday1 = {0};
    float[] coordarrayday2 = {0};
    float[] coordarrayday3 = {0};
    float[] coordarrayday4 = {0};
    float[] coordarrayday5 = {0};
    float[] coordarrayday6 = {0};

    /** Store the values of the used learning objectives. */
    ArrayList<String> loids;

    /** Store the values of the used dates. */
    ArrayList<String> dates;

    /** Store the numeric value of the day that the app is used. */
    int USAGEDAY;

    /** Store the positions of the flag as received from the API*/
    FlagPositions flagPositions;

    /** Store the ELO score values as retrieved from the API*/
    float[] eloScores2 = {0, 0, 0, 0, 0, 0};

    /** Keep track of how many values of the API have been received correctly. */
    int apiValuesReceived = 0;

    /** Store the user ID received from the API. Used to communicate with the API. */
    private String User_id = null;

    /**
     * Setting up the login screen.
     * Sets up variables of learning objective ID's, dates that the app will be used, and finds out
     * what the current day is that the app is used. Sets up the listener for the login button and
     * the queue that will handle the API requests. Registers the text boxes and button.
     * <p>
     * Notice that the learning objective IDs and the dates that the app is used are hardcoded. This
     * is because of the need of the API for hardcoded dates and learning objectives.
     * <p>
     * At the moment there is no implementation for saved instances, so savedInstanceState is not
     * used.
     *
     * @author Rick Dijkstra
     * @author Katrin Bujari
     * @param savedInstanceState a Bundle containing the saved instance. Not used.
     * @since 1.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard screen setup.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Hide the action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Create the requestqueue for communication with the API.
        queue = Volley.newRequestQueue(this);

        // Hardcode which learning objectives and dates will be used for API communication.
        loids = new ArrayList<>(Arrays.asList("8025", "7789", "7771"));
        dates = new ArrayList<>(Arrays.asList("2017-11-28", "2017-11-29", "2017-11-30",
                "2017-12-01"));

        // Register objects for easier handling.
        username = findViewById(R.id.username);
        usr_password = findViewById(R.id.usr_password);
        login = findViewById(R.id.sing_in);

        // Set listener to login button
        login.setOnClickListener(this);

        // Get date of today
        Calendar cal = Calendar.getInstance();
        cal.setTime(Calendar.getInstance().getTime());
        USAGEDAY = cal.get((Calendar.DAY_OF_WEEK))-2; // Monday =0, Tuesday=1 etc.
    }

    /**
     * Handles what happens when a click is registered. Only handles case of login bundle being
     * clicked. In that case it changes the text of the login button and starts the process of
     * logging in.
     *
     * @author Rick Dijkstra
     *
     * @param view  the screen in which the onClick action is happening
     *
     * @since 1.0
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sing_in) {
            // Retrieve values needed to check whether login is valid.
            usrnm = username.getText().toString();
            String psswrd = usr_password.getText().toString();

            // Change text on login button to show that program is busy logging in.
            login.setText("Inloggen...");

            // Start Logging in.
            startLoggingIn(usrnm, psswrd);
        }
    }

    /**
     * Handles the starting phase of logging in.
     * <p>First checks with the API whether the username and password are given correctly. Shows a
     * popup message if that this is not the case. Alerts the user if there is some connection error
     * and asks it to try again.
     * <p>Starts requesting user data from the API when the username and password match. Starting
     * with the locations of the flags.
     * <p>All contact with the API is through authorization security
     *
     * @author Rick Dijkstra
     * @author Katrin Bujari
     * @param user  The username given by the user in the corresponding EditText field.
     * @param pass  The password given by the user in the corresponding EditText field.
     * @since 1.0
     */
    private void startLoggingIn(final String user, final String pass){
        // Setting up URL to request the user ID and password from the API.
        final String url = "http://applab.ai.ru.nl:5000/login_users/username="+user;

        // Creating the request for the log in (long function).
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    /**
                     * Starts when the Listener has received an answer from the API. Checks whether
                     * the given password and stored password match. Creates a message if passwords
                     * do not match. Otherwise starts retrieving the rest of the data starting with
                     * the flags.
                     *
                     * @author Rick Dijkstra
                     * @author Katrin Bujari
                     * @param response  JSONObject containing the username, password and ID for the
                     *                  user as specified in the url.
                     * @since 1.0
                     */
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        try
                        {
                            if (response.length() > 0) { // The username is in our database
                                // Get first instance in database (In case of duplicates).
                                JSONObject users = response.getJSONObject(0);

                                // Store the password.
                                String Passw = users.getString("Password");

                                if (pass.equals(Passw)) { // Password is correct
                                    // Store user ID
                                    User_id = users.getString("UserId");

                                    // Start retrieving data with the flags.
                                    getFlags(User_id);
                                } else { // Passwords do not match. Give error message.
                                    Toast.makeText(getApplicationContext(), "Gebruikersnaam" +
                                            " of wachtwoord is fout. Probeer het nog een keer.",
                                            Toast.LENGTH_SHORT).show();

                                    // Reset text of login button.
                                    login.setText("Log in");
                                }
                            }
                        } catch (JSONException e) { // If the response is not the correct object.
                            // Show error message on screen with pop up.
                            Toast.makeText(getApplicationContext(), "Er ging iets fout " +
                                    "tijdens het inlogggen. Probeer het nog een keer.",
                                    Toast.LENGTH_SHORT).show();

                            // Reset text of login button.
                            login.setText("Log in");

                            // Print Stack Trace for debugging.
                            e.printStackTrace();
                        }
                    }
        }, new Response.ErrorListener()
        {
            /**
             * Handles errors happening during retrieving of information from the API. Only errors
             * being handled are Timeout and Server error.
             *
             * @author Rick Dijkstra
             * @param error VolleyError describing the error. Has information about what happened.
             * @since 1.0
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (error instanceof TimeoutError || error instanceof ServerError){
                    // Create error message on screen with a pop up.
                    Toast.makeText(LogIn.this,"Er ging iets fout tijdens het " +
                            "inlogggen. Probeer het nog een keer", Toast.LENGTH_SHORT).show();
                    login.setText("Inloggen");
                }

                // Print Stack Trace for debugging.
                error.printStackTrace();
            }
        }){
            /**
             * Adds headers to the request in order to authenticate the app and get the data from
             * the API.
             *
             * @author Alessandro Ardu
             * @return the headers to be added to the request.
             * @since 1.0
             */
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String credentials = "Group2:Group2-1234";
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        }; // End of creating the request

        // Add the request to the queue
        queue.add(arrReq);
    }

    /**
     * Retrieves the coordinates of the flags that the user has entered before from the API.
     * Receives all flag coordinates at once in a list, where coordinates that have not yet been
     * entered are represented as -1.
     * <p>Stores the coordinates in the class flagPositions, sets the flag for flags being received
     * to true, calls method to retrieve ELO score and coordinates.
     *
     * @author Rick Dijkstra
     * @param user_id   String of the ID of the user, used to retrieve the correct flags from the
     *                  API.
     * @since 1.0
     * @see FlagPositions
     */
    private void getFlags(final String user_id){
        // Create new instance to store positions.
        flagPositions = new FlagPositions();

        // Set up URL for API
        final String flagurl = "http://applab.ai.ru.nl:5000/list_flag_positions";

        // Create request to API to receive flag positions.
        JsonArrayRequest flagrequest = new JsonArrayRequest(Request.Method.GET, flagurl,
                null, new Response.Listener<JSONArray>() {
            /**
             * Starts when the listener has a response from the API.
             * @param response
             */
            @Override
            public void onResponse(JSONArray response) {
                for(int i =0; i<response.length();i++) {
                    try {
                        JSONObject flag_block = response.getJSONObject(i);
                        if (flag_block.getString("UserId").equals(user_id)) {
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
                    Toast.makeText(LogIn.this,"Er ging iets fout tijdens het " +
                            "inlogggen. probeer het nog een keer", Toast.LENGTH_SHORT).show();
                    login.setText("Inloggen");
                }
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
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
        String curlbase = "http://applab.ai.ru.nl:5000/fast_m2m/user_id=";
        final String eurlbase = "http://applab.ai.ru.nl:5000/scores/day=";
        for (int day=0; day<4; day++){
            final int d = day;
            int reps = day==3? 3:1;
            for (int rep = 0; rep < reps; rep++) {
                final int r = rep;
                if (d>=USAGEDAY){
                    eloScores2[d+r] = (float) 0;
                    apiValuesReceived +=2;
                    Log.d("elonumber", String.valueOf(apiValuesReceived));
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
                                                        apiValuesReceived++;
                                                        Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                        Log.d("Elo_ready", String.valueOf(flagPositions.isFlagsreceived()));
                                                        if (isReady()) {
                                                            allReady();
                                                        }

                                                    } catch (JSONException e) {
                                                        eloScores2[d+r] = (float) 0;
                                                        apiValuesReceived++;
                                                        Log.d("elonumber", String.valueOf(apiValuesReceived));
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
                                        public Map<String, String> getHeaders() {
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
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 1:
                                                    coordarrayday2 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday2[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 2:
                                                    coordarrayday3 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday1[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 3:
                                                    coordarrayday4 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday4[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 4:
                                                    coordarrayday5 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday5[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 5:
                                                    coordarrayday6 = new float[jsonArray.length()];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday6[i] = (float) jsonArray.getDouble(i);
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
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
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 1:
                                                    coordarrayday2 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday2[i] = (float) 0;
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 2:
                                                    coordarrayday3 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday1[i] = (float) 0;
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 3:
                                                    coordarrayday4 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday4[i] = (float) 0;
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 4:
                                                    coordarrayday5 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday5[i] = (float) 0;
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                    break;
                                                case 5:
                                                    coordarrayday6 = new float[1];
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        coordarrayday6[i] = (float) 0;
                                                    }
                                                    apiValuesReceived++;
                                                    Log.d("elonumber", String.valueOf(apiValuesReceived));
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
                                                apiValuesReceived++;
                                                Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                break;
                                            case 1:
                                                coordarrayday2 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday2[i] = (float) 0;
                                                }
                                                apiValuesReceived++;
                                                Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                break;
                                            case 2:
                                                coordarrayday3 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday1[i] = (float) 0;
                                                }
                                                apiValuesReceived++;
                                                Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                break;
                                            case 3:
                                                coordarrayday4 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday4[i] = (float) 0;
                                                }
                                                apiValuesReceived++;
                                                Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                break;
                                            case 4:
                                                coordarrayday5 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday5[i] = (float) 0;
                                                }
                                                apiValuesReceived++;
                                                Log.d("elonumber", String.valueOf(apiValuesReceived));
                                                break;
                                            case 5:
                                                coordarrayday6 = new float[1];
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    coordarrayday6[i] = (float) 0;
                                                }
                                                apiValuesReceived++;
                                                Log.d("elonumber", String.valueOf(apiValuesReceived));
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
                        public Map<String, String> getHeaders() {
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

    private boolean isReady() {
        return flagPositions.isFlagsreceived() && apiValuesReceived >= 12;
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
