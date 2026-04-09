package game.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;


public class GamePanel extends JPanel implements Runnable, MouseListener, MouseMotionListener, KeyListener {

    // set grid variables
    private int CELL_SIZE;
    private int COLS = 30; // default cols
    private int ROWS = 30; // default rows

    // https://www.color-hex.com/color-palette/1058379
    private static final int ALIVE = 0xC2FE0B;
    private static final int DEAD = 0x000000;
    private static final int GRID_COLOR = 0x404040;

    // grid data structure (current iteration, next iteration)
    private boolean[][] grid;
    private boolean[][] next_grid;

    // game variables
    private BufferedImage image;
    private int[] pixels;
    private boolean running = false;

    public GamePanel(int panelWidth, int panelHeight) {

        // calculate cell size
        this.CELL_SIZE = Math.min(panelWidth / COLS, panelHeight / ROWS);

        // init grids arrays
        this.grid = new boolean[COLS][ROWS];
        this.next_grid = new boolean[COLS][ROWS];

        // for image buffer
        int gridPixelWidth = COLS * CELL_SIZE;
        int gridPixelHeight = ROWS * CELL_SIZE;

        // set the window
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        this.image = new BufferedImage(gridPixelWidth, gridPixelHeight, BufferedImage.TYPE_INT_RGB);
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);

        new Thread(this).start();
    }


    // game loop
    public void run() {

        while (true) {
            if (this.running) {
                update();
            }

            render();
            repaint();

            try {
                Thread.sleep(60);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    // update grid
    private void update() {

        for (int col = 0; col < this.COLS; col++) {
            for (int row = 0; row < this.ROWS; row++) {

                // count number of neighbors
                int neighbors = countNeighbors(col, row);

                if (this.grid[col][row]) {
                    // if the cell neighbor count is 2 or 3, it survives
                    this.next_grid[col][row] = neighbors == 2 || neighbors == 3;
                } else {
                    // if cell has 3 neighbors, it becomes populated
                    this.next_grid[col][row] = neighbors == 3;
                }
            }
        }

        // swap grids
        boolean[][] temp = this.grid;
        this.grid = this.next_grid;
        next_grid = temp;
    }

    // check neighbor cells (infinite looping)
    private int countNeighbors(int col, int row) {

        int count = 0;

        for (int colDirection = -1; colDirection <= 1; colDirection++)
            for (int rowDirection = -1; rowDirection <= 1; rowDirection++) {

                // skip 0 (no movement)
                if (colDirection == 0 && rowDirection == 0) {
                    continue;
                }

                // calculate surrounding cell
                int newCol = (col + colDirection + this.COLS) % this.COLS;
                int newRow = (row + rowDirection + this.ROWS) % this.ROWS;

                // check if new cell is within the grid
                if (newCol >= 0 && newRow >= 0 && newCol < this.COLS && newRow < this.ROWS) {
                    // if new cell is within grid, it is a neighbor so add 1 to counter
                    if (this.grid[newCol][newRow]) {
                        count++;
                    }
                }
            }

        return count;
    }

    // write pixels to the BufferedImage
    private void render() {
        int width = this.image.getWidth();

        for (int row = 0; row < this.ROWS * this.CELL_SIZE; row++) {
            for (int col = 0; col < this.COLS * this.CELL_SIZE; col++) {

                int cellCol = col / this.CELL_SIZE;
                int cellRow = row / this.CELL_SIZE;

                int index = row * width + col;

                if (row % this.CELL_SIZE == 0 || col % this.CELL_SIZE == 0) {
                    this.pixels[index] = this.GRID_COLOR;
                } else {
                    this.pixels[index] = this.grid[cellCol][cellRow] ? this.ALIVE : this.DEAD;
                }
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
        int xOffset = (getWidth() - (COLS * CELL_SIZE)) / 2;
        int yOffset = (getHeight() - (ROWS * CELL_SIZE)) / 2;

        g.drawImage(this.image, xOffset, yOffset, null);
    }



    // toggle cell on or off (invert)
    private void toggleCell(int t_col, int t_row) {

        // match the offsets used in paintComponent
        int xOffset = (getWidth() - (COLS * CELL_SIZE)) / 2;
        int yOffset = (getHeight() - (ROWS * CELL_SIZE)) / 2;

        // adjust click coordinates
        int adjustedX = t_col - xOffset;
        int adjustedY = t_row - yOffset;

        // ignore clicks not in grid
        if (adjustedX < 0 || adjustedY < 0) return;

        int col = adjustedX / this.CELL_SIZE;
        int row = adjustedY / this.CELL_SIZE;

        if (row >= 0 && col >= 0 && row < this.COLS && col < this.ROWS) {
            this.grid[col][row] = !this.grid[col][row];
        }
    }

    // toggle simulation on or off
    public void togglePause() {
        this.running = !this.running;
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
            // if right key is pressed continue simulation by 1 iteration
            update();
        }
    }

    public void mouseReleased(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
}
