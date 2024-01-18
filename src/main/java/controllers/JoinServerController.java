package controllers;

import java.io.IOException;
import java.net.UnknownHostException;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import utils.utils;

public class JoinServerController {

  @FXML
  private TextField ipTextfield;

  @FXML
  void joinServer(ActionEvent event) throws UnknownHostException, IOException {
    String ip = ipTextfield.getText().toString();

    try {
      if (utils.isValidIP(ip)) {
        Main.server.joinServer(ip,false);
      } else {
        utils.displayMessage("invalid ip, please try again!");
      }
    } catch (InterruptedException e) {
      utils.displayMessage("invalid ip, please try again!");
    }
  }
}
