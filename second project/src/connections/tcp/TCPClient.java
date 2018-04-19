package connections.tcp;

import connections.tcp.data.DataBase;
import connections.tcp.data.PeerID;
import connections.tcp.data.RoomID;
import connections.server.messages.ClientMessage;
import graphics.gameFrame.CenterPanel;
import graphics.gameFrame.Chat;
import graphics.gameFrame.NumbersPanel;
import org.json.JSONArray;
import org.json.JSONObject;
import utilities.Constants;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by Pedro Fraga on 22-May-16.
 */

public class TCPClient extends Thread {
    private PeerID peerId;

    private InetAddress adress;
    private int port;
    private DatagramSocket socket;
    private ArrayList<JSONObject> responses;

    private DataBase database;
    private static TCPClient instance;


    public TCPClient() throws UnknownHostException, SocketException {
        this.peerId = new PeerID(Constants.ANONYMOUS);
        database = new DataBase();
        adress = InetAddress.getByName(ClientMessage.getHostname());
        socket = new DatagramSocket();
        responses = new ArrayList();
        instance = this;
    }


    public PeerID getPeerID() {
        return this.peerId;
    }

    public static TCPClient getInstance() {
        return instance;
    }

    public void run() {
        JSONObject responseJson = new JSONObject();
        try {
            JSONObject request = new JSONObject();
            request.put(Constants.REQUEST, Constants.R_U_THERE);
            responseJson = sendRequest(request);
        } catch (Exception e) {
            System.err.println("Could not send a message through tcp");
        }
        while (true) {
            try {
                handleJson(responseJson);
                byte[] buf = new byte[utilities.Constants.MSG_SIZE];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                responseJson = new JSONObject(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleJson(JSONObject jsonObject) throws IOException {
        System.out.println(jsonObject);
        String response = jsonObject.getString(Constants.REQUEST);
        JSONObject msg = new JSONObject();
        JSONObject jsonObj;
        String name;
        String text;
        switch (response) {
            case Constants.R_U_THERE_ACK:
                msg.put(Constants.REQUEST, Constants.R_U_THERE);
                break;
            case Constants.JOINED_ROOM:
                System.out.println(jsonObject);
                jsonObj = jsonObject.getJSONObject(Constants.PEER_ID);
                name = jsonObj.getString(Constants.USERNAME);
                text = "<" + name + "> Joined the room.";
                Chat.getInstance().add2Chat(text);
                msg.put(Constants.REQUEST, Constants.R_U_THERE);
                break;
            case Constants.TIMEDOUT:
                jsonObj = jsonObject.getJSONObject(Constants.PEER_ID);
                name = jsonObj.getString(Constants.USERNAME);
                text = "<" + name + "> Disconnected. (Timeout)";
                Chat.getInstance().add2Chat(text);
                msg.put(Constants.REQUEST, Constants.R_U_THERE);
                break;
            case Constants.MESSAGE:
                jsonObj = jsonObject.getJSONObject(Constants.PEER_ID);
                name = jsonObj.getString(Constants.USERNAME);
                String message = jsonObject.getString(Constants.MESSAGE);
                text = "<" + name + "> said: " + message;
                Chat.getInstance().add2Chat(text);
                msg.put(Constants.REQUEST, Constants.R_U_THERE);
                break;
            case Constants.WINNER:
                jsonObj = jsonObject.getJSONObject(Constants.PEER_ID);
                name = jsonObj.getString(Constants.USERNAME);
                String equation = jsonObject.getString(Constants.EQUATION);
                text = "<" + name + "> Reached 24: " + equation + ". A new board was generated.";
                Chat.getInstance().add2Chat(text);
                msg.put(Constants.REQUEST, Constants.R_U_THERE);
                JSONArray array = jsonObject.getJSONArray(Constants.GAME);
                TCPClient.getInstance().set24Game(array);
                CenterPanel.getInstance().updateNumbersPanel();
                NumbersPanel.getInstance().resetEquation();
                break;
            default:
                msg.put(Constants.REQUEST, Constants.ERROR_STRING);
                break;
        }
        if (responses.size() > 0) {
            msg = responses.get(0);
            responses.remove(msg);
        }
        byte[] buf = msg.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, adress, port);
        socket.send(packet);
    }

    public void createRoom(String roomName) {
        RoomID createdRoom = new RoomID(roomName);
        database.setCurrentRoom(createdRoom);

        JSONObject peerInfo = new JSONObject();
        peerInfo.put(Constants.PEER_ID, new JSONObject(peerId));
        peerInfo.put(Constants.ROOM_ID, new JSONObject(createdRoom));

        JSONObject msgJson = new JSONObject();
        msgJson.put(Constants.REQUEST, Constants.CREATE_ROOM);
        msgJson.put(Constants.CREATE_ROOM, peerInfo);
        ClientMessage msg = new ClientMessage(msgJson);
        port = msg.handleCreateRoom(msg.send());
        start();
    }

    public JSONObject sendRequest(JSONObject request) throws IOException {
        byte[] buf = request.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, adress, port);
        socket.send(packet);

        buf = new byte[utilities.Constants.MSG_SIZE];
        packet = new DatagramPacket(buf, buf.length);
        this.socket.receive(packet);
        String response = new String(packet.getData(), 0, packet.getLength());
        return new JSONObject(response);
    }

    public DataBase getDataBase() {
        return database;
    }

    public void setPeerUsername(String username) {
        this.peerId.setUsername(username);
    }

    public void joinRoom(RoomID room) {
        database.setCurrentRoom(room);
        JSONObject peerInfo = new JSONObject();
        peerInfo.put(Constants.PEER_ID, new JSONObject(peerId));
        peerInfo.put(Constants.ROOM_ID, new JSONObject(room));

        JSONObject msgJson = new JSONObject();
        msgJson.put(Constants.REQUEST, Constants.JOIN_ROOM);
        msgJson.put(Constants.JOIN_ROOM, peerInfo);

        ClientMessage msg = new ClientMessage(msgJson);
        port = msg.handleJoinRoom(msg.send());
        System.out.println(port);
        if (port != Constants.ERROR)
            start();
    }

    public void add2Responses(JSONObject msg) {
        responses.add(msg);
    }

    public ArrayList<Integer> getCurrentGame() {
        return database.getCurrentRoom().getCurrentGame();
    }

    public void set24Game(JSONArray array) {
        ArrayList<Integer> game = new ArrayList<>();
        for (int i = 0; i < array.length(); i++){
            game.add((Integer)array.get(i));
        }
        database.getCurrentRoom().set24Game(game);
    }

    public void setWinner(String equation) {
        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put(Constants.REQUEST, Constants.WINNER);
        jsonMsg.put(Constants.PEER_ID, TCPClient.getInstance().getPeerID().getJSON());
        jsonMsg.put(Constants.EQUATION, equation);
        responses.add(jsonMsg);
    }

    public int getPort() {
        return port;
    }
}
