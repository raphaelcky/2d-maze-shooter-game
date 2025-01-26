package gremlins;

import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;

public class Wizard {
    private int x, y; // Current position in pixels
    private int tileX, tileY; // Current position in grid
    private int targetX, targetY; // Target position in pixels
    private int speed = 2; // Speed of movement in pixels/frame
    private boolean moving; // Whether the wizard is currently moving
    private String direction; // Current direction

    private int cooldownTimer = 0; // Timer for fireball cooldown
    private int cooldownFrames; // Cooldown duration in frames

    private PImage currentImage;
    private PImage leftImage, rightImage, upImage, downImage;
    private PImage fireballImage;

    private ArrayList<Fireball> fireballs; // List of active fireballs

    public Wizard(int x, int y, PImage leftImage, PImage rightImage, PImage upImage, PImage downImage, int cooldownFrames, PImage fireballImage) {
        this.x = x;
        this.y = y;
        this.tileX = x / App.SPRITESIZE;
        this.tileY = y / App.SPRITESIZE;
        this.targetX = x;
        this.targetY = y;
        this.currentImage = rightImage;
        this.leftImage = leftImage;
        this.rightImage = rightImage;
        this.upImage = upImage;
        this.downImage = downImage;
        this.cooldownFrames = cooldownFrames;
        this.fireballs = new ArrayList<>();
        this.direction = "right"; // Default direction
        this.fireballImage = fireballImage;
    }

    public void draw(PApplet app) {
        app.image(this.currentImage, this.x, this.y);
        for (Fireball fireball : fireballs) {
            fireball.draw(app);
        }
    }

    public void move(int dx, int dy, GameMap map) {
        if (!moving) {
            int nextTileX = this.tileX + dx;
            int nextTileY = this.tileY + dy;
            
            // Update direction and sprite
            if (dx > 0) {
                this.direction = "right";
                this.currentImage = this.rightImage;
            } else if (dx < 0) {
                this.direction = "left";
                this.currentImage = this.leftImage;
            } else if (dy > 0) {
                this.direction = "down";
                this.currentImage = this.downImage;
            } else if (dy < 0) {
                this.direction = "up";
                this.currentImage = this.upImage;
            }

            // Log the requested move
            //System.out.println("Attempting to move to tile (" + nextTileX + ", " + nextTileY + ")");
    
            // Check collision with stone and brick walls
            Tile nextTile = map.getTile(nextTileY, nextTileX);
            if (nextTile == null || nextTile.getType() == Tile.STONE || nextTile.getType() == Tile.BRICK) {
                //System.out.println("Movement blocked by obstacle.");
                return;
            }
    
            // Set movement target
            this.moving = true;
            this.tileX = nextTileX;
            this.tileY = nextTileY;
            this.targetX = this.tileX * App.SPRITESIZE;
            this.targetY = this.tileY * App.SPRITESIZE;
    
            // Log movement initiation
            //System.out.println("Moving to target pixel (" + this.targetX + ", " + this.targetY + ")");
    

        }
    
    }

    public void shootFireball() {
        if (this.cooldownTimer == 0) {
            Fireball fireball = new Fireball(this.x, this.y, this.direction, this.fireballImage);
            this.fireballs.add(fireball);
            this.cooldownTimer = this.cooldownFrames;
        }
    }

    public void update(GameMap map) {
        if (this.cooldownTimer > 0) {
            this.cooldownTimer--;
        }
        fireballs.removeIf(Fireball::isExpired);
        for (Fireball fireball : fireballs) {
            fireball.update(map);
        }
    
        // Access movement flags via App's public methods
        App app = (App) map.getApp(); // Assume GameMap has a reference to the app instance
        if (app.isMovingUp()) {
            move(0, -1, map);
        }
        if (app.isMovingDown()) {
            move(0, 1, map);
        }
        if (app.isMovingLeft()) {
            move(-1, 0, map);
        }
        if (app.isMovingRight()) {
            move(1, 0, map);
        }
    
        if (moving) {
            if (this.x < this.targetX) this.x += speed;
            if (this.x > this.targetX) this.x -= speed;
            if (this.y < this.targetY) this.y += speed;
            if (this.y > this.targetY) this.y -= speed;
    
            // Stop movement when reaching the target tile
            if (this.x == this.targetX && this.y == this.targetY) {
                this.moving = false;
            }
        }
    }

    public void drawCooldownBar(PApplet app) {
        int barWidth = 100;
        int barHeight = 5;
        int barX = App.WIDTH - 120;
        int barY = App.HEIGHT - 40;
    
        // Draw black outline
        app.stroke(0);
        app.noFill();
        app.rect(barX, barY, barWidth, barHeight);
    
        // Draw cooldown progress
        int cooldownWidth = (int) (barWidth * (1 - (cooldownTimer / (float) cooldownFrames)));

        app.fill(0, 0, 255); // Blue when recharging
        if (cooldownTimer > 0) {
            app.fill(0, 0, 255); // Blue when recharging
        }
        app.rect(barX, barY, cooldownWidth, barHeight);
    }

    public int getSpeed() {
        return this.speed;
    }

    public boolean getMoving() {
        return this.moving;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public ArrayList<Fireball> getFireballs() {
        return this.fireballs;
    }
}