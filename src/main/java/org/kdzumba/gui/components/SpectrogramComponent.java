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
    private final int WIDTH = 740;
    private final int HEIGHT = 500;
    private double[][] spectrogramData;
    private final AudioFormat audioFormat;

    public SpectrogramComponent(AudioFormat format) {
        this.audioFormat = format;
    }

    public void setSpectrogramData(double[][] spectrogramData) {
        this.spectrogramData = spectrogramData;
    }

    private void drawSpectrogram(Graphics g) {
        if(spectrogramData == null) return;

        int numberOfWindows = spectrogramData.length; // Each window represent time
        int numberOfBins = spectrogramData[0].length; // Each bin represent frequency

        double colWidth = (double) getWidth() / numberOfWindows;
        double rowHeight = (double) getHeight() / numberOfBins;

        Range fromRange = getIntensityRange(numberOfWindows, numberOfBins);
        Range toRange = new Range(0.0, 1.0);

        for(int i = 0; i < numberOfWindows; i++) {
            for(int j = 0; j < numberOfBins; j++) {
                float intensity = (float) Math.log1p(spectrogramData[i][j]);
                float normalizedIntensity = (float) MathUtils.convertToRange(intensity, fromRange, toRange);
                Color color = UIUtils.getColorForRatio(SPECTROGRAM_GRADIENT, normalizedIntensity);
                g.setColor(color);
                g.fillRect((int) (i * colWidth + 70), (int) (j * rowHeight), (int) colWidth + 70, (int) rowHeight);
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
        double windowDuration = (double) 1024 / audioFormat.getSampleRate(); // Example window size
        for (int j = 0; j < 85; j += 85 / 10) {
            int x = (int) (j * getWidth() / 85);
            g.drawLine(x, HEIGHT, x, HEIGHT + 10);
            g.drawString(String.format("%.2f", j * windowDuration), x, HEIGHT + 20);
        }
    }

    private void drawFrequencyTicks(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform original = g2d.getTransform();
        g2d.rotate(Math.toRadians(-90), (double) getWidth() / 2, (double) getHeight() / 2);
        FontMetrics fm = g2d.getFontMetrics();
        String label = "Frequency (Hz)";
        int x = (getWidth() - fm.stringWidth(label)) / 2;
        int yPos = -95; //(getHeight() + fm.getAscent()) / 2;
        g2d.drawString("Frequency (kHz)", x, yPos);
        g2d.setTransform(original);

        g.setColor(Color.BLACK);
        double binFrequency = (double) audioFormat.getSampleRate() / 1024;
        for (int i = 0; i < 512; i += 512 / 10) {
            int y = (int) (i * (getHeight() / 512));
            g.drawLine(65, y, 80, y);
            g.drawString(String.format("%.2f", (i * binFrequency) / 1000), 25, y);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(VISUALIZER_BACKGROUND_COLOR);
        g2d.fillRect(80, 0, WIDTH, HEIGHT);

        UIUtils.showGrid(g, 80, 0, WIDTH, HEIGHT - 5);
        drawTimeTicks(g);
        drawFrequencyTicks(g);
        drawSpectrogram(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT + 30);
    }
}
