package graphics.gameFrame;

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
class OperationsPanel extends JPanel {

    private static final int PREF_W = 200;
    private static final int PREF_H = 400;
    private static final int REC_WIDTH = 100;
    private ArrayList<BufferedImage> squares = new ArrayList<BufferedImage>();
    private ArrayList<String> scores = new ArrayList<String>();

    public OperationsPanel(ArrayList<String> scores, Game24 game) {
        this.scores = scores;
        //setBorder(BorderFactory.createLineBorder(Color.black));


        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 2; i++) {
                addSquare(REC_WIDTH, REC_WIDTH, i * 2 + j, i, j, i * 2 + j);
            }
        }

        mouseListener(game);

    }

    public void addSquare(int width, int height, int number, int i, int j, int position) {
        BufferedImage rect;
        int posX = number % 4;
        int posY = number / 4;
        try {
            rect = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/images/operators.png"));
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();
            int h = rect.getHeight() / 1;
            int w = rect.getWidth() / 4;
                g.drawImage(rect.getSubimage(posX * w, posY * h, w, h), 0, 0, width, height, null);
            g.dispose();
            squares.add(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mouseListener(Game24 game) {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println(e.getX() + " Y " + e.getY());
                if (!game.check4for3()) {
                    if (e.getX() >= 0 && e.getX() <= 100) {
                        if (e.getY() >= 200 && e.getY() <= 300) {
                            System.out.println("Primeiro quadrado " + " eq " + game.getEquation());
                            game.setEquation(game.getEquation() + "+");
                            game.stateMachine("+");
                        } else if (e.getY() > 300 && e.getY() <= 400) {
                            System.out.println("Terceiro quadrado " + " eq " + game.getEquation());
                            game.setEquation(game.getEquation() + "/");
                            game.stateMachine("/");
                        }
                    } else if (e.getX() > 100 && e.getX() <= 200) {
                        if (e.getY() >= 200 && e.getY() <= 300) {
                            System.out.println("Segundo quadrado " + " eq " + game.getEquation());
                            game.setEquation(game.getEquation() + "-");
                            game.stateMachine("-");

                        } else if (e.getY() > 300 && e.getY() <= 400) {
                            System.out.println("Quarto quadrado " + " eq " + game.getEquation());
                            game.setEquation(game.getEquation() + "*");
                            game.stateMachine("*");

                        }
                    } else {
                        if (game.check24(game.getEquation())) {
                            scores.add("entrou");
                        }
                    }
                }

                GameFrame.getSouth().getEquation().setText("Equation: " + game.getEquation());
                GameFrame.getSouth().getEquation().paintImmediately(GameFrame.getSouth().getEquation().getVisibleRect());
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
                g2.drawImage(squares.get(i * 2 + j), i * REC_WIDTH, j * REC_WIDTH + REC_WIDTH * 2, null);
            }
        }
    }

}