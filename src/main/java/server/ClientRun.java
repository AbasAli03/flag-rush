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

public class ClientRun extends Server implements Runnable {
    public ClientRun() {
        super();
    }

    @Override
    public void run() {
        try {

            RemoteSpace activeServers = new RemoteSpace(MatchMakingServer.ACTIVE_SERVERS_URI);

            String hostIp = InetAddress.getLocalHost().getHostAddress().toString();

            activeServers.put(hostIp, "new Client");
            System.out.println("Clients put");
            // Sleep for a while before checking again
            Thread.sleep(5000); // Adjust the sleep duration as needed

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }
}
