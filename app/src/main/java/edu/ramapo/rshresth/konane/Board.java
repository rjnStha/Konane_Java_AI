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

import java.util.Random;

public class Board {

    // Member Variables
    public static final char BLACKSTONE = 'B';
    public static final char WHITESTONE = 'W';
    public static final char PUKA = 'O';
    public static int BOARD_SIZE;
    private char[][] board_stones;

    // constructor
    public Board(int boardSize){
        BOARD_SIZE = boardSize;
        board_stones = new char[BOARD_SIZE][BOARD_SIZE];
        homeBoard();
        startBoard();
    }

    // fills the board with black and white stones
    //returns void and no parameter
    private void homeBoard() {
        for(int i=0;i<BOARD_SIZE;i++) {
            for(int j=0;j<BOARD_SIZE;j++) {
                if((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)) board_stones[i][j] = BLACKSTONE;
                else board_stones[i][j] = WHITESTONE;
            }
        }
    }

    // randomly removes one black and white stone from the board
    //returns void and no parameter
    private void startBoard() {
        //create instance of Random class
        Random rand = new Random();
        char temp = WHITESTONE;

        //generate random integers between 0 to 6
        int row = rand.nextInt(BOARD_SIZE);
        int col = rand.nextInt(BOARD_SIZE);

        //check if the stone is black or white
        if((row+col)%2 == 0) temp = BLACKSTONE;

        //remove first stone
        board_stones[row][col] = PUKA;

        //generate random integers between 0 to 6
        col = rand.nextInt(BOARD_SIZE);
        row = rand.nextInt(BOARD_SIZE);

        //remove second stone
        if(board_stones[row][col] != temp) board_stones[row][col] = PUKA;
        else board_stones[row][(col+1) % BOARD_SIZE] = PUKA;
    }

    //update the position with the given stoneType
    //returns void
    //parameter --> 2 integers - the position in the board and
    //              one character - type of the stone to be replaced with
    public void setBoard_aftMove(int row, int col, char stoneType ) {
        board_stones[row][col] = stoneType;
    }

    //returns the stone type in given position
    //parameters --> 2 integers - the position in the board
    //returns the type of stone in the given position
    public char getStoneType(int row, int col) {
        return board_stones[row][col];
    }

    //prints board in console
    //returns void
    public void printBoard(){
        for (int i =0; i<BOARD_SIZE;i++){
            for (int j=0;j<BOARD_SIZE;j++){
                System.out.print(getStoneType(i,j)+" ");
            }
            System.out.println("");
        }
    }
}

