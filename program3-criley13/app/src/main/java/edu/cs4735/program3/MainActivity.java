package edu.cs4735.program3;

import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.nearby.connection.Strategy;

public class MainActivity extends AppCompatActivity implements  StartFragment.OnFragmentInteractionListener {
    //draw mdraw;
    String TAG = "MainActivity";
    public static final String ServiceId = "edu.cs4735.program3";  //need a unique value to identify app.
    public static final int REQUEST_ACCESS_COURSE_LOCATION= 1;


    FragmentManager fragmentManager;

    public static final Strategy STRATEGY = Strategy.P2P_STAR;

    // on create for tic tac toe
    /*
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
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, new StartFragment()).commit();

    }

    @Override
    public void onFragmentInteraction(int id) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,

        if (id == 2) { //client
            transaction.replace(R.id.container, new ClientFragment());
        } else { //server
            transaction.replace(R.id.container, new ServerFragment());
        }

        //mdraw = new draw();
        //transaction.replace(R.id.container, mdraw);
        // and add the transaction to the back stack so the user can navigate back
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }
}