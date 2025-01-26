package gremlins;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GameMap {
    private int rows;
    private int cols;
    private Tile[][] grid;
    private PImage stoneImage;
    private PImage brickImage;
    private PImage gremlinImage;
    private PImage slimeImage;
    private PImage exitImage;

    private PImage[] brickDestructionImages; // Destruction images for bricks
    private ArrayList<Gremlin> gremlins; // List of gremlins
    private int slimeCooldown;
    private App app;
    
    private Random random = new Random();

    public GameMap(App app, PImage stoneImage, PImage brickImage, PImage gremlinImage, PImage slimeImage, PImage exitImage, PImage[] brickDestructionImages, int slimeCooldown) {
        this.app = app;
        this.stoneImage = stoneImage;
        this.brickImage = brickImage;
        this.gremlinImage = gremlinImage;
        this.slimeImage = slimeImage;
        this.exitImage = exitImage;
        this.brickDestructionImages = brickDestructionImages;
        this.slimeCooldown = slimeCooldown;
        this.gremlins = new ArrayList<>();
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
                            grid[row][col] = new Tile(Tile.EMPTY, null); // Initialize gremlins separately
                            gremlins.add(new Gremlin(col * App.SPRITESIZE, row * App.SPRITESIZE, gremlinImage, slimeImage, slimeCooldown));
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
        // Draw gremlins
        for (Gremlin gremlin : gremlins) {
            gremlin.draw(app);
        }
    }

    public void updateGremlins(Wizard wizard) {
        for (Gremlin gremlin : gremlins) {
            gremlin.update(this, wizard.getX(), wizard.getY());
            
            // Check collision with fireballs
            for (Fireball fireball : wizard.getFireballs()) {
                if (gremlin.checkFireballCollision(fireball)) {
                    fireball.setExpired(true); // Remove fireball
                    gremlin.respawn(this, wizard.getX(), wizard.getY()); // Respawn gremlin
                    break;
                }
            }
    
            // Check collision with wizard
            if (Math.abs(gremlin.getX() - wizard.getX()) < App.SPRITESIZE &&
                Math.abs(gremlin.getY() - wizard.getY()) < App.SPRITESIZE) {
                App.resetLevel();
                return;
            }
        }
    
        // Check collision between fireball and slime
        ArrayList<Slime> expiredSlimes = new ArrayList<>();
        for (Gremlin gremlin : gremlins) {
            for (Slime slime : gremlin.getSlimes()) {
                for (Fireball fireball : wizard.getFireballs()) {
                    if (Math.abs(fireball.getX() - slime.getX()) < App.SPRITESIZE &&
                        Math.abs(fireball.getY() - slime.getY()) < App.SPRITESIZE) {
                        fireball.setExpired(true);
                        slime.setExpired(true);
                        expiredSlimes.add(slime);
                    }
                }
            }
        }
        for (Gremlin gremlin : gremlins) {
            gremlin.getSlimes().removeAll(expiredSlimes);
        }
    
        // Check collision between slime and wizard
        for (Gremlin gremlin : gremlins) {
            for (Slime slime : gremlin.getSlimes()) {
                if (Math.abs(slime.getX() - wizard.getX()) < App.SPRITESIZE &&
                    Math.abs(slime.getY() - wizard.getY()) < App.SPRITESIZE) {
                    App.resetLevel();
                    return;
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

    public boolean isPlayerCollidingWithGremlinOrSlime(Wizard wizard) {
        // Check collision with gremlins
        for (Gremlin gremlin : gremlins) {
            if (Math.abs(gremlin.getX() - wizard.getX()) < App.SPRITESIZE &&
                Math.abs(gremlin.getY() - wizard.getY()) < App.SPRITESIZE) {
                return true; // Wizard collides with a gremlin
            }
        }
    
        // Check collision with slimes
        for (Gremlin gremlin : gremlins) {
            for (Slime slime : gremlin.getSlimes()) {
                if (Math.abs(slime.getX() - wizard.getX()) < App.SPRITESIZE &&
                    Math.abs(slime.getY() - wizard.getY()) < App.SPRITESIZE) {
                    return true; // Wizard collides with a slime
                }
            }
        }
    
        return false; // No collisions detected
    }

    public ArrayList<int[]> getEmptyTilesFarFrom(int wizardX, int wizardY, int minDistance) {
        ArrayList<int[]> emptyTiles = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col].getType() == Tile.EMPTY) {
                    int tileX = col * App.SPRITESIZE;
                    int tileY = row * App.SPRITESIZE;
                    double distance = Math.sqrt(Math.pow(tileX - wizardX, 2) + Math.pow(tileY - wizardY, 2));
                    if (distance >= minDistance * App.SPRITESIZE) {
                        emptyTiles.add(new int[]{col, row});
                    }
                }
            }
        }
        return emptyTiles;
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

    public App getApp() {
        return app;
    }
}