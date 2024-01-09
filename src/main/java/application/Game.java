package application;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class Game {

    public static final int HEIGHT = 700;
    public static final int WIDTH = 1000;
    public static int SPEED = 5;
    public static final int BOXW = 20;
    public static final int BOXH = 20;
    public static final int ROWS = WIDTH / BOXW;
    public static final int COLS = HEIGHT / BOXH;
    public static int[][] grid = new int[ROWS][COLS];
    static ArrayList<Tile> tiles = new ArrayList<Tile>();

    public Game() {
        initializeGrid();
    }

    // Game Loop
    private static final long NANO_PER_SECOND = 1_000_000_000L;
    private static final double TARGET_UPS = 60.0; 
    private static final double TIME_PER_UPDATE = 1.0 / TARGET_UPS;
    long lastUpdateTime;
    AnimationTimer gameLoop;
    GraphicsContext ctx = Main.canvas.getGraphicsContext2D();
    BulletController bulletController1 = new BulletController();
    BulletController bulletController2 = new BulletController();
    Player player = new Player(40, 40, new Base(40, 40), BOXW, BOXH, Color.BLUE, bulletController1);
    Player player2 = new Player(WIDTH - 100, HEIGHT - 100, new Base(WIDTH - 100, HEIGHT - 100), BOXW, BOXH, Color.RED,
            bulletController2);
    Flag flag = new Flag(WIDTH / 2, HEIGHT / 2, WIDTH / 2, HEIGHT / 2, BOXW / 2, BOXH / 2);

    boolean wPressed = false;
    boolean aPressed = false;
    boolean sPressed = false;
    boolean dPressed = false;
    boolean ePressed = false;
    boolean winner = false;
    boolean spacePressed = false;
    String lastPressed = "";

    public static void initializeGrid() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (i == 0 || i == ROWS - 1 || j == 0 || j == COLS - 1) {
                    grid[i][j] = 1;
                }
            }
        }

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] == 1) {
                    tiles.add(new Tile(i * BOXW, j * BOXW, BOXW, BOXH));
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
            case SPACE:
                spacePressed = false;
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
            case SPACE:
                spacePressed = true;
            default:
                break;
        }
    }

    private void update() {

        SPEED = 5;

        int deltaX = 0;
        int deltaY = 0;

        if (wPressed) {
            lastPressed = "W";
            deltaY = -SPEED;
        } else if (aPressed) {
            lastPressed = "A";
            deltaX = -SPEED;
        } else if (sPressed) {
            lastPressed = "S";
            deltaY = SPEED;
        } else if (dPressed) {
            lastPressed = "D";
            deltaX = SPEED;
        }
        if (spacePressed) {
            int speed = 5;
            int delay = 7;

            int bulletX = player.x + player.width / 2;
            int bulletY = player.y;
            if (lastPressed.equals("A") || lastPressed.equals("D")) {
                bulletY = player.y + player.height / 2;
            }
            player.bulletController.shoot(bulletX, bulletY, speed, delay, lastPressed);

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
        int dropRange = 50;

        if (Math.abs(player.base.x - flag.x) < dropRange && Math.abs(player.base.y - flag.y) < dropRange
                && !flag.equiped && !player.flagEquipped) {
            gameLoop.stop();
            displayWinnerPopup();

        }

        Iterator<Bullet> iterator = player.bulletController.bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            if (bullet.isColliding(player2)) {
                iterator.remove();
            }
        }

        for (Tile tile : tiles) {
            player.bulletController.isColliding(tile);

        }

    }

    private void displayWinnerPopup() {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Winner!");
            alert.setHeaderText(null);
            alert.setContentText("Congratulations! You are the winner.");

            alert.showAndWait();
        });
    }

    private boolean isCollidingWithTiles(int newX, int newY) {
        for (Tile tile : tiles) {
            if (newX < tile.getX() + tile.getwidth() &&
                    newX + player.getwidth() > tile.getX() &&
                    newY < tile.getY() + tile.getHeight() &&
                    newY + player.getHeight() > tile.getY()) {
                return true; // Collision detected
            }
        }
        return false; // No collision
    }

    private void draw() {
        // Clear canvas
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        player2.draw(ctx);
        player.draw(ctx);
        flag.draw(ctx);
        tiles.forEach(tile -> {
            tile.draw(ctx);
        });
        player.bulletController.draw(ctx);

    }

    public void startGame() {
        Main.canvas.setOnKeyReleased(this::handleKeyReleased);
        Main.canvas.setOnKeyPressed(this::handleKeyPressed);

        lastUpdateTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
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

        gameLoop.start();

    }
}
