package org.kdzumba;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;


public class SineWaveSound {

    public static void playTone(int frequency, int msDuration, double volume) throws LineUnavailableException {
        var audioFormat = new AudioFormat(8000f, 8, 1, true, false);
        var sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
        var buffer = new byte[1];

        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        for(int i = 0; i < msDuration * 8; i++) {
            double angle = i / (8000f / frequency) * 2.0 * Math.PI;
            buffer[0] = (byte) (Math.sin(angle) * 127.0 * volume);
            sourceDataLine.write(buffer, 0, 1);
        }
        sourceDataLine.stop();
        sourceDataLine.close();
    }
    public static void main(String[] args) throws LineUnavailableException {
        playTone(14500, 5000, 1);

        var projectDir = System.getProperty("user.dir");
        System.out.println(projectDir);
    }
}
