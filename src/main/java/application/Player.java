package application;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Player implements Collidable {
    public int x;
    public int y;
    public int baseX;
    public int baseY;
    public int height;
    public int width;
    private Color color;
    public boolean flagEquipped;
    public int health;
    public int speed = Game.SPEED;
    public Base base;
    public BulletController bulletController;

    public Player(int x, int y, Base base, int height, int width, Color color, BulletController bulletController) {
        this.bulletController = bulletController;
        this.base = base;
        this.x = x;
        this.y = y;
        this.baseX = baseX;
        this.baseY = baseY;
        this.height = height;
        this.width = width;
        this.color = color;
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

    public void draw(GraphicsContext ctx) {
        ctx.setFill(this.color);
        ctx.fillRect(x, y, width, height);
        base.draw(ctx, this.color);

    }

    public boolean isColliding(Collidable obj) {

        if (this.x + speed < obj.getX() + obj.getwidth() &&
                this.x + this.width - speed > obj.getX() &&
                this.y + speed < obj.getY() + obj.getHeight() &&
                this.y + this.height + speed > obj.getY()) {
            return true;
        }

        return false;
    }

}
