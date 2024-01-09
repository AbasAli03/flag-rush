package application;


import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class Game {

  
    public static final int HEIGHT = 700;
    public static final int WIDTH = 1000;
    public static int SPEED = 5;
    public static final int BOXW = 20;
    public static final int BOXH = 20;  
    public static final int ROWS = WIDTH/BOXW;
    public static final int COLS = HEIGHT/BOXH;
    public static int  [][] grid = new int[ROWS][COLS];
    static ArrayList<Tile> tiles = new ArrayList<Tile>();

    public Game() {
    	initializeGrid();
    }


    // Game Loop
    private static final long NANO_PER_SECOND = 1_000_000_000L;
    private static final double TARGET_UPS = 60.0; // Target updates per second
    private static final double TIME_PER_UPDATE = 1.0 / TARGET_UPS;
    long lastUpdateTime;
    GraphicsContext ctx = Main.canvas.getGraphicsContext2D();
    BulletController bulletController1 = new BulletController();
    Player player = new Player(40,40,10,10,BOXW,BOXH,Color.BLUE,bulletController1);
    Flag flag = new Flag(WIDTH/2,HEIGHT/2,WIDTH/2,HEIGHT/2,BOXW/2,BOXH/2);

    boolean wPressed = false;
    boolean aPressed = false;
    boolean sPressed = false;
    boolean dPressed = false;
    boolean ePressed = false;
    int eCount = 0;
    public static void initializeGrid() {
        // Fill edges with 1s
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (i == 0 || i == ROWS - 1 || j == 0 || j == COLS - 1) {
                    grid[i][j] = 1;
                }
            }
        }
        
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
            	if(grid[i][j]==1) {
            		tiles.add(new Tile(i*BOXW,j*BOXW,BOXW,BOXH));
            	}
            	
            }
            }
    }
    
    private void handleKeyReleased(KeyEvent event) {
        switch (event.getCode()) {
            case W:
                wPressed = false;
                break;
            case A:
                aPressed = false;
                break;
            case S:
                sPressed = false;
                break;
            case D:
                dPressed = false;
                break;
            case E: 
            	ePressed = false;
            // Add more cases if needed for other keys
		default:
			break;
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case W:
                wPressed = true;
                break;
            case A:
                aPressed = true;
                break;
            case S:
                sPressed = true;
                break;
            case D:
                dPressed = true;
                break;
            case E: 
            	ePressed = true;
            // Add more cases if needed for other keys
		default:
			break;
        }
    }
    private void update() {
        SPEED = 5;
    
        int deltaX = 0;
        int deltaY = 0;
    
        if (wPressed) {
            deltaY = -SPEED;
        } else if (aPressed) {
            deltaX = -SPEED;
        } else if (sPressed) {
            deltaY = SPEED;
        } else if (dPressed) {
            deltaX = SPEED;
        }
    
        if (!isCollidingWithTiles(player.x + deltaX, player.y + deltaY)) {
            player.x += deltaX;
            player.y += deltaY;
        }
    
        SPEED = 5;
        
        int pickupRange = 20;

     // Check if the player is within the pickup range of the flag
        if (Math.abs(player.x - flag.x) < pickupRange && Math.abs(player.y - flag.y) < pickupRange) {
            if (ePressed) {
                if (flag.equiped && player.flagEquipped) {
                    flag.equiped = false;
                    player.flagEquipped = false;
                } else {
                    flag.equiped = true;
                    player.flagEquipped = true;
                }
            }
        }
        // move flag with player
        if (player.flagEquipped && flag.equiped) {
            flag.x = player.x + 5;
            flag.y = player.y + 5;
        }
    
        
        
        
        
    }
    
    private boolean isCollidingWithTiles(int newX, int newY) {
        for (Tile tile : tiles) {
            if (newX < tile.getX() + tile.getwidth() &&
                newX + player.getwidth() > tile.getX() &&
                newY < tile.getY() + tile.getHeight() &&
                newY + player.getHeight() > tile.getY()) {
                return true;  // Collision detected
            }
        }
        return false;  // No collision
    }
    private void draw() {
        // Clear canvas
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
    
        player.draw(ctx);
        flag.draw(ctx);
        tiles.forEach(tile -> {
        	tile.draw(ctx);
        });
       
        
    }
   
    
   
    public void startGame() {
        // Set up key event handlers for the canvas
        Main.canvas.setOnKeyReleased(this::handleKeyReleased);
        Main.canvas.setOnKeyPressed(this::handleKeyPressed);

        // Initialize time variables
        lastUpdateTime = System.nanoTime();

        // Set up the game loop
        AnimationTimer gameLoop = new AnimationTimer() {
            double accumulatedTime = 0.0;

            @Override
            public void handle(long currentTime) {
                double elapsedTime = (currentTime - lastUpdateTime) / (double) NANO_PER_SECOND;

                accumulatedTime += elapsedTime;

                while (accumulatedTime >= TIME_PER_UPDATE) {
                    update();
                    accumulatedTime -= TIME_PER_UPDATE;
                }

                draw();

                lastUpdateTime = currentTime;
            }
        };

        // Start the game loop
        gameLoop.start();
    }
}
