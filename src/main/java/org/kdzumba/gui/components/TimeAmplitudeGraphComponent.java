package org.kdzumba.gui.components;

import org.kdzumba.graphics2d.Coordinate;
import org.kdzumba.graphics2d.Line;
import org.kdzumba.utils.MathUtils;
import org.kdzumba.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Queue;

import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class TimeAmplitudeGraphComponent extends JComponent {
    private final int WIDTH = 600;
    private final int HEIGHT = 200;
    private boolean showGrid = true;
    private final Queue<Short> samples;

    public TimeAmplitudeGraphComponent(Queue<Short> samples) {
        this.samples = samples;
    }

    private void drawSoundWave(Graphics g) {
        if(samples.isEmpty()) { return ; }

        int width = getWidth();
        int height = getHeight();
        int midHeight = (height / 2);
        double xIncrement = (double) width / samples.size();

        double x = 0;
        for(Short sample : samples) {
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
            UIUtils.showGrid(g, WIDTH, HEIGHT);
        }
        drawSoundWave(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }
}
