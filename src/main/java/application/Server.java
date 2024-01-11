package application;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;

public class Server {

    private static final String PLAYING_SPACE_NAME = "playing";
    private static final String PING_SPACE_NAME = "ping";
    private static final String SERVER_INFO_SPACE_NAME = "serverInfo";
    static ArrayList<String> clients = new ArrayList<>();
    private String ip;
    static ArrayList<String> activeServers = new ArrayList<>();

    public Server(String ip) throws InterruptedException {
    	this.ip = ip;
		if(!Server.activeServers.contains(this.ip)) {
			Server.activeServers.add(this.ip);
			startServer(this.ip);
		} else {
			joinServer(this.ip);
		}

    }


    private static SpaceRepository initializeSpaces() {
        SpaceRepository repository = new SpaceRepository();
        repository.add(PLAYING_SPACE_NAME, new SequentialSpace(2));
        repository.add(PING_SPACE_NAME, new SequentialSpace());
        repository.add(SERVER_INFO_SPACE_NAME, new SequentialSpace());
        return repository;
    }

    private void startServer(String ip) throws InterruptedException {
    	SpaceRepository repository = initializeSpaces();
        repository.addGate("tcp://" + ip + ":9001/?keep");
        System.out.println("Server is up...");

        UUID id = UUID.randomUUID();
        clients.add(id.toString());
        startGameThreads(repository, clients, id.toString());


    }
    
    private void joinServer(String ip) throws InterruptedException {
    	 
    	try {
    	    UUID id = UUID.randomUUID();
    	    clients.add(id.toString());
			RemoteSpace space = new RemoteSpace("tcp://" + ip + ":9001/"+ PLAYING_SPACE_NAME+"?keep");
			RemoteSpace space2 = new RemoteSpace("tcp://" + ip + ":9001/"+ SERVER_INFO_SPACE_NAME+"?keep");
			space2.put("new Client");
			SpaceRepository repository = new SpaceRepository();
			repository.add(PLAYING_SPACE_NAME,space);
			repository.add(SERVER_INFO_SPACE_NAME,space2);

			
			startGameThreads(repository, clients, id.toString());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
 
        
    
    }
    private void startGameThreads(SpaceRepository repository, ArrayList<String> clients, String id) throws InterruptedException {
    	Main.root = new BorderPane();
    	
		Game game = new Game(repository.get(PLAYING_SPACE_NAME), repository.get(SERVER_INFO_SPACE_NAME), new Canvas(1000,700), clients, id);
		Main.root.getChildren().add(game.canvas);
		Parent root =  Main.root;
		Main.scene.setRoot(root);
		Main.stage.show();
		 
        new Thread(game).start();
        

    }

    private void handleServerInfo(SpaceRepository repository, List<String> clients) throws InterruptedException {
        // Handle server information updates from clients
        handlePlayerConnection(repository, clients);
    }
    

    private void handlePlayerConnection(SpaceRepository repository, List<String> clients) throws InterruptedException {
        // Listen for player connection requests
        Object[] connectionRequest = repository.get(SERVER_INFO_SPACE_NAME).get(new ActualField("Connect"), new FormalField(String.class));

        if (connectionRequest != null) {
            String newClient = (String) connectionRequest[1];
            clients.add(newClient);

            // Notify other players about the new connection
            repository.get(SERVER_INFO_SPACE_NAME).put("NewPlayer", newClient);
        }
    }


}