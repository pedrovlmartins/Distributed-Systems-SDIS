package connections.tcp.data;

import graphics.initialFrame.JoinRoomPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Pedro Fraga on 22-May-16.
 */
public class DataBase extends Thread {

    HashMap<RoomID, ArrayList<PeerID>> availableRooms;
    RoomID currentRoom; /* A sala em que o jogador se encontra, null se não está em nenhuma sala */
    static DataBase instance;

    public DataBase() {
        currentRoom = null;
        instance = this;
        availableRooms = new HashMap<>();
    }

    public static DataBase getInstance() {
        return instance;
    }

    public RoomID getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(RoomID room) {
        this.currentRoom = room;
    }

    public void clearAvailableRooms() {
        availableRooms.clear();
        if (JoinRoomPanel.getInstance() != null)
            JoinRoomPanel.getInstance().getListbox().setModel(new DefaultListModel());
    }
}
