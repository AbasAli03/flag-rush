package application;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet {
	public int x;
	public int y;
	private int speed;
	private int damage;
	private int height;
	private int width;
	private  Color color;
	
	public Bullet(int x, int y, int speed, int damage) {
		super();
		this.x = x;
		this.y = y;
		this.speed = speed;
		this.damage = damage;
		this.width = 5;
		this.height = 5;
		this.color = Color.RED;
	}
	
	public void draw(GraphicsContext ctx) {
		ctx.setFill(this.color);
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
	
