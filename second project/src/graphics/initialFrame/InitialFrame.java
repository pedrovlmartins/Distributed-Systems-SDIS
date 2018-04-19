package graphics.initialFrame;

import graphics.gameFrame.NorthPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by inesa on 22/05/2016.
 */
public class InitialFrame extends JFrame {

    static GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    public static int width = gd.getDisplayMode().getWidth();
    public static int height = gd.getDisplayMode().getHeight();


    public InitialFrame() throws HeadlessException {
        super("Jogo 24");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        CenterInitialPanel centerPanel = new CenterInitialPanel();
        setLayout(new BorderLayout());
        NorthPanel north = new NorthPanel();
        Panel east = new Panel();
        east.add(Box.createHorizontalStrut((int)Math.floor(width/10)));
        Panel west = new Panel();
        west.add(Box.createHorizontalStrut((int)Math.floor(width/10)));
        Panel south = new Panel();
        south.add(Box.createVerticalStrut((int)Math.floor(width/10)));

        getContentPane().add(north, BorderLayout.PAGE_START);
        getContentPane().add(west, BorderLayout.WEST);
        getContentPane().add(south, BorderLayout.SOUTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(east, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
