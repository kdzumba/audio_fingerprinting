package org.kdzumba.graphics2d;

import java.awt.*;
import java.awt.geom.Line2D;

import static org.kdzumba.gui.common.Constants.GRID_LINES_COLOR;

public class Line {

    private final Coordinate start;
    private final Coordinate end;
    private Color color;

    public Line(Coordinate start, Coordinate end) {
        this.start = start;
        this.end = end;
        this.color = GRID_LINES_COLOR;
    }

    public Line(Coordinate start, Coordinate end, Color color) {
        this(start, end);
        this.setColor(color);
    }

    public void setColor(Color color) { this.color = color; }

    public void draw(Graphics g) {
        var g2d = (Graphics2D) g;
        g2d.setColor(color);
        var line = new Line2D.Double(this.start.x, this.start.y, this.end.x, this.end.y);
        g2d.draw(line);
    }

    public void update(Coordinate start, Coordinate end) {
        this.start.x = start.x;
        this.start.y = start.y;
        this.end.x = end.x;
        this.end.y = end.y;
    }
}
