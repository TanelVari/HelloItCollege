package edu.tanelvari.java.pinks;

import com.sun.jdi.LongValue;
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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.io.InputStream;

public class Pinks extends Application {

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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Pinks");

        Group root = new Group();
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.rgb(0, 0, 0));
        primaryStage.setScene(scene);

        Canvas canvas = new Canvas(SCENE_WIDTH, SCENE_HEIGHT);
        root.getChildren().add(canvas);

        InputStream is = this.getClass().getResourceAsStream(FONT_FILE);
        if (is != null) {
            myFont = Font.loadFont(is, 64);
        }
        else {
            try {
                throw new IOException("Could not load font: " + FONT_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Random random = new Random(System.currentTimeMillis());

        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        Sprite leftPaddle = new Sprite(2 * BASE_UNIT, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT, 0, BALL_SIZE / 2);
        Sprite rightPaddle = new Sprite(SCENE_WIDTH - (2 * BASE_UNIT) - BALL_SIZE, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT, 0, BALL_SIZE / 2);

        Sprite ball = new Sprite((SCENE_WIDTH - BALL_SIZE) / 2, (SCENE_HEIGHT - BALL_SIZE) / 2, BALL_SIZE, BALL_SIZE, random.nextInt((int) (BALL_SIZE / 2)) + (BALL_SIZE / 4), random.nextInt((int) (BALL_SIZE / 2)) + (BALL_SIZE / 4));

        // set up score labels
        Label leftScoreLabel = CreateLabel();
        leftScoreLabel.setLayoutX((SCENE_WIDTH / 2) - 144);
        root.getChildren().add(leftScoreLabel);

        Label rightScoreLabel = CreateLabel();
        rightScoreLabel.setLayoutX((SCENE_WIDTH / 2) + 80);
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

                // check the collision between ball and bounds or right paddle
//                if (ball.getX() > MARGIN_RIGHT - BALL_SIZE) {
//                    ball.setX(MARGIN_RIGHT - BALL_SIZE);
//                    ball.setVelX(-ball.getVelX());
//                }
                if (ball.intersects(rightPaddle)) {
                    ball.setX(MARGIN_RIGHT - BALL_SIZE);
                    ball.setVelX(-ball.getVelX());
                    if (ball.getVelY() > 0 && inputKeys.contains("UP")){
                        ball.setVelY(-ball.getVelY());
                    } else if (ball.getVelY() < 0 && inputKeys.contains("DOWN")){
                        ball.setVelY(-ball.getVelY());
                    }
                }
                else if (ball.getX() > MARGIN_RIGHT - BALL_SIZE) {
                    ball.setX(MARGIN_LEFT);
                    ball.setY((SCENE_HEIGHT - BALL_SIZE) / 2);
                    ball.setVelX(Math.abs(ball.getVelX()));
                    leftScore++;
                    leftScoreLabel.setText(Integer.toString(leftScore));
                }

                if (ball.getY() > SCENE_HEIGHT - BALL_SIZE) {
                    ball.setY(SCENE_HEIGHT - BALL_SIZE);
                    ball.setVelY(-ball.getVelY());
                }

                if (ball.getX() < MARGIN_LEFT) {
                    ball.setX(MARGIN_LEFT);
                    ball.setVelX(-ball.getVelX());
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

                // render sprites on screen
                graphicsContext.clearRect(0, 0, SCENE_WIDTH, SCENE_HEIGHT);

                ball.renderSprite(graphicsContext);
                leftPaddle.renderSprite(graphicsContext);
                rightPaddle.renderSprite(graphicsContext);

            }
        }.start();
    }

    private Label CreateLabel() {
        Label label = new Label(Integer.toString(leftScore));
        label.setFont(myFont);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setLayoutY(BASE_UNIT * 2);
        label.setTextFill(Constants.mainColor);
        return label;
    }
}
