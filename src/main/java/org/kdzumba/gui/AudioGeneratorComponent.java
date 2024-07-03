package org.kdzumba.gui;

import javax.swing.*;
import org.kdzumba.AudioGenerator;
import java.awt.*;

public class AudioGeneratorComponent extends JPanel {
  private final AudioGenerator generator;

  public AudioGeneratorComponent() {
    this.generator = new AudioGenerator(5000, 1, 5000);

    //Here we are placing the various UI control elements for controlling various aspects
    // of the AudioGenerator 
    var volumeSlider = new JSlider();
    var frequencySlider = new JSlider();

    var sliderControlsPanel = new JPanel(new GridLayout(2, 2));
    var volumeLabel = new JLabel("Volume:");
    sliderControlsPanel.add(volumeLabel);
    sliderControlsPanel.add(volumeSlider);

    var frequencyLabel = new JLabel("Frequency:");
    sliderControlsPanel.add(frequencyLabel);
    sliderControlsPanel.add(frequencySlider);

    var formatControlsPanel = new JPanel();
    var verticalBoxLayoutManager = new BoxLayout(formatControlsPanel, BoxLayout.Y_AXIS);
    formatControlsPanel.add(sliderControlsPanel);
    formatControlsPanel.setLayout(verticalBoxLayoutManager);
    var playButton = new JButton("Play");
    playButton.addActionListener(e -> this.playGeneratedAudio());

    this.add(formatControlsPanel);
    this.add(playButton);
  }

  private void handleIncreaseVolume() {
    this.generator.increaseVolume();
  }

  private void playGeneratedAudio() {
    System.out.println("Playing the generated Audio");

    try {
      this.generator.generateTone();
      var sampleSize = this.generator.getSamples().size();
      System.out.println("The number of samples is : " + sampleSize);
      var audioData = new byte[sampleSize];

      for(int i = 0; i < sampleSize; i++) {
        audioData[i] = this.generator.getSamples().get(i).byteValue();
      }

      var sourceDataLine = AudioGenerator.getSourceDataLine();
      sourceDataLine.start();
      sourceDataLine.write(audioData, 0, sampleSize);
      sourceDataLine.stop();
    } catch(Exception e) {
      System.out.println("An error occurred when trying to open a sourceDataLine");
    }
  }
}
