package gremlins;

import processing.core.PApplet;
import processing.core.PImage;

public class Slime {
    private int x, y;
    private int speed = 4;
    private String direction;
    private boolean expired;
    private PImage image;

    public Slime(int x, int y, String direction, PImage image) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.image = image;
        this.expired = false;
    }

    public void draw(PApplet app) {
        app.image(this.image, this.x, this.y);
    }

    public void update(GameMap map) {
        // Move slime in the current direction
        switch (this.direction) {
            case "up":
                this.y -= speed;
                break;
            case "down":
                this.y += speed;
                break;
            case "left":
                this.x -= speed;
                break;
            case "right":
                this.x += speed;
                break;
        }
        
        // Convert current position to grid coordinates
        int currentGridX = this.x / App.SPRITESIZE;
        int currentGridY = this.y / App.SPRITESIZE;

        if (this.direction.equals("up")) {
            currentGridY++;
        } else if (this.direction.equals("left")) {
            currentGridX++;
        }
    
        // Determine the next grid coordinates (based on direction)
        int nextGridX = currentGridX;
        int nextGridY = currentGridY;
    
        switch (this.direction) {
            case "up":
                nextGridY--;
                break;
            case "down":
                nextGridY++;
                break;
            case "left":
                nextGridX--;
                break;
            case "right":
                nextGridX++;
                break;
        }

        Tile nextTile = map.getTile(nextGridY, nextGridX);
        if (nextTile != null && (nextTile.getType() == Tile.STONE || nextTile.getType() == Tile.BRICK)) {
            this.expired = true; // Slime stops when it hits a wall
        }

        // Mark slime as expired if out of bounds
        if (this.x < 0 || this.y < 0 || this.x >= App.WIDTH || this.y >= App.HEIGHT - App.BOTTOMBAR) {
            this.expired = true;
        }
    }

    public boolean isExpired() {
        return this.expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}