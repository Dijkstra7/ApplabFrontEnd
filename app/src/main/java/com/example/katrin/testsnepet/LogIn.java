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
 * <p>
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

    /** Used to send requests to the API*/
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
     * <p>Author: Rick Dijkstra
     * <p>Author: Katrin Bujari
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
     * <p>Author: Rick Dijkstra
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
     * <p>Author: Rick Dijkstra
     * <p>Author: Katrin Bujari
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
                     * <p>Author: Rick Dijkstra
                     * <p>Author: Katrin Bujari
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
             * being handled are Timeout and Server error. If that happens indicate to the user to
             * try again.
             *
             * <p>Author: Rick Dijkstra
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
             * <p>Author: Alessandro Ardu
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
     * <p>Author: Rick Dijkstra
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
             * Starts when the listener has a response from the API. Finds the correct set of
             * coordinates in a list of coordinates and users. Stores those values in a
             * FlagPositions instance and indicates that the flags have been received. Sets all
             * coordinates to the default value when there is no set of coordinates for the given
             * user. Calls method to retrieve the ELO scores and car coordinates after everything is
             * done.
             *
             * <p>Author: Rick Dijkstra
             * @param response  Should be a list of sets of flag coordinates. Where every entry has
             *                  a user ID and 6 coordinates
             * @since 1.0
             */
            @Override
            public void onResponse(JSONArray response) {
                for(int i =0; i<response.length();i++) { // Go through all objects.
                    try {
                        // Unpack a response object
                        JSONObject flag_block = response.getJSONObject(i);

                        // Checks whether this is the right object.
                        if (flag_block.getString("UserId").equals(user_id)) {
                            // Store coordinates for all flags (1-6).
                            for (int j = 1; j < 7; j++) {
                                int coord = flag_block.getInt("Flag" + String.valueOf(j));
                                flagPositions.setCoord(coord, j);
                            }

                            // Indicate that the coordinates have been stored.
                            flagPositions.setFlagsreceived(true);
                        }
                    } catch (JSONException e) { // When the JSONObject passed by the response throws
                                                // an error during unpacking.
                        e.printStackTrace();
                    }
                }

                // Check whether there has been a match in received list.
                if (!flagPositions.isFlagsreceived()){ // No match has happened.
                    // set all flags to -1 (default).
                    for (int j = 0; j < 6; j++) {
                        flagPositions.setCoord(-1, j+1);
                        flagPositions.setFlagsreceived(true);
                    }
                }

                // Start retrieving the ELO scores and car coordinates.
                getEloAndCoordinates(user_id);

            }
        },
            new Response.ErrorListener() {
                /**
                 * Handles errors happening during retrieving of information from the API. Only
                 * errors being handled are Timeout and Server error. If that happens indicate to
                 * the user to try again.
                 *
                 * <p>Author: Rick Dijkstra
                 * @param error VolleyError containing information about the error.
                 * @since 1.0
                 */
                @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof ServerError){
                    // Create error message pop up.
                    Toast.makeText(LogIn.this,"Er ging iets fout tijdens het " +
                            "inlogggen. probeer het nog een keer", Toast.LENGTH_SHORT).show();

                    // Reset the text on the log in button.
                    login.setText("Inloggen");
                }

                // Print stack trace for debugging.
                error.printStackTrace();
            }
        }){
            /**
             * Adds headers to the request in order to authenticate the app and get the data from
             * the API.
             *
             * <p>Author: Alessandro Ardu
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
        };

        // Add the request to the queue.
        queue.add(flagrequest);
    }

    /**
     * Tries to receive the ELO scores and car coordinate arrays from the API. Only retrieves for
     * the days that have passed. Other days are skipped.
     * <p>This function is a bit of a mess due to the inability of the app to marshall the classes
     * to send the coordinate arrays and ELO scores to the next screen.
     * <p>Author: Rick Dijkstra
     * @param userid    the ID of the user for which the data is retrieved from the API.
     */
    private void getEloAndCoordinates(final String userid) {
        // Allocate storage of ELO scores
        eloScores2 = new float[6];

        // Generate base URLs for both car coordinates and ELO scores
        String curlbase = "http://applab.ai.ru.nl:5000/fast_m2m/user_id=";
        final String eurlbase = "http://applab.ai.ru.nl:5000/scores/day=";

        // Data per day
        for (int day=0; day<4; day++){
            final int d = day;

            // At last day retrieve data from all learning goals not just one.
            int reps = day==3? 3:1;
            for (int rep = 0; rep < reps; rep++) {
                final int r = rep;

                // Check whether we need to retrieve data for this day. if not set value to 0.
                if (d>=USAGEDAY){ // Don't need to load data
                    eloScores2[d+r] = (float) 0;
                    apiValuesReceived +=2;

                    // Check whether we are ready to launch the new screen.
                    if (isReady()) allReady();
                } else { // Need to load data.
                    // Setup URL to retrieve car coordinates. Needs user ID, learning goal and day.
                    final String coordurl1 = curlbase + userid + "/loid=" + loids.get((day+rep)%3)+
                            "/day="+String.valueOf(day+1);

                    // Generate request for car coordinates
                    JsonArrayRequest arrReq1 = new JsonArrayRequest(Request.Method.GET, coordurl1,
                            null, new Response.Listener<JSONArray>() {
                        /**
                         * Starts when the listener has a response from the API. Starts a request
                         * for the ELO score. Stores the car coordinates in the right array. updates
                         * the number of received data fragments from the API and if all data is
                         * received calls the method to start the new screen.
                         * <p>Author: Rick Dijkstra</p>
                         * @param response  A JSONArray containing either the coordinates or {0}.
                         */
                        @Override
                        public void onResponse(final JSONArray response) {
                            // Set up URL to receive ELO scores. Needs User ID, Larning objective ID
                            // and day.
                            final String elourlday1 = eurlbase + String.valueOf(d+1) + "&user_id="
                                    + userid + "&learning_obj_id=" + loids.get((d+r)%3);

                            // Generate request for the ELO score.
                            final JSONArray jsonArray = response;
                            JsonArrayRequest eloScore1 = new JsonArrayRequest(Request.Method.GET, elourlday1, null,
                                    new Response.Listener<JSONArray>() {
                                        /**
                                         * Starts when the Listener has a response from the API.
                                         * Calculates relative value of the ELO score. Stores the
                                         * ELO score in the correct place in its array. Updates the
                                         * number of received fragments from the API and if all data
                                         * is received calls the method to start the new screen.
                                         * <p>Author: Rick Dijkstra
                                         * @param responseelo   A float containing the ELO value
                                         */
                                        @Override
                                        public void onResponse(JSONArray responseelo) {
                                            // Storage for ELO value.
                                            double elodouble;
                                            try {
                                                // Calculate relative value of ELO. Make sure it is
                                                // between 0 and 1.
                                                elodouble = max(min(responseelo.getDouble(0) / 600., 1.), 0.);

                                                // Store ELO score in right position in ELO array.
                                                eloScores2[d+r] = (float) elodouble;

                                                // Update number of data fragments received by API.
                                                apiValuesReceived++;

                                                // Check whether next screen can be called.
                                                if (isReady()) allReady();
                                            } catch (JSONException e) {// If JSONObject throws error
                                                // Set ELO score to default (0).
                                                eloScores2[d+r] = (float) 0;

                                                // Update number of data fragments received by API.
                                                apiValuesReceived++;

                                                // For debugging
                                                e.printStackTrace();

                                                // Check whether next screen can be called.
                                                if (isReady()) allReady();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                /**
                                 * When the API takes to long or gives an error, this pops up a
                                 * warning for the user to try again and resets the text of the log
                                 * in button. Additionally prints the stack trace.
                                 * <p>Author: Rick Dijkstra</p>
                                 * @param error VolleyError containing information about the error.
                                 */
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error instanceof TimeoutError ||
                                            error instanceof ServerError){
                                        // Show pop up message
                                        Toast.makeText(LogIn.this,"Er ging " +
                                                "iets fout tijdens het inlogggen. probeer" +
                                                " het nog een keer",
                                                Toast.LENGTH_SHORT).show();

                                        // Reset text on log in button
                                        login.setText("Inloggen");
                                    }
                                    // For debugging, print the stack trace.
                                    error.printStackTrace();
                                }
                            }){
                                /**
                                 * Adds headers to the request in order to authenticate the app and
                                 * get the data from the API.
                                 *
                                 * <p>Author: Alessandro Ardu
                                 * @return the headers to be added to the request.
                                 * @since 1.0
                                 */
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> headers = new HashMap<>();
                                    String credentials = "Group2:Group2-1234";
                                    String auth = "Basic "
                                            + Base64.encodeToString(credentials.getBytes(),
                                            Base64.NO_WRAP);
                                    headers.put("Content-Type", "application/json");
                                    headers.put("Authorization", auth);
                                    return headers;
                                }
                            };

                            // Add the request for the elo score to the queue
                            queue.add(eloScore1);

                            // Try to save the car coordinates
                            try {
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    switch (d+r) { // Select the right array based on the day and
                                                   // Learning goal used.
                                        case 0:
                                            // Set up array to save the coordinates
                                            coordarrayday1 = new float[jsonArray.length()];

                                            // Save the coordinates from the JSONArray.
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                coordarrayday1[i] = (float) jsonArray.getDouble(i);
                                            }

                                            // Increase counter for the data fragments received.
                                            apiValuesReceived++;
                                            break;
                                        case 1:
                                            coordarrayday2 = new float[jsonArray.length()];
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                coordarrayday2[i] = (float) jsonArray.getDouble(i);
                                            }
                                            apiValuesReceived++;
                                            break;
                                        case 2:
                                            coordarrayday3 = new float[jsonArray.length()];
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                coordarrayday1[i] = (float) jsonArray.getDouble(i);
                                            }
                                            apiValuesReceived++;
                                            break;
                                        case 3:
                                            coordarrayday4 = new float[jsonArray.length()];
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                coordarrayday4[i] = (float) jsonArray.getDouble(i);
                                            }
                                            apiValuesReceived++;
                                            break;
                                        case 4:
                                            coordarrayday5 = new float[jsonArray.length()];
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                coordarrayday5[i] = (float) jsonArray.getDouble(i);
                                            }
                                            apiValuesReceived++;
                                            break;
                                        case 5:
                                            coordarrayday6 = new float[jsonArray.length()];
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                coordarrayday6[i] = (float) jsonArray.getDouble(i);
                                            }
                                            apiValuesReceived++;
                                            break;
                                        default:
                                            // Something went wrong. Show this on the log.
                                            Log.d("error in cases case", String.valueOf(d+r));
                                    }

                                    // Check whether all data has been received to start new screen.
                                    if (isReady()) allReady();
                                } else { // JSONArray is empty, so store default value instead
                                    switch (d+r) {
                                        case 0:
                                            // Set up array for car coordinates
                                            coordarrayday1 = new float[1];

                                            // Set to default value.
                                            for (int i = 0; i < 1; i++) {
                                                coordarrayday1[i] = (float) 0;
                                            }

                                            // Update counter of data fragments received from API.
                                            apiValuesReceived++;
                                            break;
                                        case 1:
                                            coordarrayday2 = new float[1];
                                            for (int i = 0; i < 1; i++) {
                                                coordarrayday2[i] = (float) 0;
                                            }
                                            apiValuesReceived++;
                                            break;
                                        case 2:
                                            coordarrayday3 = new float[1];
                                            for (int i = 0; i < 1; i++) {
                                                coordarrayday1[i] = (float) 0;
                                            }
                                            apiValuesReceived++;
                                            break;
                                        case 3:
                                            coordarrayday4 = new float[1];
                                            for (int i = 0; i < 1; i++) {
                                                coordarrayday4[i] = (float) 0;
                                            }
                                            apiValuesReceived++;
                                            break;
                                        case 4:
                                            coordarrayday5 = new float[1];
                                            for (int i = 0; i < 1; i++) {
                                                coordarrayday5[i] = (float) 0;
                                            }
                                            apiValuesReceived++;
                                            break;
                                        case 5:
                                            coordarrayday6 = new float[1];
                                            for (int i = 0; i < 1; i++) {
                                                coordarrayday6[i] = (float) 0;
                                            }
                                            apiValuesReceived++;
                                            break;
                                        default:
                                            Log.d("Foutje in cases, case", String.valueOf(d+r));
                                    }
                                    // Check whether everything has been received.
                                    if (isReady()) allReady();
                                }
                            } catch (JSONException e) { // JSONArray unpacking failed.
                                switch (d+r) {
                                    case 0:
                                        // set up car coordinate array
                                        coordarrayday1 = new float[1];

                                        // Set it to default value.
                                        for (int i = 0; i < 1; i++) {
                                            coordarrayday1[i] = (float) 0;
                                        }

                                        //Update counter for data fragments received from API.
                                        apiValuesReceived++;
                                        break;
                                    case 1:
                                        coordarrayday2 = new float[1];
                                        for (int i = 0; i < 1; i++) {
                                            coordarrayday2[i] = (float) 0;
                                        }
                                        apiValuesReceived++;
                                        break;
                                    case 2:
                                        coordarrayday3 = new float[1];
                                        for (int i = 0; i < 1; i++) {
                                            coordarrayday1[i] = (float) 0;
                                        }
                                        apiValuesReceived++;
                                        break;
                                    case 3:
                                        coordarrayday4 = new float[1];
                                        for (int i = 0; i < 1; i++) {
                                            coordarrayday4[i] = (float) 0;
                                        }
                                        apiValuesReceived++;
                                        break;
                                    case 4:
                                        coordarrayday5 = new float[1];
                                        for (int i = 0; i < 1; i++) {
                                            coordarrayday5[i] = (float) 0;
                                        }
                                        apiValuesReceived++;
                                        break;
                                    case 5:
                                        coordarrayday6 = new float[1];
                                        for (int i = 0; i < 1; i++) {
                                            coordarrayday6[i] = (float) 0;
                                        }
                                        apiValuesReceived++;
                                        break;
                                    default:
                                        // Something went wrong with the cases.
                                        Log.d("Fout bij case", String.valueOf(d+r));
                                }
                                // Check whether the data has been received to create new screen.
                                if (isReady()) allReady();

                                // For debugging, print Stack Trace.
                                e.printStackTrace();
                            }
                        }
                            }, new Response.ErrorListener() {
                        /**
                         * When the API takes to long or gives an error, this pops up a
                         * warning for the user to try again and resets the text of the log
                         * in button. Additionally prints the stack trace.
                         * <p>Author: Rick Dijkstra</p>
                         * @param error VolleyError containing information about the error.
                         */
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof TimeoutError || error instanceof ServerError){
                                // Pops up error message.
                                Toast.makeText(LogIn.this,"Er ging iets fout tijdens" +
                                        " het inlogggen. probeer het nog een keer",
                                        Toast.LENGTH_SHORT).show();

                                // Reset text of log in button.
                                login.setText("Inloggen");
                            }
                            // For debugging, print stack trace.
                            error.printStackTrace();
                        }
                    }){
                        /**
                         * Adds headers to the request in order to authenticate the app and get the
                         * data from the API.
                         *
                         * <p>Author: Alessandro Ardu
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
                    };

                    // Add the request for the car coordinates.
                    queue.add(arrReq1);
                }
            }
        }
    }

    /**
     * Checks whether all data has been received from the API. This is done when the flags have been
     * received and there have been 12 instances of either ELO or car coordinates received which is
     * assumed to be all ELO scores and car coordinates.
     *
     * BUG: When the log in button is pressed again, the number of values received is not reset.
     *      However, this way we can ensure that we still go to the next screen if only part of the
     *      API is not received.
     *
     * Author: Rick Dijkstra
     *
     * @return  true if application is ready to go to the next screen.
     *
     * @since 1.0
     */
    private boolean isReady() {
        return flagPositions.isFlagsreceived() && apiValuesReceived >= 12;
    }

    /**
     * Prevents the back button from returning back to the previous screen. Instead closes the app.
     *
     * Author: Rick Dijkstra
     * @since 1.0
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Starts new screen once everything is ready to be send. Sets up an intent that passes on the
     * data of usageday, ELO scores, car coordinates, flag coordinates and user ID.
     *
     * <p>Author: Rick Dijkstra
     * @since 1.0
     */
    private void allReady(){
        // Create Bundle containing special values
        Bundle b = new Bundle();

        // Fill bundle
        b.putInt("USAGEDAY", USAGEDAY);

        // Create Intent to start the Math_1 screen
        Intent intent = new Intent(LogIn.this, Math_1.class);

        // Give extra variables to the intent
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

        // Start the new screen.
        startActivity(intent);
    }
}
