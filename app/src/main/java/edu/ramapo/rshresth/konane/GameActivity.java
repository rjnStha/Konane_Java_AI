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
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.Queue;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{
    //member variables
    Game KonaneGame;
    int [] movesStore; //to store the initial and final position of the move
    boolean firstMove, successFirstMove; //flag for first move selected(true if not selected), first move made
    Switch switchB, switchW;
    long timeTake; //time cal minMax

    //member variables for algorithm
    String algorithmValue;  //to store what algorithm is currently selected
    Queue nextMovesList;    //to store all the moves in given algorithm
    boolean nextButtonFirst; //flag to check if next button pressed in given turn and given algorithm
    int[] constrainAlorithm; //user input constrain for branchBound, MinMax --> cutoff ply, alphaBetaPruning

    @Override
    protected void onCreate(Bundle savedInstanceState)   {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Load the required game
        //new game or load game
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String gameStatus = bundle.getString("fileName");


        //initialize switch black and white
        switchB = ((Switch)findViewById(R.id.switchBlack));
        switchW = ((Switch)findViewById(R.id.switchWhite));


        //load new game
        if(gameStatus.equals("newGame0")||gameStatus.equals("newGame1")) {

            String temp = bundle.getString("size");
            int boardSize = Integer.parseInt(temp);

            //check if user got the black stone and first turn
            if(gameStatus.equals("newGame1")) KonaneGame = new Game(boardSize,Board.BLACKSTONE);
            else KonaneGame = new Game(boardSize,Board.WHITESTONE);

            switchB.setChecked(true);
            switchW.setChecked(false);
        }
        //load the selected game
        else{
            loadGame(gameStatus);
            if(KonaneGame.getTurn() == Board.BLACKSTONE){
                switchB.setChecked(true);
                switchW.setChecked(false);
            }
            else{
                switchB.setChecked(false);
                switchW.setChecked(true);
            }
        }

        //display human color
        int id = getResources().getIdentifier("textViewStatus","id",getPackageName());
        TextView display = (TextView)findViewById(id);
        display.setText("Human: "+KonaneGame.getHumanStone());

        //Initialize member variables
        movesStore = new int[4];
        firstMove = true;
        successFirstMove = false;
        nextButtonFirst = false;
        timeTake = 0;

        //updates the initial state of board and score
        createButton();
        updateBoard();
        updateScore();

        //set on click listener to reset button
        String buttonId = "buttonReset";
        id = getResources().getIdentifier(buttonId,"id",getPackageName());
        Button buttonTemp = (Button)findViewById(id);
        buttonTemp.setOnClickListener(this);

        //set on click listener to next button
        buttonId = "buttonNext";
        id = getResources().getIdentifier(buttonId,"id",getPackageName());
        buttonTemp = (Button)findViewById(id);
        buttonTemp.setOnClickListener(this);

        //set on click listener to next button
        buttonId = "buttonSave";
        id = getResources().getIdentifier(buttonId,"id",getPackageName());
        buttonTemp = (Button)findViewById(id);
        buttonTemp.setOnClickListener(this);

        buttonId = "CompMakeMove";
        id = getResources().getIdentifier(buttonId,"id",getPackageName());
        buttonTemp = (Button)findViewById(id);
        buttonTemp.setOnClickListener(this);

        //Spinner and set item selecter
        final Spinner userInputSpinner = (Spinner) findViewById(R.id.spinnerAlgorithm);
        algorithmValue = userInputSpinner.getSelectedItem().toString();
        userInputSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                algorithmValue = parent.getItemAtPosition(position).toString();
                nextButtonFirst = false;
                if(firstMove) resetColor();

            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {
                algorithmValue = Game.DEPTHFIRST;
            }
        });

        //populate the button attaching onClick listener
        populateButton();

        //set on click listener to the switches
        switchB.setOnClickListener(this);
        switchW.setOnClickListener(this);

        //Disabling the swap gesture of switch
        switchB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getActionMasked() == MotionEvent.ACTION_MOVE;
            }


        });
        switchW.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getActionMasked() == MotionEvent.ACTION_MOVE;
            }


        });
    }

    //action carried out after each click of the stone buttons
    //parameter --> View
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.switchBlack){

            //restricts turning on the black switch from off state
            //when its not black's turn
            if(KonaneGame.getTurn() != Board.BLACKSTONE)
            {
                switchW.setChecked(true);
                switchB.setChecked(false);
                return;
            }

            //checks if there are valid moves for both players
            // ends activity if no more moves left
            if(!checkValidMoves(Board.BLACKSTONE) && !checkValidMoves(Board.WHITESTONE))endActivity();

            //check the requirements for switching the turn
            //check no more moves available
            //check if a move is already made
            if(!checkValidMoves(KonaneGame.getTurn())|| successFirstMove)
            {
                //switching the both black and white switch
                switchB.setChecked(false);
                switchW.setChecked(true);

                //update turn and relevant flags
                KonaneGame.updateTurn();
                successFirstMove = false;
                firstMove = true;
                nextButtonFirst = false;

                //change the color of the previous button to default
                String buttonId = ""+movesStore[0]+movesStore[1];
                selectChangeColor(buttonId, true);

                //change the color of the previous button to default
                buttonId = ""+movesStore[1]+movesStore[2];
                selectChangeColor(buttonId, true);

            }
            else {

                //Restricting the switch change when no moves made
                switchB.setChecked(true);
                switchW.setChecked(false);
            }
        }
        //action for white switch
        else if(view.getId() == R.id.switchWhite) {

            if(KonaneGame.getTurn() != Board.WHITESTONE)
            {
                switchW.setChecked(false);
                switchB.setChecked(true);
                return;
            }
            if(!checkValidMoves(Board.BLACKSTONE) && !checkValidMoves(Board.WHITESTONE))endActivity();

            //check the requirements for switching the turn
            //check no more moves available
            //check if a move is already made
            if(!checkValidMoves(KonaneGame.getTurn())|| successFirstMove)
            {
                //switching the both black and white switch
                switchW.setChecked(false);
                switchB.setChecked(true);

                //update firstmove
                KonaneGame.updateTurn();
                successFirstMove = false;
                firstMove = true;
                nextButtonFirst = false;

                //change the color of the previous button to default
                String buttonId = ""+movesStore[0]+movesStore[1];
                selectChangeColor(buttonId, true);

                //change the color of the previous button to default
                buttonId = ""+movesStore[1]+movesStore[2];
                selectChangeColor(buttonId, true);


            }
            else {
                //Restricting the switch change when no moves made
                switchW.setChecked(true);
                switchB.setChecked(false);
            }
        }
        //Reset button for the color
        else if(view.getId() == R.id.buttonReset){
            if(!firstMove && !successFirstMove){
                resetColor();
                firstMove = true;
            }

        }
        //Next button for next moves
        else if(view.getId() == R.id.buttonNext){
            //check if first move already made by player in his turn
            //check if there are moves in given turn
            if (firstMove && checkValidMoves(KonaneGame.getTurn())) {
                resetColor();

                //Check for algorithm branch and bound
                //required to get the constrain
                if(algorithmValue.equals(Game.BRANCHBOUND)) {
                    constrainAlorithm = new int[1];
                    constrainAlorithm[0] = 1;
                    inputDialogueBranchBound();
                    return;
                }
                else if(algorithmValue.equals(Game.MINMAX)) {
                    constrainAlorithm = new int[2];
                    constrainAlorithm[0] = 1;
                    constrainAlorithm[1] = 0; //default value for no alphabeta pruning

                    inputDialogueMinMax();

                    return;
                }

                //check if next button is not pressed already in given turn and given algorithm
                //gets the list of valid moves in queue
                if(!nextButtonFirst ||(nextButtonFirst && nextMovesList.isEmpty())) {
                    nextMove();
                    nextButtonFirst = true;
                }

                //animate display in the board
                displayAlgorithm();
            }
        }
        //Save button to save the current status of the game
        else if(view.getId() == R.id.buttonSave) {
            inputFileName(); //inputs the file name and calls the saveGame
        }
        else if(view.getId() == R.id.CompMakeMove){
            if(KonaneGame.getTurn() != KonaneGame.getHumanStone()){
                compMove();
            }
        }
        else{
            int id = view.getId();
            String temp;
            if(id<10){
                temp ="0"+id;
            }
            else temp = ""+id;

            //calls the function to store- the row and col of the selected button
            //WARNING: not generic, assumes max id number is 99
            //SOLUTION: change parameter of storeMakeMove() to int and use / and mod to find the value
            storeMakeMove(temp.charAt(0),temp.charAt(1));

        }
    }

    //populate button with relevant stone and color and
    //attach onClick property
    //no parameters and returns void
    public void populateButton(){
        //gets the stone button's row and column when selected by user
        //and sets the onClick function parameter
        for(int i =0; i<Board.BOARD_SIZE;i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {

                //set the on click listener property for all the buttons in the board
                //access all the buttons via string id where i and j are the row and column num
                String buttonId = ""+i+j;
                int id = Integer.parseInt(buttonId);
                Button buttonTemp = (Button)findViewById(id);

                buttonTemp.setOnClickListener(this);

                if(KonaneGame.getKonaneBoard(i,j) == Board.BLACKSTONE){
                    buttonTemp.setTextColor(Color.BLACK);
                }
                else if(KonaneGame.getKonaneBoard(i,j) == Board.WHITESTONE){
                    buttonTemp.setTextColor(Color.WHITE);
                }
            }
        }
    }

    //called once to dynamically create button
    //returns void, no parameter
    public void createButton(){
        TableLayout board = findViewById(R.id.tableButtons);
        String idString = new String();
        int id;

        //updates the board
        for(int i =0; i<Board.BOARD_SIZE;i++) {
            TableRow row = new TableRow(this);

            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                Button button = new Button(this);
                idString = ""+i+j;
                id = Integer.parseInt(idString);
                button.setId(id);
                row.addView(button);
            }
            board.addView(row);
        }
    }

    //updates the board with relevant stone and color
    //no parameter and returns void
    public void updateBoard(){
        char temp;
        //updates the board
        for(int i =0; i<Board.BOARD_SIZE;i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                temp = KonaneGame.getKonaneBoard(i, j);
                String buttonID = "" + i + j;
                int id = Integer.parseInt(buttonID);

                if(temp == Board.BLACKSTONE) ((Button) findViewById(id)).setText("\u26AB");
                else if (temp == Board.WHITESTONE)((Button) findViewById(id)).setText("\u26AA");
                else ((Button) findViewById(id)).setText("");

                if(KonaneGame.getKonaneBoard(i,j) == Board.BLACKSTONE){
                    ((Button) findViewById(id)).setTextColor(Color.BLACK);
                }
                else if(KonaneGame.getKonaneBoard(i,j) == Board.BLACKSTONE){
                    ((Button) findViewById(id)).setTextColor(Color.WHITE);
                }
            }
        }

    }

    //updates the score of the players
    //no parameter and returns void
    public void updateScore(){
        //updates the black player score
        String temp2 = "" + KonaneGame.getScore(Board.BLACKSTONE);
        ((TextView) findViewById(R.id.textScoreBlack)).setText(temp2);

        //updates the white player score
        temp2 = "" + KonaneGame.getScore(Board.WHITESTONE);
        ((TextView) findViewById(R.id.textScoreWhite)).setText(temp2);

    }

    //checks and returns true if there are valid moves
    //parameter --> character, turn of the player
    //returns boolean, true if there are valid moves left
    public boolean checkValidMoves(char turn){

        for(int i =0; i<Board.BOARD_SIZE;i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                //if player turn = value board and valid checks remaining then return true
                if(turn == KonaneGame.getKonaneBoard(i,j) &&
                        KonaneGame.moveInitCheck(i,j))
                {
                    return true;
                }
            }
        }
        return false;
    }

    //stores move, makes move and updates the screen
    //parameter --> row and col of given position
    //returns void
    public void storeMakeMove(char row, char col){
        //Status text view
        TextView textViewTemp = (TextView) findViewById(R.id.textViewStatus);
        String buttonId;

        if (firstMove) {
            resetColor();
            movesStore[0] = Character.getNumericValue(row);
            movesStore[1] = Character.getNumericValue(col);

            //display row and column in the status TextView
            textViewTemp.setText("" + movesStore[0] + movesStore[1]);


            //changes the color of the selected button
            buttonId = ""+row+col;
            selectChangeColor(buttonId, false);

            //checks if selected position can make valid move
            //checks if the selected position equal to the turn (player and stone same color)
            if(KonaneGame.moveInitCheck(movesStore[0],movesStore[1]) &&
                    KonaneGame.getKonaneBoard(movesStore[0],movesStore[1]) == KonaneGame.getTurn()) {

                //signifies that the selected position is valid
                //and next position be selected
                firstMove = false;
            }
            else{
                //reset the color of button if not a valid position
                buttonId = ""+row+col;
                selectChangeColor(buttonId, true);
            }
        }
        else{
            //when the user selects the same position second time in row
            //restrict any movement
            if(movesStore[0] == Character.getNumericValue(row) &&
                    movesStore[1] == Character.getNumericValue(col)){
                return;
            }

            movesStore[2] = Character.getNumericValue(row);
            movesStore[3] = Character.getNumericValue(col);

            //make a move when 2 values are stored
            switch (KonaneGame.makeMove(movesStore[0], movesStore[1], movesStore[2] , movesStore[3])) {

                //invalid move
                case -1:
                    textViewTemp.setText("invalid move");

                    //changes the color of the selected button to default
                    buttonId = ""+movesStore[2]+movesStore[3];
                    selectChangeColor(buttonId, true);

                    break;

                //mismatch player and stone
                case 0:
                    textViewTemp.setText("mismatch move");

                    //change the color of the previous button to default
                    buttonId = ""+movesStore[0]+movesStore[1];
                    selectChangeColor(buttonId, true);

                    //changes the color of the selected button to default
                    buttonId = ""+movesStore[2]+movesStore[3];
                    selectChangeColor(buttonId, true);

                    firstMove = true;
                    break;

                //successful move made
                case 1:
                    //display success in the status TextView
                    textViewTemp.setText("move sucessful: \n"+movesStore[0]+movesStore[1]+" -> "+movesStore[2]+movesStore[3]);

                    //update the score after move
                    updateScore();

                    //update board
                    updateBoard();

                    //change the color of the previous button to default
                    buttonId = ""+movesStore[0]+movesStore[1];
                    selectChangeColor(buttonId, true);

                    //changes the color of the selected button to blue
                    buttonId = ""+movesStore[2]+movesStore[3];
                    selectChangeColor(buttonId, false);

                    //Store the current position as initial position for next move
                    movesStore[0] = movesStore[2];
                    movesStore[1] = movesStore[3];

                    successFirstMove = true;

                    break;
            }
        }
    }

    //changes the color of the selected button
    //paramter --> String, the button Id of the given buttton and
    //             boolean, true if the color is default
    //returns void
    //also clears animation
    public void selectChangeColor(String buttonId, boolean defaultColor){

        int id = Integer.parseInt(buttonId);
        Button buttonCurrent = (Button)findViewById(id);

        if(defaultColor == true) buttonCurrent.setBackgroundResource(R.drawable.konane_board);
        else buttonCurrent.setBackgroundResource(R.drawable.konane_board_select);

        buttonCurrent.clearAnimation();
    }

    //reset the highlight of board
    //function specific to reset button
    //no parameter and returns void
    public void resetColor(){
        for(int i =0; i<Board.BOARD_SIZE;i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {

                //set the on click listener property for all the buttons in the board
                //access all the buttons via string id where i and j are the row and column num
                String buttonId = "" + i + j;
                selectChangeColor(buttonId,true);
            }
        }
    }

    //activity to direct to result activity
    //no parameter and returns void
    public void endActivity(){
        Intent intent = new Intent(this, ResultActivity.class);

        //Sending data to result activity
        int tempScoreB = KonaneGame.getScore('B');
        int tempScoreW = KonaneGame.getScore('W');
        intent.putExtra("WhiteScore",tempScoreW);
        intent.putExtra("BlackScore",tempScoreB);
        startActivity(intent);
    }

    //read file from assets and call loadGame function
    //      of Game class
    //parameter --> String, name of the file
    //returns boolean, true/false for successful/unsuccessful file read
    public boolean loadGame(String fileName){
        try {

            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            //get the number of lines in the board
            LineNumberReader lnr = new LineNumberReader(reader);
            int linenumber = 0;
            while (lnr.readLine() != null){
                linenumber++;
            }
            //restart reading the file
            reader = new BufferedReader(new FileReader(fileName));

            //new game with size of the board and
            // default human stone as black

            KonaneGame = new Game((linenumber-5),Board.BLACKSTONE);

            String line;
            //rowMarker as flag and row number
            //-3 for when while loop reaches board it sets to 0 in Game class
            int rowMarker= -3;
            while ((line = reader.readLine()) != null) {
                KonaneGame.loadGame(line,rowMarker);
                rowMarker++;
            }
            reader.close();



            //return true for successful read
            return true;
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
            //Unable to open file
            //Error reading file
        }

        //false for error reading file
        return false;
    }

    //gets next moves from the Game class
    //game class' nextMoveAlgorithm function called
    //returns void and no parameter
    public void nextMove(){
        nextMovesList = KonaneGame.nextMoveAlgorithm(algorithmValue,constrainAlorithm);
    }

    //display the moves in the board when pressed next button
    //      animation / background color change
    //returns void and no parameter
    public void displayAlgorithm(){
        String buttonId, printString = new String();
        int countScore = 0;
        while(nextMovesList.peek() != Game.MOVESEPARATOR) {
            //remove the elements of list and add to ButtonID
            buttonId = ""+nextMovesList.remove()+nextMovesList.remove();

            //change the color of all the movements
            selectChangeColor(buttonId, false);

            //animate for the last position of the given move
            if(nextMovesList.peek() == Game.MOVESEPARATOR){
                //string button to id button
                int id = Integer.parseInt(buttonId);
                Button buttonCurrent = findViewById(id);

                buttonCurrent.setBackgroundResource(R.drawable.animation_blink);
                AnimationDrawable blinkAnimation = (AnimationDrawable) buttonCurrent.getBackground();

                blinkAnimation.start();
            }

            //update score
            countScore++;
            printString = ""+printString+buttonId+"->";
        }

        //removing the mark MOVESPERATOR
        nextMovesList.remove();

        //display info in status bar
        TextView textViewTemp = (TextView) findViewById(R.id.textViewStatus);
        textViewTemp.setText("" + algorithmValue +": \n"+printString + "\nScore: "+(countScore-1)+"\ntime taken:"+timeTake+"ms");
    }

    //dialog for user input branch and bound
    //returns void and no parameter
    public void inputDialogueBranchBound(){
        //Dialog
        final Dialog d = new Dialog(GameActivity.this);
        d.setTitle("Branch and Bound Constrain");
        d.setContentView(R.layout.dialog_userinput);

        //NumberPicker
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
        //min and max value for the Num picker
        np.setMaxValue(11);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        //onValue change listener to the num picker
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
               //update the constrain value with the user selected num
                constrainAlorithm[0] = newVal;
            }
        });

        //OK button
        Button b1 = (Button) d.findViewById(R.id.okButton);
        //on click listener to OK button
        b1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                //Call next move after getting user constrain
                nextMove();
                //animate/display the algorithm in board
                displayAlgorithm();
                //close the dialog
                d.dismiss();
            }
        });

        //Cancel button
        b1 = (Button) d.findViewById(R.id.cancelButton);
        //on click listener to cancel button
        b1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) { d.dismiss(); }
        });

        d.show();
    }

    //saves game to the device
    //no parameter
    //returns void
    public void saveGame(String fileName){


        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+"/"+fileName);
        FileOutputStream fos;

        boolean Board = false; //marks the start of the board
        try {
            fos = new FileOutputStream(file);
            //check whether the file exists, not then creates
            if (!file.exists()) {
                file.createNewFile();
            }

            for(int n =0; n<6;n++) {
                String content;
                if(Board){
                    for(int i = 0; i < getNextdata(n).length; i++){
                        content = getNextdata(n)[i] + '\n';
                        byte[] bytesArray = content.getBytes();
                        fos.write(bytesArray);
                        fos.flush();
                    }
                    Board =false;
                }
                else {
                    content = getNextdata(n)[0] + '\n';
                    if (getNextdata(n)[0].equals("Board:")) Board = true;

                    //String content cannot be directly written
                    // into a file so converted into bytes
                    byte[] bytesArray = content.getBytes();
                    fos.write(bytesArray);
                    fos.flush();
                }
            }
            fos.close();
        }

        catch (IOException e) {
            // handle exception
            System.out.println("ERROR");
        }

        //display in the status bar
        TextView status = findViewById(R.id.textViewStatus);
        status.setText("Saving File...and Exiting");
       //delaying the endActivity to maintain display
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        endActivity();
                    }
                },
                1500
        );
    }

    //dialog for user input file name
    //calls saveGame function
    //returns void and no parameter
    public void inputFileName(){
        final Dialog d = new Dialog(GameActivity.this);
        d.setTitle("Save Game");
        d.setContentView(R.layout.dialog_loadgame);

        final EditText e1 = d.findViewById(R.id.userInputFile);
        //OK button
        Button b1 = (Button) d.findViewById(R.id.okButton1);
        //on click listener to OK button
        b1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                //call function to save the game
                saveGame(e1.getText().toString()+".txt");
                //close the dialog
                d.dismiss();
            }
        });
        d.show();
    }

    //parameter int, marker for the level of the data to get
    public String[] getNextdata(int marker){
        String[] data = new String[1];
        switch (marker) {
            case 0:
                data[0] = "Black: "+ KonaneGame.getScore(Board.BLACKSTONE);
                break;
            case 1:
                data[0] =  "White: "+ KonaneGame.getScore(Board.WHITESTONE);
                break;
            case 2:
                data[0] = "Board:";
                break;

            case 3:
                data = new String[Board.BOARD_SIZE];
                java.util.Arrays.fill(data,"");
                for(int i = 0; i<Board.BOARD_SIZE;i++) {
                    for (int j = 0; j < Board.BOARD_SIZE; j++) {
                        data[i] = data[i] + KonaneGame.getKonaneBoard(i,j)+" ";
                    }
                }
                break;

            case 4:
                if(!successFirstMove){
                    if(KonaneGame.getTurn() == Board.BLACKSTONE) data[0] = "Next Player: Black";
                    else data[0] = "Next Player: White";
                }
                else{
                    if(KonaneGame.getTurn() == Board.BLACKSTONE) data[0] = "Next Player: White";
                    else data[0] = "Next Player: Black";
                }
                break;
            case 5:
                if(KonaneGame.getHumanStone() == Board.BLACKSTONE) data[0] = "Human: Black";
                else data[0] = "Human: White";
                break;
        }
        return data;
    }

    //makes the move by the computer
    //returns void and no parameter
    public void compMove() {
        //delay making move after displaying the moves
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        //initial position of the move
                        final char row, col;
                        row = nextMovesList.remove().toString().charAt(0);
                        col = nextMovesList.remove().toString().charAt(0);
                        //UI-Thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                storeMakeMove(row, col);
                            }
                        });

                        //get the other positions of the move
                        //make the move
                        while (nextMovesList.peek() != Game.MOVESEPARATOR) {
                            final char row1, col1;
                            row1 = nextMovesList.remove().toString().charAt(0);
                            col1 = nextMovesList.remove().toString().charAt(0);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    storeMakeMove(row1, col1);
                                }
                            });
                        }
                    }
                },
                1500
        );

    }

    //dialog for user input branch and bound
    //returns void and no parameter
    public void inputDialogueMinMax(){
        //Dialog
        final Dialog d = new Dialog(GameActivity.this);
        d.setTitle("MinMax");
        d.setContentView(R.layout.dialog_userinput);

        //NumberPicker
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
        //min and max value for the Num picker
        np.setMaxValue(11);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        //onValue change listener to the num picker
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //update the constrain value with the user selected num
                constrainAlorithm[0] = newVal;
            }
        });

        //OK button
        Button b1 = (Button) d.findViewById(R.id.okButton);
        //on click listener to OK button
        b1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                //Call next move after getting user constrain
                long startTime = System.currentTimeMillis();

                nextMove();

                long stopTime = System.currentTimeMillis();
                timeTake = stopTime - startTime;

                //prevent calling nextMove for memory/time save
                //after displayAlgorithm()
                Queue temp = new LinkedList(nextMovesList);
                displayAlgorithm();

                if(KonaneGame.getTurn() != KonaneGame.getHumanStone()){
                    nextMovesList = new LinkedList(temp);
                }

                //close the dialog
                d.dismiss();

            }
        });

        //Cancel button
        b1 = (Button) d.findViewById(R.id.cancelButton);
        b1.setText("With AlphaBeta Pruning");
        b1.setTextSize(20);
        //on click listener to cancel button
        b1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                //set the value to 1 for selecting alphabeta pruning
                constrainAlorithm[1] = 1;
                long startTime = System.currentTimeMillis();

                nextMove();

                long stopTime = System.currentTimeMillis();
                timeTake = stopTime - startTime;

                //prevent calling nextMove for memory/time save
                //after displayAlgorithm()
                Queue temp = new LinkedList(nextMovesList);
                displayAlgorithm();
                nextMovesList = new LinkedList(temp);

                if(KonaneGame.getTurn() != KonaneGame.getHumanStone()){
                    nextMovesList = new LinkedList(temp);
                }

                //close the dialog
                d.dismiss();
            }
        });

        d.show();
    }

}