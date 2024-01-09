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

    boolean wPressed = false;
    boolean aPressed = false;
    boolean sPressed = false;
    boolean dPressed = false;
    boolean ePressed = false;
    boolean isColliding =  false;
    
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

        if (wPressed) {
            player.y -= SPEED;
        } else if (aPressed) {
            player.x -= SPEED;
        } else if (sPressed) {
            player.y += SPEED;
        } else if (dPressed) {
            player.x += SPEED;
        }
        
        boolean isColliding = false;
        for (Tile tile : tiles) {
            if (player.isColliding(tile)) {
                SPEED = 0;
                break;
            }
        }

        // Additional game logic can be added here

        // if(ePressed) {
        //    int speed = 5;
        //    int delay = 7;
        //    int damage = 1;
        //    int bulletX = player.x + player.width / 2;
        //    int bulletY = player.y;
        //    player.bulletController.shoot(bulletX, bulletY, speed, damage, delay);
        // }
    }


    private void draw() {
        // Clear canvas
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
    
        player.draw(ctx);
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
