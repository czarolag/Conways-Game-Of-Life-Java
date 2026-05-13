package game.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class GamePanel extends JPanel implements Runnable, MouseListener, MouseMotionListener, KeyListener {

    // SimulationEngine (handle grid logic)
    private SimulationEngine engine;

    // grid variables
    private int CELL_SIZE;
    private int COLS = 50;
    private int ROWS = 50;

    // https://www.color-hex.com/color-palette/1058379
    private static final int ALIVE = 0xC2FE0B;
    private static final int DEAD = 0x000000;
    private static final int GRID_COLOR = 0x404040;

    // game variables
    private BufferedImage image;
    private int[] pixels;
    private boolean running = false;
    private static final int PADDING_TOP = 40;
    private static final int PADDING_BOTTOM = 40;
    private Runnable onStateChange;

    public GamePanel(int panelWidth, int panelHeight) {

        // calculate cell size
        this.CELL_SIZE = Math.min(panelWidth / this.COLS, panelHeight / this.ROWS);

        this.engine = new SimulationEngine(this.COLS, this.ROWS);

        // for image buffer
        int gridPixelWidth = this.COLS * this.CELL_SIZE;
        int gridPixelHeight = this.ROWS * this.CELL_SIZE;

        // set the window
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        this.image = new BufferedImage(gridPixelWidth, gridPixelHeight, BufferedImage.TYPE_INT_RGB);
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);

        Thread simThread = new Thread(this, "SimulationLoop");
        simThread.setDaemon(true);
        simThread.start();
    }


    // game loop
    public void run() {

        while (true) {
            if (!engine.getBatchMode() && this.running) {
                engine.update();
                engine.recordStats();
            }

            if (!engine.getBatchMode()) {
                render();
                repaint();
            }

            try {
                Thread.sleep(60);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // write pixels to the BufferedImage
    private void render() {

        int width = this.image.getWidth();

        for (int row = 0; row < this.ROWS * this.CELL_SIZE; row++) {
            for (int col = 0; col < this.COLS * this.CELL_SIZE; col++) {

                int cellCol = col / this.CELL_SIZE;
                int cellRow = row / this.CELL_SIZE;

                int index = row * width + col;

                this.pixels[index] = engine.getGrid()[cellCol][cellRow] ? this.ALIVE:this.DEAD;
            }
        }
    }

    // paint BufferedImage to screen
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        // fill background with black
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // calculate offsets to center the image
        int xOffset = getXOffset();
        int yOffset = getYOffset();

        g.drawImage(this.image, xOffset, yOffset, null);

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(this.GRID_COLOR));
        g2.setStroke(new BasicStroke(1));

        // vertical lines
        for (int col = 0; col < this.COLS; col++) {
            int x = xOffset + col * this.CELL_SIZE;
            g2.drawLine(x, yOffset, x, yOffset + this.ROWS * this.CELL_SIZE);
        }

        // horizontal lines
        for (int row = 0; row < this.ROWS; row++) {
            int y = yOffset + row * this.CELL_SIZE;
            g2.drawLine(xOffset, y, xOffset + this.COLS * this.CELL_SIZE, y);
        }

        // borders
        g2.drawRect(xOffset, yOffset, this.COLS * this.CELL_SIZE, this.ROWS * this.CELL_SIZE);

        // draw stats
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));

        int textX = 20;
        int textY = 30;

        g.drawString("Generation: " + this.engine.getGeneration(), textX, textY);
        g.drawString("Live Cells: " + this.engine.getLiveCount(), textX, textY + 20);
        g.drawString("Births: " + this.engine.getBirths(), textX, textY + 40);
        g.drawString("Deaths: " + this.engine.getDeaths(), textX, textY + 60);
        g.drawString("Growth: " + this.engine.getGrowthRate(), textX, textY + 80);
    }



    // toggle cell on or off (invert)
    private void toggleCell(int t_col, int t_row) {

        // don't allow toggle cell if in batch mode
        if (this.engine.getBatchMode()) {
            return;
        }

        // match the offsets used in paintComponent
        int xOffset = getXOffset();
        int yOffset = getYOffset();

        // adjust click coordinates
        int adjustedX = t_col - xOffset;
        int adjustedY = t_row - yOffset;

        // ignore clicks not in grid
        if (adjustedX < 0 || adjustedY < 0) return;

        int col = adjustedX / this.CELL_SIZE;
        int row = adjustedY / this.CELL_SIZE;

        if (col >= 0 && row >= 0 && col < this.COLS && row < this.ROWS) {
            boolean[][] grid = this.engine.getGrid();
            grid[col][row] = !grid[col][row];
        }
    }

    // get XOffset
    private int getXOffset() {

        return (getWidth() - (this.COLS * this.CELL_SIZE)) / 2;
    }

    // get YOffset
    private int getYOffset() {

        int usableHeight = getHeight() - this.PADDING_TOP - this.PADDING_BOTTOM;
        return this.PADDING_TOP + (usableHeight - (this.ROWS * this.CELL_SIZE)) / 2;
    }



    // toggle simulation on or off
    public void togglePause() {

        this.running = !this.running;

        if (onStateChange != null) {
            SwingUtilities.invokeLater(this.onStateChange);
        }
    }

    // go next one generation (only works if paused)
    public void skip() {

        if (this.engine.getBatchMode() && this.running) {
            return;
        }

        this.engine.update();
    }

    // reset simulation
    public void reset() {

        if (this.engine.getBatchMode()) {
            return;
        }

        this.running = false;
        this.engine.clearRun(true);

        if (onStateChange != null) {
            SwingUtilities.invokeLater(this.onStateChange);
        }
    }

    // handle user inputs
    public void mouseClicked(MouseEvent e) {

        toggleCell(e.getX(), e.getY());
    }

    public void mouseDragged(MouseEvent e) {

        toggleCell(e.getX(), e.getY());
    }

    public void keyPressed(KeyEvent e) {

        // if space is pressed, continue or pause simulation
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            togglePause();
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            skip();
        }
    }
    
    public void mousePressed(MouseEvent e) {

        requestFocusInWindow();
    }

    public void setOnStateChange(Runnable listener) {
        this.onStateChange = listener;
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    // passthrough method for batch simulations
    public void runBatchSimulations(int sims, int steps, double density, String filename) {

        this.running = false;

        this.engine.runBatchSimulations(sims, steps, density, filename);
        repaint();
    }

    // export current run as csv
    public void exportRun(String filename) {

        this.engine.exportRun(filename);
    }

    // getter for simulation running state
    public boolean isRunning() {

        return this.running;
    }
}
