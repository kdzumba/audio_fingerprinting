package org.kdzumba.gui.components;

import org.kdzumba.utils.MathUtils;
import org.kdzumba.utils.MathUtils.Range;
import org.kdzumba.utils.UIUtils;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;

import static org.kdzumba.gui.common.Constants.SPECTROGRAM_GRADIENT;
import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class SpectrogramComponent extends JComponent {
    private final int WIDTH = 964;
    private final int HEIGHT = 400;
    private double[][] spectrogramData;
    private final AudioFormat audioFormat;

    public SpectrogramComponent(AudioFormat format) {
        this.audioFormat = format;
    }

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

        Range fromRange = getIntensityRange(numberOfRows, numberOfCols);
        Range toRange = new Range(0.0, 1.0);

        for(int j = 0; j < numberOfCols; j++) {
            for(int i = 0; i < numberOfRows; i++) {
                float intensity = (float) Math.log1p(spectrogramData[i][j]);
                float normalizedIntensity = (float) MathUtils.convertToRange(intensity, fromRange, toRange);
                Color color = UIUtils.getColorForRatio(SPECTROGRAM_GRADIENT, normalizedIntensity);
                g.setColor(color);
                g.fillRect((int) (j * colWidth), (int) (i * rowHeight), (int) colWidth, (int) rowHeight);
            }
        }

        drawTimeTicks(g, numberOfCols, colWidth);
//        drawFrequencyTicks(g, numberOfRows, rowHeight);
    }

    private Range getIntensityRange(int numberOfRows, int numberOfCols) {
        double maxIntensity = Double.NEGATIVE_INFINITY;
        double minIntensity = Double.POSITIVE_INFINITY;

        // Calculate max and min intensity values
        for(int i = 0; i < numberOfRows; i++) {
            for(int j = 0; j < numberOfCols; j++) {
                double intensity = Math.log1p(spectrogramData[i][j]);
                if(intensity > maxIntensity) {
                    maxIntensity = intensity;
                }
                if(intensity < minIntensity) {
                    minIntensity = intensity;
                }
            }
        }

        // Avoid division by zero
        if(maxIntensity == minIntensity) {
            maxIntensity = minIntensity + 1;
        }
        return new Range(minIntensity, maxIntensity);
    }

    private void drawTimeTicks(Graphics g, int numberOfCols, double colWidth) {
        g.setColor(Color.BLACK);
        double windowDuration = (double) 1024 / audioFormat.getSampleRate(); // Example window size
        for (int j = 0; j < numberOfCols; j += numberOfCols / 10) {
            int x = (int) (j * colWidth);
            g.drawLine(x, HEIGHT - 10, x, HEIGHT);
            g.drawString(String.format("%.2f", j * windowDuration), x, HEIGHT - 20);
        }
    }

    private void drawFrequencyTicks(Graphics g, int numberOfRows, double rowHeight) {
        g.setColor(Color.BLACK);
        double binFrequency = (double) audioFormat.getSampleRate() / 1024; // Example window size
        for (int i = 0; i < numberOfRows; i += numberOfRows / 10) {
            int y = (int) (i * rowHeight);
            g.drawLine(0, y, 10, y);
            g.drawString(String.format("%.2f", i * binFrequency), 15, y);
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
