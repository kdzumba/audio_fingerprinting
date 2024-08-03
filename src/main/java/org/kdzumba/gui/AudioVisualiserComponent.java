package org.kdzumba.gui;

import org.kdzumba.graphics2d.Coordinate;
import org.kdzumba.graphics2d.Line;

import javax.swing.*;
import java.awt.*;
import java.util.Queue;

import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class AudioVisualiserComponent extends JComponent {
    private final int WIDTH = 700;
    private final int HEIGHT = 200;
    private boolean showGrid = false;
    private final Queue<Short> samples;

    public AudioVisualiserComponent(Queue<Short> samples) {
        this.samples = samples;
    }

    private void showGrid(Graphics g) {
        int BLOCK_SIZE = 10;
        for(int i = 0; i < WIDTH; i ++) {
            // Create a new line at every ith multiple of BLOCK_SIZE
            Coordinate start = new Coordinate(i * BLOCK_SIZE, 0);
            Coordinate end = new Coordinate(i * BLOCK_SIZE, HEIGHT);
            Line line = new Line(start, end);
            line.draw(g);
        }

        for(int j = 0; j < HEIGHT; j++) {
            // Create a new vertical line for every jth multiple of BLOCK_SIZE
            Coordinate start = new Coordinate(0, j * BLOCK_SIZE);
            Coordinate end = new Coordinate(WIDTH, j * BLOCK_SIZE);
            Line line = new Line(start, end);
            line.draw(g);
        }
    }

    private void drawSoundWave(Graphics g) {
        if(samples.isEmpty()) { return ; }

        int width = getWidth();
        int height = getHeight();
        int midHeight = height / 2;
        double xIncrement = (double) width / samples.size();
        double yScale = 32768.0 / midHeight;

        double x = 0;
        for(Short sample : samples) {
            double y = midHeight - sample / yScale;

            Coordinate start = new Coordinate(x, midHeight);
            Coordinate end = new Coordinate(x, y);

            Line line = new Line(start, end, Color.WHITE);
            line.draw(g);
            x += xIncrement;
        }
    } 

    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; }
    public boolean getShowGrid() { return this.showGrid; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(VISUALIZER_BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        if(this.showGrid) {
            showGrid(g);
        }
        drawSoundWave(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }
}
