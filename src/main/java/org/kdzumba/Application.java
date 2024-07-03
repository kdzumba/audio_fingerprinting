package org.kdzumba;

import org.kdzumba.gui.*;

import javax.swing.*;
import java.awt.*;

public class Application extends JFrame {
  public static final int WIDTH = 600;
  public static final int HEIGHT = 400;

  public static void main(String[] args) {
    var app = new Application();

    EventQueue.invokeLater(() -> {
      var audioGraphPanel = new AudioGraphPanel();
      var audioGeneratorComponent = new AudioGeneratorComponent();

      var contentPanel = new JPanel();
      contentPanel.add(audioGeneratorComponent);

      app.add(contentPanel);
      app.pack();
      app.setVisible(true);
    });
  }
}
