package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jspace.ActualField;
import org.jspace.Space;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class Game implements Runnable {

	public static final int HEIGHT = 700;
	public static final int WIDTH = 1000;
	public static int SPEED = 5;
	public static final int BOXW = 20;
	public static final int BOXH = 20;
	public static final int ROWS = WIDTH / BOXW;
	public static final int COLS = HEIGHT / BOXH;
	public static int[][] grid = new int[ROWS][COLS];
	static ArrayList<Tile> tiles = new ArrayList<Tile>();

	// Game Loop
	private static final long NANO_PER_SECOND = 1_000_000_000L;
	private static final double TARGET_UPS = 60.0;
	private static final double TIME_PER_UPDATE = 1.0 / TARGET_UPS;
	long lastUpdateTime;
	AnimationTimer gameLoop;

	// game
	Canvas canvas;
	GraphicsContext ctx;
	BulletController bulletController1 = new BulletController();
	BulletController bulletController2 = new BulletController();

	Map<String, Image> bluePlayer = new HashMap<String, Image>();
	Map<String, Image> redPlayer = new HashMap<String, Image>();

	Player player;
	Player player2;
	Flag flag = new Flag(WIDTH / 2, HEIGHT / 2, WIDTH / 2, HEIGHT / 2, BOXW / 2, BOXH / 2);
	String winningPlayer = "";

	// controls
	boolean wPressed = false;
	boolean aPressed = false;
	boolean sPressed = false;
	boolean dPressed = false;
	boolean ePressed = false;
	boolean winner = false;
	boolean spacePressed = false;
	String lastPressed = "";
	boolean gameRunning = false;

	Space playing, infoSpace;
	private int connected = 0;
	private boolean started = false;
	private ArrayList<String> clients = new ArrayList<>();

	public Game(Space playing, Space infoSpace, Canvas canvas) throws InterruptedException {
		this.playing = playing;
		this.infoSpace = infoSpace;
		infoSpace.put("needPlayer");
		infoSpace.put("needPlayer");
		ctx.setFill(Color.BLACK);

		ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		bluePlayer.put("A", new Image("./assets/BA1.png"));
		bluePlayer.put("D", new Image("./assets/BD1.png"));
		bluePlayer.put("S", new Image("./assets/BS1.png"));
		bluePlayer.put("W", new Image("./assets/BW1.png"));

		redPlayer.put("A", new Image("./assets/RA1.png"));
		redPlayer.put("D", new Image("./assets/RD1.png"));
		redPlayer.put("S", new Image("./assets/RS1.png"));
		redPlayer.put("W", new Image("./assets/RW1.png"));

		player = new Player(40, 40, new Base(40, 40), BOXW, BOXH, Color.BLUE, bluePlayer, bulletController1);
		player2 = new Player(WIDTH - 100, HEIGHT - 100, new Base(WIDTH - 100, HEIGHT - 100), BOXW, BOXH, Color.RED,
				redPlayer, bulletController2);
		initializeGrid();

	}

	@Override
	public void run() {
		try {
			if (connected < 2) {
				System.out.println("Waiting for players");
				infoSpace.get(new ActualField("gotPlayers"));
			}
			if (connected == 2) {
				gameRunning = true;

			}
			while (gameRunning) {
				gameLoop.start();

				// Send data 60 times per second
				/*
				 * ScheduledExecutorService dataSenderScheduler =
				 * Executors.newScheduledThreadPool(1);
				 * dataSenderScheduler.scheduleAtFixedRate(() -> {
				 * System.out.println("hello");
				 * }, 0, 16, TimeUnit.MILLISECONDS);
				 * 
				 * // Recieve data 60 times per second
				 * ScheduledExecutorService dataReceiverScheduler =
				 * Executors.newScheduledThreadPool(1);
				 * dataReceiverScheduler.scheduleAtFixedRate(() -> {
				 * 
				 * }, 0, 16, TimeUnit.MILLISECONDS);
				 */
			}
		} catch (InterruptedException e) {
			// TODO: handle exception
		}

	}

	public static void initializeGrid() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				if (i == 0 || i == ROWS - 1 || j == 0 || j == COLS - 1) {
					grid[i][j] = 1;
				}
			}
		}

		Random random = new Random();
		double probability = 0.2;

		// randomize map
		for (int i = 1; i < ROWS - 1; i++) {
			for (int j = 1; j < COLS - 1; j++) {
				if (random.nextDouble() < probability) {
					grid[i][j] = 1;
				}
			}
		}

		// avoid base
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				grid[i][j] = 0;

			}
		}
		// avoid base
		for (int i = ROWS - 6; i < ROWS - 1; i++) {
			for (int j = COLS - 6; j < COLS - 1; j++) {
				grid[i][j] = 0;

			}
		}

		// avoid flag
		for (int i = (ROWS / 2) - 2; i < (ROWS / 2) + 3; i++) {
			for (int j = (COLS / 2) - 2; j < (COLS / 2) + 3; j++) {
				grid[i][j] = 0;

			}
		}

		// create tiles
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				if (grid[i][j] == 1) {
					tiles.add(new Tile(i * BOXW, j * BOXH, BOXW, BOXH));
				}

			}
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
		} else if (spacePressed) {
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
			displayWinnerPopup(winningPlayer);

		}

		if (player.health == 0) {
			winningPlayer = "Red";
			gameLoop.stop();
			displayWinnerPopup(winningPlayer);
		}
		if (player2.health == 0) {
			winningPlayer = "Blue";
			gameLoop.stop();
			displayWinnerPopup(winningPlayer);
		}

		// removing bullets that collide with enemy
		Iterator<Bullet> iterator = player.bulletController.bullets.iterator();
		while (iterator.hasNext()) {
			Bullet bullet = iterator.next();
			if (bullet.isColliding(player2)) {
				iterator.remove();
			}
		}
		// checking for collisions
		for (Tile tile : tiles) {
			player.bulletController.isColliding(tile);

		}

	}

	private void draw() {
		// Clear canvas
		ctx.clearRect(0, 0, WIDTH, HEIGHT);
		player2.draw(ctx, lastPressed);
		player.draw(ctx, lastPressed);
		flag.draw(ctx);
		tiles.forEach(tile -> {
			tile.draw(ctx);
		});
		player.bulletController.draw(ctx);

	}

	private void displayWinnerPopup(String winner) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Winner!");
			alert.setHeaderText(null);
			alert.setContentText("Congratulations! " + winner + " You win!");

			alert.showAndWait();
		});
	}

	private boolean isCollidingWithTiles(int newX, int newY) {
		for (Tile tile : tiles) {
			if (newX < tile.getX() + tile.getwidth() && newX + player.getwidth() > tile.getX()
					&& newY < tile.getY() + tile.getHeight() && newY + player.getHeight() > tile.getY()) {
				return true; // Collision detected
			}
		}
		return false; // No collision
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

	public void startGame() {
		canvas.setOnKeyReleased(this::handleKeyReleased);
		canvas.setOnKeyPressed(this::handleKeyPressed);

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

	}

	public void addPlayer(String name) throws InterruptedException {
		if (clients.contains(name)) {
			infoSpace.put("needPlayer");
			return;
		}
		clients.add(name);
		connected++;
		if (connected == 2) {
			infoSpace.put("gotPlayers");
		}
	}

	public int connectedPlayers() {
		return connected;
	}

}