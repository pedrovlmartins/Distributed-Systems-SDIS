package graphics.gameFrame;

import connections.tcp.TCPClient;
import game.Game24;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by inesa on 19/05/2016.
 */
public class CenterPanel extends JPanel {

    private static final int SPACE = 50;
    static CenterPanel instance;
    private Game24 g;

    public CenterPanel(TCPClient TCPClient, Game24 g) {
        this.g = g;
        addComponents(g);
        instance = this;
    }

    private void addComponents(Game24 g) {
        ArrayList<String> scores = new ArrayList<String>();
        NumbersPanel numbersPanel = new NumbersPanel(TCPClient.getInstance().getCurrentGame(), g);
        OperationsPanel operationsPanel = new OperationsPanel(scores, g);
        add(numbersPanel);
        add(Box.createHorizontalStrut(SPACE));
        add(operationsPanel);
    }

    public static CenterPanel getInstance() {
        return instance;
    }

    public void updateNumbersPanel() {
        g.resetEquation();
        removeAll();
        addComponents(g);
        repaint();
        revalidate();
        GameFrame.getInstance().repaint();
        GameFrame.getInstance().revalidate();
    }
}
