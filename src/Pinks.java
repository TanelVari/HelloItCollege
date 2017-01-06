package edu.tanelvari.java.pinks;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
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

public class Pinks extends Application {

    private static final Boolean DEBUG = false;

    private static final String FONT_FILE = "/fonts/kongtext.ttf";
    private static final String CSS_FILE = "/css/Buttons.css";

    private static final String PLINK_LEFT_SOUND_FILE = "/sound/pinks1.mp3";
    private static final String PLINK_RIGHT_SOUND_FILE = "/sound/pinks2.mp3";
    private static final String PLONK_TABLE_SOUND_FILE = "/sound/pinks_laud.mp3";
    private static final String SCORE_SOUND_FILE = "/sound/buzz.mp3";
    private static final String WIN_SOUND_FILE = "/sound/tada.mp3";

    private static final double SCENE_WIDTH = 1024;
    private static final double SCENE_HEIGHT = 768;

    private static final double BASE_UNIT = 24;

    private static final double BALL_SIZE = BASE_UNIT;

    private static final double PADDLE_WIDTH = BASE_UNIT;
    private static final double PADDLE_HEIGHT = BASE_UNIT * 8;
    private static final double PC_PADDLE_VELOCITY = BALL_SIZE / 3;

    private static final double MARGIN = 2 * BASE_UNIT + PADDLE_WIDTH;
    private static final double MARGIN_LEFT = MARGIN;
    private static final double MARGIN_RIGHT = SCENE_WIDTH - MARGIN;

    private static final int WINNING_SCORE = 5;
    private static final double TIME_TO_SHOW_BANNER = 3;

    private static final String PC_WINS = "COMPUTER WINS";
    private static final String PLAYER_WINS = "PLAYER WINS";
    private static final String LEFT_PLAYER_WINS = "LEFT PLAYER WINS";
    private static final String RIGHT_PLAYER_WINS = "RIGHT PLAYER WINS";

    private double minBallVeloX;
    private double maxBallVeloX;

    private double minBallVeloY;
    private double maxBallVeloY;

    private int leftScore = 0;
    private int rightScore = 0;
    private Font myLabelFont;
    private double accumulatedTime;
    private long lastTime;

    private Random random = new Random(System.currentTimeMillis());
    private double leftPaddleVelocity = PC_PADDLE_VELOCITY;
    private double rightPaddleVelocity = BALL_SIZE / 2;

    private Sprite ball;
    private Sprite leftPaddle;
    private Sprite rightPaddle;

    private ArrayList<String> inputKeys = new ArrayList<String>();

    private Label leftScoreLabel;
    private Label rightScoreLabel;

    private GraphicsContext graphicsContext;

    private enum GameState {
        MENU,
        ONE_PLAYER,
        TWO_PLAYER,
        RESULT
    }

    private GameState currentGameState = GameState.MENU;

    private enum Side {
        LEFT,
        RIGHT
    }

    private VBox menuVBox;
    private VBox winnerBox;

    private javafx.scene.media.AudioClip plinkLeftSound;
    private javafx.scene.media.AudioClip plinkRightSound;
    private javafx.scene.media.AudioClip plonkTableSound;
    private javafx.scene.media.AudioClip scoreSound;
    private javafx.scene.media.AudioClip winSound;

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

        leftPaddle = new Sprite(2 * BASE_UNIT, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT, 0, leftPaddleVelocity);
        rightPaddle = new Sprite(SCENE_WIDTH - (2 * BASE_UNIT) - BALL_SIZE, (SCENE_HEIGHT - PADDLE_HEIGHT) / 2, PADDLE_WIDTH, PADDLE_HEIGHT, 0, rightPaddleVelocity);

        ball = new Sprite((SCENE_WIDTH - BALL_SIZE) / 2, (SCENE_HEIGHT - BALL_SIZE) / 2, BALL_SIZE, BALL_SIZE, 0, 0);
        UpdateBallVelocity();

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

        // initialize sounds
        InitSounds();

        // create the Menu box
        menuVBox = CreateMenuBox();
        root.getChildren().add(menuVBox);

        lastTime = System.nanoTime();

        // show all the stuff on stage
        primaryStage.show();

        /*** EVENTS AND TIMERS ***/

        // set up event handlers for keypress
        scene.setOnKeyPressed(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent e) {
                        if (currentGameState == GameState.ONE_PLAYER || currentGameState == GameState.TWO_PLAYER) {
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
            public void handle(long currentTime) {

                // do the time math
                double elapsedTime = (currentTime - lastTime) / 1000000000.0;
                lastTime = currentTime;

                if (currentGameState == GameState.RESULT) {
                    if (accumulatedTime < TIME_TO_SHOW_BANNER) {
                        accumulatedTime += elapsedTime;
                        PlayGame(true);
                        return;
                    }
                    else {
                        accumulatedTime = 0;
                        if (root.getChildren().contains(winnerBox)) {
                            root.getChildren().remove(winnerBox);
                            winnerBox = null;
                        }
                        rightScore = 0;
                        leftScore = 0;
                        currentGameState = GameState.MENU;
                    }
                }

                if (leftScore >= WINNING_SCORE) {
                    ShowWinnerBox(GameEnded(Side.LEFT), root);

                }
                else if (rightScore >= WINNING_SCORE) {
                    ShowWinnerBox(GameEnded(Side.RIGHT), root);
                }

                switch (currentGameState) {
                    case MENU:
                        ShowMenu();
                        break;
                    case ONE_PLAYER:
                    case TWO_PLAYER:
                        PlayGame(false);
                        break;
                    case RESULT:
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

    private VBox CreateMenuBox() {

        double width = SCENE_WIDTH - (26 * BASE_UNIT);
        double height = SCENE_HEIGHT - (16 * BASE_UNIT);

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
        computerPlayButton.setMouseTransparent(true);
        computerPlayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                InitGame(GameState.ONE_PLAYER);
                StartBallFromRightPaddle();
            }
        });

        Button humanPlayButton = new Button("2 players");
        humanPlayButton.setMinWidth(width - (2 * BASE_UNIT));
        humanPlayButton.setFont(font);
        humanPlayButton.setMouseTransparent(true);
        humanPlayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                InitGame(GameState.TWO_PLAYER);
                if (random.nextBoolean()) {
                    StartBallFromRightPaddle();
                }
                else {
                    StartBallFromRightPaddle();
                }
            }
        });

        vBox.getChildren().addAll(computerPlayButton, humanPlayButton);

        return vBox;
    }

    private void InitGame(GameState gameState) {
        menuVBox.setVisible(false);

        leftScoreLabel.setVisible(true);
        rightScoreLabel.setVisible(true);

        inputKeys.clear();
        CenterPaddles();

        UpdateBallVelocity();

        switch (gameState) {
            case ONE_PLAYER:
                UpdateLeftPaddleVelocity(PC_PADDLE_VELOCITY);
                currentGameState = GameState.ONE_PLAYER;
                break;
            case TWO_PLAYER:
                UpdateLeftPaddleVelocity(rightPaddleVelocity);
                currentGameState = GameState.TWO_PLAYER;
                break;
        }
    }

    private void UpdateBallVelocity() {
        minBallVeloX = BALL_SIZE / 2 * 0.85;
        maxBallVeloX = BALL_SIZE / 2 * 1.1;

        minBallVeloY = BALL_SIZE / 2 * 0.8;
        maxBallVeloY = BALL_SIZE / 2 * 1.05;

        ball.setVelX(random.nextInt((int) (maxBallVeloX - minBallVeloX)) + minBallVeloX);
        ball.setVelY(random.nextInt((int) (maxBallVeloY - minBallVeloY)) + minBallVeloY);

        if (DEBUG) {
            System.out.println("Velocity X: " + ball.getVelX() + "\nVelocity Y: " + ball.getVelY());
        }
    }

    private void ShowMenu() {
        menuVBox.setVisible(true);

        leftScoreLabel.setVisible(false);
        rightScoreLabel.setVisible(false);

        PlayGame(true);

        rightScore = 0;
        leftScore = 0;
    }

    private void PlayGame(Boolean isInDemoMode) {

        // update the ball position
        ball.updateSpritePosition();

        // update the position for right paddle if applicable
        if (!isInDemoMode) {
            if (inputKeys.contains("UP")) {
                rightPaddle.setVelY(-(Math.abs(rightPaddle.getVelY())));
                rightPaddle.updateSpritePosition();
            }
        }

        if (!isInDemoMode) {
            if (inputKeys.contains("DOWN")) {
                rightPaddle.setVelY(Math.abs(rightPaddle.getVelY()));
                rightPaddle.updateSpritePosition();
            }
        }

        if (!isInDemoMode && currentGameState == GameState.TWO_PLAYER) {
            if (inputKeys.contains("W")) {
                leftPaddle.setVelY(-(Math.abs(leftPaddle.getVelY())));
                leftPaddle.updateSpritePosition();
            }
        }

        if (!isInDemoMode && currentGameState == GameState.TWO_PLAYER) {
            if (inputKeys.contains("S")) {
                leftPaddle.setVelY(Math.abs(leftPaddle.getVelY()));
                leftPaddle.updateSpritePosition();
            }
        }


        // move the left paddle in demo mode or in 2 player game
        if (isInDemoMode || currentGameState == GameState.ONE_PLAYER) {
            double leftPaddleDiff = (leftPaddle.getY() + (PADDLE_HEIGHT / 2)) - (ball.getY() + (BALL_SIZE / 2));
            if (leftPaddleDiff > 0 && leftPaddleDiff > leftPaddleVelocity) {
                leftPaddle.setVelY(-(Math.abs(leftPaddle.getVelY())));
                leftPaddle.updateSpritePosition();
            }
            else if (leftPaddleDiff < 0 && leftPaddleDiff < -leftPaddleVelocity) {
                leftPaddle.setVelY(Math.abs(leftPaddle.getVelY()));
                leftPaddle.updateSpritePosition();
            }
        }

        // move the right paddle in demo mode
        if (isInDemoMode) {
            double rightPaddleDiff = (rightPaddle.getY() + (PADDLE_HEIGHT / 2)) - (ball.getY() + (BALL_SIZE / 2));
            if (rightPaddleDiff > 0 && rightPaddleDiff > rightPaddleVelocity) {
                rightPaddle.setVelY(-(Math.abs(rightPaddle.getVelY())));
                rightPaddle.updateSpritePosition();
            }
            else if (rightPaddleDiff < 0 && rightPaddleDiff < -rightPaddleVelocity) {
                rightPaddle.setVelY(Math.abs(rightPaddle.getVelY()));
                rightPaddle.updateSpritePosition();
            }
        }

        // check the collision between ball and left paddle
        if (ball.intersects(leftPaddle)) {
            ball.setX(MARGIN_LEFT);
            ball.setVelX(-ball.getVelX());
            if (currentGameState == GameState.TWO_PLAYER) {
                // if the paddle was still moving opposite direction then reverse the Y angle to give the ball a backward spin
                if (ball.getVelY() > 0 && inputKeys.contains("W")) {
                    ball.setVelY(-ball.getVelY());
                }
                else if (ball.getVelY() < 0 && inputKeys.contains("S")) {
                    ball.setVelY(-ball.getVelY());
                }
            }
            if (!isInDemoMode) {
                plinkLeftSound.play();
            }
        }
        else if (ball.getX() < MARGIN_LEFT) {
            if (!isInDemoMode) {
                scoreSound.play();
            }
            rightScore++;
            StartBallFromRightPaddle();
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
            if (!isInDemoMode) {
                plinkRightSound.play();
            }
        }
        else if (ball.getX() > MARGIN_RIGHT - BALL_SIZE) {
            if (!isInDemoMode) {
                scoreSound.play();
            }
            leftScore++;
            StartBallFromLeftPaddle();
        }

        // check the top and bottom bounds against the ball movement
        if (ball.getY() > SCENE_HEIGHT - BALL_SIZE) {
            ball.setY(SCENE_HEIGHT - BALL_SIZE);
            ball.setVelY(-ball.getVelY());
            if (!isInDemoMode) {
                plonkTableSound.play();
            }
        }

        if (ball.getY() < 0) {
            ball.setY(0);
            ball.setVelY(-ball.getVelY());
            if (!isInDemoMode) {
                plonkTableSound.play();
            }
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

        if (!isInDemoMode) {
            rightScoreLabel.setText(Integer.toString(rightScore));
            leftScoreLabel.setText(Integer.toString(leftScore));
        }
    }

    private void StartBallFromLeftPaddle() {
        ball.setX(MARGIN_LEFT);
        ball.setY(leftPaddle.getY() + (PADDLE_HEIGHT / 2) - (BALL_SIZE / 2));
        ball.setVelX(Math.abs(ball.getVelX()));
        ball.setVelY(ball.getVelY() * randomReverse());
    }

    private void StartBallFromRightPaddle() {
        ball.setX(MARGIN_RIGHT - BALL_SIZE);
        ball.setY(rightPaddle.getY() + (PADDLE_HEIGHT / 2) - (BALL_SIZE / 2));
        ball.setVelX(-(Math.abs(ball.getVelX())));
        ball.setVelY(ball.getVelY() * randomReverse());
    }

    private void CenterPaddles() {
        leftPaddle.setY((SCENE_HEIGHT - PADDLE_HEIGHT) / 2);
        rightPaddle.setY((SCENE_HEIGHT - PADDLE_HEIGHT) / 2);
    }

    private void UpdateLeftPaddleVelocity(double vel) {
        leftPaddleVelocity = vel;
        leftPaddle.setVelY(vel);
    }

    private void ShowWinnerBox(String message, Group root) {
        Label label = new Label(message);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setTextFill(Constants.mainColor);

        Font font = LoadCustomFont(24);
        label.setFont(font);

        winnerBox = new VBox();
        winnerBox.setMinWidth(SCENE_WIDTH);
        winnerBox.setMinHeight(SCENE_HEIGHT);
        winnerBox.setAlignment(Pos.CENTER);

        VBox box = new VBox();
        box.setStyle("-fx-background-color: #000000; -fx-border-color: #FFFFFF; -fx-border-width: 4;");
        box.setPadding(new Insets(BASE_UNIT, BASE_UNIT, BASE_UNIT, BASE_UNIT));
        box.setAlignment(Pos.CENTER);

        box.getChildren().add(label);
        winnerBox.getChildren().add(box);
        root.getChildren().addAll(winnerBox);
    }

    private String GameEnded(Side side) {
        String message = "";

        if (side == Side.LEFT) {
            if (currentGameState == GameState.ONE_PLAYER) {
                message = PC_WINS;
            }
            else if (currentGameState == GameState.TWO_PLAYER) {
                message = LEFT_PLAYER_WINS;
            }
        }
        else if (side == Side.RIGHT) {
            if (currentGameState == GameState.ONE_PLAYER) {
                message = PLAYER_WINS;
            }
            else if (currentGameState == GameState.TWO_PLAYER) {
                message = RIGHT_PLAYER_WINS;
            }
        }

        winSound.play();

        UpdateLeftPaddleVelocity(PC_PADDLE_VELOCITY);
        currentGameState = GameState.RESULT;

        return message;
    }

    private void InitSounds() {
        try {
            plinkLeftSound = new javafx.scene.media.AudioClip(getClass().getResource(PLINK_LEFT_SOUND_FILE).toString());
            plinkRightSound = new javafx.scene.media.AudioClip(getClass().getResource(PLINK_RIGHT_SOUND_FILE).toString());
            plonkTableSound = new javafx.scene.media.AudioClip(getClass().getResource(PLONK_TABLE_SOUND_FILE).toString());
            scoreSound = new javafx.scene.media.AudioClip(getClass().getResource(SCORE_SOUND_FILE).toString());
            winSound = new javafx.scene.media.AudioClip(getClass().getResource(WIN_SOUND_FILE).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
