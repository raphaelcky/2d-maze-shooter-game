package gremlins;

import processing.core.PApplet;
import processing.core.PImage;

public class Fireball {
    private int x, y; // Current position
    private int speed = 4; // Speed of the fireball
    private String direction; // Direction of movement
    private boolean expired; // Whether the fireball is expired
    private PImage image; // Fireball sprite

    public Fireball(int x, int y, String direction, PImage image) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.image = image;
        this.expired = false;
    }

    public void draw(PApplet app) {
        app.image(this.image, this.x, this.y);
    }

    public void update() {
        // Move fireball in the current direction
        switch (this.direction) {
            case "up":
                this.y -= this.speed;
                break;
            case "down":
                this.y += this.speed;
                break;
            case "left":
                this.x -= this.speed;
                break;
            case "right":
                this.x += this.speed;
                break;
        }

        // Mark fireball as expired if out of bounds
        if (this.x < 0 || this.y < 0 || this.x >= App.WIDTH || this.y >= App.HEIGHT - App.BOTTOMBAR) {
            this.expired = true;
        }
    }

    public boolean isExpired() {
        return this.expired;
    }
}