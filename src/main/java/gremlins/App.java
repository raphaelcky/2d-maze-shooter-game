package gremlins;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;
import processing.data.JSONArray;

import java.util.Random;
import java.io.*;


public class App extends PApplet {

    public static final int WIDTH = 720;
    public static final int HEIGHT = 720;
    public static final int SPRITESIZE = 20;
    public static final int BOTTOMBAR = 60;

    public static final int FPS = 60;

    public static final Random randomGenerator = new Random();

    public String configPath;
    
    public PImage brickwall;
    public PImage stonewall;
    public PImage wizard;
    public PImage gremlin;
    public PImage exit;

    private GameMap gameMap;

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
    */
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player, enemies and map elements.
    */
    public void setup() {
        frameRate(FPS);

        // Load images during setup
        this.stonewall = loadImage(this.getClass().getResource("stonewall.png").getPath().replace("%20", ""));
        this.brickwall = loadImage(this.getClass().getResource("brickwall.png").getPath().replace("%20", ""));
        this.wizard = loadImage(this.getClass().getResource("wizard1.png").getPath().replace("%20", ""));
        this.gremlin = loadImage(this.getClass().getResource("gremlin.png").getPath().replace("%20", ""));
        this.exit = loadImage(this.getClass().getResource("exit.png").getPath().replace("%20", ""));
        //this.slime = loadImage(this.getClass().getResource("slime.png").getPath().replace("%20", ""));
        //this.fireball = loadImage(this.getClass().getResource("fireball.png").getPath().replace("%20", ""));
        
        // Initialize the map
        this.gameMap = new GameMap(stonewall, brickwall, wizard, gremlin, exit);

        // Load the configuration
        JSONObject config = loadJSONObject(new File(this.configPath));
        String layoutFile = config.getJSONArray("levels").getJSONObject(0).getString("layout");

        // Load the first level's map
        this.gameMap.loadMap(layoutFile);
    }

    /**
     * Receive key pressed signal from the keyboard.
    */
    public void keyPressed(){

    }
    
    /**
     * Receive key released signal from the keyboard.
    */
    public void keyReleased(){

    }


    /**
     * Draw all elements in the game by current frame. 
	 */
    public void draw() {
        background(13544591);
        this.gameMap.draw(this);
    }

    public static void main(String[] args) {
        PApplet.main("gremlins.App");
    }
}
