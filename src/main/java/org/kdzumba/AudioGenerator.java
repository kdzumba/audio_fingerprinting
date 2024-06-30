package org.kdzumba;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

public class AudioGenerator {
  private int frequency;                            //The frequency of the generated tone 
  private double amplitude;                         //The volume of the generated tone 
  private int duration;                             //How long the tone should play for 
  private static AudioFormat audioFormat;
  private static SourceDataLine sourceDataLine;
  private final List<Integer> samples;

  public AudioGenerator(int frequency, double volume, int duration) {
    this.frequency = frequency;
    this.amplitude = volume;
    this.duration = duration;
    audioFormat = new AudioFormat(8000f, 8, 1, true, false);
    this.samples = new ArrayList<>();
  }

  public void increaseVolume() { this.amplitude++;}
  public void decreaseVolume() { this.amplitude--;}
  public void speedUp() {this.frequency++;}
  public void slowDown() {this.frequency--;}
  public void clip(int duration) {this.duration = duration;}

  public void generateTone() throws LineUnavailableException {
    var audioData = new byte[(int)AudioGenerator.audioFormat.getSampleRate() * (this.duration / 1000)];
    var sourceDataLine = AudioSystem.getSourceDataLine(AudioGenerator.audioFormat);
    sourceDataLine.open();
    sourceDataLine.start();

    for (int i = 0; i < audioData.length; i++) {
      double angle = 2.0 * Math.PI * this.frequency * i / AudioGenerator.audioFormat.getSampleRate();
      var value = this.amplitude * Math.sin(angle) * 127;
      audioData[i] = (byte) value;
      samples.add((int) value);
    }
    sourceDataLine.stop();
    sourceDataLine.close();
  }

  public static final SourceDataLine getSourceDataLine() throws LineUnavailableException {
    if(sourceDataLine == null){
      sourceDataLine = AudioSystem.getSourceDataLine(AudioGenerator.audioFormat);
      sourceDataLine.open(AudioGenerator.audioFormat);
    }
    return sourceDataLine; 
  }


  public List<Integer> getSamples() { return this.samples; }

  public static void main(String[] args) throws LineUnavailableException {
    var audioGenerator = new AudioGenerator(45000, 1, 5000);
    audioGenerator.generateTone();
  }
}
