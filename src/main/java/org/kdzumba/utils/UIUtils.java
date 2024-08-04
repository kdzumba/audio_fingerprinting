package org.kdzumba.utils;

import org.kdzumba.graphics2d.Coordinate;
import org.kdzumba.graphics2d.Line;

import java.awt.*;

public class UIUtils {
    public static void showGrid(Graphics g, int width, int height) {
        int BLOCK_SIZE = 10;
        for(int i = 0; i < width; i ++) {
            // Create a new line at every ith multiple of BLOCK_SIZE
            Coordinate start = new Coordinate(i * BLOCK_SIZE, 0);
            Coordinate end = new Coordinate(i * BLOCK_SIZE, height);
            Line line = new Line(start, end);
            line.draw(g);
        }

        for(int j = 0; j < height; j++) {
            // Create a new vertical line for every jth multiple of BLOCK_SIZE
            Coordinate start = new Coordinate(0, j * BLOCK_SIZE);
            Coordinate end = new Coordinate(width, j * BLOCK_SIZE);
            Line line = new Line(start, end);
            line.draw(g);
        }
    }
}
