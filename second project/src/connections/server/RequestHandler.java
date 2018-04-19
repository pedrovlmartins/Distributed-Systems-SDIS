package connections.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import connections.tcp.TCPServer;
import connections.tcp.data.PeerID;
import connections.tcp.data.RoomID;
import org.json.JSONArray;
import org.json.JSONObject;
import utilities.Constants;
import utilities.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Pedro Fraga on 26-May-16.
 */
public class RequestHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        System.out.println(1);
        InputStream in = t.getRequestBody();
        System.out.println(2);
        String request = Utilities.getBytesFromInputStream(in);
        System.out.println(request);
        handleRequest(request, t);
        in.close();
    }

    private void handleRequest(String request, HttpExchange t) throws IOException {
        JSONObject jsonRequest = new JSONObject();
        if (Utilities.isJSONValid(request)) {
            jsonRequest = new JSONObject(request);
            request = jsonRequest.getString(Constants.REQUEST);
        } else {
            request = Constants.ERROR_STRING;
        }
        switch (request) {
            case Constants.GET_ROOMS:
                JSONObject roomsJson = new JSONObject();
                JSONArray roomsArray = new JSONArray();
                if (Server.getAvailableRooms() == null)
                    System.out.println("get rooms null");

                for (Map.Entry<RoomID, ArrayList<PeerID>> entry : Server.getAvailableRooms().entrySet()) {
                    JSONObject roomAndPeers = new JSONObject();

                    RoomID roomId = entry.getKey();
                    ArrayList<PeerID> peerArray = entry.getValue();
                    JSONArray array = new JSONArray();
                    for (PeerID peer : peerArray) {
                        JSONObject json = new JSONObject(peer.toString());
                        array.put(json);
                    }
                    JSONObject roomIdJson = new JSONObject(roomId);
                    roomAndPeers.put(Constants.ROOM_ID, roomIdJson);
                    roomAndPeers.put(Constants.PEER_ARRAY, array);
                    roomsArray.put(roomAndPeers);
                }
                roomsJson.put(Constants.ROOMS, roomsArray);
                sendJson(roomsJson, t, Constants.OK);
                break;
            case Constants.CREATE_ROOM:
                JSONObject createdRoom = jsonRequest.getJSONObject(Constants.CREATE_ROOM);
                handleRoom(createdRoom, t, request);
                break;
            case Constants.JOIN_ROOM:
                JSONObject joinRoom = jsonRequest.getJSONObject(Constants.JOIN_ROOM);
                handleRoom(joinRoom, t, request);
                break;
            default:
                System.err.println("Unknown request (" + request + ")");
                JSONObject jsonError = new JSONObject();
                jsonError.put(Constants.ERROR_STRING, Constants.NOT_FOUND);
                sendJson(jsonError, t, Constants.OK);
                break;
        }
    }

    private void handleRoom(JSONObject createdRoom, HttpExchange t, String constraint) throws IOException {
        JSONObject roomJson = createdRoom.getJSONObject(Constants.ROOM_ID);
        JSONObject peerJson = createdRoom.getJSONObject(Constants.PEER_ID);
        RoomID roomId = new RoomID(roomJson);
        PeerID peerId = new PeerID(peerJson);
        ArrayList<PeerID> peerArray;
        TCPServer TCPServer = new TCPServer(roomId, peerId);
        TCPServer.start();
        String message = getGame(roomId) || constraint.equals(Constants.CREATE_ROOM) ? TCPServer.getPort() + "" : Constants.ERROR + "";
        JSONObject jsonOk = new JSONObject();
        String name = peerId.getUsername();
        if (Server.getAvailableRooms().get(roomId) != null) {
            peerArray = Server.getAvailableRooms().get(roomId);
            if (constraint.equals(Constants.JOIN_ROOM)) {
                JSONObject joinedJson = new JSONObject();
                joinedJson.put(Constants.REQUEST, Constants.JOINED_ROOM);
                int tries = 0;
                for (int i = 0; i < peerArray.size(); i++) {
                    if (peerArray.get(i).getUsername().equals(peerId.getUsername())) {
                        tries++;
                        peerId.setUsername(name + " (" + tries + ")");
                        i = 0;
                    }
                }
                jsonOk.put(Constants.NAME, peerId.getUsername());
                joinedJson.put(Constants.PEER_ID, peerId.getJSON());
                for (int i = 0; i < peerArray.size(); i++) {
                    peerArray.get(i).getTCPServer().add2MsgArray(joinedJson);
                }
            }
            peerArray.add(peerId);
        } else if (!message.equals(Constants.ERROR + "")) {
            peerArray = new ArrayList<>();
            peerArray.add(peerId);
            Server.getAvailableRooms().put(roomId, peerArray);
        }
        jsonOk.put(constraint, message);
        if (roomId.getCurrentGame() == null) {
            roomId.set24Game(Server.getGame24().getRandomGame());
        }
        JSONArray array = new JSONArray(roomId.getCurrentGame());
        jsonOk.put(Constants.GAME, array);
        sendJson(jsonOk, t, Constants.OK);
    }

    private void sendJson(JSONObject json, HttpExchange t, int code) throws IOException {
        OutputStream os = t.getResponseBody();
        String response = json.toString();
        t.sendResponseHeaders(code, response.length());
        os.write(response.getBytes());
        os.close();
    }


    public boolean getGame(RoomID roomId) {
        for (RoomID room : Server.getAvailableRooms().keySet()) {
            if (room.equals(roomId)) {
                if (room.getCurrentGame() == null) {
                    System.out.println("Game null");
                    roomId.set24Game(Server.getGame24().getRandomGame());
                    return true;
                } else {
                    System.out.println("Game found");
                    roomId.set24Game(room.getCurrentGame());
                    return true;
                }
            }
        }
        System.out.println("Game not found");
        roomId.set24Game(Server.getGame24().getRandomGame());
        return false;
    }
}
