package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.MatchMakingServer;
import server.Server;

public class Main extends Application {
    public static Parent root;
    public static Scene scene;
    public static Stage stage;
    public static Server server;
    public static MatchMakingServer matchMakingServer;

    @Override
    public void start(Stage primaryStage) {
        try {
            matchMakingServer = new MatchMakingServer();
            server = new Server();
            stage = primaryStage;

            root = FXMLLoader.load(getClass().getResource("Home.fxml"));
            scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setRoot(Parent newRoot) {
        root = newRoot;
    }

    public static void setScene(Scene newScene) {
        scene = newScene;
        stage.setScene(scene);
    }

    public static void setStage(Stage newStage) {
        stage = newStage;
    }
}

