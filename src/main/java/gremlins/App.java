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
    public PImage wizardImage;

    private GameMap gameMap;
    private Wizard wizard;

    private JSONObject config; // Instance-level config variable
    private int initialLives; // Store initial lives from config
    public static int lives;
    public static boolean gameOver = false;
    private int collisionCooldown = 0;
    // Movement flags for simultaneous actions
    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

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
        this.wizardImage = wizardRight;
        
        PImage[] brickDestructionImages = new PImage[4];
        for (int i = 0; i < 4; i++) {
            brickDestructionImages[i] = loadImage(this.getClass().getResource("brickwall_destroyed" + i + ".png").getPath().replace("%20", ""));
        }

        // Load the configuration
        JSONObject config = loadJSONObject(new File(this.configPath));
        String layoutFile = config.getJSONArray("levels").getJSONObject(0).getString("layout");

        int slimeCooldown = (int) (config.getJSONArray("levels").getJSONObject(0).getFloat("enemy_cooldown") * App.FPS);

        // Initialize the map
        this.gameMap = new GameMap(this, stonewall, brickwall, gremlin, slime, exit, brickDestructionImages, slimeCooldown);
        lives = config.getInt("lives");

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
        if (gameOver && key == 'r') {
            // Restart the game
            lives = initialLives;
            gameOver = false;
            setup();
            return;
        }

        if (this.wizard == null) return;
    
        // Track movement keys
        if (keyCode == UP) {
            isMovingUp = true;
        } else if (keyCode == DOWN) {
            isMovingDown = true;
        } else if (keyCode == LEFT) {
            isMovingLeft = true;
        } else if (keyCode == RIGHT) {
            isMovingRight = true;
        }

        // Allow shooting fireballs
        if (key == ' ') { // Spacebar to shoot
            this.wizard.shootFireball();
        }
    }
    
    /**
     * Receive key released signal from the keyboard.
    */
    public void keyReleased() {
        // Reset movement flags when keys are released
        if (keyCode == UP) {
            isMovingUp = false;
        } else if (keyCode == DOWN) {
            isMovingDown = false;
        } else if (keyCode == LEFT) {
            isMovingLeft = false;
        } else if (keyCode == RIGHT) {
            isMovingRight = false;
        }
    }


    /**
     * Draw all elements in the game by current frame. 
	 */
    public void draw() {
        if (gameOver) {
            drawGameOverScreen();
            return;
        }

        background(13544591);
        this.gameMap.draw(this);
        this.gameMap.updateGremlins(this.wizard);

        if (collisionCooldown > 0) {
            collisionCooldown--;
        } else {
            checkPlayerCollisions();
        }

        this.wizard.draw(this);
        this.wizard.update(this.gameMap);

        // Draw cooldown timer bar
        this.wizard.drawCooldownBar(this);

        // Draw lives
        drawLives();
    }

    private void drawLives() {
        for (int i = 0; i < lives; i++) {
            image(wizardImage, 10 + i * (App.SPRITESIZE + 5), App.HEIGHT - App.BOTTOMBAR + 10);
        }
    }

    private void checkPlayerCollisions() {
        if (collisionCooldown > 0) {
            return; // Prevent collisions during cooldown
        }
    
        // Check collisions with gremlins
        if (gameMap.isPlayerCollidingWithGremlinOrSlime(this.wizard)) {
            collisionCooldown = 60; // 30-frame cooldown to avoid repeated life loss
            loseLife();
            return;
        }
    
        // Check collisions with slime
        if (gameMap.isPlayerCollidingWithGremlinOrSlime(this.wizard)) {
            collisionCooldown = 60; // 30-frame cooldown to avoid repeated life loss
            loseLife();
        }
    }
    
    private void loseLife() {
        App.lives--;
        System.out.println("Lives: " + lives);
        if (App.lives <= 0) {
            App.gameOver = true; // Trigger game over
        } else {
            resetLevel(); // Reset level if lives remain
        }
    }

    private void drawGameOverScreen() {
        background(255);
        fill(0);
        textSize(50);
        textAlign(CENTER, CENTER);
        text("Game Over", WIDTH / 2, HEIGHT / 2 - 20);

        textSize(25);
        text("Press R to Restart", WIDTH / 2, HEIGHT / 2 + 40);
    }

    public static void resetLevel() {
        lives--;
        System.out.println("Lives: " + lives);
        if (lives == 0) {
            gameOver = true;
        }
    }

    public void keyTyped() {
        if (gameOver && key == 'r') {
            // Restart the game
            lives = config.getInt("lives");
            gameOver = false;
            resetLevel();
        }
    }

    // Add these public getters for movement flags
    public boolean isMovingUp() {
        return isMovingUp;
    }

    public boolean isMovingDown() {
        return isMovingDown;
    }

    public boolean isMovingLeft() {
        return isMovingLeft;
    }

    public boolean isMovingRight() {
        return isMovingRight;
    }

    public static void main(String[] args) {
        PApplet.main("gremlins.App");
    }
}
