package application;

import org.jspace.Space;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    public static Canvas canvas = new Canvas(1000,700);
    public static BorderPane root;
    public static Scene scene;
    public static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        try {
        	stage = primaryStage;
        	root = new BorderPane();
            Game.ctx.setFill(Color.BLACK);
            
            Game.ctx.fillRect(0, 0, Main.canvas.getWidth(), Main.canvas.getHeight());
        	root.getChildren().add(canvas);
        	
            Scene scene = new Scene(root, 1000, 700);
        	stage.setScene(scene);
        	stage.show();
            canvas.setFocusTraversable(true);
            Game game = new Game();
            
            game.startGame();
            
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}