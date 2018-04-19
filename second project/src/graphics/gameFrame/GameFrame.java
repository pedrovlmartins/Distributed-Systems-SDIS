package graphics.gameFrame;

import connections.tcp.TCPClient;
import game.Game24;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    static South south;

    static GameFrame instance;

    public GameFrame(TCPClient TCPClient, Game24 g) {
        super("Jogo 24");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        CenterPanel centerPanel = new CenterPanel(TCPClient, g);
        setLayout(new BorderLayout());

        NorthPanel north = new NorthPanel();
        Chat east = new Chat(TCPClient, g);
        east.add(Box.createHorizontalStrut(100));
        Panel west = new Panel();

        west.add(Box.createHorizontalStrut(10));
        south = new South(TCPClient, g);
        south.add(Box.createVerticalStrut(100));

        getContentPane().add(north, BorderLayout.PAGE_START);
        getContentPane().add(west, BorderLayout.WEST);
        getContentPane().add(south, BorderLayout.SOUTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(east, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        instance = this;
    }

    public static GameFrame getInstance() {
        return instance;
    }

    public static South getSouth() {
        return south;
    }
}

