package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

import javafx.scene.canvas.Canvas;

public class Server {

    private static final String PLAYING_SPACE_NAME = "playing";
    private static final String PING_SPACE_NAME = "ping";
    private static final String SERVER_INFO_SPACE_NAME = "serverInfo";

    public static void main(String[] args) {
        try {
            SpaceRepository repository = initializeSpaces();
            startServer(repository);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static SpaceRepository initializeSpaces() {
        SpaceRepository repository = new SpaceRepository();
        repository.add(PLAYING_SPACE_NAME, new SequentialSpace(2));
        repository.add(PING_SPACE_NAME, new SequentialSpace());
        repository.add(SERVER_INFO_SPACE_NAME, new SequentialSpace());
        return repository;
    }

    private static void startServer(SpaceRepository repository) throws InterruptedException {
        Scanner input = new Scanner(System.in);
        System.out.print("Please enter the room's IP address (or localhost to play locally): ");
        String ip = input.nextLine();
        repository.addGate("tcp://" + ip + ":9001/?keep");
        System.out.println("Server is up...");

        List<String> clients = new ArrayList<>();

        startGameThreads(repository, clients);

        while (true) {
            handleServerInfo(repository, clients);
        }
    }

    private static void startGameThreads(SpaceRepository repository, List<String> clients) throws InterruptedException {
        Game game = new Game(repository.get(PLAYING_SPACE_NAME), repository.get(SERVER_INFO_SPACE_NAME), new Canvas(1000,700));
        new Thread(game).start();
        
        Mover mover = new Mover(repository.get(SERVER_INFO_SPACE_NAME), game);
        new Thread(mover).start();
    }

    private static void handleServerInfo(SpaceRepository repository, List<String> clients) throws InterruptedException {
        // Handle server information updates from clients
        handlePlayerConnection(repository, clients);
    }

    private static void handlePlayerConnection(SpaceRepository repository, List<String> clients) throws InterruptedException {
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