package application;

import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Player implements Collidable {
    public int x;
    public int y;
    public int height;
    public int width;
    private Color color;
    public boolean flagEquipped;
    public int health;
    public Base base;
    public BulletController bulletController;
    public Map<String,Image> images;
    

    public Player(int x, int y, Base base, int height, int width,Color color, Map<String,Image> images , BulletController bulletController) {
        this.bulletController = bulletController;
        this.base = base;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.color = color;
        this.images = images;
        flagEquipped = false;
        health = 100;

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

    public void draw(GraphicsContext ctx, String lastPressed) {
        ctx.drawImage(images.get(lastPressed), x, y, width, height);
        base.draw(ctx, this.color);

    }

    public boolean isColliding(Collidable obj) {

        if (this.x  < obj.getX() + obj.getwidth() &&
                this.x + this.width > obj.getX() &&
                this.y  < obj.getY() + obj.getHeight() &&
                this.y + this.height > obj.getY()) {
            return true;
        }

        return false;
    }

}
