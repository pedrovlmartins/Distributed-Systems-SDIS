
package graphics.gameFrame;

import connections.tcp.TCPClient;
import game.Game24;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by inesa on 19/05/2016.
 */
public class NumbersPanel extends JPanel {
    private static final int PREF_W = 400;
    private static final int PREF_H = 400;
    private static final int REC_WIDTH = 200;
    private ArrayList<BufferedImage> squares = new ArrayList<BufferedImage>();
    private ArrayList<Integer> challenges = new ArrayList<>();
    private Game24 game;
    private static NumbersPanel instance;



    public NumbersPanel(ArrayList<Integer> challenges, Game24 game) {
        this.game = game;
        this.challenges = challenges;
        // setBorder(BorderFactory.createLineBorder(Color.red));
        setPreferredSize(new Dimension(PREF_W, PREF_H));
        instance = this;

        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 2; i++) {
                addSquare(REC_WIDTH, REC_WIDTH, this.challenges.get(i * 2 + j), i * 2 + j);
            }
        }

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getX() >= 0 && e.getX() <= 200) {
                    if (e.getY() >= 0 && e.getY() <= 200) {
                        game.setEquation(game.getEquation() + String.valueOf(challenges.get(0)));
                        game.stateMachine(String.valueOf(challenges.get(0)));
                    } else if (e.getY() > 200 && e.getY() <= 400) {
                        game.setEquation(game.getEquation() + String.valueOf(challenges.get(2)));
                        game.stateMachine(String.valueOf(challenges.get(2)));
                    }
                } else if (e.getX() > 200 && e.getX() <= 400) {
                    if (e.getY() >= 0 && e.getY() <= 200) {
                        game.setEquation(game.getEquation() + String.valueOf(challenges.get(1)));
                        game.stateMachine(String.valueOf(challenges.get(1)));

                    } else if (e.getY() > 200 && e.getY() <= 400) {
                        game.setEquation(game.getEquation() + String.valueOf(challenges.get(3)));
                        game.stateMachine(String.valueOf(challenges.get(3)));
                    }
                }
                GameFrame.getSouth().getEquation().setText("Equation: " + game.getEquation());
                GameFrame.getSouth().getEquation().paintImmediately(GameFrame.getSouth().getEquation().getVisibleRect());
                if ( game.check24(game.getEquation())) {
                    TCPClient.getInstance().setWinner(game.getEquation());
                } else {
                    System.out.println(game.getEquation() + " it's not 24");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        System.out.println(this.toString());
    }

    public static NumbersPanel getInstance() {
        return instance;
    }

    public Game24 getGame() {
        return game;
    }

    public void addSquare(int width, int height, int number, int position) {
        BufferedImage rect;
        BufferedImage whiteRect;
        int posX = number % 5;
        int posY = number / 5;
        try {
            rect = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/images/all.png"));
            whiteRect = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/images/allwhite.png"));
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();
            int h = rect.getHeight() / 2;
            int w = rect.getWidth() / 5;
            if(position == 1 || position == 2)
                g.drawImage(rect.getSubimage(posX * w, posY * h, w, h), 0, 0, width, height, null);
            else
                g.drawImage(whiteRect.getSubimage(posX * w, posY * h, w, h), 0, 0, width, height, null);
            g.dispose();
            squares.add(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREF_W, PREF_H);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                g2.drawImage(squares.get(i * 2 + j), i * REC_WIDTH, j * REC_WIDTH, null);
            }
        }
    }

    public void resetEquation() {
        game.resetEquation();
        GameFrame.getSouth().getEquation().setText("Equation: " + game.getEquation());
        GameFrame.getSouth().getEquation().paintImmediately(GameFrame.getSouth().getEquation().getVisibleRect());
    }

}