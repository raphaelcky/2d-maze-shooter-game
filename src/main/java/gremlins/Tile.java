package gremlins;

import processing.core.PApplet;
import processing.core.PImage;

public class Tile {
    public static final char STONE = 'X';
    public static final char BRICK = 'B';
    public static final char EMPTY = ' ';
    public static final char WIZARD = 'W';
    public static final char EXIT = 'E';
    public static final char GREMLIN = 'G';
    private int destructionFrames = 16; // Total frames for destruction animation
    private int currentFrame = 0;
    private boolean destroying = false;
    private PImage[] destructionImages; // Array of destruction images

    private char type;
    private PImage image;

    public Tile(char type, PImage image) {
        this.type = type;
        this.image = image;
    }

    public void startDestructionAnimation(PImage[] destructionImages) {
        this.destroying = true;
        this.currentFrame = 0;
        this.destructionImages = destructionImages;
    }
    
    public void draw(PApplet app, int x, int y) {
        if (destroying) {
            // Display the destruction animation
            int frameIndex = currentFrame / 4; // 4 frames per image
            app.image(this.destructionImages[frameIndex], x, y);
            currentFrame++;
            if (currentFrame >= destructionFrames) {
                this.destroying = false;
                this.type = EMPTY; // Mark as empty after animation
            }
        } else if (this.image != null) {
            // Draw regular tile
            app.image(this.image, x, y);
        }
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public PImage getImage() {
        return image;
    }

    public void setImage(PImage image) {
        this.image = image;
    }

    public boolean isDestroyed() {
        return this.type == EMPTY && !this.destroying;
    }
}