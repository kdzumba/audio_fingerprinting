package org.kdzumba.gui;

import org.kdzumba.AudioGenerator;
import org.kdzumba.graphics2d.*;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class AudioGraphPanel extends JPanel {

  private final Line xAxis;
  private final Line yAxis;
  private final AudioGenerator audioGenerator;
  private ArrayList<Integer> audioSamples;

  public AudioGraphPanel() {
    this.xAxis = new Line(new Coordinate(), new Coordinate());
    this.yAxis = new Line(new Coordinate(), new Coordinate());
    this.setPreferredSize(new Dimension(450, 400));

    this.audioGenerator = new AudioGenerator(150, 1, 5);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    xAxis.update(new Coordinate(0, getHeight() / 2), new Coordinate(getWidth(), getHeight() / 2));
    yAxis.update(new Coordinate(getWidth() / 2, 0), new Coordinate(getWidth() / 2, getHeight()));
    xAxis.draw(g);
    yAxis.draw(g);
    drawAudioGraphics(g);
    playAudio();
  }

  public void drawAudioGraph(Graphics g) {
    int samplesCount = this.audioSamples.size();
    int panelWidth = getWidth();
    int panelHeight = getHeight();
  }

  public void setAudioSamples(ArrayList<Integer> samples) {
    this.audioSamples = samples;
  }

  public void drawAudioGraphics(Graphics g) {
    try {
      if(audioGenerator.getSamples().size() > 0) {
        System.out.println("Found some samples already, clearing them out...");
        audioGenerator.getSamples().clear();
      }
      audioGenerator.generateTone();
    } catch (LineUnavailableException e) {
      throw new RuntimeException(e);
    }

    if(audioGenerator.getSamples().isEmpty()) {
      System.out.println("No samples to draw a graph for");
      return;
    }

    System.out.println("Drawing some samples");

    int samplesCount = audioGenerator.getSamples().size();
    System.out.println("The number of samples is: " + samplesCount);

    int width = getWidth();

    System.out.println("The width of the panel is: " + width);

    int height = getHeight();
    int midHeight = height / 2;
    double scalingFactor = 1;

    System.out.println("The scalingFactor is: " + scalingFactor);

    for (int i = 1; i < samplesCount; i++) {
      int x1 = (int)((i - 1) * scalingFactor);
      int y1 = midHeight - audioGenerator.getSamples().get(i - 1);
      int x2 = (int) (x1 + scalingFactor);
      int y2 = midHeight - audioGenerator.getSamples().get(i);

      var graph = new Line(new Coordinate(x1, y1), new Coordinate(x2, y2));
      graph.draw(g);
    }
  }

  private void playAudio(){

    try{
      var sampleSize = this.audioGenerator.getSamples().size();

      byte[] audioData = new byte[sampleSize];
      for(int i = 0; i < sampleSize; i++){
        audioData[i] = this.audioGenerator.getSamples().get(i).byteValue();
      }

      var sourceDataLine = AudioGenerator.getSourceDataLine();
      sourceDataLine.start();
      sourceDataLine.write(audioData, 0, sampleSize);
      sourceDataLine.stop();

    } catch(Exception e){
      System.out.println("An exception occurred when trying to open a data line");
    }
  }
}
