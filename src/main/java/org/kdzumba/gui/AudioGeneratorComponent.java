package org.kdzumba.gui;

import javax.swing.*;
import org.kdzumba.AudioGenerator;
import java.awt.*;

public class AudioGeneratorComponent extends JPanel {
  private final AudioGenerator generator;
  private final int MIN_AUDIBLE_FREQUENCY = 20;
  private final int MAX_AUDIBLE_FREQUENCY = 20000;
  public static final int MAX_VOLUME = 100;
  public static final int MIN_VOLUME = 0;

  public AudioGeneratorComponent() {
    this.generator = new AudioGenerator(5000, 0, 5);

    //Here we are placing the various UI control elements for controlling various aspects
    // of the AudioGenerator 
    var volumeSlider = new JSlider(MIN_VOLUME, MAX_VOLUME, MIN_VOLUME);
    volumeSlider.addChangeListener(e -> {
      setGeneratedToneVolume(volumeSlider.getValue());
    });
    
    var frequencyTextField = new JTextField(Integer.toString(MIN_AUDIBLE_FREQUENCY));
    var frequencySlider = new JSlider(MIN_AUDIBLE_FREQUENCY, MAX_AUDIBLE_FREQUENCY, MIN_AUDIBLE_FREQUENCY);

    frequencySlider.addChangeListener(e -> {
      frequencyTextField.setText(Integer.toString(frequencySlider.getValue()));
      setGeneratedToneFrequency(frequencySlider.getValue());
    });

    frequencyTextField.addActionListener(e -> {
      var setValue = Integer.parseInt(frequencyTextField.getText());
      setGeneratedToneFrequency(setValue);
    });

    var sliderControlsPanel = new JPanel(new GridLayout(2, 2));
    var volumeLabel = new JLabel("Volume:");
    sliderControlsPanel.add(volumeLabel);
    sliderControlsPanel.add(volumeSlider);

    var frequencyControlsPanel = new JPanel();
    frequencyControlsPanel.add(frequencySlider);
    frequencyControlsPanel.add(frequencyTextField);
    var frequencyLabel = new JLabel("Frequency (Hz):");
    sliderControlsPanel.add(frequencyLabel);
    sliderControlsPanel.add(frequencyControlsPanel);

    var formatControlsPanel = new JPanel();
    var verticalBoxLayoutManager = new BoxLayout(formatControlsPanel, BoxLayout.Y_AXIS);
    formatControlsPanel.add(sliderControlsPanel);
    formatControlsPanel.setLayout(verticalBoxLayoutManager);
    var playButton = new JButton("Play");
    playButton.addActionListener(e -> new Thread(() -> this.playGeneratedAudio()).start());

    this.add(formatControlsPanel);
    this.add(playButton);
  }

  private void setGeneratedToneFrequency(int frequency) {
    System.out.println("Tone frequency now set to: " + this.generator.getFrequency());
    this.generator.setFrequency(frequency);
  }

  private void setGeneratedToneVolume(int volume) {
    System.out.println("The tone volume is now set to: " + this.generator.getVolume());
    this.generator.setVolume(volume);
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
