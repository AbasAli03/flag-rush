package server;

import java.io.IOException;

import java.util.List;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

import application.Main;

public class MatchMakingServer implements Runnable {
    private static String ip = "192.168.50.190";
    private static String URI = "tcp://" + ip + ":9001/?keep";
    public static String ACTIVE_SERVERS = "activeServer";
    public static String ALL_SERVERS = "allServer";
    public static String ACTIVE_SERVERS_URI = "tcp://" + ip + ":9001/" + ACTIVE_SERVERS + "?keep";
    public static String ALL_SERVERS_URI = "tcp://" + ip + ":9001/" + ALL_SERVERS + "?keep";
    public static SpaceRepository repository = new SpaceRepository();

    public static void main(String[] args) {
        repository.add(ACTIVE_SERVERS, new SequentialSpace());
        repository.add(ALL_SERVERS, new SequentialSpace());
        repository.addGate(URI);

        System.out.println("Server is running...");

    }

    public MatchMakingServer() {
        super();
    }

    @Override
    public void run() {
        try {
            RemoteSpace activeServers = new RemoteSpace(ACTIVE_SERVERS_URI);
            RemoteSpace allServers = new RemoteSpace(MatchMakingServer.ALL_SERVERS_URI);

            boolean clientConnected = false;
            while (!clientConnected) {
                List<Object[]> activeServerObjects = activeServers.queryAll(new FormalField(String.class),
                        new ActualField("new Client"));
                List<Object[]> allServerObjects = allServers.queryAll(new FormalField(
                        String.class),
                        new ActualField("new Client"));
                Object[] serverObject = null;
                if (allServerObjects.size() > 0) {
                    serverObject = allServers.queryp(new ActualField((String) activeServerObjects
                            .get(0)[0]),
                            new ActualField("new Client"));
                }

                System.out.println("active servers in side matchmaking server " + activeServerObjects.size());
                if (activeServerObjects.size() == 1
                        && (serverObject == null || !serverObject[0].equals(activeServerObjects.get(0)[0]))) {
                    clientConnected = true;
                    try {
                        String serverIp = (String) activeServerObjects.get(0)[0];
                        Main.server.startServer(serverIp, true);
                        //allServers.put(serverIp, "new Client");
                        System.out.println("Player 1 connected");

                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }

                } else if (activeServerObjects.size() == 2) {
                    String existingServerIp = (String) activeServerObjects.get(0)[0];
                    clientConnected = true;
                    try {
                        Main.server.joinServer(existingServerIp, true);
                        System.out.println("else inside matchmaking is running");

                        allServers.put(existingServerIp, "new Client");
                        System.out.println("Player 2 connected");

                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }

                    activeServers.getAll(new FormalField(String.class),
                            new ActualField("new Client"));

                }

            }
        } catch (InterruptedException |

                IOException e) {
            e.printStackTrace();
        }
    }

}
