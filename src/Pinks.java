package edu.tanelvari.java.pinks;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;


import java.util.ArrayList;
import java.util.Random;
import java.io.InputStream;

public class Pinks extends Application {

    private static final Boolean DEBUG = true;

    private static final String FONT_FILE = "/fonts/kongtext.ttf";

    private static final double SCENE_WIDTH = 1024;
    private static final double SCENE_HEIGHT = 768;

    private static final double BASE_UNIT = 24;

    private static final double BALL_SIZE = BASE_UNIT;

    private static final double PADDLE_WIDTH = BASE_UNIT;
    private static final double PADDLE_HEIGHT = BASE_UNIT * 8;

    private static final double MARGIN = 2 * BASE_UNIT + PADDLE_WIDTH;
    private static final double MARGIN_LEFT = MARGIN;
    private static final double MARGIN_RIGHT = SCENE_WIDTH - MARGIN;

    private int leftScore = 0;
    private int rightScore = 0;
    private Font myFont;

    private Random random = new Random(System.currentTimeMillis());
    private double paddleVelocity = BALL_SIZE / 2;
    private double computerPaddleVelocity = BALL_SIZE / 3;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Pinks v1.o");

        Group root = new Group();
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.BLACK);
        primaryStage.setScene(scene);

        Canvas canvas = new Canvas(SCENE_WIDTH, SCENE_HEIGHT);
        root.getChildren().add(canvas);

        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        Sprite leftPaddle = new Sprite(2 * BASE_UNIT, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT, 0, computerPaddleVelocity);
        Sprite rightPaddle = new Sprite(SCENE_WIDTH - (2 * BASE_UNIT) - BALL_SIZE, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT, 0, paddleVelocity);

        double minVeloX = BALL_SIZE / 2 * 0.85;
        double maxVeloX = BALL_SIZE / 2 * 1.15;

        double minVeloY = BALL_SIZE / 2 * 0.75;
        double maxVeloY = BALL_SIZE / 2 * 1.05;

        double veloX = random.nextInt((int) (maxVeloX - minVeloX)) + minVeloX;
        double veloY = random.nextInt((int) (maxVeloY - minVeloY)) + minVeloY;

        Sprite ball = new Sprite((SCENE_WIDTH - BALL_SIZE) / 2, (SCENE_HEIGHT - BALL_SIZE) / 2, BALL_SIZE, BALL_SIZE, veloX, veloY);

        if (DEBUG) {
            System.out.println("Velocity X: " + veloX + "\nVelocity Y: " + veloY);
        }

        // set up score labels
        Label leftScoreLabel = CreateLabel();
        Label rightScoreLabel = CreateLabel();

        InputStream is = this.getClass().getResourceAsStream(FONT_FILE);
        if (is != null) {
            myFont = Font.loadFont(is, 64);
            leftScoreLabel.setFont(myFont);
            rightScoreLabel.setFont(myFont);
        }
        else {
            try {
                throw new Exception("Error loading font: " + FONT_FILE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        // show all the stuff on stage
        primaryStage.show();

        /*** EVENTS AND TIMERS ***/

        // set up event handlers for keypress
        ArrayList<String> inputKeys = new ArrayList<String>();

        scene.setOnKeyPressed(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent e) {
                        String code = e.getCode().toString();
                        if (!inputKeys.contains(code)) {
                            inputKeys.add(code);
                        }
                    }
                }
        );

        scene.setOnKeyReleased(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent e) {
                        String code = e.getCode().toString();
                        inputKeys.remove(code);
                    }
                }
        );

        // define and start the timer
        new AnimationTimer() {
            @Override
            public void handle(long arg) {

                // update the ball position
                ball.updateSpritePosition();

                // update the position for right paddle if applicable
                if (inputKeys.contains("UP")) {
                    rightPaddle.setVelY(-(Math.abs(rightPaddle.getVelY())));
                    rightPaddle.updateSpritePosition();
                }

                if (inputKeys.contains("DOWN")) {
                    rightPaddle.setVelY(Math.abs(rightPaddle.getVelY()));
                    rightPaddle.updateSpritePosition();
                }

                // move the left paddle with AI
                double difference = (leftPaddle.getY() + (PADDLE_HEIGHT / 2)) - (ball.getY() + (BALL_SIZE / 2));
                if (difference > 0 && difference > computerPaddleVelocity) {
                    leftPaddle.setVelY(-(Math.abs(leftPaddle.getVelY())));
                    leftPaddle.updateSpritePosition();
                }
                else if (difference < 0 && difference < -computerPaddleVelocity) {
                    leftPaddle.setVelY(Math.abs(leftPaddle.getVelY()));
                    leftPaddle.updateSpritePosition();
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
                    ball.setX(MARGIN_LEFT);
                    ball.setY(leftPaddle.getY() + (PADDLE_HEIGHT / 2) - (BALL_SIZE / 2));
                    ball.setVelX(Math.abs(ball.getVelX()));
                    ball.setVelY(ball.getVelY() * randomReverse());
                    leftScore++;
                    leftScoreLabel.setText(Integer.toString(leftScore));
                }

                // check the collision between ball and right paddle
                if (ball.intersects(leftPaddle)) {
                    ball.setX(MARGIN_LEFT);
                    ball.setVelX(-ball.getVelX());
                }
                else if (ball.getX() < MARGIN_LEFT) {
                    ball.setX(MARGIN_RIGHT - BALL_SIZE);
                    ball.setY(rightPaddle.getY() + (PADDLE_HEIGHT / 2) - (BALL_SIZE / 2));
                    ball.setVelX(-(Math.abs(ball.getVelX())));
                    ball.setVelY(ball.getVelY() * randomReverse());
                    rightScore++;
                    rightScoreLabel.setText(Integer.toString(rightScore));
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
}
