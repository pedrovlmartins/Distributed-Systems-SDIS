package connections.server;

/**
 * Created by Pedro Fraga on 26-May-16.
 */

import connections.tcp.data.PeerID;
import connections.tcp.data.RoomID;
import connections.server.messages.ClientMessage;
import graphics.initialFrame.JoinRoomPanel;
import org.json.JSONArray;
import org.json.JSONObject;
import utilities.Constants;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Client {

    private HashMap<RoomID, ArrayList<PeerID>> availableRooms;
    private static Client instance;

    private String host;


    public Client(String host) {
        availableRooms = new HashMap<>();
        this.host = host;
        instance = this;
    }

    private boolean handleAvailableRooms() throws IOException {


        JSONObject message = new JSONObject();
        message.put(Constants.REQUEST, Constants.GET_ROOMS);
        ClientMessage roomRequest = new ClientMessage(message);
        JSONObject rooms = roomRequest.send();
        if (!handleRooms(rooms)) {
            System.err.println("Cant handle rooms message");
            return false;
        }
        DefaultListModel model = new DefaultListModel();
        for (RoomID room : availableRooms.keySet()) {
            String players = availableRooms.get(room).size() == 1 ? "player" : "players";
            model.addElement(room.getName() + "  ( " + availableRooms.get(room).size() + " " + players + " )");
        }
        if (JoinRoomPanel.getInstance() != null)
            JoinRoomPanel.getInstance().getListbox().setModel(model);
        return true;
    }

    public boolean requestAvailableRooms() {
        try {
            return handleAvailableRooms();
        } catch (IOException e) {
            System.err.println("Could not get available rooms from server");
            return false;
        }
    }


    private boolean handleRooms(JSONObject rooms) {
        if (rooms.isNull(Constants.ROOMS))
            return false;
        JSONArray roomArray = rooms.getJSONArray(Constants.ROOMS);
        for (int i = 0; i < roomArray.length(); i++) {
            JSONObject roomInfo = roomArray.getJSONObject(i);

            JSONObject roomIdJson = roomInfo.getJSONObject(Constants.ROOM_ID);
            RoomID roomId = new RoomID(roomIdJson);
            JSONArray peerArray = roomInfo.getJSONArray(Constants.PEER_ARRAY);
            ArrayList<PeerID> peerIdArray = new ArrayList<>();
            for (int a = 0; a < peerArray.length(); a++) {
                JSONObject peerIdJson = peerArray.getJSONObject(a);
                PeerID peerId = new PeerID(peerIdJson);
                peerIdArray.add(peerId);
            }
            availableRooms.put(roomId, peerIdArray);
        }
        return true;
    }

    public static Client getInstance() {
        return instance;
    }

    public void clearAvailableRooms() {
        availableRooms.clear();
    }

    public HashMap<RoomID, ArrayList<PeerID>> getAvailableRooms() {
        return availableRooms;
    }

    public String getHost() {
        return host;
    }
}
