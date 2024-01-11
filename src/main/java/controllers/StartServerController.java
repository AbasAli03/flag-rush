package controllers;

import application.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class StartServerController {
		
    @FXML
    private static TextField ipTextfield;
    
    public void startServer(ActionEvent event) {
    	String ip = ipTextfield.getText();
    	try {
			Server server = new Server(ip);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	
    }


}
