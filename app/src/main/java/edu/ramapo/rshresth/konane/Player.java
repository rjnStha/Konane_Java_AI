/*

    *********************************************************
    * Name:  Rojan Shrestha                                 *
    * Project:  Project1, Konane                            *
    * Class: CMPS 331 - Artificial Intelligence             *
    * Date:  2/3/2018                                       *
    *********************************************************

*/

package edu.ramapo.rshresth.konane;

/**
 * Created by lenovo on 1/30/2018.
 */

public class Player {
    private int score;
    private char type;

    // constructor
    //parameter - character for the type of player
    public Player(char type) {
        score = 0;
        this.type = type;
    }

    // increse the score by 1
    //no parameters and returns void
    public void updateScore(int addScore) {
        score+=addScore;
    }

    //returns the score of the player as integer
    //no parameter
    public int getScore() {
        return score;
    }

    //set the score
    //returns void
    //parameter --> int score, the value of the score to be set
    public void setScore(int score)
    {
        this.score = score;
    }


}
