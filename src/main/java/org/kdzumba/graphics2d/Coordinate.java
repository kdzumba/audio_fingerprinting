package org.kdzumba.graphics2d;

import java.awt.*;

public class Coordinate {
    public double x;
    public double y;
    private Color color;

    public Coordinate() {
        this.x = 0.0;
        this.y = 0.0;
        this.color = Color.WHITE;
    }

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
        this.color = Color.WHITE;
    }

    public Coordinate(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);
        g2d.fillOval((int) x, (int) y, 5, 5);
    }
}
