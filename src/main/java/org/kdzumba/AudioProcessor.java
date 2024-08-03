package org.kdzumba;

import javax.sound.sampled.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioProcessor {
    private final AudioFormat audioFormat;
    private final ConcurrentLinkedQueue<Short> samples = new ConcurrentLinkedQueue<>();
    private static final int BUFFER_SIZE = 600;
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioProcessor.class);

    public AudioProcessor() {
        // The audio format tells Java how to interpret and handle the bits of information
        // in the incoming sound stream.

        float SAMPLE_RATE = 44100f;     // Rate at which an audio file is sampled (Hz)
        int SAMPLE_SIZE_IN_BITS = 16;   // Number of bits used to represent each sample of the audio. Determines
                                        // the bit depth for the audio signal
        int CHANNELS = 2;               // Mono or Stereo Sound
        boolean SIGNED = true;
        boolean BIG_ENDIAN = true;

        audioFormat = new AudioFormat(SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN);
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public ConcurrentLinkedQueue<Short> getSamples() { return samples; }


    public void captureAudioDataFromMicrophone(PipedOutputStream outputStream) throws IOException {
        int numberOfBytesRead;
        TargetDataLine line = getTargetDataLine();
        int frameSize = audioFormat.getFrameSize();
        byte[] writeBuffer = new byte[frameSize];

        // Start Capturing Audio
        Objects.requireNonNull(line).start();
        while (true) {
            numberOfBytesRead = line.read(writeBuffer, 0, writeBuffer.length);
            outputStream.write(writeBuffer, 0, numberOfBytesRead);
        }
//        line.flush();
//        outputStream.close();
    }

    public void processCapturedSamples(PipedInputStream inputStream) throws IOException {
        int frameSize = audioFormat.getFrameSize();
        byte[] readBuffer = new byte[frameSize];

        while (true) {
            int numberOfBytesRead = inputStream.read(readBuffer, 0, readBuffer.length);
            short[] samplesArray = new short[numberOfBytesRead / audioFormat.getChannels()];
            ByteBuffer.wrap(readBuffer).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(samplesArray);
            for(short sample : samplesArray) {
                if(samples.size() >= BUFFER_SIZE) {
                  samples.poll();
                }
                samples.add(sample); 
            }
        }
    }

    private TargetDataLine getTargetDataLine() {
        TargetDataLine line = null;
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            LOGGER.debug("Failed to access the DataLine with given type and format as it's not supported in this system");
            return null;
        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch(LineUnavailableException exception) {
            LOGGER.debug("There was no DataLine available for the application to acquire");
        }
        return line;
    }

    private static double[] convertQueueToDoubleArray(ConcurrentLinkedQueue<Short> queue) {
        Object[] objectArray = queue.toArray();
        double[] doubleArray = new double[objectArray.length];
        for(int i = 0; i < objectArray.length; i++){
            doubleArray[i] = (double) objectArray[i];
        }
        return doubleArray;
    }
}
