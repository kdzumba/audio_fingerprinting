package org.kdzumba.gui.components;

import javax.swing.*;
import java.awt.*;

public class ColorBarComponent extends JComponent {
    private final Color[] colors;
    private final double minMagnitude;
    private final double maxMagnitude;
    private final int tickInterval;

    public ColorBarComponent(Color[] colors, double minMagnitude, double maxMagnitude, int tickInterval) {
        this.colors = colors;
        this.minMagnitude = minMagnitude;
        this.maxMagnitude = maxMagnitude;
        this.tickInterval = tickInterval;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // Draw gradient bar
        for (int y = 0; y < height; y++) {
            float ratio = (float) y / (float) height;
            Color color = getColorForRatio(ratio);
            g2d.setColor(color);
            g2d.drawLine(0, y, width - 30, y); // Leave space for labels and ticks
        }

        // Draw ticks and labels
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        int numTicks = (int) ((maxMagnitude - minMagnitude) / tickInterval);
        for (int i = 0; i <= numTicks; i++) {
            int y = height - (int) ((double) i / numTicks * height);
            g2d.drawLine(width - 30, y, width - 25, y); // Tick mark

            String label = String.format("%.0f", minMagnitude + i * tickInterval);
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, width - 25, y); // Label
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(55, 200);
    }

    private Color getColorForRatio(float ratio) {
        int index = (int) (ratio * (colors.length - 1));
        float fraction = ratio * (colors.length - 1) - index;

        if (index >= colors.length - 1) {
            return colors[colors.length - 1];
        } else {
            Color color1 = colors[index];
            Color color2 = colors[index + 1];
            int red = (int) (color1.getRed() * (1 - fraction) + color2.getRed() * fraction);
            int green = (int) (color1.getGreen() * (1 - fraction) + color2.getGreen() * fraction);
            int blue = (int) (color1.getBlue() * (1 - fraction) + color2.getBlue() * fraction);
            return new Color(red, green, blue);
        }
    }

}
