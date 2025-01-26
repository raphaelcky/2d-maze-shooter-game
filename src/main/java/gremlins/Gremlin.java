package gremlins;

import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;
import java.util.Random;

public class Gremlin {
    private int x, y; // Current position in pixels
    private int tileX, tileY; // Grid position
    private int speed = 1; // Movement speed
    private String direction; // Current direction of movement
    private int moveCooldown = 30; // Time in frames between movements
    private int moveTimer = 0; // Timer for movement
    private int slimeCooldown; // Cooldown between slime shots (frames)
    private int slimeTimer = 0; // Timer for shooting slime
    private boolean moving; // Track if gremlin is currently moving
    private int targetX, targetY; // Target pixel coordinates


    private PImage image;
    private PImage slimeImage;

    private ArrayList<Slime> slimes; // Active slime projectiles
    private Random random = new Random();

    public Gremlin(int x, int y, PImage image, PImage slimeImage, int slimeCooldown) {
        this.x = x;
        this.y = y;
        this.tileX = x / App.SPRITESIZE;
        this.tileY = y / App.SPRITESIZE;
        this.targetX = this.x;
        this.targetY = this.y;
        this.image = image;
        this.slimeImage = slimeImage;
        this.slimeCooldown = slimeCooldown;
        this.slimes = new ArrayList<>();
        this.direction = "down"; // Default initial direction
    }

    public void draw(PApplet app) {
        app.image(this.image, this.x, this.y);
        for (Slime slime : slimes) {
            slime.draw(app);
        }
    }

    public void update(GameMap map, int wizardX, int wizardY) {
        move(map);

        // Handle slime shooting
        if (slimeTimer > 0) {
            slimeTimer--;
        } else {
            shootSlime();
        }

        // Update slimes
        slimes.removeIf(Slime::isExpired);
        for (Slime slime : slimes) {
            slime.update(map);
        }
    }

    private void move(GameMap map) {
        // Smoothly move toward the target pixel
        if (this.x < this.targetX) {
            this.x += speed;
        } else if (this.x > this.targetX) {
            this.x -= speed;
        }
    
        if (this.y < this.targetY) {
            this.y += speed;
        } else if (this.y > this.targetY) {
            this.y -= speed;
        }
    
        // If the Gremlin reaches the target, calculate the next tile
        if (this.x == this.targetX && this.y == this.targetY) {
            // Update tile position
            this.tileX = this.targetX / App.SPRITESIZE;
            this.tileY = this.targetY / App.SPRITESIZE;
    
            // Determine next tile based on current direction
            int nextTileX = tileX;
            int nextTileY = tileY;
    
            switch (direction) {
                case "up":
                    nextTileY--;
                    break;
                case "down":
                    nextTileY++;
                    break;
                case "left":
                    nextTileX--;
                    break;
                case "right":
                    nextTileX++;
                    break;
            }
    
            Tile nextTile = map.getTile(nextTileY, nextTileX);
            if (nextTile == null || nextTile.getType() == Tile.STONE || nextTile.getType() == Tile.BRICK) {
                // Collision: Choose a new valid direction
                ArrayList<String> possibleDirections = getPossibleDirections(map, tileX, tileY);
                possibleDirections.remove(getOppositeDirection(direction));
    
                // If no valid directions are left, allow reversing direction
                if (possibleDirections.isEmpty()) {
                    possibleDirections = getPossibleDirections(map, tileX, tileY);
                }
    
                // Pick a new direction randomly if possible
                if (!possibleDirections.isEmpty()) {
                    direction = possibleDirections.get(random.nextInt(possibleDirections.size()));
                } else {
                    // If completely stuck, stay in place
                    return;
                }
            }
    
            // Set target pixel for smooth movement
            switch (direction) {
                case "up":
                    this.targetX = tileX * App.SPRITESIZE;
                    this.targetY = (tileY - 1) * App.SPRITESIZE;
                    break;
                case "down":
                    this.targetX = tileX * App.SPRITESIZE;
                    this.targetY = (tileY + 1) * App.SPRITESIZE;
                    break;
                case "left":
                    this.targetX = (tileX - 1) * App.SPRITESIZE;
                    this.targetY = tileY * App.SPRITESIZE;
                    break;
                case "right":
                    this.targetX = (tileX + 1) * App.SPRITESIZE;
                    this.targetY = tileY * App.SPRITESIZE;
                    break;
            }
        }
    }

    private ArrayList<String> getPossibleDirections(GameMap map, int tileX, int tileY) {
        ArrayList<String> directions = new ArrayList<>();
        if (map.getTile(tileY - 1, tileX) != null && map.getTile(tileY - 1, tileX).getType() == Tile.EMPTY) {
            directions.add("up");
        }
        if (map.getTile(tileY + 1, tileX) != null && map.getTile(tileY + 1, tileX).getType() == Tile.EMPTY) {
            directions.add("down");
        }
        if (map.getTile(tileY, tileX - 1) != null && map.getTile(tileY, tileX - 1).getType() == Tile.EMPTY) {
            directions.add("left");
        }
        if (map.getTile(tileY, tileX + 1) != null && map.getTile(tileY, tileX + 1).getType() == Tile.EMPTY) {
            directions.add("right");
        }
        return directions;
    }

    private String getOppositeDirection(String direction) {
        switch (direction) {
            case "up":
                return "down";
            case "down":
                return "up";
            case "left":
                return "right";
            case "right":
                return "left";
            default:
                return null;
        }
    }

    private void shootSlime() {
        Slime slime = new Slime(this.x, this.y, this.direction, slimeImage);
        slimes.add(slime);
        slimeTimer = slimeCooldown;
    }

    public boolean checkFireballCollision(Fireball fireball) {
        // Fireball collision with gremlin
        return fireball.getX() / App.SPRITESIZE == this.tileX && fireball.getY() / App.SPRITESIZE == this.tileY;
    }

    public void respawn(GameMap map, int wizardX, int wizardY) {
        // Find a random empty tile at least 10 tiles away from the wizard
        ArrayList<int[]> possibleTiles = map.getEmptyTilesFarFrom(wizardX, wizardY, 10);
        if (!possibleTiles.isEmpty()) {
            int[] respawnTile = possibleTiles.get(random.nextInt(possibleTiles.size()));
            this.tileX = respawnTile[0];
            this.tileY = respawnTile[1];
            this.x = tileX * App.SPRITESIZE;
            this.y = tileY * App.SPRITESIZE;
            this.targetX = this.x;
            this.targetY = this.y;
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public ArrayList<Slime> getSlimes() {
        return this.slimes;
    }
}