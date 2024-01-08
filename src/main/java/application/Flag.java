package application;


import javafx.scene.canvas.*;

import javafx.scene.image.*;

public class Flag {

	private Image image;
	public int x;
	public int y;
	private int spawnX;
	private int spawnY;
	public int height;
	public int width;
	private boolean equiped = false;
	
	public Flag(Image image, int x, int y, int spawnX, int spawnY, int height, int width) {
		super();
		this.image = image;
		this.x = x;
		this.y = y;
		this.spawnX = spawnX;
		this.spawnY = spawnY;
		this.height = height;
		this.width = width;
	}


	public void draw(GraphicsContext ctx) {
		ctx.drawImage(image, x, y,height,width);
	}
	
	
}

