/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chessengine;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Scott
 */

public class BoardGUI extends Application {
    final int DIMENSION = 8;
    final int SQUARE_WIDTH = 100;
    final int SQUARE_HEIGHT = 100;
    final int PIECE_WIDTH = 70;
    final int PIECE_HEIGHT = 70;
    final double STROKE_WIDTH = 50;
    final Font COORDINATE_SIZE = new Font(20);
    String[] fileLex = {"A", "B", "C", "D", "E", "F", "G", "H"};
    String[] rankLex = {"1", "2", "3", "4", "5", "6", "7", "8"};
    ArrayList<Square> squares;
    ArrayList<Piece> whitePieces;
    ArrayList<Piece> blackPieces;
    ArrayList<PieceImageView> whitePieceImages = new ArrayList<PieceImageView>();
    ArrayList<PieceImageView> blackPieceImages = new ArrayList<PieceImageView>();;
    Square[][] squaresFromBoard;
    Square target;
    PieceTypeEnum typeOfPiece;
    String DIVIDOR = "--------------------------";
    
    @Override
    public void start(Stage primaryStage) {   
        Board board = new Board();
        Group root = new Group();
        Scene scene = new Scene(root, DIMENSION * SQUARE_WIDTH, 
                                    DIMENSION * SQUARE_HEIGHT);
        
        whitePieces = board.getWhitePieces();
        blackPieces = board.getBlackPieces();
        
        PieceImageView pv;        
        for (Piece whitePiece : whitePieces) {
            target = whitePiece.getCurrentSquare(); 
            pv = new PieceImageView(whitePiece, PIECE_HEIGHT, PIECE_WIDTH, 
                target.getColumn() * SQUARE_WIDTH + SQUARE_WIDTH / 5, 
                target.getRow() * SQUARE_HEIGHT + SQUARE_WIDTH / 10);
            
            System.out.println(whitePiece.toString() + "\t| row: " +
                    target.getRow() + "\t| column: " +
                    target.getColumn());
            whitePieceImages.add(pv); 
        }
        
        whitePieceImages.stream().forEach((v) -> {
            givePieceEvents(v);
        });
        
        // my version vs theirs
//        for (PieceImageView v : whitePieceImages){
//            givePieceEvents(v);
//        }
        
        for (Piece blackPiece : blackPieces) {
            target = blackPiece.getCurrentSquare(); 
            pv = new PieceImageView(blackPiece, PIECE_HEIGHT, PIECE_WIDTH, 
                target.getColumn() * SQUARE_WIDTH + SQUARE_WIDTH / 5, 
                target.getRow()* SQUARE_HEIGHT + SQUARE_WIDTH / 10);
            
            System.out.println(blackPiece.toString() + "\t| row: " +
                    target.getRow() + "\t| column: " +
                    target.getColumn());
            blackPieceImages.add(pv); 
        }
        
        blackPieceImages.stream().forEach((v) -> {
            givePieceEvents(v);
        });
        
        board.setUpNewGame();
        squaresFromBoard = board.getSquares();
        squares = new ArrayList<Square>();
        convertFrom2DArrayToArrayList(squares, squaresFromBoard);
              
        giveSquaresColor(squares);
        root.getChildren().addAll(squares);
        //root.getChildren().addAll(whitePieces);
        //root.getChildren().addAll(blackPieces);
        addRankAndFileTexts(root);
        
        for (int i = 0; i < squares.size(); i++){
            giveSquareEventHandling(squares.get(i));
        }
        
        root.getChildren().addAll(whitePieceImages);
        root.getChildren().addAll(blackPieceImages);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void addRankAndFileTexts(Group root){
        for (int i = 0; i < DIMENSION; ++i){
            for (int j = DIMENSION-1; j >= 0; --j){
                Text text = new Text (
                        ((i * SQUARE_WIDTH)),
                        ((Math.abs(j-(DIMENSION-1)) * SQUARE_HEIGHT) + SQUARE_HEIGHT),
                        (fileLex[i] + rankLex[j]));
                
                text.setFont(COORDINATE_SIZE);
                root.getChildren().add(text);
            }
        }
    }
    
    private void giveSquaresColor(ArrayList<Square> squares){
        for (int i = 0; i < DIMENSION; ++i){
            for (int j = 0; j < DIMENSION; ++j){
                
                // Acts as a normal 2D array access
                target = squares.get((i * DIMENSION) + j);
                
                // Sets color
                if (i % 2 == 0){
                    if (j % 2 == 0){
                        target.setFill(Color.BEIGE);
                    }
                    else{
                        target.setFill(Color.DARKGREEN);
                    }
                }
                else{
                    if (j % 2 != 0){
                        target.setFill(Color.BEIGE);
                    }
                    else{
                        target.setFill(Color.DARKGREEN);
                    }
                }
            } // end j for
        } // end i for
    }
    
    private void giveSquareEventHandling (Square square){        
        square.setOnMouseClicked(new EventHandler <MouseEvent>() {
            
            public void handle(MouseEvent event) {
                System.out.println("clicked square | row: " + 
                        square.getRow() + "\tcolumn: " +
                        square.getColumn() + "\toccupied: " +
                        square.getOccupied());       
            }
        });
    }
    
    // Converts the 2D array of squares into an array list. Annoying result
    // of using two different data structures without thinking about it
    private void convertFrom2DArrayToArrayList(ArrayList<Square> squares,
                                                Square[][] s){
        for (int i = 0; i < DIMENSION; i++){
            for (int j = 0; j < DIMENSION; j++){
                squares.add(s[i][j]);
            }
        }
    }
    
    public void givePieceEvents(PieceImageView pv){
        final Delta dragDelta = new Delta();
        final Delta start = new Delta();
        
        pv.setOnMouseClicked(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    Piece piece = pv.getPiece();
                    
                    System.out.println(piece.toString());
                }    
            }
        );
        
        pv.setOnMousePressed(new EventHandler<MouseEvent>() {         
            @Override
            public void handle(MouseEvent mouseEvent) {
                start.x = pv.getLayoutX();
                start.y = pv.getLayoutY();
                System.out.println("start x: " + start.x + " | start y: " + start.y);
                
                // record a delta distance for the drag and drop operation.
                dragDelta.x = pv.getLayoutX() - mouseEvent.getSceneX();
                dragDelta.y = pv.getLayoutY() - mouseEvent.getSceneY();
                pv.setCursor(Cursor.MOVE);
            }
        });
        
        pv.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override 
            public void handle(MouseEvent mouseEvent) {
                pv.setCursor(Cursor.HAND);

                double xDrop = pv.getLayoutX() + (SQUARE_WIDTH / 2);
                double yDrop = pv.getLayoutY() + (SQUARE_HEIGHT / 2);

                // Squares a piece is moving from and to
                Square sourceSquare;
                Square targetSquare;
                int count = 0;

                // finds the index of the square that the piece
                // was dropped into
                while (!squares.get(count).contains(xDrop, yDrop) && 
                        count < squares.size()){
                    count++;
                }

                // Checks if the piece is brought off the board.
                // If the piece is, put it back to its starting posision.
                if (count >= squares.size()){
                    pv.setLayoutX(start.x);
                    pv.setLayoutY(start.y);
                }
                else{
                
                    targetSquare = squares.get(count);

                    if (!targetSquare.getOccupied()){
                        System.out.println("TARGET SQUARE NOT OCCUPIED");

                        // make the square that the piece was occupying empty
                        Piece piece = pv.getPiece();
                        sourceSquare = piece.getCurrentSquare();
                        sourceSquare.setOccupied(false);
                        sourceSquare.setOccupyingPiece(null);
                        System.out.println("source square |\trow: " + 
                                sourceSquare.getRow() + "\tcolumn: " +
                                sourceSquare.getColumn() + "\toccupied: " +
                                sourceSquare.getOccupied());
                        
                        // move image of piece to target sqaure
                        pv.setLayoutX(targetSquare.getX() + (SQUARE_WIDTH / 5));
                        pv.setLayoutY(targetSquare.getY() + (SQUARE_HEIGHT / 10));
                        
                        // make the sqaure that the piece moves to occupied
                        targetSquare.setOccupied(true);
                        targetSquare.setOccupyingPiece(piece);
                        
                        // set piece's current square
                        piece.setCurrentSquare(targetSquare);
                        System.out.println("target square |\trow: " + 
                                targetSquare.getRow() + "\tcolumn: " +
                                targetSquare.getColumn() + "\toccupied: " +
                                targetSquare.getOccupied());
                    }
                    else{
                        System.out.println("TARGET SQUARE OCCUPIED");
                                     
                        // move the image of the piece back to the 
                        // starting pos
                        pv.setLayoutX(start.x);
                        pv.setLayoutY(start.y);
                    }
                }
            }
        });
        
        pv.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override 
            public void handle(MouseEvent mouseEvent) {
                //System.out.println("setOnMouseDragged");
                pv.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
                pv.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            }
        });
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    // records relative x and y co-ordinates.
  class Delta { double x, y; }
    
}
