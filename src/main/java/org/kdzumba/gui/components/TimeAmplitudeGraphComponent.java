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
    private final short[] samplesArray; // Array of amplitudes that we want to plot

    public TimeAmplitudeGraphComponent(short[] samplesArray) {
        this.samplesArray = samplesArray;
    }

    private void drawSoundWave(Graphics g) {
        if(samplesArray.length == 0) { return ; }

        int width = getWidth();
        int height = getHeight();
        int midHeight = (height / 2);
        int displayWidth = Math.min(samplesArray.length, width);
        double xIncrement = (double) samplesArray.length / displayWidth;

        double x = 0;
        for(Short sample : samplesArray) {
            MathUtils.Range samplingRange = new MathUtils.Range(Short.MIN_VALUE, Short.MAX_VALUE);
            MathUtils.Range displayRange = new MathUtils.Range(midHeight * -1, midHeight);
            double normalizedY = MathUtils.convertToRange(sample, samplingRange, displayRange);

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
