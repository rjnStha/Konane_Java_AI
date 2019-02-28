/*

    *********************************************************
    * Name:  Rojan Shrestha                                 *
    * Project:  Project1, Konane                            *
    * Class: CMPS 331 - Artificial Intelligence             *
    * Date:  2/3/2018                                       *
    *********************************************************

*/

package edu.ramapo.rshresth.konane;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class WelcomeActivity extends AppCompatActivity{

    Button buttonTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        //set onclick listener to play button
        buttonTemp = (Button)findViewById(R.id.playButton);
        buttonTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputSize(view);
            }
        });

        //set onclick listener to load game button
        buttonTemp = (Button)findViewById(R.id.resumeGame);
        buttonTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputDialogue();
            }
        });

        //set onclick listener to tutorial button
        buttonTemp = (Button)findViewById(R.id.tutorial);
        buttonTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    //Ends activity per the parameter
    //new game or load game
    public void endActivity(String gameName, String boardSize){
        final Intent intent = new Intent(this, GameActivity.class);
        //Sending data to result activity
        intent.putExtra("fileName",gameName);
        intent.putExtra("size",boardSize);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, 1000);

    }

    //dialog for user input file name
    //returns void and no parameter
    public void inputDialogue(){
        //Dialog
        final Dialog d = new Dialog(WelcomeActivity.this);
        d.setTitle("Resume Game");
        d.setContentView(R.layout.dialog_loadgame);

        final EditText e1 = d.findViewById(R.id.userInputFile);

        //OK button
        Button b1 = (Button) d.findViewById(R.id.okButton1);
        //on click listener to OK button
        b1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                //ends activity passing the file name
                endActivity(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+
                        "/"+e1.getText().toString()+".txt","");
                //close the dialog
                d.dismiss();
            }
        });

        d.show();
    }

    //Alert dialog for user input size of the board
    //parameter --> View, for alertDialog
    //returns void
    public void inputSize(View view){
        final String[] boardSize = new String[]{"6","8", "10"};

        //dialog
        AlertDialog.Builder size = new AlertDialog.Builder(view.getContext());
        size.setTitle("Select the size of the board");
        size.setItems(boardSize, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //call the function for selecting the turn
                inputTurn(boardSize[i]);
            }
        });
        size.show();


    }

    //dialog for user input choose the hand and decide player turn
    //parameter --> final String, size of the board passed by inputSize function
    //returns void
    public void inputTurn(final String boardSize){
        //Dialog
        final Dialog d = new Dialog(WelcomeActivity.this);
        d.setTitle("Choose a hand");
        d.setContentView(R.layout.dialog_playerturn);

        //Random generate 1 and 2
        //1 --> left and  2 --> right
        Random rand = new Random();
        final int turn = rand.nextInt(2) + 1;

        //textView
        final TextView t1 = (TextView) d.findViewById(R.id.statusPrint);

        //button
        final Button left = (Button) d.findViewById(R.id.fistLeft);
        final Button right = (Button) d.findViewById(R.id.fistRight);
        //on click listener to buttons
        left.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                //restrict right button selection
                right.setClickable(false);

                //ends activity passing the file name
                if(turn == 1){
                    t1.setText("Black.You move first");
                    left.setBackgroundResource(R.drawable.black);
                    endActivity("newGame1",boardSize);
                }
                else {
                    t1.setText("White.Your opponent moves first");
                    left.setBackgroundResource(R.drawable.white);
                    endActivity("newGame0",boardSize);
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                //restrict left button selection
                left.setClickable(false);

                //ends activity passing the file name
                if(turn == 2){
                    t1.setText("Black.You move first");
                    right.setBackgroundResource(R.drawable.black);
                    endActivity("newGame1",boardSize);
                }
                else {
                    t1.setText("White.Your opponent moves first");
                    right.setBackgroundResource(R.drawable.white);
                    endActivity("newGame0",boardSize);
                }
            }
        });
        d.show();
    }
}
