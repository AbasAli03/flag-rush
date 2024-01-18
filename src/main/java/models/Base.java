package models;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Base {
    public int x;
    public int y;
    public boolean hasFlag;
    public String stringColor;

    public Base(int x, int y) {
        super();
        this.x = x;
        this.y = y;
        this.hasFlag = false;
    }

    public void draw(GraphicsContext ctx, String stringColor) {
        Color modifiedColor = new Color(getColor(stringColor).getRed(), getColor(stringColor).getGreen(),
                getColor(stringColor).getBlue(), 0.50);

        ctx.setFill(modifiedColor);
        ctx.fillOval(x, y, 50, 50);
    }

    public Color getColor(String color) {
        if (color.equals("RED")) {
            return Color.RED;
        } else {
            return Color.BLUE;
        }

    }
}
