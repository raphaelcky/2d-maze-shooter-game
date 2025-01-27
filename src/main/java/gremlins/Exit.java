package gremlins;

import processing.core.PApplet;
import processing.core.PImage;

public class Exit {
    private int x;
    private int y;
    private PImage image;

    public Exit(int x, int y, PImage image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public void draw(PApplet app) {
        app.image(this.image, this.x, this.y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}