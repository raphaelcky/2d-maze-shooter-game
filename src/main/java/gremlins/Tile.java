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

    private char type;
    private PImage image;

    public Tile(char type, PImage image) {
        this.type = type;
        this.image = image;
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
}