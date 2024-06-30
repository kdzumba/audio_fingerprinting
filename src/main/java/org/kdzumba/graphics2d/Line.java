package org.kdzumba.graphics2d;

import java.awt.*;
import java.awt.geom.Line2D;

public class Line {

    private final Coordinate start;
    private final Coordinate end;

    public Line(Coordinate start, Coordinate end) {
        this.start = start;
        this.end = end;
    }

    public void draw(Graphics g) {
        var g2d = (Graphics2D) g;
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
