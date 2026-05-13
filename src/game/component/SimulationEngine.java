package game.component;

import java.io.*;
import java.util.*;

public class SimulationEngine {

    // grid variables
    private int COLS;
    private int ROWS;

    // grid data structure (current iteration, next iteration)
    private boolean[][] grid;
    private boolean[][] nextGrid;

    // stats
    private int generation;
    private int liveCount;
    private int births;
    private int deaths;
    private int growthRate;

    private List<String> statsLog = Collections.synchronizedList(new ArrayList<>());

    // misc
    private Random rand = new Random();
    private volatile boolean batchMode = false;

    public SimulationEngine(int cols, int rows) {
        this.COLS = cols;
        this.ROWS = rows;

        this.grid = new boolean[this.COLS][this.ROWS];
        this.nextGrid = new boolean[this.COLS][this.ROWS];
    }

    // update grid
    public void update() {

        int newLiveCount = 0;
        int newBirths = 0;
        int newDeaths = 0;

        for (int col = 0; col < this.COLS; col++) {
            for (int row = 0; row < this.ROWS; row++) {

                int neighbors = countNeighbors(col, row);

                boolean alive = this.grid[col][row];

                // rules of conways game of life
                boolean nextAlive = alive ? (neighbors == 2 || neighbors == 3):(neighbors == 3);

                this.nextGrid[col][row] = nextAlive;

                // stats tracking
                if (nextAlive) {
                    newLiveCount++;
                }

                if (!alive && nextAlive) {
                    newBirths++;
                }

                if (alive && !nextAlive) {
                    newDeaths++;
                }

            }
        }

        // swap grids
        boolean[][] temp = this.grid;
        this.grid = this.nextGrid;
        this.nextGrid = temp;

        // update stats
        this.growthRate = newLiveCount - this.liveCount;
        this.liveCount = newLiveCount;
        this.births = newBirths;
        this.deaths = newDeaths;
        this.generation++;
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

    // randomize grid with density amount
    public void randomize(double density) {

        for (int col = 0; col < this.COLS; col++) {
            for (int row = 0; row < this.ROWS; row++) {
                this.grid[col][row] = rand.nextDouble() < density;
                this.nextGrid[col][row] = false;
            }
        }

        resetStats();
    }

    // reset simulation stats
    public void resetStats() {

        this.generation = 0;
        this.liveCount = 0;
        this.births = 0;
        this.deaths = 0;
        this.growthRate = 0;
    }

    // reset run
    public void clearRun(boolean resetVisuals) {

        resetStats();
        this.statsLog.clear();

        if (resetVisuals) {
            for (boolean[] row: this.grid) {
                Arrays.fill(row, false);
            }
        }
    }

    // record stats in csv format
    public void recordStats() {

        String row = String.format("%s,%s,%s,%s", this.generation, this.liveCount, this.births, this.deaths);
        this.statsLog.add(row);
    }

    // save simulation of current run
    public void exportRun(String filename) {

        // output to current directory in 'output' folder
        String basePath = System.getProperty("user.dir");
        File outputDir = new File(basePath, "output");

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Failed to create output directory: " + outputDir.getAbsolutePath());
            return;
        }

        File file = new File(outputDir, filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            writer.println("generation,live,births,deaths");

            for (String row: this.statsLog) {
                writer.println(row);
            }

            System.out.println("Exported current run: " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // run batch simulations (no UI)
    public void runBatchSimulations (int numSimulations, int steps, double density, String filename) {

        this.batchMode = true;


        // output to current directory in 'output' folder
        String basePath = System.getProperty("user.dir");
        File outputDir = new File(basePath, "output");

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Failed to create output directory: " + outputDir.getAbsolutePath());
            return;
        }

        File file = new File(outputDir, filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            // csv header
            writer.println("simulation,generation,live,births,deaths");

            for (int sim = 0; sim < numSimulations; sim++) {

                resetStats();
                randomize(density);

                for (int step = 0; step < steps; step++) {

                    update();

                    // write row
                    writer.printf("%d,%d,%d,%d,%d%n",
                            sim,
                            this.generation,
                            this.liveCount,
                            this.births,
                            this.deaths
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.batchMode = false;
    }



    // getters for GamePanel
    public boolean[][] getGrid() {

        return this.grid;
    }

    public int getGeneration() {

        return this.generation;
    }

    public int getLiveCount() {

        return this.liveCount;
    }

    public int getBirths() {

        return this.births;
    }

    public int getDeaths() {

        return this.deaths;
    }

    public int getGrowthRate() {

        return this.growthRate;
    }

    public boolean getBatchMode() {

        return this.batchMode;
    }
}
