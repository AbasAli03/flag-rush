package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

import javafx.application.Platform;

public class MatchMakingServer extends Server implements Runnable {
    private static String ip = "192.168.50.84";
    private static String URI = "tcp://" + ip + ":9001/?keep";
    public static String ACTIVE_SERVERS = "activeServer";
    public static String ACTIVE_SERVERS_URI = "tcp://" + ip + ":9001/" + ACTIVE_SERVERS + "?keep";

    public static SpaceRepository repository = new SpaceRepository();

    public static void main(String[] args) {
        repository.add(ACTIVE_SERVERS, new SequentialSpace());
        repository.addGate(URI);

        // Start the server in a separate thread

        if (!Platform.isFxApplicationThread()) {
            Platform.startup(() -> {
                // Start the server in a separate thread
                Thread serverThread = new Thread(new MatchMakingServer());
                serverThread.start();
            });
        } else {
            // Start the server in the current thread
            Thread serverThread = new Thread(new MatchMakingServer());
            serverThread.start();
        }
    }

    public MatchMakingServer() {
        super();
    }

    @Override
    public void run() {
        try {
            RemoteSpace activeServers = new RemoteSpace(ACTIVE_SERVERS_URI);
            int counter = 0;
            int counter2 = 0;
            while (true) {

                List<Object[]> activeServerObjects = activeServers.queryAll(new FormalField(String.class),
                        new ActualField("new Client"));

                if (activeServerObjects.size() == 1 && counter == 0) {
                    counter++;
                    System.out.println("TEST");
                    Platform.runLater(() -> {
                        try {
                            startServer((String) activeServerObjects.get(0)[0]);

                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }

                    });

                } else if (activeServerObjects.size() == 2 && counter == 1 && counter2 == 0) {
                    counter2++;
                    String existingServerIp = (String) activeServerObjects.get(0)[0];

                    Platform.runLater(() -> {
                        try {
                            joinServer(existingServerIp);
                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                    });
                    activeServers.getAll(new FormalField(String.class),
                            new ActualField("new Client"));
                 

                }else {   counter = 0;
                    counter2 = 0;
                }

                // Sleep for a while before checking again
                Thread.sleep(1000); // Adjust the sleep duration as needed
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}
