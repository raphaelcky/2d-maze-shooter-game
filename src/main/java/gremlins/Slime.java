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

        // Check for collisions
        int gridX = this.x / App.SPRITESIZE;
        int gridY = this.y / App.SPRITESIZE;

        Tile tile = map.getTile(gridY, gridX);
        if (tile != null && (tile.getType() == Tile.STONE || tile.getType() == Tile.BRICK)) {
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
}