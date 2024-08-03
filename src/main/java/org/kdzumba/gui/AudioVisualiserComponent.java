package org.kdzumba.gui;

import org.kdzumba.graphics2d.Coordinate;
import org.kdzumba.graphics2d.Line;

import javax.swing.*;
import java.awt.*;

import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class AudioVisualiserComponent extends JComponent {
    private final int WIDTH = 800;
    private final int HEIGHT = 400;
    private boolean showGrid = false;
    short[] samples = new short[44100];

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
        for(int i = 0; i < samples.length - 1; i++) {
            Coordinate start = new Coordinate(i, samples[i]);
            Coordinate end = new Coordinate(i + 1, samples[i + 1]);
            Line line = new Line(start, end, Color.WHITE);
            line.draw(g);
        }
    } 

    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; }
    public boolean getShowGrid() { return this.showGrid; }
      
    public void setSamples(short[] samples) { this.samples = samples; }

    @Override
    protected void paintComponent(Graphics g) {
        System.out.println("Repainting the AudioVisualiserComponent");
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
