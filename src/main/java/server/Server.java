package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspace.ActualField;
import org.jspace.QueueSpace;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;
import org.jspace.StackSpace;

import application.Game;
import application.Main;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
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
    static ArrayList<String> clients = new ArrayList<>();
    static ArrayList<String> activeServers = new ArrayList<>();
    public SpaceRepository repository;

    public Server() {


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
 
        repository.closeGates();
    }

    public void startServer(String ip, boolean joiningRandom)
            throws InterruptedException, UnknownHostException, IOException {
        initializeSpaces();
        if (joiningRandom) {
            RemoteSpace allServers = new RemoteSpace(MatchMakingServer.ALL_SERVERS_URI);

            List<Object[]> currentlyActiveServers = allServers.queryAll(new ActualField(ip),
                    new ActualField("new Client"));
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
                utils.displayMessage(
                        "This Server is active with: " + occurrences + " " + (occurrences == 1 ? "Client" : "Clients"));

            } else {

                // create server
                repository.addGate("tcp://" + ip + ":9001/?keep");
                handlePlayerConnection(ip, joiningRandom);

                UUID id = UUID.randomUUID();

                List<Object[]> clientObjectsUpdated = repository.get(CLIENTS_IN_SERVER)
                        .queryAll(new ActualField("new Client"));
                int clientsJoined = clientObjectsUpdated.size();
                startGameThreads(ip, clientsJoined, id.toString(), repository);
            }
        } else {
            // create server
            repository.addGate("tcp://" + ip + ":9001/?keep");
            handlePlayerConnection(ip, joiningRandom);

            UUID id = UUID.randomUUID();

            List<Object[]> clientObjectsUpdated = repository.get(CLIENTS_IN_SERVER)
                    .queryAll(new ActualField("new Client"));
            int clientsJoined = clientObjectsUpdated.size();
            startGameThreads(ip, clientsJoined, id.toString(), repository);
        }

    }

    public void joinServer(String ip, boolean joiningRandom)
            throws InterruptedException, UnknownHostException, IOException {
        if (joiningRandom) {
            int occurrences = 0;
            boolean serverExists = false;

            RemoteSpace allServers = new RemoteSpace(MatchMakingServer.ALL_SERVERS_URI);

            List<Object[]> currentlyActiveServers = allServers.queryAll(new ActualField(ip),
                    new ActualField("new Client"));
            if (currentlyActiveServers.size() == 1) {
                occurrences = 1;
                serverExists = true;
            } else if (currentlyActiveServers.size() == 2) {
                serverExists = true;
                occurrences = 2;
            }


            if (serverExists && occurrences == 1) {

                handlePlayerConnection(ip, joiningRandom);

                UUID id = UUID.randomUUID();

                List<Object[]> clientObjectsUpdated = new RemoteSpace(
                        "tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep")
                        .queryAll(new ActualField("new Client"));

                int clientsJoined = clientObjectsUpdated.size();
                // Start game threads
                startGameThreads(ip, clientsJoined, id.toString(), repository);

            } else if (!serverExists) {

                utils.displayMessage(
                        "This server doesn't exist");
            } else if (serverExists && occurrences == 2) {
                utils.displayMessage(
                        "This Server is active with: " + occurrences + " " + (occurrences == 1 ? "Client" : "Clients"));
            }
        } else {
            handlePlayerConnection(ip, joiningRandom);

            UUID id = UUID.randomUUID();

            List<Object[]> clientObjectsUpdated = new RemoteSpace(
                    "tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep")
                    .queryAll(new ActualField("new Client"));

            int clientsJoined = clientObjectsUpdated.size();
            // Start game threads
            if (clientsJoined > 2) {
                utils.displayMessage("Server is active with 2 clients");
            } else {
                startGameThreads(ip, clientsJoined, id.toString(), repository);

            }
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

    private void handlePlayerConnection(String ip, boolean joiningRandom)
            throws InterruptedException, UnknownHostException, IOException {

        RemoteSpace space = new RemoteSpace("tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep");

        if (joiningRandom) {
            RemoteSpace allServers = new RemoteSpace(MatchMakingServer.ALL_SERVERS_URI);
            allServers.put(ip, "new Client");


            space.put("new Client");
        } else {
            if (space.queryAll(new ActualField("new Client")).size() > 2) {
                utils.displayMessage("Server is active with 2 clients");
            } else {
                space.put("new Client");

            }
            
        }

    }

}