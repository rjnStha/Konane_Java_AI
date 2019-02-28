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
import android.widget.Button;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Game {
    //Member Variables
    private Board KonaneBoard;
    private Player p_Black, p_White;
    private char humanColor;
    private static char p_turn; //to check if player and stone match
    private static Tree nextMovesTree; //to store next move in a tree
    private static Queue finalPathAlgorithm; //to store the moves in queue


    private static int depthPly;//to store the ply cutoff for minMax algorithm
    private static boolean noPreviousMove; // flag for mina
    private static boolean firstMove; //flag for minMax to identify firstMoves of the recursion
    private static Queue bestMove; //store the best move after minMax
    private static char currentPlayer; //store the turn of the player
    private static int currentPlayerScore, nextPlayerScore; //store the turn of the player
    private static boolean alphaBeta; //flag for alphabeta pruning

    //static member variables
    public static final String BREADTHFIRST = "breadthFirst";
    public static final String DEPTHFIRST = "depthFirst";
    public static final String BESTFIRST = "bestFirst";
    public static final String BRANCHBOUND = "branchBound";
    public static final String MOVESEPARATOR = ":";
    public static final String MINMAX = "minMax";

    // Constructor
    public Game(int boardSize, char humanColor) {
        KonaneBoard = new Board(boardSize);
        p_Black = new Player(Board.BLACKSTONE);
        p_White = new Player(Board.WHITESTONE);
        p_turn = Board.BLACKSTONE;
        this.humanColor = humanColor;
    }

    // make move taking initial and final position
    //parameters --> 4 integers, first two are the row and col of first position
    //              and second two are the row and col of second position
    //return int, 1 success, 0 mismatch and -1 invalid
    public int makeMove(int init_row, int init_col, int dest_row, int dest_col) {
        //if witch black turn white escape
        //get the selected stone type
        char init_type = KonaneBoard.getStoneType(init_row, init_col);

        //check if the player and the stone matches
        if (init_type == p_turn) {

            //checks if the given stone has possible moves and
            //checks if destination is valid
            if (moveInitCheck(init_row, init_col) &&
                    moveDestCheck(init_row, init_col, dest_row, dest_col)) {
                // update the initial position
                KonaneBoard.setBoard_aftMove(init_row, init_col, Board.PUKA);

                // update the mid position
                int killedStone = 1;
                if (init_row == dest_row) {

                    //left
                    if (dest_col < init_col) {
                        killedStone = -1;
                    }
                    KonaneBoard.setBoard_aftMove(init_row, init_col + killedStone, Board.PUKA);
                } else if (init_col == dest_col) {
                    //up
                    if (dest_row < init_row) {
                        killedStone = -1;
                    }
                    KonaneBoard.setBoard_aftMove(init_row + killedStone, init_col, Board.PUKA);
                }

                // update the final position
                KonaneBoard.setBoard_aftMove(dest_row, dest_col, init_type);

                //update score
                if (init_type == Board.BLACKSTONE) p_Black.updateScore(1);
                else p_White.updateScore(1);

                //move successful
                return 1;
            }
            return -1; //not a valid move
        }
        return 0; //mismatched stone and player
    }

    //Checks if the given stone can have possible moves
    //parameters --> 2 integers, row and col of given position
    //returns boolean, true if the given position has valid move to make
    public boolean moveInitCheck(int init_row, int init_col) {

        //left move --> checks if the destination is within bound
        //checks if destination is a PUKA
        //checks if left adjacent stone and player turn
        if (init_col - 2 >= 0 &&
                KonaneBoard.getStoneType(init_row, init_col - 2) == Board.PUKA &&
                KonaneBoard.getStoneType(init_row, init_col - 1) != Board.PUKA
                ) {
            return true;
        }

        //right move --> checks if the destination is within bound
        //checks if destination is a PUKA
        //checks if right adjacent stone and player turn
        else if (init_col + 2 < Board.BOARD_SIZE &&
                KonaneBoard.getStoneType(init_row, init_col + 2) == Board.PUKA &&
                KonaneBoard.getStoneType(init_row, init_col + 1) != Board.PUKA
                ) {
            return true;
        }

        //up move --> checks if the destination is within bound
        //checks if destination is a PUKA
        //checks if up adjacent stone and player turn
        else if (init_row - 2 >= 0 &&
                KonaneBoard.getStoneType(init_row - 2, init_col) == Board.PUKA &&
                KonaneBoard.getStoneType(init_row - 1, init_col) != Board.PUKA
                ) {
            return true;
        }

        //down move --> checks if the destination is within bound
        //checks if destination is a PUKA
        //checks if down adjacent stone and player turn
        else if (init_row + 2 < Board.BOARD_SIZE &&
                KonaneBoard.getStoneType(init_row + 2, init_col) == Board.PUKA &&
                KonaneBoard.getStoneType(init_row + 1, init_col) != Board.PUKA
                ) {
            return true;
        }
        return false;
    }


    //Checks if the destination of the move is valid
    //parameters --> 4 integers, first two are the row and col of first position
    //              and second two are the row and col of second position
    //returns true if destination position leads to valid move
    public boolean moveDestCheck(int init_row, int init_col, int dest_row, int dest_col) {
        //color of "to be killed" stone to make sure black not kill black and so on
        char stoneColor;
        if (p_turn == Board.BLACKSTONE) stoneColor = Board.WHITESTONE;
        else stoneColor = Board.BLACKSTONE;

        if ((dest_col < KonaneBoard.BOARD_SIZE && dest_col >= 0) &&
                (dest_row < KonaneBoard.BOARD_SIZE && dest_row >= 0) &&
                getKonaneBoard(dest_row, dest_col) == Board.PUKA) {
            if (init_col == dest_col) {
                //down move
                if (dest_row == init_row + 2 &&
                        getKonaneBoard(init_row + 1, init_col) == stoneColor) {
                    return true;
                }
                //up move
                else if (dest_row == init_row - 2 &&
                        getKonaneBoard(init_row - 1, init_col) == stoneColor) {
                    return true;
                }
            } else if (init_row == dest_row) {
                //right move
                if (dest_col == init_col + 2 &&
                        getKonaneBoard(init_row, init_col + 1) == stoneColor) {
                    return true;
                }
                //left move
                else if (dest_col == init_col - 2 &&
                        getKonaneBoard(init_row, init_col - 1) == stoneColor) {
                    return true;
                }
            }
        }
        return false;
    }

    ////parameters -->2 integers, row and col of the position in the board
    //returns the stone type in given position
    public char getKonaneBoard(int row, int col) {
        return KonaneBoard.getStoneType(row, col);
    }

    //parameters --> character, player type
    //returns int, the score of given type of player
    public int getScore(char playerType) {
        if (playerType == 'W') return p_White.getScore();
        else if (playerType == 'B') return p_Black.getScore();
        return -1;
    }

    //no parameter and returns void
    //alternates the turn when called
    public void updateTurn() {
        //update the next turn user
        if (p_turn == Board.BLACKSTONE) p_turn = Board.WHITESTONE;
        else p_turn = Board.BLACKSTONE;
    }

    //no parameter
    //returns the which player turn is at current
    public char getTurn() {
        return p_turn;
    }

    //load game from the file
    //parameters --> String, a line of the file being read
    //               integer, marker for which row is being read at the current
    //returns void
    public void loadGame(String line, int rowMarker) {
        //splitting the string line into array of strings temp
        String[] temp = line.split(" ");

        //Get black score
        if (temp[0].equals("Black:")) p_Black.updateScore(Integer.parseInt(temp[1]));
            //Get white score
        else if (temp[0].equals("White:")) p_White.updateScore(Integer.parseInt(temp[1]));

            //update rowMarker to read board placement
        else if (temp[0].equals("Board:")) rowMarker = 0;

            //Get turn
        else if (temp[0].equals("Next")) {
            if (temp[2].equals("White")) p_turn = Board.WHITESTONE;
            else p_turn = Board.BLACKSTONE;
        }
        else if(temp[0].equals("Human:")){
            //Human color stone
            if (temp[1].equals("White")) this.humanColor = Board.WHITESTONE;
            else this.humanColor = Board.BLACKSTONE;
        }
        //read stones position and update board
        else {
            int col = 0;
            for (String stoneType : temp) {
                KonaneBoard.setBoard_aftMove(rowMarker, col, stoneType.charAt(0));
                col++;
            }
        }
    }

    //makes tree of valid moves and calls required algorithm functions
    //parameters --> String, name of the algorithm selected
    //               integer, value of the constrain for Branch and Bound algorithm
    //returns Queue, list of valid moves after required algorithm implementation
    public Queue nextMoveAlgorithm(String AlgName,int[] constrain) {

        //Storing the valid moves in tree
        storeTree();

        //Check for the algorithm
        //breadthFirst Algorithm selected
        if(AlgName.equals(BREADTHFIRST)) {
            finalPathAlgorithm.clear();
            breadthFirstAlgorithm();
            return finalPathAlgorithm;
        }

        //bestFirst Algorithm selected
        else if(AlgName.equals(BESTFIRST)) {
            finalPathAlgorithm.clear();
            bestFirstAlgorithm();
            return finalPathAlgorithm;
        }

        //branchBound Algorithm selected
        else if(AlgName.equals(BRANCHBOUND)) {
            finalPathAlgorithm.clear();
            branchBoundAlgorithm(constrain[0]);
            return finalPathAlgorithm;
        }

        //MinMax Algorithms selected
        else if(AlgName.equals(MINMAX)) {
            finalPathAlgorithm.clear();

            //set the max depth ply
            this.depthPly = constrain[0];

            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;

            //set if pruning or not
            if (constrain[1] == 1) alphaBeta = true;
            else alphaBeta = false;


            noPreviousMove = false;
            firstMove = true;
            currentPlayer = p_turn;
            currentPlayerScore = 0;
            nextPlayerScore = 0;
            bestMove = new LinkedList();

            //restore score
            int blackScore = p_Black.getScore();
            int whiteScore = p_White.getScore();

            int scoreDiff = minMaxAlgorithm(alpha,beta);

            //restore score
            p_Black.setScore(blackScore);
            p_White.setScore(whiteScore);

            finalPathAlgorithm = new LinkedList(bestMove);
            return finalPathAlgorithm;

        }

        //else depthFirst Algorithm selected
        //  by default nodes stored using depth first algorithm
        return finalPathAlgorithm;
    }

    //checks for the valid moves and stores in the tree
    //returns false if no moves found
    //no parameter
    public boolean storeTree(){
        //initialize queue and tree
        finalPathAlgorithm = new LinkedList<>();
        nextMovesTree = new Tree();
        Node<Integer> initialValidPosition;

        //check for valid initial move and store in tree
        for(int i =0; i<Board.BOARD_SIZE;i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                //checks for valid move for given player
                if (p_turn == getKonaneBoard(i, j) && moveInitCheck(i, j) == true) {
                    //add valid initial position to the queue
                    initialValidPosition = new Node<>(i,j,nextMovesTree.getRoot());
                    addValidDestTree(i,j,initialValidPosition,'S');
                }
            }
        }

        if(finalPathAlgorithm.isEmpty()) return false;

        return true;
    }

    //parameters --> 2 integers, row and column of the initial valid position
    //              Node<Integer>, the initial valid position as node of tree
    //              Character, flagReverseCheck to restrict traversal to position where the function was called
    //                  Example: L_initial-->R_final then it restricts checking move from R_initial-->L_final
    //returns void
    //function uses depth first traversal to add elements to the tree
    //  therefore can be reused for depth first algorithm
    public void addValidDestTree(int i,int j,Node<Integer> initialPosition,char flagReverseCheck){

        //Initialize
        Node<Integer> newValidMoves;
        boolean flagEndRecursion = false;

        //Check 4 different valid places

        //Up or North
        if(flagReverseCheck != 'D' && moveDestCheck(i,j,i-2,j)){
            newValidMoves = new Node<>(i-2,j,initialPosition);

            char temp = KonaneBoard.getStoneType(i,j);
            char temp1 = KonaneBoard.getStoneType(i-1,j);
            KonaneBoard.setBoard_aftMove(i,j,Board.PUKA);
            KonaneBoard.setBoard_aftMove(i-1,j,Board.PUKA);

            //recursive call
            addValidDestTree(i-2,j,newValidMoves,'U');

            KonaneBoard.setBoard_aftMove(i,j,temp);
            KonaneBoard.setBoard_aftMove(i-1,j,temp1);

            flagEndRecursion = true;
        }

        //Right or East
        if(flagReverseCheck != 'L' && moveDestCheck(i,j,i,j+2)){
            newValidMoves = new Node<>(i,j+2,initialPosition);

            char temp = KonaneBoard.getStoneType(i,j);
            char temp1 = KonaneBoard.getStoneType(i,j+1);
            KonaneBoard.setBoard_aftMove(i,j,Board.PUKA);
            KonaneBoard.setBoard_aftMove(i,j+1,Board.PUKA);

            addValidDestTree(i,j+2,newValidMoves,'R');

            KonaneBoard.setBoard_aftMove(i,j,temp);
            KonaneBoard.setBoard_aftMove(i,j+1,temp1);

            flagEndRecursion = true;
        }

        //Down or South
        if(flagReverseCheck != 'U' && moveDestCheck(i,j,i+2,j)){
            newValidMoves = new Node<>(i+2,j,initialPosition);

            char temp = KonaneBoard.getStoneType(i,j);
            char temp1 = KonaneBoard.getStoneType(i+1,j);
            KonaneBoard.setBoard_aftMove(i,j,Board.PUKA);
            KonaneBoard.setBoard_aftMove(i+1,j,Board.PUKA);

            addValidDestTree(i+2,j,newValidMoves,'D');

            KonaneBoard.setBoard_aftMove(i,j,temp);
            KonaneBoard.setBoard_aftMove(i+1,j,temp1);

            flagEndRecursion = true;
        }

        //Left or West
        if(flagReverseCheck != 'R' && moveDestCheck(i,j,i,j-2)){
            newValidMoves = new Node<>(i,j-2,initialPosition);
            char temp = KonaneBoard.getStoneType(i,j);
            char temp1 = KonaneBoard.getStoneType(i,j-1);
            KonaneBoard.setBoard_aftMove(i,j,Board.PUKA);
            KonaneBoard.setBoard_aftMove(i,j-1,Board.PUKA);

            addValidDestTree(i,j-2,newValidMoves,'L');

            KonaneBoard.setBoard_aftMove(i,j,temp);
            KonaneBoard.setBoard_aftMove(i,j-1,temp1);
            flagEndRecursion = true;
        }

        //checks if it is end of the move
        //required to return the queue after Depth First Algorithm implementation
        if(!flagEndRecursion) {
            //stores all the path of the position
            addMovesSeries(initialPosition);

            //to mark the end of move series
            finalPathAlgorithm.add(MOVESEPARATOR);
        }


    }

    //BreadthFirst algorithm implementation
    //no parameter and returns void
    public void breadthFirstAlgorithm(){
        finalPathAlgorithm.clear();
        Queue<Node<Integer>> queue = new LinkedList<>();
        queue.add(nextMovesTree.getRoot());
        while (!queue.isEmpty())
        {
            //remove head
            Node<Integer> tempNode = queue.poll();
            if(tempNode != nextMovesTree.getRoot() && tempNode.getParent() != nextMovesTree.getRoot()){
                addMovesSeries(tempNode);
                finalPathAlgorithm.add(MOVESEPARATOR);
            }
            //access every children of given nodeq
            for(int i =0 ;i<tempNode.getChildren().size();i++){
                queue.add(tempNode.getChildren().get(i));
            }
        }
    }

    //BestFirst algorithm implementation
    //no parameter and returns void
    public void bestFirstAlgorithm(){
        breadthFirstAlgorithm();

        Queue tmp;
        PriorityQueue<Queue> priorityQueue = new PriorityQueue<Queue>(finalPathAlgorithm.size(),new Comparator<Queue>() {
            public int compare(Queue x, Queue y) {
                if (x.size()< y.size()) return 1;
                if (x.size() > y.size()) return -1;
                return 0;
            }
        });

        while(!finalPathAlgorithm.isEmpty()) {
            tmp = new LinkedList<>();

            while(finalPathAlgorithm.peek()!=MOVESEPARATOR) {
                tmp.add(finalPathAlgorithm.remove());
            }
            tmp.add(finalPathAlgorithm.remove());

            priorityQueue.add(tmp);
        }

        while(!priorityQueue.isEmpty()) {
            tmp = priorityQueue.poll();
            while(!tmp.isEmpty()) {
                finalPathAlgorithm.add(tmp.remove());
            }
        }
        /*
        finalPathAlgorithm.clear();

        Queue temp = new LinkedList();

        while(!finalPathAlgorithm.isEmpty()) {
            int score = 0;
            int scoreCompare = 0;
            while (score == scoreCompare) {
                scoreCompare = score;
                score = 0;
                while (finalPathAlgorithm.peek() != MOVESEPARATOR) {
                    score++;
                    temp.add(finalPathAlgorithm.remove());
                }
                temp.add(MOVESEPARATOR);
            }
        }
        /*
         */
    }

    //BestFirst algorithm implementation
    //no parameter and returns void
    public void branchBoundAlgorithm(int constrainBranchBound){
        bestFirstAlgorithm();

        //check if constrain less or equal to max score
        if(constrainBranchBound <= getHeadScore())
        {
            while(constrainBranchBound!=getHeadScore()){
                while(!finalPathAlgorithm.peek().equals(MOVESEPARATOR)){
                    finalPathAlgorithm.remove();
                }
                finalPathAlgorithm.remove();
            }
        }

        Queue temp = new LinkedList(finalPathAlgorithm);
        finalPathAlgorithm.clear();
        while(!temp.peek().equals(MOVESEPARATOR)){
            finalPathAlgorithm.add(temp.remove());
        }
        finalPathAlgorithm.add(MOVESEPARATOR);
    }

    //supports branchBoundAlgorithm function
    //no parameter
    //returns the score of the move at the head of the queue
    public int getHeadScore(){
        Queue temp = new LinkedList(finalPathAlgorithm);
        int count = 0;
        while(!temp.peek().equals(MOVESEPARATOR)) {
            temp.remove();
            count++;
        }
        return (count/2)-1;
    }

    //recursively finds the series of moves from the leaf node
    //      and stores in finalPathAlgorithm queue
    //parameter --> Node<Integer>, a valid move
    //returns void
    public void addMovesSeries(Node<Integer> validNode){

        //recurse till the root node
        if (validNode.getParent()!= nextMovesTree.getRoot()){
            addMovesSeries(validNode.getParent());
        }
        finalPathAlgorithm.add(validNode.getDataRow());
        finalPathAlgorithm.add(validNode.getDataCol());
    }

    //returns character, human stone color
    //no parameter
    public char getHumanStone() {
        return humanColor;
    }

    //MinMax Algorithm implementation
    //returns integer, the value of the max/min value selected byt maximizer/minimizer
    //paramter --> 2 integers , the value of alpha and beta for pruning
    public int minMaxAlgorithm(int alpha, int beta) {

        //System.out.println("about to store "+p_turn);
        int bestScore = 999;


        //creating a new tree of valid moves for given turn
        //WARNING: actually called twice for minMax could slow down
        if(!storeTree()){
            if(noPreviousMove){
                //System.out.println("NO moves for both player:");
                //System.out.println("1 Score:"+currentPlayerScore+" vs "+nextPlayerScore);
                noPreviousMove = false;
                return (currentPlayerScore-nextPlayerScore);
            }
            else{
                //System.out.println("NO moves Checking for next player depth"+depthPly);
                if(depthPly > 0 ) {
                    depthPly--;
                    //System.out.println("Passed depth: ");
                    noPreviousMove = true;

                    //change the turn before recursion
                    if(p_turn == KonaneBoard.BLACKSTONE) p_turn = KonaneBoard.WHITESTONE;
                    else p_turn = KonaneBoard.BLACKSTONE;

                    bestScore = minMaxAlgorithm(alpha,beta);

                    //restore the correct turn
                    if(p_turn == KonaneBoard.BLACKSTONE) p_turn = KonaneBoard.WHITESTONE;
                    else p_turn = KonaneBoard.BLACKSTONE;

                    //increasing value of ply to accommodate every moves after first move
                    depthPly++;

                    return bestScore;
                }
                else{
                    //System.out.println("2 Score:"+currentPlayerScore+" vs "+ nextPlayerScore);
                    return (currentPlayerScore-nextPlayerScore);
                }
            }
        }


        //best First to get all the moves in given turn sorted per highest score
        //WARNING: bestfirst uses breadthfirst and breadthFirst uses depthFirst
        //     time consuming
        //SOLUTION: Separate the algorithms
        breadthFirstAlgorithm();

        //Temporarily store the board info
        //minMax actually moves the stone
        // Also timeConsuming
        char[][] temp_boardStones = new char[KonaneBoard.BOARD_SIZE][KonaneBoard.BOARD_SIZE];
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                temp_boardStones[i][j] = KonaneBoard.getStoneType(i, j);
            }
        }

        while (!finalPathAlgorithm.isEmpty()) {
            int tempBestScore;

            Queue tempBestMove = new LinkedList(finalPathAlgorithm);

            //get the score of the move being made
            int tempScoreAftMove = getHeadScore();
            //update the score of the respective player
            if(p_turn == currentPlayer) currentPlayerScore+=tempScoreAftMove;
            else nextPlayerScore+=tempScoreAftMove;

            //get one move at a time from the list
            completeMove();
            //KonaneBoard.printBoard();
            //System.out.println("--"+depthPly+" "+ p_turn);

            //change the turn before calling recursion
            if(p_turn == KonaneBoard.BLACKSTONE) p_turn = KonaneBoard.WHITESTONE;
            else p_turn = KonaneBoard.BLACKSTONE;

            //check for depth cutoff to call the recursion
            if(depthPly > 0 ) {
                depthPly--;

                //save the values of finalPathAlgorithm before recursion
                Queue tempMoveStore = new LinkedList(finalPathAlgorithm);


                if(firstMove){
                    firstMove = false;
                    tempBestScore = minMaxAlgorithm(alpha,beta);
                    firstMove = true;
                }
                else{
                    tempBestScore = minMaxAlgorithm(alpha,beta);
                }


                //increasing value of ply to accommodate every moves after first move
                depthPly++;

                //restore the data
                restoreData(temp_boardStones,tempScoreAftMove);

                //restoring the values finalPathAlgorithm after recursion
                finalPathAlgorithm = new LinkedList(tempMoveStore);

                //Maximizer
                if(p_turn == currentPlayer) {
                    if (bestScore == 999 || tempBestScore > bestScore) {
                        bestScore = tempBestScore;

                        //Checking for the first move to store the best move
                        if(firstMove){
                            //save the best move
                            bestMove.clear();
                            while (tempBestMove.peek()!=MOVESEPARATOR){
                                bestMove.add(tempBestMove.remove());
                            }
                            bestMove.add(MOVESEPARATOR);
                        }

                    }
                }
                //MInimizer
                else{
                    if(bestScore == 999 || tempBestScore < bestScore) bestScore = tempBestScore;
                }

                //System.out.println("ScoreDifferene after:"+ tempBestScore+ "vs" + bestScore);
            }
            else{
                //System.out.println("Score:"+currentPlayerScore+" vs "+ nextPlayerScore);
                restoreData(temp_boardStones,tempScoreAftMove);
                return (currentPlayerScore-nextPlayerScore);
            }

            //Check for alphaBeta pruning
            if(alphaBeta){
                if(p_turn == currentPlayer && bestScore>alpha && bestScore != 999) alpha = bestScore;
                else if(p_turn != currentPlayer && bestScore<beta && bestScore != 999) beta = bestScore;
            }
            //break the while loop
            if(alphaBeta && alpha>=beta) break;
        }

        //if(finalPathAlgorithm.isEmpty()) System.out.println("Total moves finish for given turn");
        return bestScore;
    }

    //restores board, turn
    //returns void
    //paramter --> character array, the values of stone in the board
    //             integer, the value of the score
    public void restoreData(char[][] temp_boardStones,int tempScoreAftMove){
        //restore the correct turn
        if(p_turn == KonaneBoard.BLACKSTONE) p_turn = KonaneBoard.WHITESTONE;
        else p_turn = KonaneBoard.BLACKSTONE;

        //Restore the board
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                KonaneBoard.setBoard_aftMove(i, j, temp_boardStones[i][j]);
            }
        }

        //restore the score after end of recursion
        if(p_turn == currentPlayer) currentPlayerScore-=tempScoreAftMove;
        else nextPlayerScore-=tempScoreAftMove;
    }

     //called by minmax() and makes a move
    public void completeMove(){
        int row,col;
        row = (int) finalPathAlgorithm.remove();
        col = (int) finalPathAlgorithm.remove();

        //get the other positions of the move
        //make the move
        while (finalPathAlgorithm.peek() != Game.MOVESEPARATOR) {
            int row1, col1;
            row1 = (int) finalPathAlgorithm.remove();
            col1 = (int) finalPathAlgorithm.remove();
            //System.out.println("move made: "+row+col+row1+col1);
            makeMove(row,col,row1, col1);
            row = row1;
            col = col1;
        }
        //remove the move separator
        finalPathAlgorithm.remove();
    }
}