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
import org.jspace.SpaceRepository;
import org.jspace.StackSpace;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Server {

    static final String PLAYING_SPACE_NAME = "playing";
    static final String PING_SPACE_NAME = "ping";
    static final String SERVER_INFO_SPACE_NAME = "serverInfo";
    static final String CLIENTS_IN_SERVER = "clientsInServer";
    static final String GETTING_SPACE_NAME = "getting";
    static final String MAP = "map";

    static ArrayList<String> clients = new ArrayList<>();
    private String ip;
    static ArrayList<String> activeServers = new ArrayList<>();
    SpaceRepository repository;

    public Server(String ip) throws InterruptedException {
        this.ip = ip;

        /*
         * if (!Server.activeServers.contains(this.ip)) {
         * Server.activeServers.add(this.ip);
         * startServer(this.ip);
         * } else {
         * joinServer(this.ip);
         * }
         */

    }

    public SpaceRepository initializeSpaces() {
        SpaceRepository repository = new SpaceRepository();
        repository.add(PLAYING_SPACE_NAME, new StackSpace());
        repository.add(PING_SPACE_NAME, new SequentialSpace());
        repository.add(SERVER_INFO_SPACE_NAME, new SequentialSpace());
        repository.add(CLIENTS_IN_SERVER, new SequentialSpace());
        repository.add(GETTING_SPACE_NAME, new StackSpace());
        repository.add(MAP, new QueueSpace());

        return repository;
    }

    public void startServer(String ip) throws InterruptedException, UnknownHostException, IOException {
        repository = initializeSpaces();
        repository.addGate("tcp://" + ip + ":9001/?keep");
        repository.get(CLIENTS_IN_SERVER).put("new Client");
        UUID id = UUID.randomUUID();

        List<Object[]> clientObjectsUpdated = repository.get(CLIENTS_IN_SERVER).queryAll(new ActualField("new Client"));
        int clientsJoined = clientObjectsUpdated.size();

        startGameThreads(ip, clientsJoined, id.toString());

    }

    public void joinServer(String ip) throws InterruptedException, UnknownHostException, IOException {

        UUID id = UUID.randomUUID();

        handlePlayerConnection(ip);

        List<Object[]> clientObjectsUpdated = new RemoteSpace("tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep")
                .queryAll(new ActualField("new Client"));
        int clientsJoined = clientObjectsUpdated.size();

        startGameThreads(ip, clientsJoined, id.toString());

    }

    private void startGameThreads(String ip, int clientsJoined, String id)
            throws InterruptedException, UnknownHostException, IOException {

        Canvas canvas = new Canvas(1000, 700);
        Game game = new Game(ip, canvas, clientsJoined, id);

        // Set the root to the new BorderPane
        Main.root = (new BorderPane(canvas));
        Parent root = Main.root;

        // The rest of your code remains unchanged
        Main.scene.setRoot(root);
        Main.stage.show();

        new Thread(game).start();
    }

    private void handlePlayerConnection(String ip) throws InterruptedException, UnknownHostException, IOException {

        RemoteSpace space = new RemoteSpace("tcp://" + ip + ":9001/" + CLIENTS_IN_SERVER + "?keep");
        space.put("new Client");

    }

}