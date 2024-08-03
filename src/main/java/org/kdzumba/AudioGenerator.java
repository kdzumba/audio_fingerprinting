package org.kdzumba;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

import org.kdzumba.gui.AudioGeneratorComponent;
import org.kdzumba.utils.MathUtils;

public class AudioGenerator {
  private int frequency;                            //The frequency of the generated tone 
  private float volume;                            //The volume of the generated tone 
  private int duration;                             //How long the tone should play for (in ms) 
  private static AudioFormat audioFormat;
  private static SourceDataLine sourceDataLine;
  private final List<Integer> toneSamples;
  private static final int BIT_RESOLUTION_SCALE_FACTOR = 1;
  private static final float MAX_GENERATOR_VOLUME = AudioGeneratorComponent.MAX_VOLUME;

  public AudioGenerator(int frequency, float volume, int duration) {
    this.frequency = frequency;
    this.setVolume(volume);
    this.duration = duration;
    audioFormat = new AudioFormat(8000f, 8, 1, true, false);
    this.toneSamples = new ArrayList<>();
  }

  public void increaseVolume() { this.volume++; }
  public void decreaseVolume() { this.volume--; }
  public void speedUp() { this.frequency++; }
  public void slowDown() { this.frequency--; }
  public void clip(int duration) {this.duration = duration;}
  public float getVolume() { return this.volume; }
  public int getFrequency() { return this.frequency; }
  public void setFrequency(int frequency) { this.frequency = frequency; }

  public void setVolume(float volume) {
    try {

      //We want to scale the volume (in range 0-100) to the scale of a MASTER_GAIN FloatControl
      // (-80-6.0206)
      var fromRange = new MathUtils.Range(0f, MAX_GENERATOR_VOLUME);
      var toRange = new MathUtils.Range(-80f, 6.0206f); 
//      this.volume = MathUtils.convertToRange(volume, fromRange, toRange);
      var volumeControl = (FloatControl) AudioGenerator.getSourceDataLine().getControl(FloatControl.Type.MASTER_GAIN);
      volumeControl.setValue(this.volume);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

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
    var audioLength = AudioGenerator.audioFormat.getSampleRate() * this.duration;

    //Clear the previously generated samples so the duration doesn't float with each play
    this.toneSamples.clear();

    for (int i = 0; i < audioLength; i++) {
      var angle = 2.0 * Math.PI * this.frequency * i / AudioGenerator.audioFormat.getSampleRate();
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

  public static SourceDataLine getSourceDataLine() throws LineUnavailableException {
    if(sourceDataLine == null){
      sourceDataLine = AudioSystem.getSourceDataLine(AudioGenerator.audioFormat);

      //Who ever is using this SourceDataLine will have to close it after use
      sourceDataLine.open(AudioGenerator.audioFormat);
    }
    return sourceDataLine; 
  }


  public List<Integer> getSamples() { return this.toneSamples; }
}
