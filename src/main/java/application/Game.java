package application;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import org.jspace.TemplateField;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
	boolean disconected = false;

	volatile boolean gameEnded = false;

	Space playing;

	private int clientsJoined;
	private String id;
	Map<String, Player> players;
	Player currentPlayer;
	Player otherPlayer;
	RemoteSpace space3;
	RemoteSpace getting;
	RemoteSpace actionSpace;
	private String ip;

	private GameLogic gameLogic;
	SpaceRepository repository;

	public Game(String ip, Canvas canvas, int clientsJoined, String id, SpaceRepository repository)
			throws InterruptedException, UnknownHostException, IOException {
		gameLogic = new GameLogic(this);
		this.repository = repository;
		RemoteSpace space = new RemoteSpace("tcp://" + ip + ":9001/" + Server.PLAYING_SPACE_NAME + "?keep");
		RemoteSpace space4 = new RemoteSpace("tcp://" + ip + ":9001/" + Server.GETTING_SPACE_NAME + "?keep");

		space3 = new RemoteSpace("tcp://" + ip + ":9001/" + Server.CLIENTS_IN_SERVER + "?keep");
		actionSpace = new RemoteSpace("tcp://" + ip + ":9001/" + Server.CLIENTS_IN_SERVER + "?keep");
		List<Object[]> clientObjects = space3.queryAll(new ActualField("new Client"));
		this.clientsJoined = clientObjects.size();
		this.id = id;
		players = new HashMap<>();

		this.canvas = canvas;
		this.ctx = this.canvas.getGraphicsContext2D();
		this.ip = ip;
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
						currentPlayer.flagEquipped, otherPlayer.health, currentPlayer.lastPressed, flag.x, flag.y,
						flag.equiped, currentPlayer.bulletController, gameEnded);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}, 0, 16, TimeUnit.MILLISECONDS);

		// Recieve data 60 times per second
		ScheduledExecutorService dataReceiverScheduler = Executors.newScheduledThreadPool(1);
		dataReceiverScheduler.scheduleAtFixedRate(() -> {
			try {
				Object[] otherPlayerObjects = getting.get(new FormalField(Integer.class),
						new FormalField(Integer.class), new FormalField(Integer.class),
						new FormalField(Integer.class), new FormalField(Boolean.class),
						new FormalField(Integer.class),
						new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class),
						new FormalField(Boolean.class), new FormalField(BulletController.class),
						new FormalField(Boolean.class)

				);

				otherPlayer.x = (Integer) otherPlayerObjects[0];
				otherPlayer.y = (Integer) otherPlayerObjects[1];
				otherPlayer.height = (Integer) otherPlayerObjects[2];
				otherPlayer.width = (Integer) otherPlayerObjects[3];
				otherPlayer.flagEquipped = (Boolean) otherPlayerObjects[4];
				currentPlayer.health = (Integer) otherPlayerObjects[5];

				otherPlayer.lastPressed = (String) otherPlayerObjects[6];
				flag.x = (Integer) otherPlayerObjects[7];
				flag.y = (Integer) otherPlayerObjects[8];
				flag.equiped = (Boolean) otherPlayerObjects[9];
				otherPlayer.bulletController = (BulletController) otherPlayerObjects[10];
				gameEnded = (boolean) otherPlayerObjects[11];

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, 0, 16, TimeUnit.MILLISECONDS);
	}

	public static void initializeGrid() {
		grid = Grid.map;

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
		} else if (spacePressed && !ePressed) {
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

		int pickupRange = 30;

		// Check if the currentplayer is within the pickup range of the flag
		if (ePressed) {
			if (Math.abs(currentPlayer.x - flag.x) < pickupRange && Math.abs(currentPlayer.y - flag.y) < pickupRange) {

				if (flag.equiped && currentPlayer.flagEquipped) {
					flag.equiped = false;
					currentPlayer.flagEquipped = false;

				} else {
					flag.equiped = true;
					currentPlayer.flagEquipped = true;

				}

			}
		}
		// move flag with currentPLayer
		if (currentPlayer.flagEquipped && flag.equiped) {
			flag.x = currentPlayer.x;
			flag.y = currentPlayer.y;

		} else if (!currentPlayer.flagEquipped && flag.equiped) {
			flag.x = otherPlayer.x;
			flag.y = otherPlayer.y;
		}

		int dropRange = 50;

		if (Math.abs(currentPlayer.base.x - flag.x) < dropRange
				&& Math.abs(currentPlayer.base.y - flag.y) < dropRange
				&& !flag.equiped && !currentPlayer.flagEquipped) {
			winningPlayer = currentPlayer.stringColor;

			gameEnded = true;

		}

		if (currentPlayer.health == 0) {
			winningPlayer = otherPlayer.stringColor;

			gameEnded = true;
		}
		if (otherPlayer.health == 0) {
			winningPlayer = currentPlayer.stringColor;

			gameEnded = true;
		}

		// Remove bullets that collide with the enemy
		Iterator<Bullet> iterator = currentPlayer.bulletController.bullets.iterator();
		while (iterator.hasNext()) {
			Bullet bullet = iterator.next();

			// Check for collisions with the other player
			if (bullet.isColliding(otherPlayer)) {

				iterator.remove();
			}

			// Check for collisions with tiles
			for (Tile tile : tiles) {
				if (bullet.isColliding(tile)) {
					iterator.remove();
				}
			}
		}

	}

	private void draw() {
		Platform.runLater(() -> {

			// Clear canvas
			ctx.clearRect(0, 0, WIDTH, HEIGHT);
			otherPlayer.draw(ctx);
			currentPlayer.draw(ctx);
			tiles.forEach(tile -> {
				tile.draw(ctx);
			});
			flag.draw(ctx);

			currentPlayer.bulletController.draw(ctx);
			otherPlayer.bulletController.draw(ctx);

		});

	}

	private void displayWinnerPopup(String winner) {
		Platform.runLater(() -> {
			gameRunning = false;

			Alert customAlert = new Alert(Alert.AlertType.CONFIRMATION);
			customAlert.setTitle("Game Done!");
			customAlert.setHeaderText("Winner is " + winner + "Choose an action:");

			ButtonType restartButton = new ButtonType("Restart");
			ButtonType goToHomeButton = new ButtonType("Go to Home");

			customAlert.getButtonTypes().setAll(restartButton, goToHomeButton);
			AtomicBoolean stopThreadFlag = new AtomicBoolean(false);

			new Thread(() -> {
				try {
					space3.query(new ActualField("disconnect"));
					Main.server.shutdownServer(repository, ip);
					switchToHome();
					stopThreadFlag.set(true);

				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}).start();

			new Thread(() -> {
				try {
					int restarts;
					do {
						restarts = space3.queryAll(new ActualField("restart")).size();
						Thread.sleep(1000);
					} while (restarts < 2 && !stopThreadFlag.get());
					stopThreadFlag.set(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
			customAlert.showAndWait().ifPresent(response -> {

				if (response == restartButton) {
					try {
						space3.put("restart");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} else if (response == goToHomeButton) {
					try {
						stopThreadFlag.set(true);
						space3.put("disconnect");
						switchToHome();
						Main.server.shutdownServer(repository, ip);

					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}

				}
			});
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

	private void switchToHome() {
		try {
			Main.root = FXMLLoader.load(getClass().getResource("/application/Home.fxml"));
			Parent root = Main.root;
			Main.scene.setRoot(root);
			Main.stage.show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

				game.update();
				game.draw();
				if (gameEnded) {
					gameRunning = false;

					game.displayWinnerPopup(winningPlayer);

				}
			}

		}

	}
}