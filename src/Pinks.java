package edu.tanelvari.java.pinks;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;
import java.io.InputStream;

import static com.sun.corba.se.impl.util.Version.asString;

public class Pinks extends Application {

    private static final Boolean DEBUG = true;

    private static final String FONT_FILE = "/fonts/kongtext.ttf";
    private static final String CSS_FILE = "/css/Buttons.css";

    private static final double SCENE_WIDTH = 1024;
    private static final double SCENE_HEIGHT = 768;

    private static final double BASE_UNIT = 24;

    private static final double BALL_SIZE = BASE_UNIT;

    private static final double PADDLE_WIDTH = BASE_UNIT;
    private static final double PADDLE_HEIGHT = BASE_UNIT * 8;

    private static final double MARGIN = 2 * BASE_UNIT + PADDLE_WIDTH;
    private static final double MARGIN_LEFT = MARGIN;
    private static final double MARGIN_RIGHT = SCENE_WIDTH - MARGIN;

    private static final int WINNING_SCORE = 3;

    private int leftScore = 0;
    private int rightScore = 0;
    private Font myLabelFont;

    private Random random = new Random(System.currentTimeMillis());
    private double paddleVelocity = BALL_SIZE / 2;
    private double computerPaddleVelocity = BALL_SIZE / 3;

    private Sprite ball;
    private Sprite leftPaddle;
    private Sprite rightPaddle;

    private ArrayList<String> inputKeys = new ArrayList<String>();

    private Label leftScoreLabel;
    private Label rightScoreLabel;

    private GraphicsContext graphicsContext;

    private enum GameState{
        MENU,
        ONE_PLAYER,
        TWO_PLAYER,
        RESULT
    }

    private VBox menuVBox;

    private GameState currentGameState = GameState.MENU;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Pinks v1.o");

        Group root = new Group();
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.BLACK);
        scene.getStylesheets().add(getClass().getResource(CSS_FILE).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        Canvas canvas = new Canvas(SCENE_WIDTH, SCENE_HEIGHT);
        root.getChildren().add(canvas);

        graphicsContext = canvas.getGraphicsContext2D();

        leftPaddle = new Sprite(2 * BASE_UNIT, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT, 0, computerPaddleVelocity);
        rightPaddle = new Sprite(SCENE_WIDTH - (2 * BASE_UNIT) - BALL_SIZE, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT, 0, paddleVelocity);

        double minVeloX = BALL_SIZE / 2 * 0.85;
        double maxVeloX = BALL_SIZE / 2 * 1.1;

        double minVeloY = BALL_SIZE / 2 * 0.8;
        double maxVeloY = BALL_SIZE / 2 * 1.05;

        double veloX = random.nextInt((int) (maxVeloX - minVeloX)) + minVeloX;
        double veloY = random.nextInt((int) (maxVeloY - minVeloY)) + minVeloY;

        ball = new Sprite((SCENE_WIDTH - BALL_SIZE) / 2, (SCENE_HEIGHT - BALL_SIZE) / 2, BALL_SIZE, BALL_SIZE, veloX, veloY);

        if (DEBUG) {
            System.out.println("Velocity X: " + veloX + "\nVelocity Y: " + veloY);
        }

        // set up score labels
        leftScoreLabel = CreateLabel();
        rightScoreLabel = CreateLabel();

        myLabelFont = LoadCustomFont(64);

        leftScoreLabel.setFont(myLabelFont);
        rightScoreLabel.setFont(myLabelFont);

        leftScoreLabel.setLayoutX((SCENE_WIDTH / 2) - 144);
        rightScoreLabel.setLayoutX((SCENE_WIDTH / 2) + 80);

        root.getChildren().add(leftScoreLabel);
        root.getChildren().add(rightScoreLabel);

        // draw the middle line
        Line middleLine = new Line(SCENE_WIDTH / 2, 0, SCENE_WIDTH / 2, SCENE_HEIGHT);
        middleLine.getStrokeDashArray().addAll(24d, 16d);
        middleLine.setStrokeLineCap(StrokeLineCap.BUTT);
        middleLine.setStrokeWidth(4);
        middleLine.setStroke(Constants.mainColor);
        middleLine.setStrokeDashOffset(8d);
        root.getChildren().add(middleLine);

        // create the Menu box
        menuVBox = CreateMenuBox();
        root.getChildren().add(menuVBox);

        // show all the stuff on stage
        primaryStage.show();

        /*** EVENTS AND TIMERS ***/

        // set up event handlers for keypress
        scene.setOnKeyPressed(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent e) {
                        if (currentGameState == GameState.ONE_PLAYER || currentGameState == GameState.TWO_PLAYER){
                            String code = e.getCode().toString();
                            if (!inputKeys.contains(code)) {
                                inputKeys.add(code);
                            }
                        }
                    }
                }
        );

        scene.setOnKeyReleased(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent e) {
                        if (currentGameState == GameState.ONE_PLAYER || currentGameState == GameState.TWO_PLAYER) {
                            String code = e.getCode().toString();
                            inputKeys.remove(code);
                        }
                    }
                }
        );

        // define and start the timer
        new AnimationTimer() {
            @Override
            public void handle(long arg) {

                if (leftScore >= WINNING_SCORE || rightScore >= WINNING_SCORE){
                    currentGameState = GameState.RESULT;
                }

                switch (currentGameState){
                    case MENU:
                    case RESULT:
                        ShowMenu();
                        break;
                    case ONE_PLAYER:
                    case TWO_PLAYER:
                        PlayGame(false);
                        break;
                }
            }
        }.start();
    }

    /*** PRIVATE METHODS ***/

    private Label CreateLabel() {
        Label label = new Label(Integer.toString(leftScore));
        label.setTextAlignment(TextAlignment.CENTER);
        label.setLayoutY(BASE_UNIT * 2);
        label.setTextFill(Constants.mainColor);
        return label;
    }

    private int randomReverse() {
        return random.nextBoolean() ? 1 : -1;
    }


    private Font LoadCustomFont(double size) {
        Font font = null;
        InputStream is = this.getClass().getResourceAsStream(FONT_FILE);

        if (is != null) {
            font = Font.loadFont(is, size);
        }
        else {
            try {
                throw new Exception("Error loading font: " + FONT_FILE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return font;
    }

    private VBox CreateMenuBox(){

        double width = SCENE_WIDTH - (26 * BASE_UNIT);
        double height = SCENE_HEIGHT - (16* BASE_UNIT);

        Font font = LoadCustomFont(32);

        VBox vBox = new VBox(2 * BASE_UNIT);
        vBox.setLayoutX((SCENE_WIDTH - width) / 2);
        vBox.setLayoutY((SCENE_HEIGHT - height) / 2);
        vBox.setMinWidth(width);
        vBox.setMinHeight(height);

        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(BASE_UNIT, BASE_UNIT, BASE_UNIT, BASE_UNIT));
        vBox.setStyle("-fx-background-color: #000000; -fx-border-color: #FFFFFF; -fx-border-width: 4;");

        Button computerPlayButton = new Button("1 player");
        computerPlayButton.setMinWidth(width - (2 * BASE_UNIT));
        computerPlayButton.setFont(font);

        computerPlayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                currentGameState = GameState.ONE_PLAYER;
                StartBallFromRightPaddle();
            }
        });

        Button playButton = new Button("2 players");
        playButton.setMinWidth(width - (2 * BASE_UNIT));
        playButton.setFont(font);

        vBox.getChildren().addAll(computerPlayButton, playButton);

        return vBox;
    }

    private void ShowMenu(){
        menuVBox.setVisible(true);

        leftScoreLabel.setVisible(false);
        rightScoreLabel.setVisible(false);

        PlayGame(true);

        rightScore = 0;
        leftScore = 0;
    }

    private void PlayGame(Boolean isInDemoMode){

        if (!isInDemoMode){
            menuVBox.setVisible(false);

            leftScoreLabel.setVisible(true);
            rightScoreLabel.setVisible(true);
        }

        // update the ball position
        ball.updateSpritePosition();

        // update the position for right paddle if applicable
        if (!isInDemoMode){
            if (inputKeys.contains("UP")) {
                rightPaddle.setVelY(-(Math.abs(rightPaddle.getVelY())));
                rightPaddle.updateSpritePosition();
            }
        }

        if (!isInDemoMode){
            if (inputKeys.contains("DOWN")) {
                rightPaddle.setVelY(Math.abs(rightPaddle.getVelY()));
                rightPaddle.updateSpritePosition();
            }
        }

        // move the left paddle in demo mode or in 2 player game
        if (isInDemoMode || currentGameState == GameState.ONE_PLAYER){
            double leftPaddleDiff = (leftPaddle.getY() + (PADDLE_HEIGHT / 2)) - (ball.getY() + (BALL_SIZE / 2));
            if (leftPaddleDiff > 0 && leftPaddleDiff > computerPaddleVelocity) {
                leftPaddle.setVelY(-(Math.abs(leftPaddle.getVelY())));
                leftPaddle.updateSpritePosition();
            }
            else if (leftPaddleDiff < 0 && leftPaddleDiff < -computerPaddleVelocity) {
                leftPaddle.setVelY(Math.abs(leftPaddle.getVelY()));
                leftPaddle.updateSpritePosition();
            }
        }

        // move the right paddle in demo mode
        if (isInDemoMode){
            double rightPaddleDiff = (rightPaddle.getY() + (PADDLE_HEIGHT / 2)) - (ball.getY() + (BALL_SIZE / 2));
            if (rightPaddleDiff > 0 && rightPaddleDiff > paddleVelocity) {
                rightPaddle.setVelY(-(Math.abs(rightPaddle.getVelY())));
                rightPaddle.updateSpritePosition();
            }
            else if (rightPaddleDiff < 0 && rightPaddleDiff < -paddleVelocity) {
                rightPaddle.setVelY(Math.abs(rightPaddle.getVelY()));
                rightPaddle.updateSpritePosition();
            }
        }

        // check the collision between ball and right paddle
        if (ball.intersects(rightPaddle)) {
            ball.setX(MARGIN_RIGHT - BALL_SIZE);
            ball.setVelX(-ball.getVelX());
            // if the paddle was still moving opposite direction then reverse the Y angle to give the ball a backward spin
            if (ball.getVelY() > 0 && inputKeys.contains("UP")) {
                ball.setVelY(-ball.getVelY());
            }
            else if (ball.getVelY() < 0 && inputKeys.contains("DOWN")) {
                ball.setVelY(-ball.getVelY());
            }
        }
        else if (ball.getX() > MARGIN_RIGHT - BALL_SIZE) {
            StartBallFromLeftPaddle();
            leftScore++;
        }

        // check the collision between ball and left paddle
        if (ball.intersects(leftPaddle)) {
            ball.setX(MARGIN_LEFT);
            ball.setVelX(-ball.getVelX());
        }
        else if (ball.getX() < MARGIN_LEFT) {
            StartBallFromRightPaddle();
            rightScore++;
        }

        // check the top and bottom bounds against the ball movement
        if (ball.getY() > SCENE_HEIGHT - BALL_SIZE) {
            ball.setY(SCENE_HEIGHT - BALL_SIZE);
            ball.setVelY(-ball.getVelY());
        }

        if (ball.getY() < 0) {
            ball.setY(0);
            ball.setVelY(-ball.getVelY());
        }

        //check boundaries for right paddle
        if (rightPaddle.getY() > SCENE_HEIGHT - PADDLE_HEIGHT) {
            rightPaddle.setY(SCENE_HEIGHT - PADDLE_HEIGHT);
        }
        if (rightPaddle.getY() < 0) {
            rightPaddle.setY(0);
        }

        //check boundaries for the computer player
        if (leftPaddle.getY() > SCENE_HEIGHT - PADDLE_HEIGHT) {
            leftPaddle.setY(SCENE_HEIGHT - PADDLE_HEIGHT);
        }
        if (leftPaddle.getY() < 0) {
            leftPaddle.setY(0);
        }

        // render sprites on screen
        graphicsContext.clearRect(0, 0, SCENE_WIDTH, SCENE_HEIGHT);

        ball.renderSprite(graphicsContext);
        leftPaddle.renderSprite(graphicsContext);
        rightPaddle.renderSprite(graphicsContext);

        if (!isInDemoMode){
            rightScoreLabel.setText(Integer.toString(rightScore));
            leftScoreLabel.setText(Integer.toString(leftScore));
        }
    }

    private void  StartBallFromLeftPaddle(){
        ball.setX(MARGIN_LEFT);
        ball.setY(leftPaddle.getY() + (PADDLE_HEIGHT / 2) - (BALL_SIZE / 2));
        ball.setVelX(Math.abs(ball.getVelX()));
        ball.setVelY(ball.getVelY() * randomReverse());
    }

    private void StartBallFromRightPaddle(){
        ball.setX(MARGIN_RIGHT - BALL_SIZE);
        ball.setY(rightPaddle.getY() + (PADDLE_HEIGHT / 2) - (BALL_SIZE / 2));
        ball.setVelX(-(Math.abs(ball.getVelX())));
        ball.setVelY(ball.getVelY() * randomReverse());
    }
}
