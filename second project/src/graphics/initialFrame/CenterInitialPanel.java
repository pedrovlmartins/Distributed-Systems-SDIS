package graphics.initialFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Created by inesa on 22/05/2016.
 */
public class CenterInitialPanel extends JPanel {

    private static final int PREF_W = (int)Math.floor((8*InitialFrame.width)/10);
    private static final int PREF_H = (int)Math.floor((6*InitialFrame.height)/8);

    public CenterInitialPanel() {
        setPreferredSize(new Dimension(PREF_W, PREF_H));
        //System.out.println("Center Room : H: " + PREF_H + " W: " + PREF_W);
        //setBorder(BorderFactory.createLineBorder(Color.blue));

        CreateRoomPanel createRoom = new CreateRoomPanel();
        add(createRoom);


        add(Box.createHorizontalStrut(50));
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 450));
        add(separator);

        JoinRoomPanel joinRoomPanel = new JoinRoomPanel();
        add(joinRoomPanel);


    }
}
