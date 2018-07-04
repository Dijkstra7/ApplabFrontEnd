package com.example.katrin.testsnepet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Handles overview of different topics. Since we used only one topic this class has been
 * deprecated.
 * @deprecated
 * @author Katrin Bujari
 */
public class Home extends AppCompatActivity implements View.OnClickListener{

    TextView resultview;
    String userid;
    String user;
    Button math;
    Button lite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        resultview = (TextView) findViewById(R.id.resultview);
        math = (Button) findViewById(R.id.math);
        lite = (Button) findViewById(R.id.lite);

        Intent intent = getIntent();
        userid = intent.getStringExtra("userID");
        user = intent.getStringExtra("username");
        resultview.setText("Hi "+user);
        math.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.math:
                Intent intent2 = new Intent(this, Math_1.class);
                intent2.putExtra("userID", userid);
                startActivity(intent2);
                break;

            case R.id.lite:
                lite.setEnabled(false);
                break;
        }
    }
}
