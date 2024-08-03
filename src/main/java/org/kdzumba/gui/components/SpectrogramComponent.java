package org.kdzumba.gui.components;

import javax.swing.*;
import java.awt.*;

import static org.kdzumba.gui.common.Constants.VISUALIZER_BACKGROUND_COLOR;

public class SpectrogramComponent extends JComponent {
    private final int WIDTH = 540;
    private final int HEIGHT = 200;

    public SpectrogramComponent() {
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(VISUALIZER_BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    public void updateSpectrogram(double[] magnitudes) {

    }
}
