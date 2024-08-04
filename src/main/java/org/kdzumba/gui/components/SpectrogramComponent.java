package org.kdzumba.gui.components;

import org.kdzumba.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class SpectrogramComponent extends JComponent {
    private final int WIDTH = 540;
    private final int HEIGHT = 200;
    private double[][] spectrogramData;

    public SpectrogramComponent() {}

    public void setSpectrogramData(double[][] spectrogramData) {
        this.spectrogramData = spectrogramData;
        repaint();
    }

    private void drawSpectrogram(Graphics g) {
        if(spectrogramData == null) return;

        int numberOfRows = spectrogramData.length;
        int numberOfCols = spectrogramData[0].length;

        double colWidth = (double) WIDTH / numberOfCols;
        double rowHeight = (double) HEIGHT / numberOfRows;

        for(int i = 0; i < numberOfRows; i++) {
            for(int j = 0; j < numberOfCols; j++) {
                float intensity = (float) Math.log1p(spectrogramData[i][j]);
                g.setColor(new Color(intensity, intensity, intensity));
                g.fillRect((int) (j * colWidth), (int) (i * rowHeight), (int) colWidth, (int) rowHeight);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(VISUALIZER_BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        UIUtils.showGrid(g, WIDTH, HEIGHT);
        drawSpectrogram(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }
}
