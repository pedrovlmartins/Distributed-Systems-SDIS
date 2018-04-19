package connections.server;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import connections.tcp.data.PeerID;
import connections.tcp.data.RoomID;
import game.Game24;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Pedro Fraga on 25-May-16.
 */


public class Server {
    public static HashMap<RoomID, ArrayList<PeerID>> availableRooms;
    public static HashMap<PeerID, Integer> establishedConnections;
    public static Game24 game;

    public static void main(String[] args) throws Exception {

        availableRooms = new HashMap<>();
        establishedConnections = new HashMap<>();
        game = new Game24();
        game.readFile();
        HttpsServer server = HttpsServer.create(new InetSocketAddress(4563), 0);
        SSLContext sslContext = SSLContext.getInstance("TLS");

        char[] password = "123456".toCharArray ();
        KeyStore ks = KeyStore.getInstance ("JKS");
        FileInputStream fis = new FileInputStream("server.keys");
        ks.load(fis, password);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        sslContext.init ( kmf.getKeyManagers (), tmf.getTrustManagers (), null );
        server.setHttpsConfigurator ( new HttpsConfigurator( sslContext )
        {
            public void configure ( HttpsParameters params )
            {
                try
                {
                    SSLContext c = SSLContext.getDefault ();
                    SSLEngine engine = c.createSSLEngine ();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    SSLParameters defaultSSLParameters = c.getDefaultSSLParameters ();
                    params.setSSLParameters ( defaultSSLParameters );
                }
                catch ( Exception ex )
                {
                    System.err.println("Failed to create HTTPS port");
                }
            }
        } );
        server.createContext("/24game", new RequestHandler());
        server.setExecutor(null);
        server.start();
    }

    public static HashMap<RoomID, ArrayList<PeerID>> getAvailableRooms() {
        return availableRooms;
    }
    public static HashMap<PeerID, Integer> getEstablishedConnections() {
        return establishedConnections;
    }

    public static Game24 getGame24() {
        return game;
    }
}