package models;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet {
	public int x;
	public int y;
	public int speed;
	private int damage;
	private int height;
	private int width;
	private String stringColor;
	public String direction;

	public Bullet(int x, int y, int speed, String direction) {
		this.direction = direction;
		this.x = x;
		this.y = y;
		this.speed = speed;
		this.width = 5;
		this.height = 5;
		this.stringColor = "BLACK";
		this.damage = 10;
	}

	public Color getColor() {
		return Color.BLACK;

	}

	public void draw(GraphicsContext ctx) {
		ctx.setFill(getColor());
		ctx.fillRect(x, y, width, height);
	}

	public boolean isColliding(Collidable obj) {

		if (this.x < obj.getX() + obj.getwidth() &&
				this.x + this.width > obj.getX() &&
				this.y < obj.getY() + obj.getHeight() &&
				this.y + this.height > obj.getY()) {

			if (obj instanceof Player) {
				Player player = (Player) obj;
				player.health -= this.damage;
			}

			return true;
		}

		return false;
	}

}