package application;

import javafx.scene.canvas.*;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class Flag {

	private Image image = new Image("./assets/flag.png");
	public int x;
	public int y;
	public int spawnX;
	public int spawnY;
	public int height;
	public int width;
	public boolean equiped = false;

	public Flag(int x, int y, int spawnX, int spawnY, int height, int width) {
		this.x = x;
		this.y = y;
		this.x = spawnX;
		this.y = spawnY;
		this.height = height;
		this.width = width;
	}

	public void draw(GraphicsContext ctx) {
		ctx.drawImage(this.image, x, y, width, height);
	}

	public void restart() {
		this.x = spawnX;
		this.y = spawnY;
		this.equiped = false;
	}

}
