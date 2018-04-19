package graphics.initialFrame;

import connections.tcp.TCPClient;
import game.Game24;
import graphics.gameFrame.GameFrame;
import graphics.utilities.CustomTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by inesa on 22/05/2016.
 */
public class CreateRoomPanel extends JPanel {

    private static final int PREF_W = (int)Math.floor((3*InitialFrame.width)/10);
    private static final int PREF_H = (int)Math.floor((6*InitialFrame.height)/8);

    private JLabel jlabel;
    private JLabel jlabel1;
    private CustomTextField textFieldNick;
    private CustomTextField textFieldRoom;
    private JButton createButton;

    public CreateRoomPanel() {
        setPreferredSize(new Dimension(PREF_W, PREF_H));
        //System.out.println("Create Room : H: " + PREF_H + " W: " + PREF_W);
        //setBorder(BorderFactory.createLineBorder(Color.blue));

        setLayout(new FlowLayout(FlowLayout.CENTER));

        jlabel = new JLabel("Create Room: ");
        jlabel.setFont(new Font("Verdana", 1, 30));
        jlabel.setPreferredSize(new Dimension(PREF_W, 30));
        //jlabel.setBorder(BorderFactory.createLineBorder(Color.blue));
        add(jlabel);

        add(Box.createVerticalStrut(80));

        jlabel1 = new JLabel("Room name: ");
        jlabel1.setFont(new Font("Verdana", 1, 15));
        jlabel1.setPreferredSize(new Dimension(PREF_W/3, 30));
        //jlabel1.setBorder(BorderFactory.createLineBorder(Color.blue));


        //enterText
        textFieldRoom = new CustomTextField("Name a room...", 23);


        add(jlabel1);
        add(textFieldRoom);

        jlabel = new JLabel("Your nickname: ");
        jlabel.setFont(new Font("Verdana", 1, 15));
        jlabel.setPreferredSize(new Dimension(PREF_W/3, 30));
        //jlabel.setBorder(BorderFactory.createLineBorder(Color.blue));

        //enterText
        textFieldNick = new CustomTextField("insert your nickname...", 22);


        add(jlabel);
        add(textFieldNick);

        add(Box.createVerticalStrut(80));

        createButton = new JButton("CREATE ROOM");
        createButton.setPreferredSize(new Dimension(200, 30));
        add(createButton);
        buttonListener();


    }


    private void buttonListener() {
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String nickName = textFieldNick.getText();
                String roomName = textFieldRoom.getText();

                if (nickName.equals("insert your nickname...") || roomName.equals("Name a room...")) {
                    JOptionPane.showMessageDialog(InitialFrame.getFrames()[0],
                            "Missing Room Name or Nickname",
                            "Missing parameter error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    InitialFrame.getFrames()[0].setVisible(false);
                    TCPClient TCPClient = null;
                    try {
                        TCPClient = new TCPClient();
                    } catch (Exception e1) {
                        System.err.println("Could not create client");
                    }
                    TCPClient.setPeerUsername(nickName);
                    TCPClient.createRoom(roomName);
                    new GameFrame(TCPClient, new Game24());
                }
            }
        });
    }

}
