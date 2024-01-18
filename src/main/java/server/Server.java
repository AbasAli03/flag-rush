package server;

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

import application.Game;
import application.Main;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.utils;

public class Server {
    public static final String PLAYING_SPACE_NAME = "playing";
    static final String PING_SPACE_NAME = "ping";
    static final String SERVER_INFO_SPACE_NAME = "serverInfo";
    public static final String CLIENTS_IN_SERVER = "clientsInServer";
    public static final String GETTING_SPACE_NAME = "getting";
    static final String ACTION_SPACE = "action";
    static final String ACTIVE_SERVERS = "activeServer";
    public static final String GAME_INFO_SPACE = "infoSpace";
    public static final String FLAG_SPACE = "flagSpace";
    // static final String ip = "";
    // static final String uri = "tcp://" + ip + ":9001/?keep";

    static ArrayList<String> clients = new ArrayList<>();
    // private String ip;
    static ArrayList<String> activeServers = new ArrayList<>();
    public SpaceRepository repository;
    private SpaceRepository repositoryOfServers;

    public Server() {

        // repositoryOfServers.add(ACTIVE_SERVERS, new SequentialSpace());
        // repositoryOfServers.addGate(uri);

    }

    public SpaceRepository initializeSpaces() {
        repository = new SpaceRepository();
        // repositoryOfServers = new SpaceRepository();
        repository.add(PLAYING_SPACE_NAME, new QueueSpace());
        repository.add(CLIENTS_IN_SERVER, new SequentialSpace());
        repository.add(GETTING_SPACE_NAME, new QueueSpace());
        repository.add(ACTION_SPACE, new QueueSpace());
        repository.add(GAME_INFO_SPACE, new SequentialSpace());
        repository.add(FLAG_SPACE, new StackSpace());
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
        repository = initializeSpaces();
        RemoteSpace allServers = new RemoteSpace(MatchMakingServer.ALL_SERVERS_URI);

        List<Object[]> currentlyActiveServers = allServers.queryAll(new ActualField(ip), new ActualField("new Client"));
        int occurrences = 0;
        boolean serverExists = false;
        for (Object[] serverInfo : currentlyActiveServers) {
            String activeIp = (String) serverInfo[0];

            // Check if the ip matches the activeIp
            if (ip.equals(activeIp)) {
                serverExists = true;
                occurrences++;
            }
        }
        if (serverExists) {
            System.out.println("occurrences: " + occurrences);
            utils.displayMessage(
                    "This Server is active with: " + occurrences + " " + (occurrences == 1 ? "Client" : "Clients"));

        } else {
            System.out.println("occurrences: " + occurrences);

            // create server
            repository.addGate("tcp://" + ip + ":9001/?keep");
            handlePlayerConnection(ip);

            UUID id = UUID.randomUUID();

            List<Object[]> clientObjectsUpdated = repository.get(CLIENTS_IN_SERVER)
                    .queryAll(new ActualField("new Client"));
            int clientsJoined = clientObjectsUpdated.size();
            startGameThreads(ip, clientsJoined, id.toString(), repository);
            System.out.println("ocureenses in starrtserver: " + occurrences);
        }

    }

    public void joinServer(String ip) throws InterruptedException, UnknownHostException, IOException {
        RemoteSpace allServers = new RemoteSpace(MatchMakingServer.ALL_SERVERS_URI);

        List<Object[]> currentlyActiveServers = allServers.queryAll(new ActualField(ip), new ActualField("new Client"));
        System.out.println("query all in join");
        int counter = 0;
        int occurrences = 0;
        boolean serverExists = false;
        for (Object[] serverInfo : currentlyActiveServers) {
            String activeIp = (String) serverInfo[0];

            // Check if the ip matches the activeIp
            if (ip.equals(activeIp)) {
                serverExists = true;
                occurrences++;
            }
        }
        System.out.println("occurrences after check in join: " + occurrences);

        if (serverExists && occurrences == 1) {
            handlePlayerConnection(ip);

            UUID id = UUID.randomUUID();

            List<Object[]> clientObjectsUpdated = new RemoteSpace(
                    "tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep")
                    .queryAll(new ActualField("new Client"));

            int clientsJoined = clientObjectsUpdated.size();
            System.out.println("clients joined " + clientsJoined);
            // Start game threads
            startGameThreads(ip, clientsJoined, id.toString(), repository);

        } else if (!serverExists) {

            utils.displayMessage(
                    "This server doesn't exist");
        } else if (serverExists && occurrences == 2) {
            utils.displayMessage(
                    "This Server is active with: " + occurrences + " " + (occurrences == 1 ? "Client" : "Clients"));
        }

    }

    private void startGameThreads(String ip, int clientsJoined, String id, SpaceRepository repository)
            throws InterruptedException, UnknownHostException, IOException {

        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(1000, 700);
                Game game = new Game(ip, canvas, clientsJoined, id, repository);

                // Set the root to the new BorderPane
                Main.setRoot(new BorderPane(canvas));
                Parent root = Main.root;

                // Check if Main.scene is not null before updating its root
                if (Main.scene != null) {
                    Main.scene.setRoot(root);
                } else {
                    // If Main.scene is null, you might want to create a new Scene
                    Main.setScene(new Scene(root, 1000, 700));
                }

                // Update the existing stage
                Main.stage.show();
                // Main.stage.sizeToScene();

                // Start the game thread
                new Thread(game).start();

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handlePlayerConnection(String ip) throws InterruptedException, UnknownHostException, IOException {

        RemoteSpace space = new RemoteSpace("tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep");
        RemoteSpace activeServers = new RemoteSpace(MatchMakingServer.ACTIVE_SERVERS_URI);
        RemoteSpace allServers = new RemoteSpace(MatchMakingServer.ALL_SERVERS_URI);
        List<Object[]> currentlyActiveServers = allServers.queryAll(new ActualField(ip), new ActualField("new Client"));

        int occurrences = 0;
        boolean serverExists = false;
        for (Object[] serverInfo : currentlyActiveServers) {
            String activeIp = (String) serverInfo[0];
            // Check if the ip matches the activeIp
            if (ip.equals(activeIp)) {
                serverExists = true;
                occurrences++;
            }
        }
        if (!serverExists) {
            System.out.println("new put in all servers");
            allServers.put(ip, "new Client");
            System.out.println(" handleplayer connection "
                    + allServers.queryAll(new ActualField(ip), new ActualField("new Client")).size());

            space.put("new Client");

        } else if (occurrences == 1) {
            space.put("new Client");

        }

    }

}