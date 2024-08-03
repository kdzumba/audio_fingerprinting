package org.kdzumba;

import org.kdzumba.gui.panels.AudioVisualizer;

import javax.swing.*;
import java.awt.*;

public class Application extends JFrame {
  public static final int WIDTH = 1000;
  public static final int HEIGHT = 600;

  public static void main(String[] args) {
    var app = new Application();
    app.setSize(new Dimension(WIDTH, HEIGHT));
    app.setDefaultCloseOperation(EXIT_ON_CLOSE);
    app.setResizable(false);

    EventQueue.invokeLater(() -> {
      var audioVisualiser = new AudioVisualizer();
      var contentPanel = new JPanel();
      contentPanel.add(audioVisualiser);

      app.add(contentPanel);
//      app.pack();
      app.setVisible(true);
    });
  }
}
