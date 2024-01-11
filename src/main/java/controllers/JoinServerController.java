package controllers;

import application.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class JoinServerController {

    @FXML
    private TextField ipTextfield;

    @FXML
    void joinServer(ActionEvent event) {
    	String ip = ipTextfield.getText().toString();
    	System.out.println(ip);
    	try {
            Server server = new Server(ip);
            server.joinServer(ip);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
    }
}
