package application;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.QueueSpace;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import org.jspace.StackSpace;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.utils;

public class Server {
    static final String PLAYING_SPACE_NAME = "playing";
    static final String PING_SPACE_NAME = "ping";
    static final String SERVER_INFO_SPACE_NAME = "serverInfo";
    public static final String CLIENTS_IN_SERVER = "clientsInServer";
    static final String GETTING_SPACE_NAME = "getting";
    static final String ACTION_SPACE = "action";
    static final String ACTIVE_SERVERS = "activeServer";
    static final String GAME_INFO_SPACE = "infoSpace";
    static final String FLAG_SPACE = "flagSpace";
    // static final String ip = "10.209.205.74";
    // static final String uri = "tcp://" + ip + ":9001/?keep";

    static ArrayList<String> clients = new ArrayList<>();
    // private String ip;
    static ArrayList<String> activeServers = new ArrayList<>();
    public SpaceRepository repository;
    private SpaceRepository repositoryOfServers;

    public Server() {
        repository = new SpaceRepository();
        // repositoryOfServers = new SpaceRepository();
        repository.add(PLAYING_SPACE_NAME, new QueueSpace());
        repository.add(CLIENTS_IN_SERVER, new SequentialSpace());
        repository.add(GETTING_SPACE_NAME, new QueueSpace());
        repository.add(ACTION_SPACE, new QueueSpace());
        repository.add(GAME_INFO_SPACE, new SequentialSpace());
        repository.add(FLAG_SPACE, new StackSpace());

        // repositoryOfServers.add(ACTIVE_SERVERS, new SequentialSpace());
        // repositoryOfServers.addGate(uri);

    }

    public SpaceRepository initializeSpaces() {

        return repository;
    }

    public void shutdownServer(SpaceRepository repository, String ip) throws UnknownHostException, IOException {
        // Space activeServersSpace = new RemoteSpace("tcp://" + Server.ip + ":9001/" +
        // ACTIVE_SERVERS + "?keep");
        // try {
        // activeServersSpace.getAll(new ActualField("new Client"));
        // activeServersSpace.get(new ActualField(ip));
        // } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        repository.closeGates();
    }

    public void startServer(String ip) throws InterruptedException, UnknownHostException, IOException {

        // check wether the server already exists
        // Space activeServersSpace = new RemoteSpace("tcp://" + Server.ip + ":9001/" +
        // ACTIVE_SERVERS + "?keep");

        // List<Object[]> currentlyActiveServers = activeServersSpace.queryAll(new
        // ActualField(ip));
        // for (Object[] serverInfo : currentlyActiveServers) {
        // String activeIp = (String) serverInfo[0];
        // if (ip.equals(activeIp)) {
        // int clientsInServer = activeServersSpace.queryAll(new ActualField("new
        // Client"))
        // .size();
        // utils.displayMessage("this Server is active with: " + clientsInServer + "
        // Clients");
        // return;
        // }
        // }
        // create server
        repository.addGate("tcp://" + ip + ":9001/?keep");
        handlePlayerConnection(ip);

        // add the ip to active servers
        // activeServersSpace.put(ip);
        // activeServersSpace.put("new Client");

        UUID id = UUID.randomUUID();

        List<Object[]> clientObjectsUpdated = repository.get(CLIENTS_IN_SERVER).queryAll(new ActualField("new Client"));
        int clientsJoined = clientObjectsUpdated.size();
        startGameThreads(ip, clientsJoined, id.toString(), repository);

    }

    public void joinServer(String ip) throws InterruptedException, UnknownHostException, IOException {
        // Space activeServersSpace = new RemoteSpace("tcp://" + Server.ip + ":9001/" +
        // ACTIVE_SERVERS + "?keep");

        // List<Object[]> currentlyActiveServers = activeServersSpace.queryAll(new
        // ActualField(ip));
        // boolean foundServer = false;
        // for (Object[] serverInfo : currentlyActiveServers) {
        // String activeIp = (String) serverInfo[0];

        // if (ip.equals(activeIp)) {
        // foundServer = true;
        // Query the number of clients in the server
        int clientsInServer = repository.get(CLIENTS_IN_SERVER).queryAll(new ActualField("new Client")).size();

        // if (clientsInServer < 2) {
        // foundServer = false;
        // Add the player as a client to the server
        handlePlayerConnection(ip);

        // Add the players as a new Client in the active servers space
        // activeServersSpace.put("new Client");

        UUID id = UUID.randomUUID();

        List<Object[]> clientObjectsUpdated = new RemoteSpace(
                "tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep")
                .queryAll(new ActualField("new Client"));

        int clientsJoined = clientObjectsUpdated.size();
        // Start game threads
        startGameThreads(ip, clientsJoined, id.toString(), repository);
        // } else {
        // utils.displayMessage("This Server is already active with 2 clients. Try
        // another server.");
        // return;
        // }
        // }

        // }if(foundServer){utils.displayMessage("The provided server IP is not active.
        // Try another server or create your own.");}

    }

    private void startGameThreads(String ip, int clientsJoined, String id, SpaceRepository repository)
            throws InterruptedException, UnknownHostException, IOException {

        Canvas canvas = new Canvas(1000, 700);
        Game game = new Game(ip, canvas, clientsJoined, id, repository);

        // Set the root to the new BorderPane
        Main.root = (new BorderPane(canvas));
        Parent root = Main.root;

        Main.scene.setRoot(root);
        Main.stage.show();

        new Thread(game).start();
    }

    private void handlePlayerConnection(String ip) throws InterruptedException, UnknownHostException, IOException {

        RemoteSpace space = new RemoteSpace("tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep");
        space.put("new Client");

    }

}