package application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;

public class BulletController {
		
	public List<Bullet> bullets;
	private int time = 0;
	
	public BulletController() {
		bullets = new ArrayList<>(); 
	}
	
	
	public void shoot(int x, int y, int speed, int damage, int delay) {
			if(this.time <= 0) {
				this.bullets.add(new Bullet(x,y,speed,damage));
				this.time = delay;
			}
			
			this.time--;
	}
	
	public void draw(GraphicsContext ctx) {
		for(Bullet bullet : this.bullets) {
			bullet.draw(ctx);
		}
	}
	
	public boolean isColliding(Collidable obj){
		for(Bullet bullet : this.bullets) {
			if(bullet.isColliding(obj)) {
				this.bullets.remove(bullet);
				return true;
			}
		}
		
		return false;
		
	}
}
