package graphics.initialFrame;

import connections.tcp.TCPClient;
import connections.tcp.data.RoomID;
import connections.server.Client;
import game.Game24;
import graphics.gameFrame.GameFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by inesa on 22/05/2016.
 */
public class JoinRoomPanel extends JPanel {

    private JList listbox;

    private ArrayList<String> roomsName = new ArrayList<>();

    public static JoinRoomPanel instance;
    private JButton refreshButton;


    public JoinRoomPanel() {

        int PREF_W = (int) Math.floor((4 * InitialFrame.width) / 10);
        int PREF_H = (int) Math.floor((6 * InitialFrame.height) / 8);

        setPreferredSize(new Dimension(PREF_W, PREF_H));
        //System.out.println("Join Room : H: " + PREF_H + " W: " + PREF_W);
        //setBorder(BorderFactory.createLineBorder(Color.blue));

        JLabel jlabel = new JLabel("Join Room: ");
        jlabel.setBorder(new EmptyBorder(0,40,40,40));
        jlabel.setFont(new Font("Verdana", 1, 30));
        add(jlabel);

        //add(Box.createVerticalStrut(80));

        JTextPane pane = new JTextPane();
        pane.setEditable(false);

        // Create a new listbox control
        listbox = new JList(roomsName.toArray());

        JScrollPane areaScroll = new JScrollPane(listbox);
        areaScroll.setPreferredSize(new Dimension(PREF_W-40, 300));
        areaScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(areaScroll);

        refreshButton = new JButton("REFRESH");
        refreshButton.setPreferredSize(new Dimension(PREF_W/2, 30));
        add(refreshButton);
        buttonListener();

        listbox.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String name = JOptionPane.showInputDialog(InitialFrame.getFrames()[0], "What's your name?");
                if (e.getValueIsAdjusting()) {
                    if (name != null) {
                        InitialFrame.getFrames()[0].setVisible(false);
                        TCPClient TCPClient = null;
                        try {
                            TCPClient = new TCPClient();
                        } catch (Exception e1) {
                            System.err.println("Could not create client");
                        }
                        TCPClient.setPeerUsername(name);
                        ArrayList keys = new ArrayList(Client.getInstance().getAvailableRooms().keySet());
                        TCPClient.joinRoom((RoomID) keys.get(e.getLastIndex()));
                        if (TCPClient.getPort() != -1)
                            new GameFrame(TCPClient, new Game24());
                        else {
                          if( JOptionPane.showConfirmDialog(InitialFrame.getFrames()[0],
                                    "Sorry that room is no longer available",
                                    "Error", JOptionPane.OK_OPTION,
                                    JOptionPane.ERROR_MESSAGE) == JOptionPane.OK_OPTION){
                              InitialFrame.getFrames()[0].setVisible(true);
                          }
                        }
                    } else {
                        listbox.clearSelection();
                    }
                }
            }
        });

        instance = this;
    }

    public static JoinRoomPanel getInstance() {
        return instance;
    }

    public JList getListbox() {
        return listbox;
    }

    private void buttonListener() {
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Refresh: " + listbox.toString());
                Client.getInstance().clearAvailableRooms();
                Client.getInstance().requestAvailableRooms();
            }
        });
    }

}