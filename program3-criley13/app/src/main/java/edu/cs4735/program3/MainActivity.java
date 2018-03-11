package edu.cs4735.program3;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    draw mdraw;
    String TAG = "MainActivity";
    public static final String ServiceId = "edu.cs4730.nearbyconnectiondemo";  //need a unique value to identify app.
    public static final int REQUEST_ACCESS_COURSE_LOCATION= 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            mdraw = new draw();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mdraw).commit();
        }
    }
}