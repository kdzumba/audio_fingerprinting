package org.kdzumba.gui.components;

import org.kdzumba.utils.MathUtils;
import org.kdzumba.utils.MathUtils.Range;
import org.kdzumba.utils.UIUtils;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

import static org.kdzumba.gui.common.Constants.SPECTROGRAM_GRADIENT;
import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class SpectrogramComponent extends JComponent {
    private final int WIDTH = 1024;
    private final int HEIGHT = 512;
    private double[][] spectrogramData;
    private final AudioFormat audioFormat;
    private int windowSize;
    private int hopSize;
    private int columnsToRender = 0;
    private Timer animationTimer;
    private int animationDuration = 2000;
    

    public SpectrogramComponent(AudioFormat format, int windowSize, int hopSize) {
        this.audioFormat = format;
        this.windowSize = windowSize;
        this.hopSize = hopSize;
    }

    public void setSpectrogramData(double[][] spectrogramData) {
        this.spectrogramData = spectrogramData;
        //columnsToRender = 0;

        if(animationTimer != null && animationTimer.isRunning()) {
            //animationTimer.stop();
        }
        startSpectrogramAnimation();
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public void setHopSize(int hopSize) {
        this.hopSize = hopSize;
    }

    private void startSpectrogramAnimation() {
        if(spectrogramData == null) return;

        int numberOfWindows = spectrogramData.length;
        int delay = animationDuration / numberOfWindows;

        System.out.println("Delay: " + delay);

        animationTimer = new Timer(delay, e -> {
            columnsToRender++;
            if(columnsToRender >= numberOfWindows) {
                animationTimer.stop();
            }
            repaint();
        });

        animationTimer.start();
    }


    private void drawSpectrogram(Graphics g) {
        if(spectrogramData == null) return;

        int numberOfWindows = spectrogramData.length; // Each window represent time
        int numberOfBins = spectrogramData[0].length; // Each bin represent frequency

        double colWidth = (double) WIDTH / numberOfWindows;
        double rowHeight = (double) HEIGHT / numberOfBins;

        Range fromRange = getIntensityRange(numberOfWindows, numberOfBins);
        Range toRange = new Range(0.0, 1.0);

        for(int i = 0; i < columnsToRender; i++) {
            for(int j = 0; j < numberOfBins; j++) {
                float intensity = (float) Math.log1p(spectrogramData[i][j]);
                float normalizedIntensity = (float) MathUtils.convertToRange(intensity, fromRange, toRange);

                Color color = UIUtils.getColorForRatio(SPECTROGRAM_GRADIENT, normalizedIntensity);
                int invertedJ = numberOfBins - 1 - j;
                g.setColor(color);
                g.fillRect((int) (i * colWidth + 80), (int) (invertedJ * rowHeight + 20), (int) colWidth, (int) rowHeight);
            }
        }
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

    private void drawTimeTicks(Graphics g) {
        g.setColor(Color.BLACK);

        // We want the entire width to represent 1 second
        double tickInterval = 0.1; // Interval for each tick (e.g., 0.1 seconds for better granularity)
        int numberOfTicks = (int) Math.ceil(1.0 / tickInterval);

        // Position ticks at intervals of tickInterval seconds
        for (int i = 0; i <= numberOfTicks; i++) {
            int x = (int) (i * tickInterval * WIDTH);
            g.drawLine(x + 80, HEIGHT + 20, x + 80, HEIGHT + 30);
            g.drawString(String.format("%.1f", i * tickInterval), x + 80, HEIGHT + 40);
        }
    }

    private void drawFrequencyTicks(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform original = g2d.getTransform();
        g2d.rotate(Math.toRadians(-90), (double) getWidth() / 2, (double) getHeight() / 2);
        FontMetrics fm = g2d.getFontMetrics();
        String label = "Frequency (Hz)";
        int x = (WIDTH - fm.stringWidth(label)) / 2;
        int yPos = -130;
        g2d.drawString("Frequency (kHz)", x, yPos);
        g2d.setTransform(original);

        g.setColor(Color.BLACK);
        double binFrequency = (double) audioFormat.getSampleRate() / windowSize;
        int numberOfBins = windowSize / 2;
        for (int i = 0; i < numberOfBins; i += numberOfBins / 10) {
            int y = HEIGHT + 20 - i;
            g.drawLine(65, y, 80, y);
            g.drawString(String.format("%.2f", (i * binFrequency) / 1000), 25, y);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(VISUALIZER_BACKGROUND_COLOR);
        g2d.fillRect(80, 20, WIDTH, HEIGHT);

        drawTimeTicks(g);
        drawFrequencyTicks(g);

        //startSpectrogramAnimation();
        this.drawSpectrogram(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT + 50);
    }
}
