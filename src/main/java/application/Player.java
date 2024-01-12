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
    public int speed = Game.SPEED;
    public Base base;
    public BulletController bulletController;
    public Map<String, Image> images;
    public String stringColor;
    public String lastPressed;

    public Player(int x, int y, Base base, int height, int width, String stringColor, Map<String, Image> images,
            BulletController bulletController) {
        this.lastPressed="W";
        this.bulletController = bulletController;
        this.base = base;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;

        this.images = images;
        flagEquipped = false;
        health = 100;
        this.stringColor = stringColor;
        if (stringColor.equals("RED")) {
            this.color = Color.RED;
        } else {
            this.color = Color.BLUE;
        }

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
        ctx.drawImage(images.get(lastPressed), x, y, width, height);
        base.draw(ctx,this.stringColor);

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

    public Color getColor(String color) {
        if (color.equals("RED")) {
            return Color.RED;
        } else {
            return Color.BLUE;
        }
    }

}