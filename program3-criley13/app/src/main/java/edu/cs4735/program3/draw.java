package edu.cs4735.program3;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.nearby.connection.PayloadCallback;

import java.util.Arrays;


public class draw extends Fragment {

    // label for displaying whose turn it is
    TextView turnLab;

    // track whose turn it is
    int turn =1;
    int player;
    String mEndpoint;

    // items needed for creating a space to draw on
    ImageView grid;
    Bitmap bm;
    final int boardsize = 990;
    Canvas mCanvas;

    // line colors
    Paint black, red, blue, white;

    PayloadCallback mpc;

    // track the filed grid squares
    int gridCheck[]= new int[9];

    public draw() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public draw(PayloadCallback payloadCallback, int player,String endpoint) {
        this.player = player;
        mpc = payloadCallback;
        mEndpoint = endpoint;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.fragment_draw, container, false);

        turnLab = myView.findViewById(R.id.turnView);
        if(turn == player)
            turnLab.setText("Player 1 (X)/ You");
        else
            turnLab.setText("Player 1 (X)");

        // initialize the image view and canvas for drawing on
        grid = myView.findViewById(R.id.imageView);
        bm = Bitmap.createBitmap(boardsize,boardsize,Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bm);
        mCanvas.drawColor(Color.WHITE);
        grid.setImageBitmap(bm);
        grid.setOnTouchListener(new myTouchListener());

        // initialize the paint black
        black = new Paint();
        black.setColor(Color.BLACK);
        black.setStyle(Paint.Style.FILL);
        black.setStrokeWidth(10);

        // initialize the paint red
        red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);
        red.setStrokeWidth(10);

        // initialize the paint blue
        blue = new Paint();
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);
        blue.setStrokeWidth(10);

        // initial the paint white
        white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL);
        white.setStrokeWidth(10);

        // fill the array gridcheck with zeros and draw lines on the boards
        Arrays.fill(gridCheck,0);
        resetBoard();
        return myView;
    }

    class myTouchListener
            implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            // get the position of the touch
            int x = (int) event.getX();
            int y = (int) event.getY();

            // fill int index with the return of posToIndex, gets the cell
            int index = posToIndex(x,y);

            //Log.e("index", String.valueOf(index));



            // if somewhere in the grid was selected
            if(index != -1) {
                // if the selected cell has not been selected before
                if (gridCheck[index] == 0) {

                    //redundant check for valid locations
                    if (x >= 0 && x <= 990 && y >= 0 && y <= 990) {
                        /*
                         *  If it is currently player one's turn
                         *  set the cell to 1 so it can't be changed, then draw an X (O for player 2)
                         *  on the square check if the game has ended, and then change turn if the
                          *  game is over
                         */

                        if (turn == 1) {
                            Log.e("1", "1");
                            gridCheck[index] = turn;
                            drawX(index);
                            //mpc.onPayloadReceived(mEndpoint);

                            checkGame(turn);
                            turn = 2;
                            if(turn == player)
                                turnLab.setText("Player 2 (O)/ You");
                            else
                                turnLab.setText("Player 2 (O)");

                        } else if (turn == 2) {
                            Log.e("2", "2");
                            gridCheck[index] = turn;
                            drawO(index);
                            checkGame(turn);
                            turn = 1;
                            if(turn == player)
                                turnLab.setText("Player 1 (X)/ You");
                            else
                                turnLab.setText("Player 1 (X)");
                        }
                    }
                }
            }
            return false;
        }
    }

    /* draw an X in the square based on the index in the game grid
     * 0|1|2
     * 3|4|5
     * 6|7|8
     */
    void drawX(int index){
        switch (index){
            case 0:
                mCanvas.drawLine(0,0,330,330,red);
                mCanvas.drawLine(330,0,0,330,red);
                break;
            case 1:
                mCanvas.drawLine(330,0,660,330,red);
                mCanvas.drawLine(660,0,330,330,red);
                break;
            case 2:
                mCanvas.drawLine(660,0,990,330,red);
                mCanvas.drawLine(990,0,660,330,red);
                break;
            case 3:
                mCanvas.drawLine(0,330,330,660,red);
                mCanvas.drawLine(330,330,0,660,red);
                break;
            case 4:
                mCanvas.drawLine(330,330,660,660,red);
                mCanvas.drawLine(660,330,330,660,red);
                break;
            case 5:
                mCanvas.drawLine(660,330,990,660,red);
                mCanvas.drawLine(990,330,660,660,red);
                break;
            case 6:
                mCanvas.drawLine(0,660,330,990,red);
                mCanvas.drawLine(330,660,0,990,red);
                break;
            case 7:
                mCanvas.drawLine(330,660,660,990,red);
                mCanvas.drawLine(660,660,330,990,red);
                break;
            case 8:
                mCanvas.drawLine(660,660,990,990,red);
                mCanvas.drawLine(990,660,660,990,red);
                break;
        }
    }

    /* draw an O in the square based on the index in the game grid
     * 0|1|2
     * 3|4|5
     * 6|7|8
     */
    void drawO(int index){
        switch(index) {
            case 0:
                mCanvas.drawCircle(165,165,165,blue);
                mCanvas.drawCircle(165,165,155,white);
                break;
            case 1:
                mCanvas.drawCircle(165+330,165,165,blue);
                mCanvas.drawCircle(165+330,165,155,white);
                break;
            case 2:
                mCanvas.drawCircle(165+660,165,165,blue);
                mCanvas.drawCircle(165+660,165,155,white);
                break;
            case 3:
                mCanvas.drawCircle(165,165+330,165,blue);
                mCanvas.drawCircle(165,165+330,155,white);
                break;
            case 4:
                mCanvas.drawCircle(165+330,165+330,165,blue);
                mCanvas.drawCircle(165+330,165+330,155,white);
                break;
            case 5:
                mCanvas.drawCircle(165+660,165+330,165,blue);
                mCanvas.drawCircle(165+660,165+330,155,white);
                break;
            case 6:
                mCanvas.drawCircle(165,165+660,165,blue);
                mCanvas.drawCircle(165,165+660,155,white);
                break;
            case 7:
                mCanvas.drawCircle(165+330,165+660,165,blue);
                mCanvas.drawCircle(165+330,165+660,155,white);
                break;
            case 8:
                mCanvas.drawCircle(165+660,165+660,165,blue);
                mCanvas.drawCircle(165+660,165+660,155,white);
                break;
        }

    }

    // check if the game has been won or ended in a draw/no moves, display dialog if it has and ask
    // if the user(s) if they want to play again or exit
    void checkGame(int player){
        if(gameWin(player)){
            Dialog dialog = null;
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Player "+ player + " wins!")
                    .setCancelable(false)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.exit(0);
                        }
                    }).setNegativeButton("Play Again", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Arrays.fill(gridCheck,0);
                    turn = 1;
                    turnLab.setText("Player 1 (X)");
                    resetBoard();
                }
            });
            dialog = builder.create();
            dialog.show();
        }
        else if(gameEnd()){
            Dialog dialog = null;
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Game over, no more moves")
                    .setCancelable(false)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.exit(0);
                        }
                    }).setNegativeButton("Play Again", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Arrays.fill(gridCheck,0);
                    turn = 1;
                    turnLab.setText("Player 1 (X)");
                    resetBoard();
                }
            });
            dialog = builder.create();
            dialog.show();
        }
    }

    /* check for game winning combinations
     * 0|1|2
     * 3|4|5
     * 6|7|8
     *
     * winning combos are 012, 345, 678, 036, 147, 258, 048, and 246
     *
     * if the game was won draw a line across the winning spaces
     */
    boolean gameWin(int turn){
        if(gridCheck[0]!=0 && gridCheck[0] == gridCheck[1] && gridCheck[1]== gridCheck[2]){
            if(turn == 1)
                mCanvas.drawLine(0,165,990,165,red);
            else if (turn == 2)
                mCanvas.drawLine(0,165,990,165,blue);
            return true;
        }
        else if(gridCheck[3]!=0 && gridCheck[3] == gridCheck[4] && gridCheck[4]== gridCheck[5]){
            if(turn == 1)
                mCanvas.drawLine(0,165+330,990,165+330,red);
            else if (turn == 2)
                mCanvas.drawLine(0,165+330,990,165+330,blue);
            return true;
        }
        else if(gridCheck[6]!=0 && gridCheck[6] == gridCheck[7] && gridCheck[7]== gridCheck[8]){
            if(turn == 1)
                mCanvas.drawLine(0,165+660,990,165+660,red);
            else if (turn == 2)
                mCanvas.drawLine(0,165+660,990,165+660,blue);
            return true;
        }
        else if(gridCheck[0]!=0 && gridCheck[0] == gridCheck[3] && gridCheck[3]== gridCheck[6]){
            if(turn == 1)
                mCanvas.drawLine(165,0,165,990,red);
            else if (turn == 2)
                mCanvas.drawLine(165,0,165,990,blue);
            return true;
        }
        else if(gridCheck[1]!=0 && gridCheck[1] == gridCheck[4] && gridCheck[4]== gridCheck[7]){
            if(turn == 1)
                mCanvas.drawLine(165+330,0,165+330,990,red);
            else if (turn == 2)
                mCanvas.drawLine(165+330,0,165+330,990,blue);
            return true;
        }
        else if(gridCheck[2]!=0 && gridCheck[2] == gridCheck[5] && gridCheck[5]== gridCheck[8]){
            if(turn == 1)
                mCanvas.drawLine(165+660,0,165+660,990,red);
            else if (turn == 2)
                mCanvas.drawLine(165+660,0,165+660,990,blue);
            return true;
        }
        else if(gridCheck[0]!=0 && gridCheck[0] == gridCheck[4] && gridCheck[4]== gridCheck[8]){
            if(turn == 1)
                mCanvas.drawLine(0,0,990,990,red);
            else if (turn == 2)
                mCanvas.drawLine(0,0,990,990,blue);
            return true;
        }
        else if(gridCheck[2]!=0 && gridCheck[2] == gridCheck[4] && gridCheck[4]== gridCheck[6]){
            if(turn == 1)
                mCanvas.drawLine(0,990,990,0,red);
            else if (turn == 2)
                mCanvas.drawLine(0,990,990,0,blue);
            return true;
        }
        return false;
    }

    // check for no more moves/ draw,  for through the gridcheck array if no indexes have a 0 there
    // are no more moves return true
    boolean gameEnd(){
        for(int i=0; i< gridCheck.length;++i){
            if(gridCheck[i]== 0)
                return false;
        }

        return true;
    }

    /* used to get the index of the gridCheck array based on the position the user clicked
     * 0|1|2
     * 3|4|5
     * 6|7|8
     */
    int posToIndex(int x, int y){
        if(y >=0 && y <= 330){
            if(x >=0 && x <= 330){
                return 0;
            }
            else if(x >=330 && x <= 660){
                return 1;
            }
            else if(x >=660 && x <= 990){
                return 2;
            }
        }
        else if(y >=330 && y <= 660){
            if(x >=0 && x <= 330){
                return 3;
            }
            else if(x >=330 && x <= 660){
                return 4;
            }
            else if(x >=660 && x <= 990){
                return 5;
            }
        }
        else if(y >=660 && y <= 990){
            if(x >=0 && x <= 330){
                return 6;
            }
            else if(x >=330 && x <= 660){
                return 7;
            }
            else if(x >=660 && x <= 990){
                return 8;
            }
        }
        return -1;
    }

    // reset the board, clear X's and O's, redraw the lines
    void resetBoard(){
        mCanvas = new Canvas(bm);
        mCanvas.drawColor(Color.WHITE);
        grid.setImageBitmap(bm);
        grid.setOnTouchListener(new myTouchListener());

        mCanvas.drawLine(330, 0, 330, 990, black);
        mCanvas.drawLine(660, 0, 660, 990, black);
        mCanvas.drawLine(0, 330, 990, 330, black);
        mCanvas.drawLine(0, 660, 990, 660, black);
    }
}
