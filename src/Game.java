import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.Ellipse2D;


import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. 

// Student ID: 3457018


@SuppressWarnings("serial")


public class Game extends GameCore 
{
	// Useful game constants
	static int screenWidth = 800;
	static int screenHeight = 445;
	private int currentLevel = 1;
	//Game Finish
	private boolean inVictoryScreen = false;
	//debug
    boolean debug = false;
    //Basic Sprite movement
    boolean moveUp = false;
    boolean moveDown = false;
    //bounding circle key events
    private boolean useBoundingCircle = false;

    // Game resources
    ArrayList<Tile>	collidedTiles = new ArrayList<Tile>();
    
    private TileMap tmap;
    boolean pause = false;
    
    long total;// The score will be the total time elapsed since a crash
    ArrayList<DelayedEnemy> delayedEnemies = new ArrayList<>();
    private Animation enemyAnim; //Enemy Sprite Animation
    final float followDelayFactor = 0.04f;//Enemy following main sprite
    ArrayList<Fireball> enemyFireballs = new ArrayList<>();//Enemy projectiles
    int hitCount = 0;//Main sprite hit counts from fireballs
    ArrayList<ExpandingCircle> expandingCircles = new ArrayList<>();
    
    //Main menu
    private Image gameLogo;
    private int logoWidth,logoHeight,logoX, logoY;
    private boolean inMainMenu = true; //Starts the game in the main menu
    private Image playButton; //Loads the play button image
    private int playX, playY, playWidth, playHeight; // Play Button dimensions
    private float menuScrollX = 0; //Separate scrolling for the main menu
    private final float menuScrollSpeed = 100.0f; //Scroll Speed for Main menu
   
    // Hover instructions
    private Rectangle instructionHoverZone;//Hover component in both main menu and pause screen
    private boolean isHoveringInstructions = false;
    //BG Sound in Main Menu
    private boolean mainMenuMusicStarted = false;
    private boolean gameMusicStarted = false;
    private boolean isSoundMuted = false;
    
    // Life system
    private int playerLives = 3;
    private final int maxLives = 3;
    private Image heartImage;
    private int heartWidth = 24;
    private int heartHeight = 24;
    private int heartSpacing = 10;

    //Pause Screen
    private Image restartButton, menuButton;// Separate buttons for pause screen only
    private int restartX, restartY, restartWidth, restartHeight;
    private int menuX, menuY, menuWidth, menuHeight;
    // Parallax Background Variables
    private Image FixedBG, ForeGroundBG;
    private float scrollX = 0; // Initial scrolling offset
    private float previousScrollX = 0;
    
    //Main Sprite (Rocket)
    Sprite MainSprite = null;
    Animation rocketAnim;
    // Rocket tilt variables
    double tiltAngle = Math.toRadians(12);
    float RocketTiltSpeed = 0.20f;
    
    //Explosion
    Animation explosionAnim;
    //Animation enemyExplosionAnim;
    long explosionStartTime;
    float explosionScaleFactor = 5.0f;//Explosion scaling
    int explosionDuration = 999;
    int pauseDuration = 900; // 1 second pause before restarting
    boolean gamePaused = false;
    long pauseStartTime;
    boolean scrollingPaused = false;
    boolean explosionPlaying = false;    
    float explosionX, explosionY;
    boolean collisionOccurred = false;
    
    //Crystals
    Animation crystalAnim;
    ArrayList<Sprite> crystals = new ArrayList<>();
    
    //Orbs
    Animation orbAnim;
    ArrayList<Sprite> orbs = new ArrayList<>();
 
    //Loading Screen
    private boolean isLoading = true;
    private long loadingStartTime;
    private final int LOADING_DURATION_MS = 8000; // 8 seconds
    
    // Fuel system variables
    float maxFuel = 100.0f;   // Maximum fuel capacity
    float fuel = maxFuel;     // Current fuel level
    float fuelDepletionRate = 5f; // Rate at which fuel depletes per second
    boolean outOfFuel = false; // Flag to check if fuel is depleted
    
    // Fuel blinking logic
    long lastBlinkTime = 0;
    boolean fuelBlinkState = true;
    long fuelBlinkDuration = 400; // Duration in milliseconds (adjustable)
    
    //Finish line
    private boolean finishCrossed = false;
    private ArrayList<Sprite> finishTiles = new ArrayList<>(); // Store finish line tiles

    /**
	 * The obligatory main method that creates
     * an instance of our class and starts it running
     * 
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false,screenWidth,screenHeight);
    }
    
    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers.
     * 
     * This shows you the general principles but you should create specific
     * methods for setting up your game that can be called again when you wish to 
     * restart the game (for example you may only want to load animations once
     * but you could reset the positions of sprites each time you restart the game).
     */
    public void init()
    {     
    	loadingStartTime = System.currentTimeMillis();
        setTitle("Kyber Blast");
        tmap = new TileMap();
        // Load the tile map and print it out, switches between Level 1 and 2.
        if (currentLevel == 1) {
            tmap.loadMap("maps", "map.txt");
            FixedBG = loadImage("images/SkyBG.png");
            ForeGroundBG = loadImage("images/MidGround.png");
        } else if (currentLevel == 2) {
            tmap.loadMap("maps", "Level2.txt");
            FixedBG = loadImage("images/NightSkyBG.png"); 
            ForeGroundBG = loadImage("images/MidGroundNight.png");
        }

        Sound.preloadSynth();
        
        setSize(tmap.getPixelWidth()/4, tmap.getPixelHeight());
        setVisible(true);
        setResizable(false);
        setSize(screenWidth, screenHeight);

        // Load Restart Button
        restartButton = loadImage("images/RestartButton.png");
        int RBorigWidthRestart = restartButton.getWidth(null);
        int RBorigHeightRestart = restartButton.getHeight(null);
        double restartScaleX = 150.0 / RBorigWidthRestart;
        double restartScaleY = 60.0 / RBorigHeightRestart;
        double restartScale = Math.min(restartScaleX, restartScaleY);
        restartWidth = (int) (RBorigWidthRestart * restartScale);
        restartHeight = (int) (RBorigHeightRestart * restartScale);
        restartX = (screenWidth / 2) - restartWidth - 10;
        restartY = (screenHeight / 2) + 50;
        
        // Load Main Menu Button
        menuButton = loadImage("images/HomeButton.png");
        int HBorigWidthMenu = menuButton.getWidth(null);
        int HBorigHeightMenu = menuButton.getHeight(null);
        double menuScaleX = 150.0 / HBorigWidthMenu;
        double menuScaleY = 60.0 / HBorigHeightMenu;
        double menuScale = Math.min(menuScaleX, menuScaleY);
        menuWidth = (int) (HBorigWidthMenu * menuScale);
        menuHeight = (int) (HBorigHeightMenu * menuScale);
        menuX = (screenWidth / 2) + 10;
        menuY = (screenHeight / 2) + 50;
    
        
        // Defines the hover text rectangle in top-right
        int hoverWidth = 200;
        int hoverHeight = 40;
        instructionHoverZone = new Rectangle(screenWidth - hoverWidth - 20, 20, hoverWidth, hoverHeight);
        
        // Loads the play button image
        playButton = loadImage("images/PlayButton_upscaled.png");

        // Calculate scale factors for the button
        int origWidthPlay = playButton.getWidth(null);
        int origHeightPlay = playButton.getHeight(null);
        double PlayscaleX = 190.0 / origWidthPlay; // Scale to 200 pixels width
        double PlayscaleY = 100.0 / origHeightPlay; // Scale to 100 pixels height
        double PlayButtonscale = Math.min(PlayscaleX, PlayscaleY); // Maintain aspect ratio

        // Resize the button dynamically
        playWidth = (int) (origWidthPlay * PlayButtonscale);
        playHeight = (int) (origHeightPlay * PlayButtonscale);
        
        // Center the button
        playX = (screenWidth - playWidth) / 2;
        playY = screenHeight / 2 + 30;
        
        // Load the game logo image
        gameLogo= loadImage("images/LogoGame-removebg.png");
        // Calculate scale factors for the logo
        int origWidthLogo = gameLogo.getWidth(null);
        int origHeightLogo = gameLogo.getHeight(null);
        double logoxScale = 250.0 / origWidthLogo; // Target width
        double logoyScale = 120.0 / origHeightLogo; // Target height
        double logoScale = Math.min(logoxScale, logoyScale); // Maintain aspect ratio

        // Resize the logo dynamically
        logoWidth = (int) (origWidthLogo * logoScale);
        logoHeight = (int) (origHeightLogo * logoScale);

        // Position the logo above the play button
        logoX = (screenWidth - logoWidth) / 2;
        logoY = 40; 
                
        //Mouse Events
        /**
         * Adds a mouse listener for handling click events (like Play or Restart).
         */
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Game.this.mousePressed(e);
            }
        });
        previousScrollX = scrollX;
        
        /**
         * Adds a mouse motion listener to detect hovering over instruction text.
         */
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                isHoveringInstructions = instructionHoverZone.contains(p);
            }
        });
        
        //Parallax BG
        FixedBG = loadImage("images/SkyBG.png");
        ForeGroundBG = loadImage("images/MidGround.png");
        
        //Main Sprite's life (hit count)
        heartImage = loadImage("images/Lives.png");
	     if (heartImage != null) {
	         heartWidth = 24;  
	         heartHeight = 24;
	     }
	     
        // Loads Enemy Sprite Animation
		enemyAnim = new Animation();
		enemyAnim.loadAnimationFromSheet("images/Enemy_Sprite.png", 3, 3, 100);
		
        // Loads the rocket sprite sheet animation
        rocketAnim = new Animation();
        rocketAnim.loadAnimationFromSheet("images/Rocket_Sprite.png", 3, 3, 120);
        
        // Create a sprite using that animation
        MainSprite = new Sprite(rocketAnim);        
        MainSprite.setPosition(300, 200);
        
        // Calculate scale factors to fit within a game world area
        int origWidth = MainSprite.getImage().getWidth(null);
        int origHeight = MainSprite.getImage().getHeight(null);
        double scaleX = 95.0 / origWidth;
        double scaleY = 55.0 / origHeight;        
        double scale = Math.min(scaleX, scaleY);
        
        // Set the sprite's scale factors
        MainSprite.setScale((float) scale);
        
        // Loads the explosion animation
        explosionAnim = new Animation();
        explosionAnim.loadAnimationFromSheet("images/Explosion.png", 4, 2, 300);      
        
        // Loads the crystal animation
        crystalAnim = new Animation();
        crystalAnim.loadAnimationFromSheet("images/Crystal_Sprite.png", 1, 3, 300);
        
        //Loads Orb animation
        orbAnim = new Animation();
        orbAnim.loadAnimationFromSheet("images/RedOrb3.png", 1, 10, 200);
        
        // Debugging: Check if the crystal animation is actually loading
        if (crystalAnim.getImage() == null) {
            System.out.println("ERROR: Crystal animation not loaded!");
        } else {
            System.out.println("Crystal animation loaded successfully.");
        }

        // Create crystal sprites, enemies, finish tiles and orbs from the map
        loadCrystalsFromMap();
        loadOrbsFromMap();
        loadEnemiesFromMap();
        loadFinishLineFromMap();
        initialiseGame();
        System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initialiseGame()
    {
    	total = 0;
    }
    
    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g)
    {    	
    	//Loading Screen
    	if (isLoading) {
    	    long elapsed = System.currentTimeMillis() - loadingStartTime;
    	    if (elapsed >= LOADING_DURATION_MS) {
    	        isLoading = false;
    	        updateMusicState();
    	        return;
    	    }

    	    g.setColor(Color.BLACK);
    	    g.fillRect(0, 0, screenWidth, screenHeight);

    	    // Texts
    	    g.setFont(new Font("Monospace", Font.BOLD, 32));
    	    g.setColor(Color.WHITE);
    	    g.drawString("Loading Game...", screenWidth / 2 - 120, screenHeight / 2 - 40);

    	    // Circular Spinner
    	    int radius = 25;
    	    int cx = screenWidth / 2;
    	    int cy = screenHeight / 2 + 30;
    	    int angle = (int) ((System.currentTimeMillis() / 5) % 360);

    	    g.setColor(new Color(0x4088BB));
    	    g.fillArc(cx - radius, cy - radius, radius * 2, radius * 2, angle, 60);
    	    return;
    	}
    	 // Enable Anti-Aliasing for smoother graphics and text
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        //Pause Screen
        if (pause) {            
            drawParallaxBackground(g);// Draw moving parallax background
            
            // Applies a semi-transparent overlay for the blur effect
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, screenWidth, screenHeight);
            
            // Draw Restart & Menu Buttons and Logo
            int logoX = (screenWidth - logoWidth) / 2;
            int logoY = screenHeight / 5; 
            int buttonSpacing = 20;
            int totalWidth = restartWidth + menuWidth + 20; // only 1 spacing

            // Calculate starting X position for centering
            int buttonX = (screenWidth - totalWidth) / 2;
            int buttonY = screenHeight / 2 + 50;

            // Set individual button positions
            restartX = buttonX;
            menuX = restartX + restartWidth + buttonSpacing;
            //draw logo
            g.drawImage(gameLogo, logoX, logoY, logoWidth, logoHeight, null);
            // Draw buttons
            g.drawImage(restartButton, restartX, buttonY, restartWidth, restartHeight, null);
            g.drawImage(menuButton, menuX, buttonY, menuWidth, menuHeight, null);
                       
            // Draw hover text in upper-right
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.setColor(Color.WHITE);
            if (isHoveringInstructions) {
            	g.drawString("Press P for Pause", screenWidth - 255, 60);
                g.drawString("Press D for Debug", screenWidth - 255, 85);
                g.drawString("Press W/up key for up control", screenWidth - 255, 110);
                g.drawString("Press S/down key for down control", screenWidth - 255, 135);
            } else {
                g.drawString("Hover for Instructions", screenWidth - 200, 60);
            }
            return;
        }
        
        //Main Menu Screen
        if (inMainMenu) {
        	 drawMainMenuParallax(g);

            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, screenWidth, screenHeight);
                       
            // Draw the game logo
            g.drawImage(gameLogo, logoX, logoY, logoWidth, logoHeight, null);

            // Draw the play button
            g.drawImage(playButton, playX, playY, playWidth, playHeight, null);
            
            // Draw hover text in upper-right
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.setColor(Color.WHITE);
            if (isHoveringInstructions) {
            	g.drawString("Press P for Pause", screenWidth - 255, 60);
                g.drawString("Press D for Debug", screenWidth - 255, 85);
                g.drawString("Press W/up key for up control", screenWidth - 255, 110);
                g.drawString("Press S/down key for down control", screenWidth - 255, 135);
            } else {
                g.drawString("Hover for Instructions", screenWidth - 200, 60);
            }
            
            return;
        }
        
        //Victory Screen
        if (inVictoryScreen) {
        	// Draws the Game Logo
        	g.drawImage(gameLogo, logoX, logoY, logoWidth, logoHeight, null);
        	
        	// Animated Gradient for "Congratulations!"
        	Font congratsFont = new Font("Arial", Font.BOLD, 40);
        	g.setFont(congratsFont);

        	int textX = screenWidth / 2 - 140;
        	int textY = screenHeight / 2 - 20;

        	String congratsText = "Congratulations!";

        	// Gradient Animation: use time to animate gradient offset
        	long time = System.currentTimeMillis() % 2000; // Loops every 2 seconds
        	float shift = (float) (Math.sin(time / 200.0) * 100); // Shift range -100 to +100

        	GradientPaint gradient = new GradientPaint(
        	    textX + shift, textY - 40, Color.BLUE,
        	    textX + shift + 200, textY + 20, Color.ORANGE, true
        	);

        	g.setPaint(gradient);
        	g.drawString(congratsText, textX, textY);
        	//drawn remaining texts
            g.setFont(new Font("Monospace", Font.PLAIN, 20));
            g.setColor(Color.decode("#00caf7"));
            g.drawString("You completed all levels.", screenWidth / 2 - 110, screenHeight / 2 + 20);
            g.drawString("Press ENTER to return to Level 1", screenWidth / 2 - 130, screenHeight / 2 + 60);
            return;
        }

    	// Be careful about the order in which you draw objects - you
    	// should draw the background first, then work your way 'forward'

    	// First work out how much we need to shift the view in order to
    	// see where the player is. To do this, we adjust the offset so that
        // it is relative to the player's position along with a shift
        // Force the screen to clear before drawing
        //Fill the background with SkyBG (FixedBG) instead of solid black
        for (int i = 0; i <= getWidth() / FixedBG.getWidth(null) + 1; i++) {
            g.drawImage(FixedBG, i * FixedBG.getWidth(null), 0, null);
        }
        drawParallaxBackground(g);
        tmap.draw(g, (int) -scrollX, 0, screenWidth, (int) scrollX);

        for (ExpandingCircle circle : expandingCircles) {
            circle.draw(g);
        }
         
        for (Sprite crystal : crystals) {
            crystal.draw(g);
        }
        
        for (Sprite orb : orbs) {
            orb.draw(g);
        }
    
        if (explosionPlaying) {
            g.drawImage(explosionAnim.getImage(), (int) explosionX - explosionAnim.getImage().getWidth(null) / 2,
                                                  (int) explosionY - explosionAnim.getImage().getHeight(null) / 2, null);
        } else if (MainSprite != null) {
            MainSprite.drawTransformed(g);
        }
        
        for (Sprite tile : finishTiles) {
        	tile.setX(tile.getX() - (scrollX - previousScrollX));
        	tile.draw(g);
        }

        for (DelayedEnemy dEnemy : delayedEnemies) {
            if (dEnemy.activated && dEnemy.sprite != null) {
                dEnemy.sprite.drawTransformed(g);
            }
        }
        
        for (Fireball fb : enemyFireballs) {
            fb.draw(g, scrollX);
        }

        //Fuel Bar Dimensions
        int barWidth = 200;
        int barHeight = 20;
        int fuelWidth = (int) ((fuel / maxFuel) * barWidth); // Adjust width based on fuel
        int cornerRadius = 10; // Soft corner radius
      
        Color borderColor = new Color(0x003366); // Dark Blue Outline
        Color fuelColor = new Color(0x33CCFF); // Light Blue Fuel
        Color emptyColor = new Color(0x666666); // Dark Gray (when fuel is empty)
        Color warningColor = new Color(0xFF0000); // Red when fuel is low
        Color textColor = new Color(0xFFFFFF); // White text
        
        //Blinking Effect When Fuel is Low
        if (fuel <= maxFuel * 0.30) { // If fuel is 20% or lower, blink
            if (System.currentTimeMillis() - lastBlinkTime > fuelBlinkDuration) {
                fuelBlinkState = !fuelBlinkState; // Toggle state
                lastBlinkTime = System.currentTimeMillis();
            }
        } else {
            fuelBlinkState = true; // Reset if fuel is above threshold
        }

        //Draw Fuel Bar Background (Border)
        g.setColor(borderColor);
        g.fillRoundRect(10, screenHeight - 30, barWidth, barHeight, cornerRadius, cornerRadius);
        
        // Draw Fuel Fill (Changes Color & Blinks if Low)
        if (fuel > 0) {
            if (fuel <= maxFuel * 0.2 && fuelBlinkState) {
                g.setColor(warningColor); // Blinking Red when low
            } else {
                g.setColor(fuelColor); // Normal Fuel Color
            }
            g.fillRoundRect(10, screenHeight - 30, fuelWidth, barHeight, cornerRadius, cornerRadius);
        }

        // Draw Fuel Fill (Changes if fuel is empty)
        g.setColor(fuel > 0 ? fuelColor : emptyColor);

        // Draw Fuel Label
        g.setColor(textColor);
        g.drawString("Fuel", 15, screenHeight - 35);
        
        //Main Sprite's Lives     
        if (!inMainMenu) {
            for (int i = 0; i < playerLives; i++) {
                int x = screenWidth - (heartWidth + heartSpacing) * (i + 1) - 15;
                int y = screenHeight - heartHeight - 15;
                g.drawImage(heartImage, x, y, heartWidth, heartHeight, null);
            }
        }             
        
        String msg = String.format("Score: %d", total/100);
        g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 100, 50);
        
        if (debug)
        {
        	// When in debug mode, you could draw borders around objects
            // and write messages to the screen with useful information.
            // Try to avoid printing to the console since it will produce 
            // a lot of output and slow down your game.
        	
        	// Show FPS
            String fpsText = String.format("FPS: %.1f", getFPS());
            g.setColor(Color.BLACK);
            g.drawString(fpsText, 10, 50);
            
            tmap.drawBorder(g, (int) -scrollX, screenHeight - tmap.getPixelHeight(), Color.BLACK);

            g.setColor(Color.BLUE);
            //switch between box and circle bounds
            if (useBoundingCircle) {
                MainSprite.drawBoundingCircle(g);
            } else {
                MainSprite.drawBoundingBox(g);
            }
            
            // Draw bounding for crystals
            g.setColor(Color.GREEN);
            for (Sprite crystal : crystals) {
                if (useBoundingCircle) {
                    crystal.drawBoundingCircle(g);
                } else {
                    crystal.drawBoundingBox(g);
                }
            }
            
            // Draw bounding for orbs
            g.setColor(Color.MAGENTA);
            for (Sprite orb : orbs) {
                if (useBoundingCircle) {
                    orb.drawBoundingCircle(g);
                } else {
                    orb.drawBoundingBox(g);
                }
            }
            
            g.setColor(Color.BLACK);
        	g.drawString(String.format("Player: %.0f,%.0f", MainSprite.getX(),MainSprite.getY()),
        								getWidth() - 120, 70);
        	
        	drawCollidedTiles(g, tmap, (int) -scrollX, screenHeight - tmap.getPixelHeight());
        }
        updateMusicState();
    }
    
    /**
     * Draws the infinitely scrolling Parallax Background in the main menu only. 
     * @param g The Graphics
     */
    private void drawMainMenuParallax(Graphics2D g) {
        int fixedBGWidth = FixedBG.getWidth(null);
        int fgWidth = ForeGroundBG.getWidth(null);

        int offsetFixed = (int) (menuScrollX * 0.4) % fixedBGWidth;//speed difference form the parallax BG
        int offsetFore = (int) (menuScrollX * 0.9) % fgWidth;
        int foregroundY = screenHeight - ForeGroundBG.getHeight(null);

        for (int i = -1; i < (getWidth() / fixedBGWidth) + 2; i++) {
            g.drawImage(FixedBG, i * fixedBGWidth - offsetFixed, 0, null);
        }
        for (int x = -offsetFore; x < getWidth() + fgWidth; x += fgWidth) {
            g.drawImage(ForeGroundBG, x, foregroundY, null);
        }
    }

    /**
     * Draws the main Parallax Background within the Game play.
     * @param g The Graphics
     */
    private void drawParallaxBackground(Graphics2D g) {
    	
        int fixedBGWidth = FixedBG.getWidth(null);
        int fgWidth = ForeGroundBG.getWidth(null);
        
        int offsetFixed = (int) ((scrollX * 0.2) % fixedBGWidth);
        int offsetFore = (int) ((scrollX * 0.8) % fgWidth);
        int foregroundY = screenHeight - ForeGroundBG.getHeight(null);
        
        for (int i = -1; i < (getWidth() / fixedBGWidth) + 2; i++) {
            g.drawImage(FixedBG, i * fixedBGWidth - offsetFixed, 0, null);
        }
        for (int x = -offsetFore; x < getWidth() + fgWidth; x += fgWidth) {
            g.drawImage(ForeGroundBG, x, foregroundY, null);
        }
    } 
    
    /**
     * Draws bounding boxes for collidable tiles like the buildings or obstacles (in debug mode)
     * @param g The Graphics to draw the bounding boxes
     * @param map The tile map reference.
     * @param xOffset Horizontal scroll offset.
     * @param yOffset Vertical scroll offset.
     */
    public void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset)
    {
    	 g.setColor(Color.RED); // Color for collision tiles (Buildinds)

    	    for (int r = 0; r < tmap.getMapHeight(); r++) {
    	        for (int c = 0; c < tmap.getMapWidth(); c++) {
    	            char tileChar = tmap.getTileChar(c, r);
    	            if (tileChar == 'p' || tileChar == 't' || tileChar == 'b' || tileChar == 'i' || tileChar == 'd' || tileChar == 'l' || tileChar == 'w' || tileChar == 'z') {
    	                int tileX = xOffset + c * tmap.getTileWidth();
    	                int tileY = yOffset + r * tmap.getTileHeight();
    	                g.drawRect(tileX, tileY, 64, 96);
    	            }
    	        }
    	    }
    }
	
    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {	
    	if (inMainMenu) {
    		 float deltaTime = elapsed / 1000.0f;
    		 menuScrollX += menuScrollSpeed * deltaTime; //Keep menu scrolling at a steady speed
    		 return;
    	}

    	if (gamePaused) {
            if (System.currentTimeMillis() - pauseStartTime > pauseDuration) {
                restartGame();
            }
            return; // Game remains paused until restart
        }
    	
    	if (pause) return;
    	
    	// Explosion Handling
        if (collisionOccurred && !scrollingPaused) {
        	Sound.run("sounds/Rocket_Explode.wav");
            scrollingPaused = true; // Immediately stop scrolling upon collision
            explosionStartTime = System.currentTimeMillis();
            MainSprite.setAnimation(explosionAnim);
            explosionPlaying = true;
            explosionX = MainSprite.getX();
            explosionY = MainSprite.getY();
            MainSprite.setVelocity(0, 0);
            MainSprite.xscale *= explosionScaleFactor;
            MainSprite.yscale *= explosionScaleFactor;
            playerLives = 0;
            System.out.println("Collision Occured...");
        }
        if (explosionPlaying) {
            MainSprite.update(elapsed);
            if (System.currentTimeMillis() - explosionStartTime > explosionDuration) {
                gamePaused = true;
                pauseStartTime = System.currentTimeMillis();
                explosionPlaying = false;
            }
            return; // Prevent further updates while explosion is playing
        }
        // Check is Main Sprite passes through the finished tiles.
        if (!finishTiles.isEmpty() && !finishCrossed) {
            if (scrollingPaused && MainSprite.getX() > screenWidth) {
            	Sound.run("sounds/LevelUp.wav");//Sound when Main Sprite finished a level
                System.out.println("Player exited screen after finish line.");
                finishCrossed = true;
                if (currentLevel == 1) {
                    currentLevel = 2;
                    restartGame();
                } else if (currentLevel == 2) {
                    inVictoryScreen = true;
                }
            }
        }


        if (!scrollingPaused) {
            float deltaTime = elapsed / 1000.0f;
            scrollX = Math.round(scrollX); //Move the game world automatically (simulating forward movement).
            scrollX += 200 * deltaTime;
            
            if (!finishTiles.isEmpty()) {
                Sprite firstFinishTile = finishTiles.get(0);
                float finishWorldX = firstFinishTile.getX();
                
                float rightEdgeBuffer = 100; // push the finish line further in view
                if (finishWorldX - scrollX < screenWidth - rightEdgeBuffer) {
                    scrollX = finishWorldX - (screenWidth - rightEdgeBuffer);
                    scrollingPaused = true; // stop scrolling when main sprite went through the finish tiles.
                }
            }
          
            total += elapsed;
        }
        
        for (Sprite crystal : crystals) {
        	float adjustedX = crystal.getX() - (scrollX - previousScrollX); //Moves only based on actual scroll difference
            crystal.setX(adjustedX);
            crystal.update(elapsed);
        }
        
        for (Sprite orb : orbs) {
        	 float adjustedX = orb.getX() - (scrollX - previousScrollX);
        	    orb.setX(adjustedX);
        	    orb.update(elapsed);
        }
        
        previousScrollX = scrollX; //Store last scroll position
        
        if (MainSprite != null) {
        	// Reduce fuel over time
            if (fuel > 0) {
                fuel -= fuelDepletionRate * (elapsed / 1000.0f);
                if (fuel <= 0) {
                    fuel = 0;
                    outOfFuel = true;
                }
            }
            // Disable Controls & Force Fall When Fuel = 0
            if (outOfFuel) {
                MainSprite.setVelocityY(RocketTiltSpeed * 1); // INSTANTLY LOSE CONTROL
                MainSprite.setRotation(0); // Remove tilt
            } else {
                // Only Allow Movement if There is Fuel
                if (moveUp && !moveDown) {
                    MainSprite.setVelocityY(-RocketTiltSpeed);
                    MainSprite.setRotation(-Math.toDegrees(tiltAngle));
                } else if (moveDown && !moveUp) {
                    MainSprite.setVelocityY(RocketTiltSpeed);
                    MainSprite.setRotation(Math.toDegrees(tiltAngle));
                } else {
                    MainSprite.setVelocityY(0);
                    MainSprite.setRotation(0);
                }
            }
            // Check for Crystal Collection
            for (int i = crystals.size() - 1; i >= 0; i--) {  
                Sprite crystal = crystals.get(i);
                float crystalWorldX = crystal.getX();
                //Crystal hit box
                float hitboxWidth = crystal.getWidth() * 0.6f;  // Reduce width by 40%
                float hitboxHeight = crystal.getHeight() * 0.6f; // Reduce height by 40%

                if (MainSprite.getX() < crystalWorldX + hitboxWidth &&
                    MainSprite.getX() + MainSprite.getWidth() > crystalWorldX &&
                    MainSprite.getY() < crystal.getY() + hitboxHeight &&
                    MainSprite.getY() + MainSprite.getHeight() > crystal.getY()) {
                    
                    System.out.println("Crystal collected at: " + crystalWorldX + ", " + crystal.getY());
                    crystals.remove(i); // Remove crystal when collected
                    Sound.run("sounds/Crystal_Collection.wav"); // Sounds of crystal collection.
                    fuel = Math.min(maxFuel, fuel + 20); // Increase fuel + 20
                    outOfFuel = false; // Allow movement again if fuel was empty
                }
            }
            
            // Check for Orbs Collection
            for (int i = orbs.size() - 1; i >= 0; i--) {  
                Sprite orb = orbs.get(i);
                float orbsWorldX = orb.getX();
               
                float scaledOrbWidth = orb.getWidth() * (float) orb.xscale;  
                float scaledOrbHeight = orb.getHeight() * (float) orb.yscale;

                // Orbs hit box
                float hitboxWidth = scaledOrbWidth * 0.6f;  
                float hitboxHeight = scaledOrbHeight * 0.6f;
                              
                    if (MainSprite.getX() < orbsWorldX + hitboxWidth &&
                            MainSprite.getX() + MainSprite.getWidth() > orbsWorldX &&
                            MainSprite.getY() < orb.getY() + hitboxHeight &&
                            MainSprite.getY() + MainSprite.getHeight() > orb.getY()) {
                            
                          System.out.println("Orb collected at: " + orbsWorldX + ", " + orb.getY());
                          orbs.remove(i);
                            
                            //Check if there are any visible enemy on screen
                            boolean enemyOnScreen = false;
                            for (DelayedEnemy dEnemy : delayedEnemies) {
                                if (dEnemy.activated && dEnemy.sprite != null) {
                                    enemyOnScreen = true;
                                    break;
                                }
                            }
                                                      
                            // Only show expanding circle if enemy is on screen
                            if (enemyOnScreen) {
                                expandingCircles.add(new ExpandingCircle(
                                    MainSprite.getX() + MainSprite.getWidth() / 2,
                                    MainSprite.getY() + MainSprite.getHeight() / 2,
                                    screenWidth * 1.2f,
                                    1400f
                                ));
                                Sound.run("sounds/Orb_Boom.wav");//Sound of Expanding Orb effect
                            }

                            // Destroy enemy/enemies
                            for (DelayedEnemy dEnemy : delayedEnemies) {
                                if (dEnemy.activated && dEnemy.sprite != null) {
                                    System.out.println("Enemy destroyed at: " + dEnemy.sprite.getX() + ", " + dEnemy.sprite.getY());
                                    dEnemy.sprite = null;
                                    dEnemy.activated = false;
                                    
                                }
                            }
                        }
            		}


            if (checkTileCollision(MainSprite)) {
                collisionOccurred = true;
            }
            MainSprite.update(elapsed);
            if (scrollingPaused && !finishCrossed) {
                float forwardSpeed = 350 * (elapsed / 1000f);  // match scroll speed
                MainSprite.setX(MainSprite.getX() + forwardSpeed);
            }

            handleScreenEdge(MainSprite, tmap, elapsed);
            
            for (DelayedEnemy dEnemy : delayedEnemies) {
            	if (dEnemy.sprite == null) continue;

                Sprite enemy = dEnemy.sprite;

                if (!dEnemy.activated) {
                    // Activate when it enters visible screen
                    if (dEnemy.worldX < scrollX + screenWidth) {
                        dEnemy.activated = true;
                    } else {
                        continue; // Skip this enemy for now
                    }
                }
                // Computer controlled sprite logic
                float targetX = MainSprite.getX() - 180;
                float targetY = MainSprite.getY();
                float enemyY = enemy.getY();
                float deltaTime = elapsed / 1000.0f;
                float verticalSpeed = 80 * deltaTime;

                int tileX = (int) ((enemy.getX() + scrollX + enemy.getWidth()) / tmap.getTileWidth());
                int tileY = (int) (enemyY / tmap.getTileHeight());

                boolean obstacleAhead = isObstacleTile(tmap.getTileChar(tileX, tileY));
                boolean spaceAbove = tileY > 0 && !isObstacleTile(tmap.getTileChar(tileX, tileY - 1));
                boolean spaceBelow = tileY + 1 < tmap.getMapHeight() && !isObstacleTile(tmap.getTileChar(tileX, tileY + 1));

                if (obstacleAhead) {
                    if (spaceAbove) {
                        enemy.setY(enemyY - verticalSpeed);
                    } else if (spaceBelow) {
                        enemy.setY(enemyY + verticalSpeed);
                    }
                } else {
                	float verticalFollow = (targetY - enemyY) * dEnemy.followDelayFactor; //Keeps distance
                    enemy.setY(enemyY + verticalFollow);
                }

                enemy.setX(targetX);

                enemy.update(elapsed);
                
                // FIREBALL SPAWN LOGIC
                long now = System.currentTimeMillis();
                if (now - dEnemy.lastFireTime > dEnemy.fireCooldown) {
                	float fireX = (float) (scrollX + enemy.getX() + (enemy.getWidth() * enemy.xscale)) - 5;
                    float fireY = (float) (enemy.getY() + (enemy.getHeight() * enemy.yscale) * 0.3f);
                    System.out.println("Firing fireball at: " + fireX + ", " + fireY);

                    Fireball fb = new Fireball(fireX, fireY, 350); // Fast enough to go forward
                    Sound.run("sounds/Fireball_Sound.wav");// Sounds of firing projectiles
                    enemyFireballs.add(fb);
                    dEnemy.lastFireTime = now;
                }
            }


        }
        // Expanding circle updation
        for (int i = expandingCircles.size() - 1; i >= 0; i--) {
            ExpandingCircle circle = expandingCircles.get(i);
            circle.update(elapsed);
            if (!circle.isActive()) {
                expandingCircles.remove(i);
            }
        }

        for (int i = enemyFireballs.size() - 1; i >= 0; i--) {
            Fireball fb = enemyFireballs.get(i);
            fb.update(elapsed);

            if (fb.intersects(MainSprite, scrollX)) {
                System.out.println("Main sprite hit by fireball!");
                Sound.run("sounds/HitRocket.wav");//sound of main sprite being hit
                playerLives--;

                if (playerLives <= 0) {
                    collisionOccurred = true;
                }
                enemyFireballs.remove(i);
                hitCount++;

                if (hitCount >= 3) {
                    collisionOccurred = true;
                }
            } else if (fb.isOffScreen(scrollX, screenWidth)) {
                enemyFireballs.remove(i);
            }
        }
    }
    
    
    /**
     * Checks and handles collisions with the edge of the screen. You should generally
     * use tile map collisions to prevent the player leaving the game area. This method
     * is only included as a temporary measure until you have properly developed your
     * tile maps.
     * 
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     * @param elapsed	How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed)
    {
    	Image img = s.getImage();
        int origW = img.getWidth(null);
        int origH = img.getHeight(null);
        
        float spriteW = origW * (float)s.xscale;
        float spriteH = origH * (float)s.yscale;
        
        float topMarginFactor = 0.50f;
        float bottomMargin = 4; // Fixed bottom margin to ensure proper clamping
        
        if (s.getY() < spriteH * topMarginFactor) {
             s.setY(spriteH * topMarginFactor);
        }
        if (s.getY() + spriteH > getHeight() - bottomMargin) {
             s.setY(getHeight() - spriteH - bottomMargin);
        }
        if (s.getX() < 0) {
             s.setX(0);
        }
        if (s.getX() + spriteW > getWidth() && !scrollingPaused) {
            s.setX(getWidth() - spriteW);
        }
    }
    
    /**
     * Resets key game variables and reinitializes the level state.
     * Called when the player collides or finishes the level.
     * This method preserves level-specific logic and ensures
     * all Sprites and sounds are reset accordingly.
     */
    public void restartGame() {
    	if (currentLevel == 1) {
    	    tmap.loadMap("maps", "map.txt");
    	    FixedBG = loadImage("images/SkyBG.png");
    	    ForeGroundBG = loadImage("images/MidGround.png");
    	} else if (currentLevel == 2) {
    	    tmap.loadMap("maps", "Level2.txt");
    	    FixedBG = loadImage("images/NightSkyBG.png"); // provide new images
    	    ForeGroundBG = loadImage("images/MidGroundNight.png");
    	}	
    	enemyFireballs.clear(); // Reset enemy projectiles
    	hitCount = 0; // Also reset hit counter
    	playerLives = maxLives;
    	scrollX = 0;
        total = 0;
        collisionOccurred = false;
        gamePaused = false;
        scrollingPaused = false;
        MainSprite.setPosition(100, 200);
        MainSprite.setRotation(0);
        MainSprite.setAnimation(rocketAnim);
        int origWidth = MainSprite.getImage().getWidth(null);
        int origHeight = MainSprite.getImage().getHeight(null);
        double scaleX = 95.0 / origWidth;
        double scaleY = 55.0 / origHeight;
        double scale = Math.min(scaleX, scaleY);
        MainSprite.setScale((float) scale);
        crystals.clear();
        loadCrystalsFromMap();
        orbs.clear();
        loadOrbsFromMap();
        MainSprite.setPosition(300, 200); // Reset player position
        MainSprite.setVelocity(0, 0);
        MainSprite.setRotation(0);
        fuel = maxFuel;  // Reset fuel to full
        outOfFuel = false; // Enable movement again
        loadEnemiesFromMap();
        finishCrossed = false;
        finishTiles.clear();
        loadFinishLineFromMap();
        previousScrollX = scrollX;
        updateMusicState();
    }                    
    
    /**
     * Loads the crystals from the current tile map and places them as sprites.
     */
    private void loadCrystalsFromMap() {
    	 crystals.clear(); // Ensure crystals resets on restart
        for (int r = 0; r < tmap.getMapHeight(); r++) {
            for (int c = 0; c < tmap.getMapWidth(); c++) {
                char ch = tmap.getTileChar(c, r);
                if (ch == 'c') { // 'c' represents a crystal
                    System.out.println("Placing crystal at Tile Row: " + r + ", Column: " + c);

                    // Creates independent animation instance for each crystals
                    Animation crystalAnimation = new Animation();
                    crystalAnimation.loadAnimationFromSheet("images/Crystal_Sprite.png", 1, 3, 300);

                    Sprite crystal = new Sprite(crystalAnimation);
                    
                    int origWidth = crystal.getImage().getWidth(null);
                    int origHeight = crystal.getImage().getHeight(null);
                    
                    double scaleX = 20.0 / origWidth;
                    double scaleY = 30.0 / origHeight;
                    double scale = Math.min(scaleX, scaleY);
                    crystal.setScale((float) scale);

                    //Set position properly relative to tiles
                    float crystalX = c * tmap.getTileWidth();
                    float crystalY = r * tmap.getTileHeight();
                    crystal.setPosition(crystalX, crystalY);

                    crystals.add(crystal);
                    System.out.println("Crystal placed at: " + crystalX + ", " + crystalY + " with Scale: " + scale);
                }
            }
        }
    }
    
    /**
     * Loads the orbs from the map and places them in their initial positions.
     */
    private void loadOrbsFromMap() {
        orbs.clear(); // Reset orbs when restarting
        
        for (int r = 0; r < tmap.getMapHeight(); r++) {
            for (int c = 0; c < tmap.getMapWidth(); c++) {
                char ch = tmap.getTileChar(c, r);
                if (ch == 'o') { // 'o' represents an orb
                    
                    System.out.println("Placing orb at Tile Row: " + r + ", Column: " + c);
                    Sprite orb = new Sprite(orbAnim);
                    
                    int origWidth = orb.getImage().getWidth(null);
                    int origHeight = orb.getImage().getHeight(null);
                    
                    double scaleX = 20.0 / origWidth;
                    double scaleY = 20.0 / origHeight;
                    double scale = Math.min(scaleX, scaleY);
                    orb.setScale((float) scale);

                    //Set position based on tile map
                    float orbX = c * tmap.getTileWidth();
                    float orbY = r * tmap.getTileHeight();
                    orb.setPosition(orbX, orbY);

                    orbs.add(orb);
                    System.out.println("Orb placed at: " + orbX + ", " + orbY + " with Scale: " + scale);
                }
            }
        }
    }
    
    /**
     * Loads enemy_sprites spawn positions from the map using 'e' characters and initializes them.
     */
    private void loadEnemiesFromMap() {
        delayedEnemies.clear();

        for (int r = 0; r < tmap.getMapHeight(); r++) {
            for (int c = 0; c < tmap.getMapWidth(); c++) {
                char ch = tmap.getTileChar(c, r);
                if (ch == 'e') {
                    System.out.println("Spawning enemy at: row=" + r + " col=" + c);
                    Sprite enemy = new Sprite(enemyAnim);

                    // Scale
                    int origW = enemy.getImage().getWidth(null);
                    int origH = enemy.getImage().getHeight(null);
                    double scaleX = 85.0 / origW;
                    double scaleY = 45.0 / origH;
                    double scale = Math.min(scaleX, scaleY);
                    enemy.setScale((float) scale);

                    // Position
                    float x = c * tmap.getTileWidth() + (float)(Math.random() * 10);
                    float y = r * tmap.getTileHeight() + (float)(Math.random() * 40 - 20);
                    enemy.setPosition(x, y);

                    delayedEnemies.add(new DelayedEnemy(enemy, x));
                }
            }
        }
    }
    
    /**
     * Checks if the given tile character represents a solid obstacle (Buildings).
     * @param tile
     * @return
     */
    
    private boolean isObstacleTile(char tile) {
        return tile == 'p' || tile == 'b' || tile == 't' || tile == 'i'|| tile == 'd'|| tile == 'l'|| tile == 'w'|| tile == 'z';
    }
    
    /**
     * Loads finish line tiles (marked as 'u' in the map) and positions them accordingly.
     */
    private void loadFinishLineFromMap() {
    	finishTiles.clear();

        Image gridImage = loadImage("images/PurpleGrid2.png");

        Animation gridAnim = new Animation();
        gridAnim.addFrame(gridImage, 1000);

        for (int r = 0; r < tmap.getMapHeight(); r++) {
            for (int c = 0; c < tmap.getMapWidth(); c++) {
                char ch = tmap.getTileChar(c, r);
                if (ch == 'u') {
                    Sprite tile = new Sprite(gridAnim);
                    tile.setScale(1.0f);

                    float x = c * tmap.getTileWidth();
                    float y = r * tmap.getTileHeight();
                    tile.setPosition(x, y);

                    finishTiles.add(tile);
                }
            }
        }
        finishTiles.sort((a, b) -> Float.compare(b.getX(), a.getX()));
    }
       
    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     * 
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) 
    { 
    	int key = e.getKeyCode();
    	
		switch (key)
		{
			case KeyEvent.VK_UP     : moveUp = true; break;
			case KeyEvent.VK_DOWN  	: moveDown = true; break;
			case KeyEvent.VK_W		: moveUp = true; break;
			case KeyEvent.VK_S		: moveDown = true; break;
			//case KeyEvent.VK_S 	: Sound s = new Sound("sounds/caw.wav");s.start();break;
			case KeyEvent.VK_ESCAPE : Sound.stopMidi(); stop(); break;//Press this Key if a slow down or lagginess is apparent when playing the Game. 
			//The Midi File May be too large for Java to process.
			case KeyEvent.VK_D 		: debug = !debug; break; // Flip the debug state
			case KeyEvent.VK_C		: useBoundingCircle = !useBoundingCircle; break;
			case KeyEvent.VK_P		: pause = !pause; break;
			case KeyEvent.VK_ENTER	: if (inVictoryScreen) {
			        					currentLevel = 1;
			        					inVictoryScreen = false;
			        					restartGame();
										} break;
			default :  break;
		}
    
    }

    /** Use the sample code in the lecture notes to properly detect
     * a bounding box collision between sprites s1 and s2.
     * 
     * @return	true if a collision may have occurred, false if it has not.
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
    	return false;   	
    }
    
    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     * 
     * @param s			The Sprite to check collisions for
     */
    public boolean checkTileCollision(Sprite s)
    {
    	 Image img = s.getImage();
    	    int origW = img.getWidth(null);
    	    int origH = img.getHeight(null);
    	    int scaledW = (int) (origW * s.xscale);
    	    int scaledH = (int) (origH * s.yscale);

    	    //Hitbox Scaling for More Accurate Collision
    	    final double collisionFactorW = 0.70; 
    	    final double collisionFactorH = 0.80;

    	    int hitW = (int) (scaledW * collisionFactorW);
    	    int hitH = (int) (scaledH * collisionFactorH);

    	    //Hitbox Offsets
    	    int offsetX = (scaledW - hitW) / 2; // Center horizontally
    	    int offsetY = (scaledH - hitH) / 2; // Center vertically

    	    //Step-Based Collision for High-Speed Movement
    	    int steps = Math.max(5, (int) Math.abs(s.getVelocityX() + s.getVelocityY())); 
    	    float stepX = s.getVelocityX() / steps;
    	    float stepY = s.getVelocityY() / steps;

    	    for (int i = 0; i <= steps; i++) {
    	        int spriteWorldX = (int) (s.getX() + scrollX + stepX * i);
    	        int spriteWorldY = (int) (s.getY() + stepY * i);

    	        Rectangle spriteRect = new Rectangle(
    	            spriteWorldX + offsetX, spriteWorldY + offsetY, hitW, hitH
    	        );

    	        //Precise Per-Tile Collision Handling
    	        for (int r = 0; r < tmap.getMapHeight(); r++) {
    	            for (int c = 0; c < tmap.getMapWidth(); c++) {
    	                char ch = tmap.getTileChar(c, r);

    	                //Only Check Obstacles
    	                if (ch == 'p' || ch == 't' || ch == 'b' || ch == 'i' || ch == 'd' || ch == 'l' || ch == 'w' || ch == 'z') {
    	                    int tx = c * tmap.getTileWidth();
    	                    int ty = r * tmap.getTileHeight();
    	                    Rectangle tileRect = new Rectangle(tx, ty, 64, 96);

    	                    if (spriteRect.intersects(tileRect)) {
    	                        return true; //Precise Collision Detected
    	                    }
    	                }  
    	            }
    	        }
    	    }
    	    return false; //No Collision
    }
    
    /**
     * Handles key release events to stop player movement or toggle debug modes.
     * @param e The key event triggered when a key is released.
     */
	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		switch (key)
		{
			case KeyEvent.VK_UP     : moveUp = false; break;
			case KeyEvent.VK_DOWN 	: moveDown = false; break;
			case KeyEvent.VK_W  	: moveUp = false; break;
			case KeyEvent.VK_S  	: moveDown = false; break;
			default :  break;
		}
	}
	
	/**
	 * Updates the music playback based on the current game state (main menu or gameplay).
	 * Ensures no overlapping of MIDI tracks.
	 */
		private void updateMusicState() {
			if (isSoundMuted) {
			    Sound.setVolume(0.0f); // mute
				} else {
				    Sound.setVolume(1.0f); // unmute
				}
		    if (inMainMenu) {
		        if (!mainMenuMusicStarted) {
		        	Sound.stopMidi();
		            Sound.playMainMenuMusic();
		            mainMenuMusicStarted = true;
		            gameMusicStarted = false;
		        }
		    	}	else {
		        if (!gameMusicStarted) {
		        	Sound.stopMidi();
		            Sound.playGameMusic();
		            gameMusicStarted = true;
		            mainMenuMusicStarted = false;
		        }
		    }
		}
		
		/**
		 * Handles mouse click events for UI buttons like Play, Home, and Restart.
		 * Includes button click detection for the Main Menu and Pause Menu.
		 * @param e The mouse event triggered by user click.
		 * @param e
		 */
	public void mousePressed(MouseEvent e) {
		int mouseX = e.getX();
	    int mouseY = e.getY();
			  
	    if (inMainMenu) {
	        //Check if the Play button was clicked
	        if (mouseX >= playX && mouseX <= playX + playWidth &&
	            mouseY >= playY && mouseY <= playY + playHeight) {
	            
	            System.out.println("Play Button Clicked! Starting game...");
	            inMainMenu = false; //Hide menu and start the game
	            menuScrollX = 0;
	            restartGame(); //Reset Game   
	        }
	    }
	    if (pause) {
	        if (mouseX >= restartX && mouseX <= restartX + restartWidth &&
	            mouseY >= restartY && mouseY <= restartY + restartHeight) {
	            System.out.println("Restart Button Clicked!");
	            restartGame();
	            pause = false; // Resume game
	        }
	        
	        if (mouseX >= menuX && mouseX <= menuX + menuWidth &&
	            mouseY >= menuY && mouseY <= menuY + menuHeight) {
	            System.out.println("Main Menu Button Clicked!");
	            inMainMenu = true;
	            menuScrollX = 0;
	            pause = false;
	            if (inMainMenu) {
	                mainMenuMusicStarted = false;
	            } else {
	                gameMusicStarted = false;
	            }
	            updateMusicState();
	        }
	    }
	}
	
	/**
	 * Stops playing the MiDi Tracks.
	 */
	@Override
	public void stop() {
	    Sound.stopMidi(); // Stop MIDI on window close
	}
}

/**
 * Represents an enemy that appears after a delay based on it's worldX position.
 * It follows the Main Sprite with a variable delay factor and can shoot projectiles.
 */
class DelayedEnemy {
    Sprite sprite;
    boolean activated = false;
    float worldX;
    float followDelayFactor;
    long lastFireTime = 0;
    long fireCooldown = 1000 + (int)(Math.random() * 2000); // 1 to 3 seconds
    
    /**
     * Constructor of DelayedEnemy Class, creates a delayed enemy with a specific spawn X position and sprite.
     * @param sprite The representation of Enemy sprites.
     * @param worldX The X coordinate in the Game world where it shall spawn.
     */
    public DelayedEnemy(Sprite sprite, float worldX) {
        this.sprite = sprite;
        this.worldX = worldX;
        this.followDelayFactor = 0.025f + (float)(Math.random() * 0.05f);
    }
}

/**
 * Represents the Fireball projectiles shot by an Enemy sprite and leaves a fading trail behind.
 */
class Fireball {
    float x, y;
    float speed;
    int radius;
    Color color;
    ArrayList<Trail> trails = new ArrayList<>();
    int trailCooldown = 0; // Timer for creating new trail segments

    /**
     * Constructs a fireball at a given position and speed.
     * @param x Initial X position (Game world coordinates)
     * @param y Initial Y position
     * @param speed Horizontal speed of the fireball
     */
    public Fireball(float x, float y, float speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.radius = 9;//size of projectile.
        this.color = new Color(0xba3d0b); // Orange-red
    }
    
    /**
     * Updates the fireball’s position and trail effects.
     * @param elapsed Elapsed time in milliseconds.
     */
    public void update(long elapsed) {
    	float deltaTime = elapsed / 1000f;
        x += speed * deltaTime; // move right ONLY
        // Add trail point periodically
        trailCooldown += elapsed;
        if (trailCooldown >= 30) { // Add trail ~every 40ms
            trails.add(new Trail(x, y, 200));
            if (trails.size() > 7) trails.remove(0); // Limit trail length
            trailCooldown = 0;
        }

        // Fade existing trails
        for (Trail t : trails) {
            t.alpha -= 10;
        }
        trails.removeIf(t -> t.alpha <= 0);
    }
    
    /**
     * Draws the fireball and its trailing visual effect.
     * @param g The graphics.
     * @param scrollX Horizontal scroll offset.
     */
    public void draw(Graphics2D g, float scrollX) {
    	for (Trail t : trails) {
    	    g.setColor(new Color(255, 165, 0, t.alpha)); // Orange with fading
    	    g.fillOval((int)(t.x - scrollX), (int)t.y, radius, radius);
    	}
         g.setColor(color);
         g.fillOval((int)(x - scrollX), (int)y, radius, radius); // Scroll-adjusted X
    }
    
    /**
     * Determines if the fireball projectile has moved beyond the screen.
     * @param scrollX Current scroll position.
     * @param screenWidth Width of the screen.
     * @return true if offscreen.
     */
    public boolean isOffScreen(float scrollX, int screenWidth) {
        return (x - scrollX) > screenWidth; // once it moves past screen right
    }
    
    /**
     * Checks for collision with a given sprite (the main sprite).
     * @param s
     * @param scrollX
     * @return
     */
    public boolean intersects(Sprite s, float scrollX) {
        // Fireball bounds (world coordinates)
    	Rectangle fireballRect = new Rectangle((int)(x - scrollX), (int)y, radius, radius);

        // Main sprite world coordinates
        float sX = s.getX();
        float sY = s.getY();

        int imgW = s.getImage().getWidth(null);
        int imgH = s.getImage().getHeight(null);

        int scaledW = (int)(imgW * s.xscale);
        int scaledH = (int)(imgH * s.yscale);

        // Shrink bounding box slightly for fairness
        float collisionFactorW = 0.6f;
        float collisionFactorH = 0.8f;

        int hitboxW = (int)(scaledW * collisionFactorW);
        int hitboxH = (int)(scaledH * collisionFactorH);
        int offsetX = (scaledW - hitboxW) / 2;
        int offsetY = (scaledH - hitboxH) / 2;

        Rectangle spriteRect = new Rectangle(
            (int)(sX + offsetX),
            (int)(sY + offsetY),
            hitboxW,
            hitboxH
        );

        return fireballRect.intersects(spriteRect);
    }
    
    /**
     * Inner class to represent a fading trail particle left by the fireball.
     */
    class Trail {
        float x, y;
        int alpha;
        
        /**
         * Constructs a fading trail particle.
         * @param x X position	
         * @param y Y position
         * @param alpha The Transparency
         */
        public Trail(float x, float y, int alpha) {
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }
    }

}

/**
 * Represents an expanding circular visual effect (after orb collection).
 * Used to indicate enemy removal.
 */
class ExpandingCircle {
    private float x, y; //Positions
    private float radius; // Current radius
    private float maxRadius; // Maximum allowed radius
    private float growthRate; // Speed at which the circle expands
    private boolean active; // Whether the effect is active
    private long startTime; // Start time of the effect
    private long duration = 1000; // 1 second
    
    /**
     * Constructs the expanding circle.
     * @param x Center X position
     * @param y	Center Y position
     * @param maxRadius Maximum radius to reach
     * @param growthRate Speed of growth
     */
    public ExpandingCircle(float x, float y, float maxRadius, float growthRate) {
        this.x = x;
        this.y = y;
        this.radius = 10;
        this.maxRadius = maxRadius;
        this.growthRate = growthRate;
        this.active = true;
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Updates the radius and checks if the animation is finished.
     * @param elapsed Elapsed time in milliseconds.
     */
    public void update(long elapsed) {
        if (!active) return;

        radius += growthRate * (elapsed / 1000f);
        if (radius >= maxRadius || System.currentTimeMillis() - startTime > duration) {
            active = false;
        }
    }
    
    /**
     * Draws the semi-transparent orange expanding circle.
     * @param g Graphics.
     */
    public void draw(Graphics2D g) {
        if (!active) return;

        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));//Compositing rule, draw the source (new pixels) over the destination (existing pixels).
        g.setColor(Color.decode("#F9412A"));
        Ellipse2D.Float circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
        g.fill(circle);
        g.setComposite(old);
    }
    
    /**
     * Returns whether the animation is still active.
     * @return true if active, false otherwise.
     */
    public boolean isActive() {
        return active;
    }
}