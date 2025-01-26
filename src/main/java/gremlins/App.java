package gremlins;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;

import java.util.Random;
import java.io.*;

public class App extends PApplet {

    public static final int WIDTH = 720;
    public static final int HEIGHT = 720;
    public static final int SPRITESIZE = 20;
    public static final int BOTTOMBAR = 60;

    public static final int FPS = 60;

    public static final Random randomGenerator = new Random();
    public static final int COLLISION_THRESHOLD = App.SPRITESIZE / 2;

    public String configPath;

    public PImage brickwall;
    public PImage stonewall;
    public PImage gremlin;
    public PImage exit;
    public PImage fireball;
    public PImage slime;
    public PImage wizardImage;
    public PImage wizardLeft;
    public PImage wizardRight;
    public PImage wizardUp;
    public PImage wizardDown;

    private GameMap gameMap;
    private Wizard wizard;

    private JSONObject config; 
    private String layoutFile;
    private int initialLives; 
    public static int lives;
    public static boolean gameOver = false;
    private int collisionCooldown = 0;
    private int wizardCooldown;

    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    public App() {
        this.configPath = "config.json";
    }

    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void setup() {
        frameRate(FPS);
    
        // Load images
        this.stonewall = loadImage(this.getClass().getResource("stonewall.png").getPath().replace("%20", ""));
        this.brickwall = loadImage(this.getClass().getResource("brickwall.png").getPath().replace("%20", ""));
        this.gremlin = loadImage(this.getClass().getResource("gremlin.png").getPath().replace("%20", ""));
        this.exit = loadImage(this.getClass().getResource("exit.png").getPath().replace("%20", ""));
        this.wizardLeft = loadImage(this.getClass().getResource("wizard0.png").getPath().replace("%20", ""));
        this.wizardRight = loadImage(this.getClass().getResource("wizard1.png").getPath().replace("%20", ""));
        this.wizardUp = loadImage(this.getClass().getResource("wizard2.png").getPath().replace("%20", ""));
        this.wizardDown = loadImage(this.getClass().getResource("wizard3.png").getPath().replace("%20", ""));
        this.slime = loadImage(this.getClass().getResource("slime.png").getPath().replace("%20", ""));
        this.fireball = loadImage(this.getClass().getResource("fireball.png").getPath().replace("%20", ""));
        this.wizardImage = wizardRight;
    
        PImage[] brickDestructionImages = new PImage[4];
        for (int i = 0; i < 4; i++) {
            brickDestructionImages[i] = loadImage(this.getClass().getResource("brickwall_destroyed" + i + ".png").getPath().replace("%20", ""));
        }
    
        // Load configuration
        this.config = loadJSONObject(new File(this.configPath));
        this.layoutFile = config.getJSONArray("levels").getJSONObject(0).getString("layout"); // Assign to instance variable
    
        int slimeCooldown = (int) (config.getJSONArray("levels").getJSONObject(0).getFloat("enemy_cooldown") * App.FPS);
        wizardCooldown = (int) (config.getJSONArray("levels").getJSONObject(0).getFloat("wizard_cooldown") * App.FPS);
    
        // Initialize the map
        this.gameMap = new GameMap(this, stonewall, brickwall, gremlin, slime, exit, brickDestructionImages, slimeCooldown);
        lives = config.getInt("lives");
    
        // Load the first level's map
        this.gameMap.loadMap(layoutFile);
    
        // Initialize the wizard
        initializeWizard();
    }


    private void initializeWizard() {
        for (int row = 0; row < gameMap.getRows(); row++) {
            for (int col = 0; col < gameMap.getCols(); col++) {
                if (gameMap.getTile(row, col).getType() == Tile.WIZARD) {
                    this.wizard = new Wizard(
                        col * SPRITESIZE,
                        row * SPRITESIZE,
                        wizardLeft,
                        wizardRight,
                        wizardUp,
                        wizardDown,
                        wizardCooldown,
                        fireball
                    );
                    return; // Stop after initializing the wizard
                }
            }
        }

        if (wizard == null) {
            throw new IllegalStateException("Wizard not found in the map layout!");
        }
    }

    public void keyPressed() {
        if (gameOver && key == 'r') {
            restartGame();
            return;
        }

        if (this.wizard == null) return;

        if (keyCode == UP) isMovingUp = true;
        else if (keyCode == DOWN) isMovingDown = true;
        else if (keyCode == LEFT) isMovingLeft = true;
        else if (keyCode == RIGHT) isMovingRight = true;

        if (key == ' ') this.wizard.shootFireball();
    }

    public void keyReleased() {
        if (keyCode == UP) isMovingUp = false;
        else if (keyCode == DOWN) isMovingDown = false;
        else if (keyCode == LEFT) isMovingLeft = false;
        else if (keyCode == RIGHT) isMovingRight = false;
    }

    public void draw() {
        if (gameOver) {
            drawGameOverScreen();
            return;
        }

        background(13544591);
        this.gameMap.draw(this);
        this.gameMap.updateGremlins(this.wizard);

        if (collisionCooldown > 0) collisionCooldown--;

        this.wizard.draw(this);
        this.wizard.update(this.gameMap);

        drawLives();
    }

    private void drawLives() {
        for (int i = 0; i < lives; i++) {
            image(wizardImage, 10 + i * (SPRITESIZE + 5), HEIGHT - BOTTOMBAR + 10);
        }
    }

    public void loseLife() {
        if (collisionCooldown > 0) return;

        lives--;
        System.out.println("Lives: " + lives);

        if (lives <= 0) {
            gameOver = true;
        } else {
            resetLevel();
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

    private void restartGame() {
        lives = config.getInt("lives");
        gameOver = false;
        setup();
    }

    private void resetLevel() {
        // Clear the existing gremlins
        gameMap.clearGremlins();

        // Reload the map to reset the level layout
        gameMap.loadMap(this.layoutFile);

        // Reset wizard position and state
        initializeWizard();

        // Reset collision cooldown
        collisionCooldown = 0;
    }

    public int getCollisionCooldown() {
        return collisionCooldown;
    }

    public void setCollisionCooldown(int cooldown) {
        this.collisionCooldown = cooldown;
    }

    public boolean isMovingUp() { return isMovingUp; }
    public boolean isMovingDown() { return isMovingDown; }
    public boolean isMovingLeft() { return isMovingLeft; }
    public boolean isMovingRight() { return isMovingRight; }

    public static void main(String[] args) {
        PApplet.main("gremlins.App");
    }
}