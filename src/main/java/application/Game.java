package application;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import org.jspace.TemplateField;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
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
	Flag flag;
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
	private int clientsJoined;
	private String id;
	Map<String, Player> players;
	Player currentPlayer;
	Player otherPlayer;
	RemoteSpace space3;
	RemoteSpace getting;
	private GameLogic gameLogic;


	public Game(String ip, Canvas canvas, int clientsJoined, String id)
			throws InterruptedException, UnknownHostException, IOException {
				gameLogic = new GameLogic(this);

		RemoteSpace space = new RemoteSpace("tcp://" + ip + ":9001/" + Server.PLAYING_SPACE_NAME + "?keep");
		RemoteSpace space2 = new RemoteSpace("tcp://" + ip + ":9001/" + Server.SERVER_INFO_SPACE_NAME + "?keep");
		RemoteSpace space4 = new RemoteSpace("tcp://" + ip + ":9001/" + Server.GETTING_SPACE_NAME + "?keep");
		space3 = new RemoteSpace("tcp://" + ip + ":9001/" + Server.CLIENTS_IN_SERVER + "?keep");

		this.infoSpace = space2;
		List<Object[]> clientObjects = space3.queryAll(new ActualField("new Client"));
		this.clientsJoined = clientObjects.size();
		this.id = id;
		players = new HashMap<>();
		infoSpace.put("needPlayer");
		infoSpace.put("needPlayer");

		this.canvas = canvas;
		this.ctx = this.canvas.getGraphicsContext2D();

		bluePlayer.put("A", new Image("./assets/BA1.png"));
		bluePlayer.put("D", new Image("./assets/BD1.png"));
		bluePlayer.put("S", new Image("./assets/BS1.png"));
		bluePlayer.put("W", new Image("./assets/BW1.png"));

		redPlayer.put("A", new Image("./assets/RA1.png"));
		redPlayer.put("D", new Image("./assets/RD1.png"));
		redPlayer.put("S", new Image("./assets/RS1.png"));
		redPlayer.put("W", new Image("./assets/RW1.png"));

		player = new Player(40, 40, new Base(40, 40), BOXW, BOXH, "BLUE", bluePlayer, bulletController1);
		player2 = new Player(WIDTH - 100, HEIGHT - 100, new Base(WIDTH - 100, HEIGHT - 100), BOXW, BOXH, "RED",
				redPlayer, bulletController2);
		flag = new Flag(WIDTH / 2, HEIGHT / 2, WIDTH / 2, HEIGHT / 2, BOXW / 2, BOXH / 2);
		if (clientsJoined == 1) {
			currentPlayer = player;
			otherPlayer = player2;
			this.getting = space4;
			this.playing = space;

		} else if (clientsJoined == 2) {
			currentPlayer = player2;
			otherPlayer = player;
			this.getting = space;
			this.playing = space4;
		}

		initializeGrid();

	}

	@Override
	public void run() {
		
		boolean waitingForPlayers = true;

		while (waitingForPlayers) {
			if (clientsJoined < 2) {
				System.out.println("Waiting for players");
				try {
					List<Object[]> clientObjectsUpdated = space3.queryAll(new ActualField("new Client"));
					this.clientsJoined = clientObjectsUpdated.size();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (clientsJoined == 2) {
				waitingForPlayers = false;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		gameRunning = true;
		startGame();
		gameLoop.start();
		new Thread(gameLogic).start();

			

			ScheduledExecutorService dataSenderScheduler = Executors.newScheduledThreadPool(1);
			dataSenderScheduler.scheduleAtFixedRate(() -> {

				try {
					playing.put(currentPlayer.x, currentPlayer.y, currentPlayer.height, currentPlayer.width,
							currentPlayer.flagEquipped, currentPlayer.health, currentPlayer.lastPressed);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}, 0, 16, TimeUnit.MILLISECONDS);

			// Recieve data 60 times per second
			ScheduledExecutorService dataReceiverScheduler = Executors.newScheduledThreadPool(1);
			dataReceiverScheduler.scheduleAtFixedRate(() -> {
				try {
					// System.out.println("before recieve");

					Object[] otherPlayerObjects = getting.get(new FormalField(Integer.class),
							new FormalField(Integer.class), new FormalField(Integer.class),
							new FormalField(Integer.class), new FormalField(Boolean.class),
							new FormalField(Integer.class),
							new FormalField(String.class)

					);

					otherPlayer.x = (Integer) otherPlayerObjects[0];
					otherPlayer.y = (Integer) otherPlayerObjects[1];
					otherPlayer.height = (Integer) otherPlayerObjects[2];
					otherPlayer.width = (Integer) otherPlayerObjects[3];
					otherPlayer.flagEquipped = (Boolean) otherPlayerObjects[4];
					otherPlayer.health = (Integer) otherPlayerObjects[5];

					otherPlayer.lastPressed = (String) otherPlayerObjects[6];

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}, 0, 16, TimeUnit.MILLISECONDS);

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

			int bulletX = currentPlayer.x + player.width / 2;
			int bulletY = currentPlayer.y;
			if (lastPressed.equals("A") || lastPressed.equals("D")) {
				bulletY = currentPlayer.y + currentPlayer.height / 2;
			}
			currentPlayer.bulletController.shoot(bulletX, bulletY, speed, delay, lastPressed);

		}

		if (!isCollidingWithTiles(currentPlayer.x + deltaX, currentPlayer.y + deltaY)) {
			currentPlayer.x += deltaX;
			currentPlayer.y += deltaY;
		}

		SPEED = 5;

		int pickupRange = 20;

		// Check if the player.get(id) is within the pickup range of the flag
		if (Math.abs(currentPlayer.x - flag.x) < pickupRange && Math.abs(currentPlayer.y - flag.y) < pickupRange) {
			if (ePressed) {
				if (flag.equiped && currentPlayer.flagEquipped) {
					flag.equiped = false;
					currentPlayer.flagEquipped = false;
				} else {
					flag.equiped = true;
					currentPlayer.flagEquipped = true;
				}
			}
		}
		// move flag with player.get(id)
		if (currentPlayer.flagEquipped && flag.equiped) {
			flag.x = currentPlayer.x + 5;
			flag.y = currentPlayer.y + 5;

		}
		int dropRange = 50;

		if (Math.abs(currentPlayer.base.x - flag.x) < dropRange
				&& Math.abs(currentPlayer.base.y - flag.y) < dropRange
				&& !flag.equiped && !currentPlayer.flagEquipped) {

			gameLoop.stop();
			displayWinnerPopup(winningPlayer);

		}

		if (currentPlayer.health == 0) {
			winningPlayer = "Red";
			gameLoop.stop();
			displayWinnerPopup(winningPlayer);
		}
		if (otherPlayer.health == 0) {
			winningPlayer = "Blue";
			gameLoop.stop();
			displayWinnerPopup(winningPlayer);
		}

		// removing bullets that collide with enemy
		Iterator<Bullet> iterator = currentPlayer.bulletController.bullets.iterator();
		while (iterator.hasNext()) {
			Bullet bullet = iterator.next();
			if (bullet.isColliding(otherPlayer)) {
				iterator.remove();
			}
		}
		// checking for collisions
		for (Tile tile : tiles) {
			currentPlayer.bulletController.isColliding(tile);

		}
		// System.out.println("Updating player at (" + currentPlayer.x + ", " +
		// currentPlayer.y + ")");

	}

	private void draw() {
		Platform.runLater(() -> {

			// Clear canvas
			ctx.clearRect(0, 0, WIDTH, HEIGHT);

			otherPlayer.draw(ctx);
			currentPlayer.draw(ctx);
			flag.draw(ctx);
			tiles.forEach(tile -> {
				tile.draw(ctx);
			});

			currentPlayer.bulletController.draw(ctx);
			// System.out.println("Drawing player at (" + currentPlayer.x + ", " +
			// currentPlayer.y + ")");

		});

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
			if (newX < tile.getX() + tile.getwidth() && newX + currentPlayer.getwidth() > tile.getX()
					&& newY < tile.getY() + tile.getHeight() && newY + currentPlayer.getHeight() > tile.getY()) {
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
		Platform.runLater(() -> {
			Main.scene.setRoot(new BorderPane(canvas));
			Main.scene.setOnKeyReleased(this::handleKeyReleased);
			Main.scene.setOnKeyPressed(this::handleKeyPressed);
		});

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


	private class GameLogic implements Runnable {
		private Game game;
 
		public GameLogic(Game game) {
			this.game = game;
		}
 
		@Override
		public void run() {
			while (game.gameRunning) {
				try {
					// Sleep for 100ms to slow down the game
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		 
				// Move the game logic here
				game.update();
				game.draw();
			}
		 }

	 
	}
}