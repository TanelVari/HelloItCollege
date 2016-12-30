package edu.tanelvari.java.pinks;

import com.sun.jdi.LongValue;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Pinks extends Application {

    private static final double SCENE_WIDTH = 1024;
    private static final double SCENE_HEIGHT = 768;

    private static final double BASE_UNIT = 24;

    private static final double BALL_SIZE = BASE_UNIT;

    private static final double PADDLE_WIDTH = BASE_UNIT;
    private static final double PADDLE_HEIGHT = BASE_UNIT * 10;

    private static final double MARGIN = 2 * BASE_UNIT + PADDLE_WIDTH;
    private static final double MARGIN_LEFT = MARGIN;
    private static final double MARGIN_RIGHT = SCENE_WIDTH - MARGIN;

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

        Random random = new Random(System.currentTimeMillis());

        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        Sprite leftPaddle = new Sprite(2 * BASE_UNIT, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT);
        Sprite rightPaddle = new Sprite(SCENE_WIDTH - (2 * BASE_UNIT) - BALL_SIZE, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT);

        Sprite ball = new Sprite(SCENE_WIDTH / 2, SCENE_HEIGHT / 2, BASE_UNIT, BASE_UNIT);

        new AnimationTimer() {
            @Override
            public void handle(long arg) {

                ball.updateSpritePosition();

                if (ball.getX() > MARGIN_RIGHT - BALL_SIZE) {
                    ball.setX(MARGIN_RIGHT - BALL_SIZE);
                    ball.setVelX(-ball.getVelX());
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

                graphicsContext.clearRect(0, 0, SCENE_WIDTH, SCENE_HEIGHT);

                ball.renderSprite(graphicsContext);
                leftPaddle.renderSprite(graphicsContext);
                rightPaddle.renderSprite(graphicsContext);

            }
        }.start();

        primaryStage.show();
    }
}
