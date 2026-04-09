package game.main;

import game.component.GamePanel;
import java.awt.*;
import javax.swing.*;

public class Main extends JFrame {

    public Main() {
        init();
    }

    private void init() {

        setTitle("Conway's Game of Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // full screen setup
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // calculate sizes
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        // ui size (20% of right side)
        int uiWidth = Math.max(300, (int)(screenWidth * 0.20));
        int gameWidth = screenWidth - uiWidth;

        GamePanel game = new GamePanel(gameWidth, screenHeight);
        add(game, BorderLayout.CENTER);

        // UI panel
        JPanel uiPanel = new JPanel();
        uiPanel.setPreferredSize(new Dimension(uiWidth, screenHeight));
        uiPanel.setBackground(Color.DARK_GRAY);
        uiPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));

        // UI components
        JLabel title = new JLabel("Controls");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        uiPanel.add(title);

        JButton playButton = new JButton("Play / Pause");
        playButton.setPreferredSize(new Dimension(200, 50));
        playButton.setFont(new Font("Arial", Font.BOLD, 18));
        playButton.addActionListener(e -> {
            game.requestFocusInWindow(); // Give focus back to the game for hotkeys
        });
        uiPanel.add(playButton);

        JButton exitButton = new JButton("Exit Game");
        exitButton.setPreferredSize(new Dimension(200, 50));
        exitButton.setFont(new Font("Arial", Font.BOLD, 18));
        exitButton.addActionListener(e -> System.exit(0));
        uiPanel.add(exitButton);

        // add the UI panel to the right side of the window
        add(uiPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            main.setVisible(true);
        });
    }
}
