/*

    *********************************************************
    * Name:  Rojan Shrestha                                 *
    * Project:  Project1, Konane                            *
    * Class: CMPS 331 - Artificial Intelligence             *
    * Date:  2/3/2018                                       *
    *********************************************************

*/

package edu.ramapo.rshresth.konane;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //to display the final score and winner of the game
        int whiteScore, blackScore;

        //receiving data from previous activity
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        blackScore = bundle.getInt("BlackScore");
        whiteScore = bundle.getInt("WhiteScore");
        String winner;
        if(blackScore>whiteScore) winner = "Black";
        else if(whiteScore>blackScore) winner = "White";
        else winner = "Its a tie";

        //display in the  textView
        TextView textViewTemp = (TextView) findViewById(R.id.textViewResult);
        textViewTemp.setText("Black : "+ blackScore+ '\n' + "White : "+ whiteScore+'\n'+ " Winner : "+ winner );
    }

    //returns void
    //paramter - View
    public void NewGame(View view){
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

}