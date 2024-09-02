package org.kdzumba.gui.components;

import org.kdzumba.graphics2d.Coordinate;
import org.kdzumba.graphics2d.Line;
import org.kdzumba.utils.MathUtils;
import org.kdzumba.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class TimeAmplitudeGraphComponent extends JComponent {
    private boolean showGrid = true;
    private final short[] samples; // Array of amplitudes that we want to plot

    public TimeAmplitudeGraphComponent(short[] samples) {
        this.samples = samples;

        Timer timer = new Timer(50, (event) -> {
            this.repaint();
        });
        timer.start();
    }

    private void drawSoundWave(Graphics g) {
        if(samples.length == 0) { return ; }

        int width = getWidth();
        int height = getHeight();
        int midHeight = (height / 2);
        int displayWidth = Math.min(samples.length, width);
        double xIncrement = (double) samples.length / displayWidth;

        double x = 0;
        for(Short sample : samples) {
            MathUtils.Range sampleRange = new MathUtils.Range(Short.MIN_VALUE, Short.MAX_VALUE);
            MathUtils.Range displayRange = new MathUtils.Range(midHeight * -1, midHeight);
            double normalizedY = MathUtils.convertToRange(sample, sampleRange, displayRange);

            Coordinate start = new Coordinate(x, midHeight);
            Coordinate end = new Coordinate(x, midHeight - normalizedY);
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
            UIUtils.showGrid(g, getWidth(), getHeight());
        }
        drawSoundWave(g);
    }

    @Override
    public Dimension getPreferredSize() {
        int WIDTH = 800;
        int HEIGHT = 200;
        return new Dimension(WIDTH, HEIGHT);
    }
}
