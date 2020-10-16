package sample;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;


public class Application extends javafx.application.Application {

    private static final int TILE_SIZE = 80;
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int RED = 1;
    private static final int YELLOW = -1;
    private int[][] discsOnGrid = new int[COLUMNS][ROWS];
    private Pane discsOnGridRoot = new Pane();
    private int turn = RED; // redTurn = 1, yellowTurn = -1, no winner = 0;

    private Parent createContent(){
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Pane root = new Pane(l);

        Shape gridShape = makeGrid();
        root.getChildren().add(discsOnGridRoot);
        root.getChildren().add(gridShape);
        root.getChildren().addAll(placeDiscDisplay());

        return root;
    }

    private Shape makeGrid(){
        Shape shape = new Rectangle((COLUMNS+1)*(TILE_SIZE+6), (ROWS+1)*(TILE_SIZE+6));
        for (int i=0; i<ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                Circle circle = new Circle();
                circle.setRadius(TILE_SIZE/2);
                circle.setCenterX(TILE_SIZE / 2);
                circle.setCenterY(TILE_SIZE / 2);
                circle.setTranslateX(j * (TILE_SIZE + 15) + TILE_SIZE / 4);
                circle.setTranslateY(i * (TILE_SIZE + 15) + TILE_SIZE / 4);

                shape = shape.subtract(shape, circle);

            }
        }
        shape.setFill(Color.BLUE);
        return shape;
    }

    private List<Rectangle> placeDiscDisplay(){
        List<Rectangle> rects = new ArrayList<>();
        for (int i=0; i< COLUMNS; i++){
            Rectangle rect = new Rectangle(TILE_SIZE+10, (ROWS+1)*TILE_SIZE+10);
            rect.setTranslateX(i * (TILE_SIZE+15) + (TILE_SIZE / 4)-5);
            rect.setTranslateY((TILE_SIZE/4)-5);

            rect.setFill(Color.TRANSPARENT);
            rect.setOnMouseEntered(e -> rect.setFill(Color.rgb(150,150,30,0.25)));
            rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));
            final int column = i;
            rect.setOnMouseClicked(e -> addDisc(column));

            rects.add(rect);
        }
        return rects;
    }

    private void addDisc(int column){
        int row = ROWS-1;                // start check from bottom
        while (row >= 0){
            if(discsOnGrid[column][row]==0) {
                break;
            }
            row--;
        }
        if(row>=0){
            final int currRow = row;
            Circle disc = new Circle();
            disc.setFill(turn==1 ? Color.RED : Color.YELLOW);
            disc.setRadius(TILE_SIZE / 2);
            disc.setCenterX(TILE_SIZE / 2);
            disc.setCenterY(TILE_SIZE / 2);
            disc.setTranslateX(column * (TILE_SIZE + 15) + TILE_SIZE / 4);

            discsOnGrid[column][row] = turn;
            discsOnGridRoot.getChildren().add(disc);
            turn = turn*(-1);
            TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), disc);
            animation.setToY(row * (TILE_SIZE + 15) + TILE_SIZE / 4);
            animation.setOnFinished(e->{ if (gameEnded(column, currRow))
                                            gameOver();});
            animation.play();
        }
    }

    private boolean gameEnded(int column, int row){
        int[] vertical = new int[ROWS];
        for (int i = 0; i < ROWS; i++) {
            vertical[i] = discsOnGrid[column][i];
        }
        int[] horizontal = new int[COLUMNS];
        for (int i = 0; i < COLUMNS; i++) {
            horizontal[i] = discsOnGrid[i][row];
        }
         // ascendingDiagonalCheck 
         for (int i=3; i<COLUMNS; i++){
             for (int j=0; j<ROWS-3; j++){
                 if (this.discsOnGrid[i][j] == turn*(-1) && this.discsOnGrid[i-1][j+1] == turn*(-1) &&
                         this.discsOnGrid[i-2][j+2] == turn*(-1) && this.discsOnGrid[i-3][j+3] == turn*(-1)) {
                     return true;
                 }
             }
         }
         // descendingDiagonalCheck
         for (int i=3; i<COLUMNS; i++){
             for (int j=3; j<ROWS; j++){
                 if (this.discsOnGrid[i][j] == turn*(-1) && this.discsOnGrid[i-1][j-1] == turn*(-1) &&
                         this.discsOnGrid[i-2][j-2] == turn*(-1) && this.discsOnGrid[i-3][j-3] == turn*(-1)) {
                     return true;
                 }
             }
         }
        return checkChain(vertical) || checkChain(horizontal)
                || isBoardFull();

    }

    private boolean checkChain(int[]chain){
        int chainLength=0;
        for (int i = 0; i < chain.length; i++) {
            if (chain[i] == turn*(-1))
                chainLength++;
            else
                chainLength=0;
            if (chainLength==4)
                return true;
        }
        return false;
    }

    private boolean isBoardFull(){
        for (int i = 0; i < discsOnGrid.length; i++) {
            for (int j = 0; j < discsOnGrid[i].length; j++) {
                if(discsOnGrid[i][j]==0)
                    return false;
            }
        }
        turn = 0;
        return true;
    }

    private void gameOver(){
        Stage gameOverWindow = new Stage();

        gameOverWindow.setTitle("Game Over");
        gameOverWindow.setMinWidth(300);

        Button exit = new Button("Exit");
        Button restart = new Button("Restart");
        exit.setMinWidth(50);
        restart.setMinWidth(50);

        Label message = new Label();
        message.setFont(Font.font("arial", FontWeight.BOLD,20));
        String winner = turn == RED ? "Yellow": turn == YELLOW ? "Red" : "Nobody :(";
        message.setText("The Winner: "+ winner);

        exit.setOnAction(e->exitGame());
        restart.setOnAction(e->restartGame(gameOverWindow, (Stage)Window.getWindows().get(0)));

        VBox vbox = new VBox();
        vbox.getChildren().add(message);
        vbox.setAlignment(Pos.CENTER);
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5));
        hbox.setSpacing(40);
        hbox.getChildren().addAll(restart, exit);
        hbox.setAlignment(Pos.CENTER);

        vbox.getChildren().add(hbox);

        Scene scene = new Scene(vbox);
        gameOverWindow.setScene(scene);
        gameOverWindow.show();
    }

    private void exitGame(){
        Platform.exit();
    }

    private void restartGame(Stage toExit, Stage s){
        toExit.close();
        startGame(s);
    }

    private void startGame(Stage stage){
        discsOnGrid = new int[COLUMNS][ROWS];
        discsOnGridRoot = new Pane();
        turn = RED;
        stage.setScene(new Scene(createContent()));
        stage.show();
    }


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Connect Four");
        //primaryStage.setScene(new Scene(root, 300, 275));
        startGame(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
