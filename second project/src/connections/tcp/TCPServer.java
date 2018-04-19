package connections.tcp;

import connections.server.Server;
import connections.tcp.data.PeerID;
import connections.tcp.data.RoomID;
import org.json.JSONArray;
import org.json.JSONObject;
import utilities.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Pedro Fraga on 26-May-16.
 */
public class TCPServer extends Thread {

    private DatagramSocket socket;
    private int port;
    private int client_port;
    private InetAddress client_address;

    private RoomID roomId;
    private PeerID peerId;

    private boolean listening;
    private ArrayList<JSONObject> messagesArray;
    private int tries;

    public TCPServer(RoomID roomId, PeerID peerId) throws SocketException {
        socket = new DatagramSocket(0);
        socket.setSoTimeout(500);
        port = socket.getLocalPort();
        client_port = -1;
        this.roomId = roomId;
        this.peerId = peerId;
        listening = true;
        messagesArray = new ArrayList<>();
        peerId.setTCPServer(this);
        tries = 0;
    }

    public int getPort() {
        return port;
    }

    private String rcvTcpData() throws IOException {
        byte[] rbuf = new byte[Constants.MSG_SIZE];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        client_port = packet.getPort();
        client_address = packet.getAddress();
        return received;
    }

    public void run() {
        try {
            String msg = rcvTcpData();
            JSONObject jsonMsg = new JSONObject(msg);
            String request = jsonMsg.getString(Constants.REQUEST);
            switch (request) {
                case Constants.R_U_THERE:
                    jsonMsg = new JSONObject();
                    jsonMsg.put(Constants.REQUEST, Constants.R_U_THERE_ACK);
                    sendTcpData(jsonMsg.toString());
                    checkPeer();
                    break;
            }
        } catch (IOException e) {
            System.err.println("Could not receive tcp message");
        }
    }

    private void checkPeer() {
        RcvRequest rcvRequest;
        while (listening) {
            try {
                rcvRequest = new RcvRequest(this);
                rcvRequest.start();
                int ms = 500;
                Thread.sleep(ms);
                tries++;
                if (tries > 3) {
                    listening = false;
                    rcvRequest.interrupt();
                }
                JSONObject jsonMsg;
                if (messagesArray.size() == 0) {
                    jsonMsg = new JSONObject();
                    jsonMsg.put(Constants.REQUEST, Constants.R_U_THERE_ACK);
                    sendTcpData(jsonMsg.toString());
                } else {
                    jsonMsg = messagesArray.get(0);
                    messagesArray.remove(jsonMsg);
                    sendTcpData(jsonMsg.toString());
                }
            } catch (Exception e) {
                System.err.println("Something was wrong");
            }
        }
        ArrayList<PeerID> peers = Server.getAvailableRooms().get(roomId);
        peers.remove(peerId);

        JSONObject disconnected = new JSONObject();
        disconnected.put(Constants.REQUEST, Constants.TIMEDOUT);
        JSONObject peerJson = new JSONObject(peerId.toString());
        disconnected.put(Constants.PEER_ID, peerJson);
        for (PeerID peer : peers) {
            peer.getTCPServer().add2MsgArray(disconnected);
        }
        if (peers.size() == 0)
            Server.getAvailableRooms().remove(roomId);
        this.socket.close();
        System.err.println("Client " + peerId.getUsername() + " connection was closed (peer size = " + peers.size() + ")");
    }

    private void sendTcpData(String msg) throws IOException {
        byte[] sbuf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, client_address, client_port);
        socket.send(packet);
    }

    public PeerID getPeerId() {
        return peerId;
    }

    public void add2MsgArray(JSONObject jsonMsg) {
        messagesArray.add(jsonMsg);
    }
    public RoomID getRoomId() {
        return roomId;
    }

    class RcvRequest extends Thread {
        TCPServer context;

        public RcvRequest(TCPServer context) {
            this.context = context;
        }

        public void run() {
            try {
                String response = context.rcvTcpData();
                JSONObject jsonMsg = new JSONObject(response);
                System.out.println(jsonMsg);
                String request = jsonMsg.getString(Constants.REQUEST);
                ArrayList<PeerID> peers;
                switch (request) {
                    case Constants.R_U_THERE:
                        context.resetTries();
                        break;
                    case Constants.MESSAGE:
                        context.resetTries();
                        peers = Server.getAvailableRooms().get(context.getRoomId());
                        for (PeerID peer : peers) {
                            peer.getTCPServer().add2MsgArray(jsonMsg);
                        }
                        break;
                    case Constants.WINNER:
                        context.resetTries();
                        ArrayList<Integer> array = new ArrayList();
                        for (RoomID room : Server.getAvailableRooms().keySet()) {
                            if (room.equals(roomId)) {
                                array = Server.getGame24().getRandomGame();
                                room.set24Game(array);
                            }
                        }
                        JSONArray jsonArray = new JSONArray(array);
                        jsonMsg.put(Constants.GAME, jsonArray);
                        peers = Server.getAvailableRooms().get(context.getRoomId());
                        for (PeerID peer : peers) {
                            peer.getTCPServer().add2MsgArray(jsonMsg);
                        }
                        break;
                }
            } catch (IOException e) {
                System.err.println("Client " + context.getPeerId().getUsername() + " timing out at port " + context.getPort());
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        this.socket.close();
    }

    private void resetTries() {
        tries = 0;
    }
}
