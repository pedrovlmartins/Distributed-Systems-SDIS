package connections.server.messages;

import connections.server.Client;
import connections.tcp.TCPClient;
import org.json.JSONArray;
import org.json.JSONObject;
import utilities.Constants;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;

/**
 * Created by Pedro Fraga on 26-May-16.
 */
public class ClientMessage {
    private JSONObject jsonMsg;
    private static String hostname = "";
    private InetAddress hostAddress;

    static {
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier(){

                    public boolean verify(String hostname,
                                          javax.net.ssl.SSLSession sslSession) {
                        return hostname.equals("localhost") || hostname != null || hostname.length() > 0;
                    }
                });
    }



    public ClientMessage(JSONObject json) {
        this.jsonMsg = json;
    }

    public JSONObject send() {

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            char[] password = "123456".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("server.keys");
            ks.load(fis, password);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            hostname = Client.getInstance().getHost();
            System.out.println("Connecting to " +  hostname);
            String urlString = "https://" + hostname + ":4563/24game";
            URL url = null;
            url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            System.out.println(1);

            connection.setRequestProperty("Content-Type", "application/json");


            PrintWriter out = new PrintWriter(connection.getOutputStream());
            System.out.println(jsonMsg);
            String msg = jsonMsg.toString();
            System.out.println(2);
            out.println(URLEncoder.encode(msg, "UTF-8"));
            out.close();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line = in.readLine();
            in.close();
            System.out.println(3);
            JSONObject rooms = new JSONObject(line);
            return rooms;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int handleCreateRoom(JSONObject serverResponse) {
        System.out.println(serverResponse);
        String result = serverResponse.getString(Constants.CREATE_ROOM);
        JSONArray array = serverResponse.getJSONArray(Constants.GAME);
        TCPClient.getInstance().set24Game(array);
        return Integer.parseInt(result);
    }

    public static String getHostname() {
        return hostname;
    }

    public int handleJoinRoom(JSONObject serverResponse) {
        System.out.println(serverResponse);
        String result = serverResponse.getString(Constants.JOIN_ROOM);
        int port = Integer.parseInt(result);
        if (port == Constants.ERROR)
            return Constants.ERROR;
        JSONArray array = serverResponse.getJSONArray(Constants.GAME);
        String name = serverResponse.getString(Constants.NAME);
        TCPClient.getInstance().set24Game(array);
        TCPClient.getInstance().getPeerID().setUsername(name);

        return port;
    }
}