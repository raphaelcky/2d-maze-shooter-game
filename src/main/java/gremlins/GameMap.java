package gremlins;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GameMap {
    private int rows;
    private int cols;
    private Tile[][] grid;
    private PImage stoneImage;
    private PImage brickImage;
    private PImage gremlinImage;
    private PImage exitImage;

    private PImage[] brickDestructionImages; // Store destruction images

    public GameMap(PImage stoneImage, PImage brickImage, PImage gremlinImage, PImage exitImage, PImage[] brickDestructionImages) {
        this.stoneImage = stoneImage;
        this.brickImage = brickImage;
        this.gremlinImage = gremlinImage;
        this.exitImage = exitImage;
        this.brickDestructionImages = brickDestructionImages;
    }

    public void loadMap(String filePath) {
        System.out.println("Attempting to load map from: " + filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            this.rows = 33; // Fixed rows based on specification
            this.cols = 36; // Fixed columns based on specification
            this.grid = new Tile[rows][cols];

            for (int row = 0; row < rows; row++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("Unexpected end of file at row: " + row);
                }
                if (line.length() != cols) {
                    throw new IllegalArgumentException("Incorrect number of columns in row " + row + ": " + line.length());
                }
                for (int col = 0; col < cols; col++) {
                    char tileChar = line.charAt(col);
                    switch (tileChar) {
                        case Tile.STONE:
                            grid[row][col] = new Tile(Tile.STONE, stoneImage);
                            break;
                        case Tile.BRICK:
                            grid[row][col] = new Tile(Tile.BRICK, brickImage);
                            break;
                        case Tile.WIZARD:
                            grid[row][col] = new Tile(Tile.WIZARD, null);
                            break;
                        case Tile.GREMLIN:
                            grid[row][col] = new Tile(Tile.GREMLIN, gremlinImage);
                            break;
                        case Tile.EXIT:
                            grid[row][col] = new Tile(Tile.EXIT, exitImage);
                            break;
                        default:
                            grid[row][col] = new Tile(Tile.EMPTY, null);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading map file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void draw(PApplet app) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = grid[row][col];
                if (tile != null) {
                    // Check if the tile is fully destroyed
                    if (tile.isDestroyed()) {
                        grid[row][col] = new Tile(Tile.EMPTY, null); // Replace with an empty tile
                    } else {
                        tile.draw(app, col * App.SPRITESIZE, row * App.SPRITESIZE);
                    }
                }
            }
        }
    }
    
    public void triggerBrickDestruction(int gridX, int gridY) {
        Tile tile = this.grid[gridY][gridX];
        if (tile != null && tile.getType() == Tile.BRICK) {
            tile.startDestructionAnimation(this.brickDestructionImages); // Use stored images
        }
    }

    public Tile getTile(int row, int col) {
        if (row < 0 || col < 0 || row >= rows || col >= cols) {
            return null; // Out of bounds
        }
        return grid[row][col];
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
}