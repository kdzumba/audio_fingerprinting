package org.kdzumba;

import org.kdzumba.gui.panels.AudioVisualizerPanel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class Application extends JFrame {
  public static final int WIDTH = 800;
  public static final int HEIGHT = 500;

  public static void main(String[] args) {
    // Start the Spring application and get the application context
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

    // Create the Application JFrame
    var app = new Application();
    app.setSize(new Dimension(WIDTH, HEIGHT));
    app.setDefaultCloseOperation(EXIT_ON_CLOSE);
    app.setResizable(false);

    EventQueue.invokeLater(() -> {
      // Retrieve the AudioVisualizerPanel bean from the Spring context
      var audioVisualizerPanel = context.getBean(AudioVisualizerPanel.class);

      // Create the content panel and add the audioVisualizerPanel to it
      var contentPanel = new JPanel();
      contentPanel.add(audioVisualizerPanel);

      // Add the content panel to the JFrame
      app.add(contentPanel);
      app.pack();
      app.setVisible(true);
    });
  }
}
