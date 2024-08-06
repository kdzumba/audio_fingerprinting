package org.kdzumba.gui.components;

import org.kdzumba.graphics2d.Coordinate;
import org.kdzumba.graphics2d.Line;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class FrequencySpectrumComponent extends JComponent {
    double[] magnitudes;

    public FrequencySpectrumComponent() {

    }

    public void setMagnitudes(double[] magnitudes) {
        this.magnitudes = magnitudes;
    }

    private void drawSpectrum(Graphics g) {
        if(magnitudes != null)
            System.out.println("The length of magnitudes is: " + magnitudes.length);
        if(magnitudes == null || magnitudes.length == 0) return;

        int width = this.getWidth();
        int height = this.getHeight();
        int binSize = this.magnitudes.length / width;

        for(int i = 0; i < width; i ++) {
            double[] binnedMagnitudes = Arrays.copyOfRange(magnitudes, i * binSize, binSize * (i + 1));
            double binMagnitude = 10 * Math.log10(getAverageBinMagnitude(binnedMagnitudes));
            System.out.println("Bin magnitude: " + binMagnitude);

            Coordinate start = new Coordinate(i, height); //The bottom of the panel is the zero line
            Coordinate end = new  Coordinate(i, height - binMagnitude);
            Line line = new Line(start, end, Color.WHITE);
            line.draw(g);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(VISUALIZER_BACKGROUND_COLOR);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

        this.drawSpectrum(g);
    }

    @Override
    public Dimension getPreferredSize() {
        int WIDTH = 800;
        int HEIGHT = 200;
        return new Dimension(WIDTH, HEIGHT);
    }

    private double getAverageBinMagnitude(double[] magnitudes) {
        if(magnitudes.length == 0) return 0;
        return Arrays.stream(magnitudes).average().getAsDouble();
    }
}
