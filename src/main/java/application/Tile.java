package application;


import javafx.scene.image.*;

import javafx.scene.canvas.GraphicsContext;

public class Tile implements Collidable{
	private Image image;
	public int x;
	public int y;
	public int height;
	public int width;

	public Tile(Image image, int x, int y, int height, int width) {
		this.image = image;
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	public void draw(GraphicsContext ctx) {
		ctx.drawImage(image,x,y,height,width);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getwidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
