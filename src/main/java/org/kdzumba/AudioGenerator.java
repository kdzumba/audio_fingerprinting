package org.kdzumba;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

public class AudioGenerator {
  private int frequency;                            //The frequency of the generated tone 
  private double volume;                            //The volume of the generated tone 
  private int duration;                             //How long the tone should play for (in ms) 
  private static AudioFormat audioFormat;
  private static SourceDataLine sourceDataLine;
  private final List<Integer> toneSamples;
  private static final int BIT_RESOLUTION_SCALE_FACTOR = 127;

  public AudioGenerator(int frequency, double volume, int duration) {
    this.frequency = frequency;
    this.volume = volume;
    this.duration = duration;
    audioFormat = new AudioFormat(8000f, 8, 1, true, false);
    this.toneSamples = new ArrayList<>();
  }

  public void increaseVolume() { this.volume++; }
  public void decreaseVolume() { this.volume--; }
  public void speedUp() { this.frequency++; }
  public void slowDown() { this.frequency--; }
  public void clip(int duration) {this.duration = duration;}


  /**
   * Generates a pure tone with a frequency defined by this.frequency
   * This does not play the generated tone, but saves the generated audio samples for the 
   * tone in this.toneSamples
   */
  public void generateTone() throws LineUnavailableException { 
    var sourceDataLine = AudioSystem.getSourceDataLine(AudioGenerator.audioFormat);
    sourceDataLine.open();
    sourceDataLine.start();
    
    //The number of samples per second * total number of seconds (this.duration / 1000)
    var audioLength = AudioGenerator.audioFormat.getSampleRate() * (this.duration / 1000);

    //Clear the previously generated samples so the duration doesn't double with each play
    this.toneSamples.clear();

    for (int i = 0; i < audioLength; i++) {
      double angle = 2.0 * Math.PI * this.frequency * i / AudioGenerator.audioFormat.getSampleRate();
      //We are using an 8-bit channel with signed data, which means the max(abs(value)) we can get 
      //is 127 (max for signed byte)
      var value = this.volume * Math.sin(angle) * BIT_RESOLUTION_SCALE_FACTOR; 
      this.toneSamples.add((int) value);
    }

    sourceDataLine.stop();
    sourceDataLine.close();
  }

  public void generateTone(int frequency) throws LineUnavailableException {
    this.frequency = frequency;
    this.generateTone();
  }

  public static final SourceDataLine getSourceDataLine() throws LineUnavailableException {
    if(sourceDataLine == null){
      sourceDataLine = AudioSystem.getSourceDataLine(AudioGenerator.audioFormat);

      //Who ever is using this SourceDataLine will have to close it after use
      sourceDataLine.open(AudioGenerator.audioFormat);
    }
    return sourceDataLine; 
  }


  public List<Integer> getSamples() { return this.toneSamples; }

}
