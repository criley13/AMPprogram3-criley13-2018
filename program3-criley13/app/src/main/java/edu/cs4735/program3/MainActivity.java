package edu.cs4735.program3;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements StartFragment.OnFragmentInteractionListener,
        draw.OnDrawFragmentInteractionListener{

    // game board
    draw mdraw;

    // variable for tracking if you are the server or client
    int servCli = 0;

    String TAG = "MainActivity";

    //public static final String ServiceId = "edu.cs4735.program3";  //need a unique value to identify app.
    //public static final String ServiceId = "meh";

    // stuff for nearby
    // service id requested by Ward
    public static final String ServiceId = "edu.cs4730.nearbyconnectiondemo";

    public static final int REQUEST_ACCESS_COURSE_LOCATION= 1;
    public String UserNickName = "Server"; //idk what this should be.  doc's don't say.
    String ConnectedEndPointId;
    public static final Strategy STRATEGY = Strategy.P2P_STAR;

    // int for tracking whether to play again, could/should be a bool
    int playAgain = 0;

    // string used to track win or loss after the string stuff is used and lost
    String winOrTie;

    // used to track the player 1 or 2 (x or o)
    int player;

    Context mContext;

    // booleans for tracking advertising and discovering, might not be in use
    boolean mIsAdvertising = false;
    boolean mIsDiscovering = false;

    // check if playing again
    boolean playagaincheck = false;

    // fragment used to determine server and client.
    public StartFragment sf = new StartFragment();

    // boolean check for being allowed send tile or win messages
    boolean moveorwin = true;

    // has the game started
    boolean started = false;

    // stores the player choice
    String playerChoice;

    // stores the players agree or disagree or player x or player o
    String playerLetter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mContext = this;
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, sf).commit();

        }
    }

    /* interaction for start fragment
    *  If 1 then server was chosen
    *  if 2 then client was chosen
    *  after 1 or 2 make buttons returning 3 or 4 visible but disabled
    *  change the text view to inform users of pregame status
    *  if 3 and server was chosen, player x
    *  if 4 and server was chosen, player o
    *  if 3 and client was chosen, agree , then launch the board
    *  if 4 and client was chosen, disagree, then exit
    * */
    @Override
    public void onFragmentInteraction(int id) {

        getSupportFragmentManager().popBackStack();
        started = true;
        if(id == 1) {
            servCli = id;
            startAdvertising();
            Log.e(TAG, "Advert");
            sf.logger.setText("Waiting for connection.");
            sf.aBut.setVisibility(View.VISIBLE);
            sf.dBut.setVisibility(View.VISIBLE);
            sf.aBut.setEnabled(false);
            sf.dBut.setEnabled(false);
        }
        if(id == 2){
            servCli = id;
            startDiscovering();
            Log.e(TAG, "disco");
            sf.logger.setText("Waiting for connection.  When prompted by next message change agree or disagree.");
            sf.aBut.setVisibility(View.VISIBLE);
            sf.dBut.setVisibility(View.VISIBLE);
            sf.aBut.setEnabled(false);
            sf.dBut.setEnabled(false);
        }
        if(id == 3){
            Log.e("draw", String.valueOf(id));
            started = true;
            if(servCli == 1){
                playerLetter = "Player X";
                Log.e(TAG, "player x");
                player = 1;
                Log.e("sending", "Player X");
                send("Player X");
            }
            else if(servCli == 2){
                started = true;
                playerLetter = "agree";
                Log.e("sending", "agree");
                send(playerLetter);
                Log.e(TAG, "agree");
                if(playerChoice.startsWith("Player X"))
                    player = 2;
                else if(playerChoice.startsWith("Player O"))
                    player = 1;
                mdraw = new draw(player);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, mdraw).commit();
                getSupportFragmentManager().beginTransaction().remove(sf).commit();
            }
        }
        if(id == 4){
            Log.e("draw", String.valueOf(id));
            started = true;
            if(servCli == 1){
                playerLetter = "Player O";
                Log.e(TAG, "player o");
                player = 2;
                Log.e("sending", "Player O");
                send("Player O");
            }
            else if(servCli == 2){
                playerLetter = "disagree";
                Log.e("sending", "disagree");
                send(playerLetter);
                Log.e(TAG, "disagree");
                System.exit(0);
            }
        }
    }

    // listens to the game board and sends the selected square
    @Override
    public void onDrawFragmentInteraction(int id, int p) {
        Log.e("sending ", String.valueOf(id));
        send(String.valueOf(id));
    }

    // start advertising, from ward
    private void startAdvertising() {

        Nearby.getConnectionsClient(mContext).startAdvertising(
                UserNickName,
                ServiceId,
                mAdvertConnectionLifecycleCallback,
                new AdvertisingOptions(STRATEGY))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We're advertising!
                                Log.e(TAG,"We're advertising");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG,"Not advert");
                                // We were unable to start advertising.
                            }
                        });
    }

    // from ward
    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                     // An endpoint was found!
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                }
            };

    // from ward
    protected void startDiscovering() {
        Nearby.getConnectionsClient(mContext).
                startDiscovery(
                        MainActivity.ServiceId,   //id for the service to be discovered.  ie, what are we looking for.

                        new EndpointDiscoveryCallback() {  //callback when we discovery that endpoint.
                            @Override
                            public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                                //we found an end point.
                                Log.e(TAG,"We found an endpoint " + endpointId + " name is " + info.getEndpointName());
                                //now make a initiate a connection to it.
                                makeConnection(endpointId);
                            }

                            @Override
                            public void onEndpointLost(String endpointId) {
                                Log.e(TAG,"End point lost  " + endpointId);
                            }
                        },

                        new DiscoveryOptions(MainActivity.STRATEGY))  //options for discovery.
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                mIsDiscovering = true;
                                Log.e(TAG,"We have started discovery.");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mIsDiscovering = false;
                                Log.e(TAG,"We failed to start discovery.");
                                e.printStackTrace();
                            }
                        });

    }

    // from ward
    public void makeConnection(String endpointId) {
        Nearby.getConnectionsClient(mContext)
                .requestConnection(
                        UserNickName,   //human readable name for the local endpoint.  if null/empty, uses device name or model.
                        endpointId,
                        mDiscoConnectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                Log.e(TAG,"Successfully requested a connection");
                                // We successfully requested a connection. Now both sides
                                // must accept before the connection is established.
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Nearby Connections failed to request the connection.
                                Log.e(TAG,"failed requested a connection");
                                e.printStackTrace();
                            }
                        });

    }

    // connection life cycle callback for advertising
    // used for specific messages only pertaining to the advertiser, then uses generic messages
    // in callbackstuff
    private final ConnectionLifecycleCallback mAdvertConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.e("Advert","Connection Initiated :" + endpointId + " Name is " + connectionInfo.getEndpointName());

                    // Automatically accept the connection on both sides.
                    // setups the callbacks to read data from the other connection.
                    Nearby.getConnectionsClient(mContext).acceptConnection(endpointId, //mPayloadCallback);
                            new PayloadCallback() {
                                @Override
                                public void onPayloadReceived(String endpointId, Payload payload) {

                                    if (payload.getType() == Payload.Type.BYTES) {
                                        String stuff = new String(payload.asBytes());
                                        Log.e("Advert","Received data is " + stuff);

                                        // if the client agreed to X or O set started true (game has started) open game board frag
                                        if (stuff.startsWith("agree") && started) {
                                            started = false;
                                            mdraw = new draw(player);
                                            getSupportFragmentManager().beginTransaction()
                                                    .add(R.id.container, mdraw).commit();
                                            getSupportFragmentManager().beginTransaction().remove(sf).commit();
                                        }

                                        // additional callback message handling used by both advertise and discover, reuses string stuff
                                        callbackstuff(stuff);


                                    } else if (payload.getType() == Payload.Type.FILE)
                                        Log.e("Advert","We got a file.  not handled");
                                    else if (payload.getType() == Payload.Type.STREAM)
                                        //payload.asStream().asInputStream()
                                        Log.e("Advert","We got a stream, not handled");
                                }

                                @Override
                                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate) {
                                    //if stream or file, we need to know when the transfer has finished.  ignoring this right now.
                                }
                            });
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    Log.e(TAG,"Connection accept :" + endpointId + " result is " + result.toString());

                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            ConnectedEndPointId = endpointId;
                            //if we don't then more can be added to conversation, when an List<string> of endpointIds to send to, instead a string.
                            // ... .add(endpointId);

                            stopAdvertising();  //and comment this out to allow more then one connection.

                            Log.e("Advert","Status ok, sending player message: " + playerLetter);

                            // connection established enable buttons and if playerletter is not null send it
                            sf.logger.setText("Select X or O");
                            sf.aBut.setEnabled(true);
                            sf.dBut.setEnabled(true);
                            if(playerLetter!=null)send(playerLetter);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.e("Advert","Status rejected.  :(");
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.e("Advert","Status error.");
                            // The connection broke before it was able to be accepted.
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.e("Advert","Connection disconnected :" + endpointId);
                    ConnectedEndPointId = "";  //need a remove if using a list.
                }
            };


    //the connection callback for discovery, handles messages specific to disco then uses generics in callbackstuff
    private final ConnectionLifecycleCallback mDiscoConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    // setups the callbacks to read data from the other connection.
                    Nearby.getConnectionsClient(mContext).acceptConnection(endpointId, //mPayloadCallback);
                            new PayloadCallback() {
                                @Override
                                public void onPayloadReceived(String endpointId, Payload payload) {

                                    if (payload.getType() == Payload.Type.BYTES) {
                                        String stuff = new String(payload.asBytes());
                                        Log.e("disco","Received data is " + stuff);

                                        // if received message is a player choice...
                                        if (stuff.startsWith("Player X")||stuff.startsWith("Player O")) {
                                            // if starting the game go here.  enable buttons for agreeing, set booleans for gameflow and inform the user or needed interaction
                                            if(started) {
                                                sf.logger.setText("Server wants to be " + stuff + ".  Do you agree or disagree?");
                                                Log.e(String.valueOf(servCli), playagaincheck + " " + started);
                                                sf.aBut.setEnabled(true);
                                                sf.dBut.setEnabled(true);
                                                playerChoice = stuff;
                                                started =false;
                                                playagaincheck = true;
                                                //send(playerLetter);
                                            }
                                            // this is for replaying, similar to above but with a dialog
                                            else if(playagaincheck){
                                                started =false;
                                                moveorwin = true;
                                                playerChoice = stuff;
                                                playAgainC(stuff);
                                            }
                                        }
                                        started =false;
                                        callbackstuff(stuff);

                                    } else if (payload.getType() == Payload.Type.FILE)
                                        Log.e("disco","We got a file.  not handled");
                                    else if (payload.getType() == Payload.Type.STREAM)
                                        //payload.asStream().asInputStream()
                                        Log.e("disco","We got a stream, not handled");
                                }

                                @Override
                                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate) {
                                    //if stream or file, we need to know when the transfer has finished.  ignoring this right now.
                                }
                            });
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            stopDiscovering();
                            ConnectedEndPointId = endpointId;

                            Log.e("disco","Status ok, sending Hi message");
                            //send("Hi from Discovery");
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.e("disco","Status rejected.  :(");
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.e("disco","Status error.");
                            // The connection broke before it was able to be accepted.
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    Log.e("disco","Connection disconnected :" + endpointId);
                    ConnectedEndPointId = "";
                }
            };

    // used to handle shared callbacks for both advertise and discover
    public void callbackstuff(String stuff){
        // check number messages
        if(stuff.startsWith("1")||stuff.startsWith("2")||stuff.startsWith("3")
                ||stuff.startsWith("4")||stuff.startsWith("5")||stuff.startsWith("6")
                ||stuff.startsWith("7")||stuff.startsWith("8")||stuff.startsWith("9"))
        {
            // check the move, if good send agree
            int grid = Integer.parseInt(stuff);
            --grid;

            //check if move is legal, send agree if so.  Then add mark to board and change label at top of game
            if(mdraw.gridCheck[grid]==0)
            {
                Log.e("Send", "agree");
                send("agree");
                if(player == 1){
                    mdraw.gridCheck[grid] = 2;
                    mdraw.drawO(grid);
                    mdraw.turnLab.setText("Player 1 (X)/ You");
                    mdraw.turn=1;
                }
                else if(player == 2){
                    mdraw.gridCheck[grid] = 1;
                    mdraw.drawX(grid);
                    mdraw.turnLab.setText("Player 2 (O)/ You");
                    mdraw.turn = 2;
                }

                // check if the game is finished
                if(player ==1) {
                    mdraw.checkGame(2);
                }
                else {
                    mdraw.checkGame(1);
                }
            }
            // move is illegal send disagree and exit
            else {
                Log.e("sending", "disagree");
                send("disagree");
                System.exit(0);
            }

        }

        // check disagree meessages and send disagree and exit
        else if(stuff.startsWith("disagree")){
            send("disagree");
            System.exit(0);
        }

        // check agree messages, the game has already been launched, send win/no win/tie messages
        else if(stuff.startsWith("agree")&& !started && moveorwin)//
        {
            //check for win in board and send appropriate response
            if(mdraw.gamewin)
            {
                Log.e("Send", "winner");
                send("winner");
                winOrTie = "win";  // store weather game was won or tied for dialog
                moveorwin = false;  // no more moves or win checking for now
                if(servCli == 1)
                {
                    endgameServer(stuff);   // pop dialog for winning as server
                }
                else if(servCli == 2){
                    if(player == 1)
                        endgameClient1(stuff,2);  // pop dialog for winning as client
                    else
                        endgameClient1(stuff,1); // pop dialog for winning as client
                }

            }
            // check for tie in board send tie
            else if(mdraw.gametie)
            {
                Log.e("Send", "tie");
                send("tie");
                winOrTie = "tie";  // store weather game was won or tied for dialog
                moveorwin = false; // no more moves or win checking on agree for now
                if(servCli == 1)
                {
                    endgameServer(stuff);  // pop dialog for tie as server
                }
                else if(servCli == 2){
                    endgameClient1(stuff,0);  // pop dialog for tie as client
                }
            }
            // game not won or tied from board, send nowinner
            else if(!mdraw.gamewin && !mdraw.gametie){
                Log.e("Send", "nowinner");
                send("nonwinner");
            }
        }

        // check no win messages
        else if(stuff.startsWith("nowinner"))
        {
            // check win cons if no winner, send agree
            if(player ==1) {
                mdraw.checkGame(2);
            }
            else {
                mdraw.checkGame(1);
            }

            // send message to agree or disagree with nowinner
            if(!mdraw.gamewin&&!mdraw.gametie) {
                Log.e("sending", "agree");
                send("agree");
            }
            else{
                Log.e("sending", "disagree");
                send("disagree");
                System.exit(0);
            }

        }

        // check winner messages
        else if(stuff.startsWith("winner"))
        {
            // check win cons, if winner , send agree
            winOrTie = "win";   // sets dialog message to win

            // check for winner
            if(player ==1) {
                mdraw.checkGame(2);
            }
            else {
                mdraw.checkGame(1);
            }

            // if winner send agree, else disagree
            if(mdraw.gamewin) {
                Log.e("sending", "agree");
                send("agree");
                moveorwin = false; // no more moves or win checking on agree
                if(servCli == 1)
                {
                    endgameServer(stuff); // pop dialog for server
                }
                else if(servCli == 2){
                    if(player == 1)
                        endgameClient1(stuff,2);  // pop dialog for client
                    else
                        endgameClient1(stuff,1);  // pop dialog for client
                }
            }
            else{
                Log.e("sending", "disagree");
                send("disagree");
                System.exit(0);
            }
        }

        // check tie messages
        else if(stuff.startsWith("tie"))
        {
            // check win cons, if all squares filed, send agree
            winOrTie = "tie";  // set dialog for tie
            moveorwin = false; //no more moves or win checking on agree

            //check game status
            if(player ==1)
                mdraw.checkGame(2);
            else
                mdraw.checkGame(1);

            // if tie, send agree, else disagree
            if(mdraw.gametie) {
                Log.e("sending", "agree");
                send("agree");
                if(servCli == 1)
                {
                    endgameServer(stuff);   // server endgame dialog
                }
                else if(servCli == 2){
                    endgameClient1(stuff,0);  // client endgame dialog
                }
            }
            else{
                Log.e("sending", "disagree");
                send("disagree");
                System.exit(0);
            }
        }

        // if receiving playagain, set boolean and call client endgame dialog
        else if(stuff.startsWith("playagain")){
            playagaincheck = true;
            endgameClientPlayAgain(stuff);
        }

        // if receiving exit, exit the game
        else if(stuff.startsWith("exit")){
            System.exit(0);
        }

        // if receiving this agree to play again, open dialog for player selection
        else if(stuff.startsWith("agree") && playAgain == 1){
            playagaincheck = true;
            playAgainS(stuff);
        }
    }

    // dialog for server endgame, may ask to play again or exit
    public void endgameServer(String stuff){
        //dialog message stuff
        String s = "";
        String temp;
        if(mdraw.turn == 1)
            temp ="2/(O)";
        else
            temp = "1/(X)";
        if(winOrTie.startsWith("win"))
            s = "Player "+ temp + " wins!";
        else if(winOrTie.startsWith("tie"))
            s = "Game over, no more moves";

        // no more moves
        mdraw.GO = true;

        Dialog dialog = null;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(mContext);
        builder.setMessage(s)
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e("sending", "exit");
                        send("exit");
                        System.exit(0);
                    }
                }).setNegativeButton("Play Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.e("sending", "playagain");
                send("playagain");
                playagaincheck = true;
                playAgain += 1;
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    // reset booleans, board, label
    public void playAgain(){

        Arrays.fill(mdraw.gridCheck,0);
        mdraw.turn = 1;
        if(player == 1)
            mdraw.turnLab.setText("Player 1 (X)/ You");
        else
            mdraw.turnLab.setText("Player 1 (X)");
        mdraw.gametie =false;
        mdraw.resetBoard();
        mdraw.gamewin = false;
        mdraw.gametie = false;
        mdraw.GO = false;

        moveorwin = true;

        playAgain = 0;
        playagaincheck = false;
    }

    // client endgame. only shows who the winner is with ok button
    public void endgameClient1(String stuff,int winner){
        String s = "";
        String temp;
        if(mdraw.turn == 1)
            temp ="2/(O)";
        else
            temp = "1/(X)";
        if(winOrTie.startsWith("win"))
            s = "Player "+ temp + " wins!";
        else if(winOrTie.startsWith("tie"))
            s = "Game over, no more moves";

        mdraw.GO = true;

        Dialog dialog = null;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(mContext);
        builder.setMessage(s)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
        });
        dialog = builder.create();
        playagaincheck = true;
        dialog.show();
    }

    // client end game dialog.  Opens if server wishes to play again.  May agree or disagree (exit)
    public void endgameClientPlayAgain(String stuff){
        Dialog dialog = null;
        playagaincheck = true;
        started = false;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(mContext);
        builder.setMessage("Server wishes to play again.")
                .setCancelable(false)
                .setPositiveButton("Disagree", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e("sending", "disagree");
                        send("disagree");
                        System.exit(0);
                    }
                }).setNegativeButton("Agree", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.e("sending", "agree");
                send("agree");
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    // server dialog opened after playagain agree.  Asks server for player choice and sends
    public void playAgainS (String stuff){
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(mContext);
        builder.setMessage("Do you want to be X or O?")
                .setCancelable(false)
                .setPositiveButton("O", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e("sending", "Player O");
                        send("Player O");
                        player = 2;
                        mdraw.player = 2;
                        playAgain();
                    }
                }).setNegativeButton("X", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.e("sending", "Player X");
                send("Player X");
                player = 1;
                mdraw.player = 1;
                playAgain();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    // client response for player choice (only available after receiving that).  May agree or disagree (exit)
    public void playAgainC(String stuff){
        String s = "";

        Dialog dialog = null;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(mContext);
        builder.setMessage("Server wishes to be " + stuff)
                .setCancelable(false)
                .setPositiveButton("Disagree", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e("sending", "disagree");
                        send("disagree");
                        System.exit(0);
                    }
                }).setNegativeButton("Agree", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.e("sending", "agree");
                send("agree");
                if(playerChoice.startsWith("Player X")) {
                    player = 2;
                    mdraw.player = 2;
                }
                else if(playerChoice.startsWith("Player O")) {
                    player = 1;
                    mdraw.player = 1;
                }
                playAgain();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    /** Stops discovery. from ward*/
    protected void stopDiscovering() {
        mIsDiscovering = false;
        Nearby.getConnectionsClient(mContext).stopDiscovery();
        Log.e(TAG,"Discovery Stopped.");
    }

    // from ward
    public void stopAdvertising() {
        mIsAdvertising = false;
        Nearby.getConnectionsClient(mContext).stopAdvertising() ;
        Log.e(TAG,"Advertising stopped.");

    }

    /**
     * Sends a {@link Payload} to all currently connected endpoints.
     * from ward
     */
    protected void send(String data) {

        //basic error checking
        if (ConnectedEndPointId.compareTo("") == 0)   //empty string, no connection
            return;

        Payload payload = Payload.fromBytes(data.getBytes());

        // sendPayload (List<String> endpointIds, Payload payload)  if more then one connection allowed.
        Nearby.getConnectionsClient(mContext).
                sendPayload(ConnectedEndPointId,  //end point to end to
                        payload)   //the actual payload of data to send.
                .addOnSuccessListener(new OnSuccessListener<Void>() {  //don't know if need this one.
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG,"Message send successfully.");
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG,"Message send completed.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"Message send failed.");
                        e.printStackTrace();
                    }
                });
    }


}