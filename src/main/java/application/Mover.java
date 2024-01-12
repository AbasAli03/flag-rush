package application;

import java.util.ArrayList;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

class Mover implements Runnable {

    private final Space infoSpace;
    private final Game game;

    public Mover(Space infoSpace, Game game) {
        this.infoSpace = infoSpace;
        this.game = game;
    }

    public void run() {
        /* 
        while (true) {
            try {

                // Wait until need new player
                infoSpace.get(new ActualField("needPlayer"));

                // Move in to fill space
                if (game.connectedPlayers() < 2) {
                    Object[] res = infoSpace.get(new ActualField("Spectators"), new FormalField(Object.class));
                    ArrayList<String> clients = (ArrayList<String>) ((ArrayList<String>) res[1]).clone();

                    String user = clients.get(0);

                    clients.remove(0);

                    infoSpace.put("Broadcast", "Moved", user);
                    game.addPlayer(user);
                }

            } catch (InterruptedException e) {

                e.printStackTrace();

            }

        }
*/
    }

}