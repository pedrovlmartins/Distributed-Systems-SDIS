package launcher;

import connections.server.Client;
import graphics.initialFrame.InitialFrame;

/**
 * Created by Pedro Fraga on 23-May-16.
 */
class Launcher {
    public static void main(String args[]) {
        if (args.length != 1)
            System.err.println("Please define an argument as your server");
        Client client = new Client(args[0]);
        new InitialFrame();
        client.requestAvailableRooms();
    }
}