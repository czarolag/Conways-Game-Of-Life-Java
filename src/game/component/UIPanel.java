package game.component;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.TitledBorder;

public class UIPanel extends JPanel {

    private final GamePanel game;

    // UI fields
    private JTextField simField;
    private JTextField stepsField;
    private JTextField densityField;
    private JTextField fileField;

    // global theme colors
    private final Color bgDark = new Color(13, 14, 15);
    private final Color panelDark = new Color(24, 26, 28);
    private final Color textLight = new Color(245, 245, 245);
    private final Color accentBlue = new Color(0, 255, 204);
    private final Color accentRed = new Color(255, 0, 102);
    private final Font mainFont = new Font("Monospaced", Font.BOLD, 14);

    public UIPanel(GamePanel game, int width, int height) {

        this.game = game;

        setPreferredSize(new Dimension(width, height));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        buildUI();
        bindEvents();
    }

    private void buildUI() {

        setBackground(this.bgDark);
        setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));

        // title
        JLabel title = new JLabel("SIM_CONTROLS");
        title.setFont(new Font("Monospaced", Font.BOLD, 24));
        title.setForeground(this.textLight);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(title);
        add(Box.createVerticalStrut(25));

        // settings panel
        JPanel settings = new JPanel(new GridBagLayout());
        settings.setBackground(this.panelDark);

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(85, 85, 85)),
                "BATCH_PARAMETERS"
        );
        titledBorder.setTitleColor(this.accentBlue);
        titledBorder.setTitleFont(new Font("Monospaced", Font.BOLD, 12));

        settings.setBorder(BorderFactory.createCompoundBorder(
                titledBorder,
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // fields
        this.simField = createField("5");
        this.stepsField = createField("200");
        this.densityField = createField("0.15");
        this.fileField = createField("results");

        // rows
        addRow(settings, gbc, 0, "Simulations", this.simField);
        addRow(settings, gbc, 1, "Steps", this.stepsField);
        addRow(settings, gbc, 2, "Density", this.densityField);
        addRow(settings, gbc, 3, "Filename", this.fileField);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1;
        settings.add(Box.createVerticalGlue(), gbc);

        settings.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        add(settings);
        add(Box.createVerticalStrut(20));

        // buttons
        JButton runBatch = createButton("RUN BATCH", this.accentBlue, this.bgDark);
        JButton export = createButton("EXPORT RUN", this.accentBlue, this.bgDark);
        JButton start = createButton("START", this.accentBlue, this.bgDark);
        JButton skip = createButton("SKIP", this.accentBlue, this.bgDark);
        JButton reset = createButton("RESET", this.accentBlue, this.bgDark);
        JButton exit = createButton("EXIT", this.accentRed, this.textLight);

        add(runBatch);
        add(Box.createVerticalStrut(10));
        add(export);
        add(Box.createVerticalStrut(50));
        add(start);
        add(Box.createVerticalStrut(10));
        add(skip);
        add(Box.createVerticalStrut(10));
        add(reset);


        add(Box.createVerticalGlue());
        add(exit);

        // button actions
        // runBatch button
        runBatch.addActionListener(e -> {
            runBatch();
            this.game.requestFocusInWindow();
        });

        // export button
        export.addActionListener(e -> {
            exportRun();
            this.game.requestFocusInWindow();
        });

        // start button
        start.addActionListener(e -> {
            this.game.togglePause();
            this.game.requestFocusInWindow();
        });

        // skip button
        skip.addActionListener(e -> {
            this.game.skip();
            this.game.requestFocusInWindow();
        });

        // reset button
        reset.addActionListener(e -> {
            this.game.reset();
            this.game.requestFocusInWindow();
        });

        // exit button
        exit.addActionListener(e -> System.exit(0));

        this.game.setOnStateChange(() -> SwingUtilities.invokeLater(() -> {
            if (this.game.isRunning()) {
                start.setText("STOP");
                start.setBackground(this.accentRed);
                start.setForeground(this.textLight);
            } else {
                start.setText("START");
                start.setBackground(this.accentBlue);
                start.setForeground(this.bgDark);
            }
        }));
    }

    // re-focus screen when background is pressed
    private void bindEvents() {

        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                game.requestFocusInWindow();
            }
        });
    }

    // logic
    private void runBatch() {

        new Thread(() -> {
            try {
                int sims = Integer.parseInt(this.simField.getText().trim());
                int steps = Integer.parseInt(this.stepsField.getText().trim());
                double density = Double.parseDouble(this.densityField.getText().trim());

                String filename = fileField.getText().trim();
                if (filename.isEmpty()) {
                    filename = "results.csv";
                }

                if (!filename.endsWith(".csv")) {
                    filename += ".csv";
                }

                this.game.runBatchSimulations(sims, steps, density, filename);
                final String finalFilename = filename;

                SwingUtilities.invokeLater(() ->
                        showThemedMessage("BATCH COMPLETE", "Exported Batch: " + finalFilename, this.accentBlue)
                );

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                                showThemedMessage("ERROR", "INVALID INPUT VALUES", this.accentRed)
                );
            }
        }).start();
    }

    private void exportRun() {

        new Thread(() -> {
            String filename = this.fileField.getText().trim();

            if (filename.isEmpty()) {
                filename = "results.csv";
            }

            if (!filename.endsWith(".csv")) {
                filename += ".csv";
            }

            this.game.exportRun(filename);
            final String finalFilename = filename;

            SwingUtilities.invokeLater(() ->
                showThemedMessage("EXPORT COMPLETE", "Exported current run: " + finalFilename, this.accentBlue)
            );
        }).start();
    }

     // UI helper methods
    private JTextField createField(String text) {

        JTextField f = new JTextField(text);
        f.setFont(this.mainFont);
        f.setBackground(new Color(18, 20, 22));
        f.setForeground(this.accentBlue);
        f.setCaretColor(this.accentRed);

        f.setPreferredSize(new Dimension(0, 32));

        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        return f;
    }

    private JButton createButton(String text, Color bg, Color fg) {

        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 2),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));

        // hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            Color currentBg;
            Color currentFg;

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                currentBg = b.getBackground();
                currentFg = b.getForeground();

                if (currentBg.equals(panelDark)) {
                    b.setBackground(new Color(85, 85, 85));
                    b.setForeground(Color.WHITE);
                } else {
                    b.setBackground(currentFg);
                    b.setForeground(currentBg);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // restore
                b.setBackground(currentBg);
                b.setForeground(currentFg);
            }
        });

        return b;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, JComponent field) {

        JLabel label = new JLabel(labelText + ":");
        label.setForeground(new Color(150, 150, 150));
        label.setFont(this.mainFont);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    // display messages to user
    private void showThemedMessage(String title, String message, Color accentColor) {

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(this.panelDark);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // message
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(this.mainFont);
        messageLabel.setForeground(this.textLight);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        // ok button
        JButton okButton = createButton("OKAY", accentColor, bgDark);
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(this.panelDark);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
