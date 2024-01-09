package application;

import javafx.scene.canvas.GraphicsContext;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Base {
    public int x;
    public int y;
    public boolean hasFlag;

    public Base(int x, int y) {
        super();
        this.x = x;
        this.y = y;
        this.hasFlag = false;
    }

    public void draw(GraphicsContext ctx, Color color) {
        Color modifiedColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.50);

        ctx.setFill(modifiedColor);
        ctx.fillOval(x, y, 50, 50);
    }
}

