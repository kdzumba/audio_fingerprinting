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

    public static void showGrid(Graphics g, int startX, int startY, int width, int height) {
        int BLOCK_SIZE = 10;
        for(int i = 0; i < width; i ++) {
            // Create a new line at every ith multiple of BLOCK_SIZE
            Coordinate start = new Coordinate(i * BLOCK_SIZE + startX, 0);
            Coordinate end = new Coordinate(i * BLOCK_SIZE + startX, height);
            Line line = new Line(start, end);
            line.draw(g);
        }

        for(int j = 0; j < height; j++) {
            // Create a new vertical line for every jth multiple of BLOCK_SIZE
            Coordinate start = new Coordinate(startX, j * BLOCK_SIZE + startY);
            Coordinate end = new Coordinate(width, j * BLOCK_SIZE + startY);
            Line line = new Line(start, end);
            line.draw(g);
        }
    }

    public static Color getColorForRatio(Color[] colors, float ratio) {
        int index = (int) (ratio * (colors.length - 1));
        float fraction = ratio * (colors.length - 1) - index;

        if (index >= colors.length - 1) {
            return colors[colors.length - 1];
        } else {
            Color color1 = colors[index];
            Color color2 = colors[index + 1];
            int red = (int) (color1.getRed() * (1 - fraction) + color2.getRed() * fraction);
            int green = (int) (color1.getGreen() * (1 - fraction) + color2.getGreen() * fraction);
            int blue = (int) (color1.getBlue() * (1 - fraction) + color2.getBlue() * fraction);
            return new Color(red, green, blue);
        }
    }
}
