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
 * Handles the screen(s) that visualize the progress. Sets up the screen, moves the car and seekbar,
 * has logic to place and save flags. And to move between the visualization of the different
 * learning goals.
 * <p>These learning goals are handled in different classes, but will in the future be handled in
 * just this class. </p>
 * <p>Known bug after field study: wrong indices for which ELO score and car
 * coordinates array will be used.</p>
 *
 * @author Rick Dijkstra
 * @version 1.0, 5 jun 2018
 */
public class Math_1 extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    /** Representation of the driving car. */
    ImageView carr;

    /** Representation of the car that will be grayed out when there are more days. */
    ImageView carr2;

    /** Representation of the flags. */
    ImageView flag_1;

    /** Representation of the flags. */
    ImageView flag_2;

    /** Representation of the seekbar thumb that will be grayed out. */
    ImageView baseballmirror;

    /** Representation of the seekbar. */
    SeekBar pbar;

    /** Representation of the button to place new flags. */
    Button add_flag_button;

    /** Store the user ID for handling with the API. */
    String userid;

    /** Needed to send requests to the API. */
    RequestQueue queue;

    /** List of where the flags are positioned. */
    FlagPositions flagPositions;

    /** List of ELO scores. */
    float[] eloScores2;

    /** A list for car coordinates. */
    float[] coordarrayday1;
    /** A list for car coordinates. */
    float[] coordarrayday2;
    /** A list for car coordinates. */
    float[] coordarrayday3;
    /** A list for car coordinates. */
    float[] coordarrayday4;
    /** A list for car coordinates. */
    float[] coordarrayday5;
    /** A list for car coordinates. */
    float[] coordarrayday6;

    /** Copy of car coordinates that will be used in this visualization. */
    float[] coords1;
    /** Copy of car coordinates that will be used in this visualization. */
    float[] coords2;

    /** Determines the behaviour of the second car. */
    boolean two_days;

    /** Representation of what day we are at. */
    int USAGEDAY;

    /** The width of the screen of the tablet. */
    int width;

    /** The ID of the flag that is moveable. */
    int flag_id;

    /** Whether the flag can be moved by the user. */
    boolean moveflag = false;

    /**
     * Prevents the app from going back to the previous screen. Instead pops up a menu that asks the
     * user to either log out, which starts the login screen, or to not log out, which dismisses the
     * pop up message.
     * @since 1.0
     */
    @Override
    public void onBackPressed() {
        // Set the options for the user to pick.
        CharSequence options[] = new CharSequence[] {"Ja, uitloggen", "nee, blijven"};

        // Create the pop up message.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wil je uitloggen?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            /**
             * Handles what happens on a click. If option is to log out, starts the login screen.
             * @param dialog    representation of the pop up menu
             * @param which     representation of which choice is made.
             * @since 1.0
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    Intent intent = new Intent(Math_1.this, LogIn.class);
                    startActivity(intent);
                }

            }
        });
        // Show the popup menu.
        builder.show();
    }

    /**
     * Handles what happens at the start of the creation of the screen. Sets up representation of
     * various objects, listeners for buttons and flags, the queue for the requests and the
     * parameters for the seekbar. Unpacks the variables which are sent from the previous screen.
     *
     * @param savedInstanceState    Unused instance for if the screen was saved.
     * @since 1.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_1);

        // Get representations of the objects in the screen.
        carr = findViewById(R.id.carr);
        carr2 = findViewById(R.id.carr2);
        baseballmirror = findViewById(R.id.thumb_mirror);
        pbar = findViewById(R.id.pbar);
        add_flag_button = findViewById(R.id.button5);
        flag_1 = findViewById(R.id.imageView5);
        flag_2 = findViewById(R.id.imageView4);

        // Set the listeners for the buttons and flags.
        add_flag_button.setOnClickListener(this);
        flag_1.setOnTouchListener(this);
        flag_2.setOnTouchListener(this);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.Button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);

        // Unpack the variables
        Intent intent = getIntent();
        userid = intent.getStringExtra("userID");
        flagPositions = intent.getParcelableExtra("flagpositions");
        coordarrayday1 = intent.getFloatArrayExtra("coord1");
        coordarrayday2 = intent.getFloatArrayExtra("coord2");
        coordarrayday3 = intent.getFloatArrayExtra("coord3");
        coordarrayday4 = intent.getFloatArrayExtra("coord4");
        coordarrayday5 = intent.getFloatArrayExtra("coord5");
        coordarrayday6 = intent.getFloatArrayExtra("coord6");
        coords1 = coordarrayday1; // Different per learning goal
        coords2 = coordarrayday2; // Different per learning goal
        eloScores2 = intent.getFloatArrayExtra("eloscores");
        Bundle b = intent.getExtras();
        USAGEDAY = b.getInt("USAGEDAY");

        // Set up queue for requests.
        queue = Volley.newRequestQueue(this);

        // Set the values of the seekbar.
        pbar.setMax(100);
        pbar.setProgress(0);
    }

    /**
     * Gets called after the screen is created, gets the size of the screen. Starts the movement of
     * the car and seekbar.
     *
     * TODO: execute allReady only after the first time.
     * @param hasFocus  Unused. Whether the window is focused.
     * @since 1.0
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Get the display which has the width.
        Display display = getWindowManager().getDefaultDisplay();

        // Retrieve size of window in point format.
        Point size = new Point();
        display.getSize(size);

        // Border so the car won't go off screen/
        width = size.x - 20;

        // Start animation of car and seekbar.
        allReady();
    }

    /**
     * Handles animation and visibility of cars, flags, flag button and seekbar. Starts once the
     * screen is set up.
     *
     * @since 1.0
     */
    public void allReady(){
        // Check what behaviour is needed depending on the day.
        if (USAGEDAY < 4){
            flag_id = R.id.imageView5; // Set movable flag to be the first flag.
            two_days = false; // There is no data about a second day
            carr2.setVisibility(View.INVISIBLE); // The second car will not be shown.
        } else{
            flag_id = R.id.imageView4; // set flag that is handled to be second flag
            add_flag_button.setVisibility(View.INVISIBLE); // There is no flag to be added left.
            two_days = true; // We have data for two days.
        }
        if (USAGEDAY==3) flag_id = R.id.imageView4; // Second flag should be moveable on third day.

        // Set value of how far the car should be moved horizontal per exercise (or per coordinate).
        float elowidthday1 = coords1.length>1?width / coords1.length * eloScores2[0]:0;
        float elowidthday2 = coords2.length>1?width / coords2.length * (eloScores2[3] - eloScores2[0]):0;

        // Start animating the car.
        animateCar(elowidthday1, elowidthday2, coords1, coords2, two_days);

        // Set up animation of the seekbar.
        final Thread thread = new Thread(){
            /**
             * Sets up the animation. Moves the bar every 30 millisecond for one step until end
             * value is reached. Places the image for the grayed out thumb on the location of the
             * first day ELO score if there is data for two days.
             *
             * @since 1.0
             */
            public void run(){
                try {
                    // Move seekbar every 30 millisecond one step until at ELO position.
                    for (int i = 0; i < 100.* eloScores2[0]; i++){
                        pbar.setProgress(i);
                        sleep(30);
                    }

                    // Needed to handle different objects on screen. TODO: change hardcoded offsets
                    runOnUiThread(new Runnable() {
                        /**
                         * Sets image of grayed out thumb at location of ELO score of first day.
                         */
                        @Override
                        public void run() {
                            // Get value for where the thumbbar begins
                            int[] baseball_location = new int[2];
                            pbar.getLocationOnScreen(baseball_location);

                            // Set location of grayed out image based on location of bar and thumb.
                            baseballmirror.setX(baseball_location[0]+pbar.getThumb().getBounds().centerX()-pbar.getThumbOffset()-7);;
                            baseballmirror.setY(baseball_location[1]+pbar.getThumb().getBounds().centerY()-pbar.getBottom()-15);

                            // Show grayed out image.
                            baseballmirror.setVisibility(View.VISIBLE);
                        }
                    });

                    // Move bar for the second time to the second ELO score.
                    // Todo: make it move back when needed.
                    for (int i = pbar.getProgress(); i<(100. * eloScores2[3]); i++) {
                        pbar.setProgress(i);
                        sleep(30);
                    }
                }catch (Exception e){ // For debugging the thread going wrong. Print stack trace.
                    e.printStackTrace();
                }
            }
        };

        // Set up location and visibility of flags.
        if (flagPositions.getCoord(1)>=0){
            findViewById(R.id.imageView5).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView5).setX(flagPositions.getCoord(1));
        } else {
            findViewById(R.id.imageView5).setVisibility(View.INVISIBLE);
        }
        if (flagPositions.getCoord(2)>=0){
            findViewById(R.id.imageView4).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView4).setX(flagPositions.getCoord(2));
        } else {
            findViewById(R.id.imageView4).setVisibility(View.INVISIBLE);
        }

        // Start animation of seekbar.
        thread.start();
    }

    /**
     * Handles the animation of the car. Moves the car horizontally until at location of ELO score.
     * Moves car vertically according to the values of the car coordinate array. If there is data
     * for two days, also sets grayed out car image at location of first ELO score.
     *
     * @param x1            ELO score of first time
     * @param x2            ELO score of second time
     * @param coordarray1   car coordinates of the first time
     * @param coordarray2   car coordinates of the second time
     * @param move_twice    whether there is data for a second time
     *
     * @since 1.0
     */
    private void animateCar(final float x1, final float x2, final float[] coordarray1, final float[] coordarray2, final boolean move_twice){
        // Set up animation sets for both days
        AnimationSet s = new AnimationSet(false);
        AnimationSet s2 = new AnimationSet(false);

        // Let car stay at position after the animation
        s.setFillAfter(true);
        s2.setFillAfter(true);

        // Add animations for the first car
        float old_y=0; // Store old location of car.
        for (int i = 0; i < coordarray1.length; i++)
        { //move car first time
            float y = coordarray1[i] * 50; // 50 is scaling the vertical position.
            if (i>0){ // get relative distance of new location of car.
                y = y - old_y;
            }
            old_y = coordarray1[i]*50;

            // add the animations
            Animation anm = new TranslateAnimation(0, x1, 0, y);
            Animation anm2 = new TranslateAnimation(0, x1, 0, y);

            // Set length of animation.
            anm.setDuration(150);
            anm2.setDuration(150);

            // Set timing offset of animation.
            anm.setStartOffset(150*i);
            anm2.setStartOffset(150*i);
            s2.addAnimation(anm2); // Let grayed out car end up at same place as normal car
            s.addAnimation(anm);
        }

        if (move_twice) { // If there is data for a second day
            // Show grayed out car
            int new_car_delay = 500; //ms waiting before moving again

            // Set animation to let grayed car show up gradually
            Animation anm_vis = new AlphaAnimation(0,1);
            anm_vis.setDuration(new_car_delay);
            anm_vis.setStartOffset(coordarray1.length*150);
            s2.addAnimation(anm_vis);

            // Set up animation for second car.
            for (int i = 0; i < coordarray2.length; i++) {
                float y = coordarray2[i] * 50;
                if (i>0){
                    y = y - old_y;
                }
                old_y = coordarray2[i]*50;

                Animation anm = new TranslateAnimation(0, x2, 0, y);
                anm.setDuration(150);
                anm.setStartOffset(150*(i+coordarray1.length)+new_car_delay);
                s.addAnimation(anm);
            }
        }
        // start first animation, provided that there are coordinates to show
        if ((coordarray1.length+coordarray2.length)>0) carr.startAnimation(s);
        // If data for second time exists, start second animation.
        if (move_twice && coordarray1.length>0) carr2.startAnimation(s2);
    }

    /**
     * Handles what happens on a click on either the flag button or the learning goal buttons.
     * @param v representation of what is clicked.
     * @since 1.0
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button5: // Flag button
                // Select id of which flag is used based on what day it is.
                int ivid = (USAGEDAY < 4) ? R.id.imageView5: R.id.imageView4; //view representation.
                int flag = (USAGEDAY < 4) ? 1: 2; // coordinate id representation
                findViewById(ivid).setVisibility(View.VISIBLE); // Show flag.
                if (moveflag){ // Flag was moving must now be saved.
                    // Set text of flag button
                    add_flag_button.setText("Verplaats vlag");

                    // get location of flag.
                    int[] location = new int[2];
                    findViewById(ivid).getLocationOnScreen(location);

                    // Save location of flag on the server.
                    save_flag_state(flag, location[0], 1);

                    // Save location of flag locally.
                    flagPositions.setCoord((int) findViewById(ivid).getX(),flag);
                } else { // Flag was not moving, make movable.
                    add_flag_button.setText("Sla vlag-positie op");
                }

                moveflag = !moveflag; // switch state of button
                break;
            case R.id.Button2: // Button for learning goal 2.
                // Pack variables
                Bundle b = new Bundle();
                b.putInt("USAGEDAY", USAGEDAY);
                Intent intent2 = new Intent(this, Math_2.class);
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

                // Start screen of second learning goal. This screen will in the future be merged
                // with this screen.
                startActivity(intent2);
                break;
            case R.id.button3: // Button for learning goal 3.
                // Pack variables.
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

                // Start screen of third learning goal. This screen will in the future be merged
                // with this screen.
                startActivity(intent3);
                break;
        }
    }

    /**
     * Listens for touch actions on the moveable flag. Moves flag to new position if it is being
     * dragged. Only drags horizontally.
     * @param v     representation of object being touched.
     * @param event representation of nature of touch action
     * @return      whether we want to continue listening to touch events on this view.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // BUG: flag_id should be ivid. Now the second flag will never be moved.
        if (v.getId() == flag_id && moveflag) { // Flag being touched and being moveable.
            int action = event.getAction(); // Get representation of what touch action happened.

            if (action == MotionEvent.ACTION_DOWN) {
                // Update location of flag.
                findViewById(flag_id).setX(event.getRawX());
                return true;
            }

            if (action == MotionEvent.ACTION_MOVE){
                // Update location of flag
                findViewById(flag_id).setX(event.getRawX());
                return true;
            }
        }
        // Stop listening to non-relevant events.
        return false;
    }

    /**
     * Saves the location of the flag to the API. On error, keeps trying for 10 times, else pops up
     * message to try again.
     * @param flag      representation of which flag is being saved
     * @param x         location of the flag
     * @param attempt   how much times we have already tried to save the flag.
     */
    public void save_flag_state(final float flag, final float x, final int attempt){
        // Set up URL for API.
        String flagurl = "http://applab.ai.ru.nl:5000/save_flag_position/user_id="+userid+
                "&flag=Flag"+String.valueOf((int) flag)+"&flag_coord="+String.valueOf((int) x);

        // Send request to API
        queue.add(new JsonObjectRequest(Request.Method.GET, flagurl, null,
                new Response.Listener<JSONObject>() {
                    /**
                     * Listens to whether API makes a response that the flag is saved. Shows pop up
                     * to user that the flag is saved.
                     * @param response  Unused. The response given by the API.
                     */
            @Override
            public void onResponse(JSONObject response) {
                // Show pop up that flag is saved.
                Toast.makeText(Math_1.this, "Vlag opgeslagen.", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            /**
             * Handles errors that occur with trying to connect to the API. Will try 10 times to try
             * again.
             * @param error representation of the error.
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                if (attempt<10){ // Not yet tried 10 times.
                    //Try again with higher attempt count
                    save_flag_state(flag, x, attempt+1);
                } else {
                    // Show pop up that flag can't be saved
                    Toast.makeText(Math_1.this, "Vlag kon niet worden opgeslagen. Probeer het nog eens.", Toast.LENGTH_SHORT).show();

                    // Set flag to be saveable again.
                    moveflag = !moveflag;

                    // Change text on flag button.
                    add_flag_button.setText("Sla vlag-positie op");
                }
                // For debugging. Print stack trace.
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
        });
    }
}
