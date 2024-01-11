package controllers;

import java.io.IOException;
import java.net.UnknownHostException;

import application.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class StartServerController {
		
    @FXML
    private TextField ipTextfield;
    
    public void startServer(ActionEvent event) {
    	String ip = ipTextfield.getText().toString();
    	System.out.println(ip);
    	try {
			Server server = new Server(ip);
			server.startServer(ip);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }    	
    	
    }


}
