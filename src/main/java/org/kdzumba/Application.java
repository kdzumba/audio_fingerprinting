package org.kdzumba;

import org.kdzumba.gui.AudioGraphPanel;

import javax.swing.*;
import java.awt.*;

public class Application extends JFrame {
  public static final int WIDTH = 600;
  public static final int HEIGHT = 400;

  public static void main(String[] args) {
    var app = new Application();

    EventQueue.invokeLater(() -> {
      app.setSize(WIDTH, HEIGHT);
      app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      var audioGraphPanel = new AudioGraphPanel();
      app.add(audioGraphPanel);
      app.pack();
      app.setVisible(true);
    });
  }
}
