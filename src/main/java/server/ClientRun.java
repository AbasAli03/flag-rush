package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

public class ClientRun extends Server implements Runnable {
    public ClientRun() {
        super();
    }

    @Override
    public void run() {
        boolean clientConnected = false;
        while (!clientConnected) {
            try {
                RemoteSpace activeServers = new RemoteSpace(MatchMakingServer.ACTIVE_SERVERS_URI);

                String hostIp = InetAddress.getLocalHost().getHostAddress().toString();
                System.out.println(hostIp);
                activeServers.put(hostIp, "new Client");
                List<Object[]> activeServerObjects = activeServers.queryAll(new FormalField(String.class),
                        new ActualField("new Client"));
                String activehostIp = (String) activeServerObjects.get(0)[0];

                clientConnected = true;

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
