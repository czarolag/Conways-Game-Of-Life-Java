package game.main;

import game.component.*;
import java.awt.*;
import javax.swing.*;

public class Main extends JFrame {

    public Main() {

        init();
    }

    private void init() {

        setTitle("Game of Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        pack();
        Insets windowInsets = getInsets();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle bounds = ge.getMaximumWindowBounds();

        int screenWidth = bounds.width - windowInsets.left - windowInsets.right;
        int screenHeight = bounds.height - windowInsets.top - windowInsets.bottom;

        int uiWidth = Math.max(340, (int)(screenWidth * 0.20));
        int gameWidth = bounds.width - uiWidth;

        GamePanel game = new GamePanel(gameWidth, screenHeight);
        UIPanel ui = new UIPanel(game, uiWidth, screenHeight);

        add(game, BorderLayout.CENTER);
        add(ui, BorderLayout.EAST);

        setLocationRelativeTo(null);
    }



    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            main.setVisible(true);
        });
    }
}
