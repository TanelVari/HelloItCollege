import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Random;

public class HelloWorld extends Application {

	private static final int SCENE_WIDTH = 640;
	private static final int SCENE_HEIGHT = 480;

	private static final int MIN_AMOUNT = 20;

	@Override
	public void start(Stage primaryStage){

		Group root = new Group();
		Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.rgb(255, 255, 255));

		Random random = new Random(System.currentTimeMillis());

		for (int i = 0; i < (random.nextInt(20) + MIN_AMOUNT); i++){
			Rectangle rectangle = new Rectangle();

			int width = random.nextInt(SCENE_WIDTH / 2) + 10;
			int height = random.nextInt(SCENE_HEIGHT / 2) + 10;
			double opacity = random.nextInt(95) * 0.01;

			rectangle.setWidth(width);
			rectangle.setHeight(height);
			rectangle.setX(random.nextInt(SCENE_WIDTH - width));
			rectangle.setY(random.nextInt(SCENE_HEIGHT - height));
			rectangle.setRotate(random.nextInt(90));
			rectangle.setFill(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255), opacity));

			root.getChildren().add(rectangle);
		}

		primaryStage.setTitle("Interlude 2 - Graphics in JavaFX");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
