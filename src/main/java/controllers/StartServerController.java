package controllers;

import application.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class StartServerController {
		
    @FXML
    private static TextField ipTextfield;
    
    public void startServer(ActionEvent event) {
    	String ip = ipTextfield.getText().toString();
    	System.out.println(ip);
    	try {
			Server server = new Server(ip);
			new Thread(server).start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	
    }


}
