package controllers;

import java.io.IOException;
import java.net.UnknownHostException;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import server.Server;
import utils.utils;

public class StartServerController {

    @FXML
    private TextField ipTextfield;

    public void startServer(ActionEvent event) {
        String ip = ipTextfield.getText().toString();

        if (utils.isValidIP(ip)) {
            try {
                Main.server.startServer(ip, false);
            } catch (UnknownHostException e) {
            } catch (InterruptedException e) {
            } catch (IOException e) {
            }
        } else {
            utils.displayMessage("invalid ip, please try again!");
        }

    }

}
