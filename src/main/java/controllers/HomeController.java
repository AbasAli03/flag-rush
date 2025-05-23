package controllers;

import java.io.IOException;
import application.Main;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import server.ClientRun;
import server.MatchMakingServer;

public class HomeController {

	public void startServer(ActionEvent event) {
		try {
			Main.root = FXMLLoader.load(getClass().getResource("/application/StartServer.fxml"));
			Main.stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			Parent root = Main.root;
			Main.scene.setRoot(root);
			Main.stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void joinServer(ActionEvent event) {
		try {
			Main.root = FXMLLoader.load(getClass().getResource("/application/JoinServer.fxml"));
			Main.stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			Parent root = Main.root;
			Main.scene.setRoot(root);
			Main.stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void joinRandomGame(ActionEvent event) {
		Platform.runLater(() -> {
			new Thread(new ClientRun()).start();
			new Thread(new MatchMakingServer()).start();

		});

	}
}
