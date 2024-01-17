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

import application.Main;
import javafx.application.Platform;

public class MatchMakingServer extends Server implements Runnable {
    private static String ip = "";
    private static String URI = "tcp://" + ip + ":9001/?keep";
    public static String ACTIVE_SERVERS = "activeServer";
    private String ACTIVE_SERVERS_URI = "tcp://" + ip + ":9001/" + ACTIVE_SERVERS + "?keep";
    
    

    public static SpaceRepository repository = new SpaceRepository();
    public static void main(String[] args) {
        repository.add(ACTIVE_SERVERS, new SequentialSpace());

        repository.addGate(URI);

    }

    public MatchMakingServer() {
        super();
    }

    @Override
    public void run() {
        try {
            RemoteSpace activeServers = new RemoteSpace(ACTIVE_SERVERS_URI);
            List<Object[]> activeServerObjects;
            activeServerObjects = activeServers.queryAll(new FormalField(String.class),
                    new ActualField("new Client"));
            if (activeServerObjects.size() == 0) {
                String hostIp = InetAddress.getLocalHost().getHostAddress().toString();
                Platform.runLater(() -> {
                    try {
                        startServer(hostIp);
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });

                activeServers.put(hostIp, "new Client");
            } else {
                Platform.runLater(() -> {
                    try {
                        joinServer((String) activeServerObjects.get(0)[0]);
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                activeServers.getAll(new FormalField(String.class), new ActualField("new Client"));
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }

}
