package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
    public static Pane root;
    public static Scene scene;
    public static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        try {
        	stage = primaryStage;
        	root = new BorderPane();
            
          	stage = primaryStage;
        	root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        	scene = new Scene(root);
        	stage.setScene(scene);
        	stage.show();

     

            
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}