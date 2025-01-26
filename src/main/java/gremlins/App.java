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
    public PImage gremlin;
    public PImage exit;
    public PImage fireball;
    public PImage slime;

    private GameMap gameMap;
    private Wizard wizard;

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
        this.gremlin = loadImage(this.getClass().getResource("gremlin.png").getPath().replace("%20", ""));
        this.exit = loadImage(this.getClass().getResource("exit.png").getPath().replace("%20", ""));

        PImage wizardLeft = loadImage(this.getClass().getResource("wizard0.png").getPath().replace("%20", ""));
        PImage wizardRight = loadImage(this.getClass().getResource("wizard1.png").getPath().replace("%20", ""));
        PImage wizardUp = loadImage(this.getClass().getResource("wizard2.png").getPath().replace("%20", ""));
        PImage wizardDown = loadImage(this.getClass().getResource("wizard3.png").getPath().replace("%20", ""));
        this.slime = loadImage(this.getClass().getResource("slime.png").getPath().replace("%20", ""));
        this.fireball = loadImage(this.getClass().getResource("fireball.png").getPath().replace("%20", ""));
        
        PImage[] brickDestructionImages = new PImage[4];
        for (int i = 0; i < 4; i++) {
            brickDestructionImages[i] = loadImage(this.getClass().getResource("brickwall_destroyed" + i + ".png").getPath().replace("%20", ""));
        }

        // Load the configuration
        JSONObject config = loadJSONObject(new File(this.configPath));
        String layoutFile = config.getJSONArray("levels").getJSONObject(0).getString("layout");

        int slimeCooldown = (int) (config.getJSONArray("levels").getJSONObject(0).getFloat("enemy_cooldown") * App.FPS);

        // Initialize the map
        this.gameMap = new GameMap(stonewall, brickwall, gremlin, slime, exit, brickDestructionImages, slimeCooldown);

        // Load the first level's map
        this.gameMap.loadMap(layoutFile);

        // Initialize the wizard at its starting position
        for (int row = 0; row < gameMap.getRows(); row++) {
            for (int col = 0; col < gameMap.getCols(); col++) {
                if (gameMap.getTile(row, col).getType() == Tile.WIZARD) {
                    this.wizard = new Wizard(col * SPRITESIZE, row * SPRITESIZE, wizardLeft, wizardRight, wizardUp, wizardDown,
                        (int) (config.getJSONArray("levels").getJSONObject(0).getFloat("wizard_cooldown") * App.FPS), 
                        fireball);
                }
            }
        }
    }

    /**
     * Receive key pressed signal from the keyboard.
    */
    public void keyPressed() {
        if (this.wizard == null) return;
    
        if (!this.wizard.getMoving()) { // Only allow new movement commands if not already moving
            if (keyCode == UP) {
                this.wizard.move(0, -1, this.gameMap);
            } else if (keyCode == DOWN) {
                this.wizard.move(0, 1, this.gameMap);
            } else if (keyCode == LEFT) {
                this.wizard.move(-1, 0, this.gameMap);
            } else if (keyCode == RIGHT) {
                this.wizard.move(1, 0, this.gameMap);
            }
        }
    
        // Fireball shooting
        if (key == ' ') { // Spacebar to shoot
            this.wizard.shootFireball();
        }
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
        this.gameMap.updateGremlins(this.wizard);
        this.wizard.draw(this);
        this.wizard.update(this.gameMap);

        // Draw cooldown timer bar
        this.wizard.drawCooldownBar(this);
    }

    public static void main(String[] args) {
        PApplet.main("gremlins.App");
    }
}
