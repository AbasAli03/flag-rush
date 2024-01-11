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

    static final String PLAYING_SPACE_NAME = "playing";
     static final String PING_SPACE_NAME = "ping";
     static final String SERVER_INFO_SPACE_NAME = "serverInfo";
     static final String CLIENTS_IN_SERVER = "clientsInServer";

    static ArrayList<String> clients = new ArrayList<>();
    private String ip;
    static ArrayList<String> activeServers = new ArrayList<>();
     SpaceRepository repository;

    public Server(String ip) throws InterruptedException {
        this.ip = ip;

        /* 
        if (!Server.activeServers.contains(this.ip)) {
            Server.activeServers.add(this.ip);
            startServer(this.ip);
        } else {
            joinServer(this.ip);
        }
        */

    }

    public  SpaceRepository initializeSpaces() {
        SpaceRepository repository = new SpaceRepository();
        repository.add(PLAYING_SPACE_NAME, new SequentialSpace());
        repository.add(PING_SPACE_NAME, new SequentialSpace());
        repository.add(SERVER_INFO_SPACE_NAME, new SequentialSpace());
        repository.add(CLIENTS_IN_SERVER, new SequentialSpace());

        return repository;
    }

    public void startServer(String ip) throws InterruptedException, UnknownHostException, IOException {
        repository = initializeSpaces();
        repository.addGate("tcp://" + ip + ":9001/?keep");
        repository.get(CLIENTS_IN_SERVER).put("new Client");
        UUID id = UUID.randomUUID();
        clients.add(id.toString());
        System.out.println("starting Server");
    Object[] clientsObjects = repository.get(CLIENTS_IN_SERVER).query( new ActualField("new Client"));
        int clientsJoined = clientsObjects.length;
        System.out.println(clientsJoined);
        startGameThreads(ip, clientsJoined, id.toString());

    }

    public void joinServer(String ip) throws InterruptedException {

        try {
            System.out.println("joining Server");

            UUID id = UUID.randomUUID();
            clients.add(id.toString());
            RemoteSpace space = new RemoteSpace("tcp://" + ip + ":9001/" + PLAYING_SPACE_NAME + "?keep");
            RemoteSpace space2 = new RemoteSpace("tcp://" + ip + ":9001/" + SERVER_INFO_SPACE_NAME + "?keep");
            RemoteSpace space3 = new RemoteSpace("tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep");
            space3.put("new Client");
           
           
            SpaceRepository repository = new SpaceRepository();

            repository.add(PLAYING_SPACE_NAME, space);
            repository.add(SERVER_INFO_SPACE_NAME, space2);
            repository.add(CLIENTS_IN_SERVER, space3);
        
              Object[] clientsObjects = repository.get(CLIENTS_IN_SERVER).query( new ActualField("new Client"));
        int clientsJoined = clientsObjects.length;
        System.out.println(clientsJoined);

            startGameThreads(ip, clientsJoined, id.toString());

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void startGameThreads(String ip, int clientsJoined, String id)
            throws InterruptedException, UnknownHostException, IOException {


        Main.root = new BorderPane();

        System.out.println(clientsJoined);

        
        Game game = new Game(ip, new Canvas(1000, 700), clientsJoined, id);
        Main.root.getChildren().add(game.canvas);
        Parent root = Main.root;
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
        Object[] connectionRequest = repository.get(SERVER_INFO_SPACE_NAME).get(new ActualField("Connect"),
                new FormalField(String.class));

        if (connectionRequest != null) {
            String newClient = (String) connectionRequest[1];
            clients.add(newClient);

            // Notify other players about the new connection
            repository.get(SERVER_INFO_SPACE_NAME).put("NewPlayer", newClient);
        }
    }

}