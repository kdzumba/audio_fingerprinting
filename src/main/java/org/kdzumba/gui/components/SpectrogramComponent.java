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

    public SpectrogramComponent(AudioFormat format) {
        this.audioFormat = format;
    }

    public void setSpectrogramData(double[][] spectrogramData) {
       
//        if(this.spectrogramData == null) {
            this.spectrogramData = spectrogramData;

//        } else {
//            double[][] newSpectrogram = new double[this.spectrogramData.length + 1][HEIGHT];
//            for(int i = 0; i < this.spectrogramData.length; i++) {
//                System.arraycopy(this.spectrogramData[i], 0, newSpectrogram[i], 0, this.spectrogramData[0].length);
//            }
//            System.arraycopy(spectrogramData[0], 0, newSpectrogram[this.spectrogramData.length - 1], 0, spectrogramData[0].length);
//
//            this.spectrogramData = newSpectrogram;
//        }
        repaint();
    }

    private void drawSpectrogram(Graphics g) {
        if(spectrogramData == null) return;

        int numberOfWindows = spectrogramData.length; // Each window represent time
        int numberOfBins = spectrogramData[0].length; // Each bin represent frequency
                                                      //
        System.out.println("NumberofWindows: " + numberOfWindows);
        System.out.println("NumberOfBins: " + numberOfBins);

        double colWidth = (double) WIDTH / numberOfWindows;
        double rowHeight = (double) HEIGHT / numberOfBins;

        Range fromRange = getIntensityRange(numberOfWindows, numberOfBins);
        Range toRange = new Range(0.0, 1.0);

        for(int i = 0; i < numberOfWindows; i++) {
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
        double binFrequency = (double) audioFormat.getSampleRate() / 1024;
        for (int i = 0; i < 512; i += 512 / 10) {
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
