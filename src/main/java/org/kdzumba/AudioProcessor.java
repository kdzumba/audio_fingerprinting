package org.kdzumba;

import javax.sound.sampled.*;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
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
    private short[] samplesArray = new short[4096 / 2];;
    private static final int BUFFER_SIZE = 44100; // Max number of samples to store
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioProcessor.class);
    public boolean capturing = false;

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
    public short[] getSamplesArray() { return samplesArray; }


    public void captureAudioDataFromMicrophone(PipedOutputStream outputStream) throws IOException {
        int numberOfBytesRead;
        TargetDataLine line = getTargetDataLine();
        int frameSize = audioFormat.getFrameSize();
        byte[] writeBuffer = new byte[4096];

        // Start Capturing Audio
        Objects.requireNonNull(line).start();
        try (outputStream) {
            while (capturing) {
                numberOfBytesRead = line.read(writeBuffer, 0, writeBuffer.length);
                outputStream.write(writeBuffer, 0, numberOfBytesRead);
            }
            line.flush();
        } finally {
            line.stop();
            line.close();
        }
    }

    public void processCapturedSamples(PipedInputStream inputStream) throws IOException {
        int frameSize = audioFormat.getFrameSize();
        byte[] readBuffer = new byte[4096];

        while (capturing) {
            int numberOfBytesRead = inputStream.read(readBuffer, 0, readBuffer.length);
//            samplesArray = new short[numberOfBytesRead / audioFormat.getChannels()];
            ByteBuffer.wrap(readBuffer).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(samplesArray);

            for(short sample : samplesArray) {
                if(samples.size() >= BUFFER_SIZE) {
                    System.out.println("Buffer overflow.....");
                  samples.poll();
                }
                System.out.println("I'm stuck on add");
                samples.add(sample);
            }
        }
    }

    public double[][] generateSpectrogram(int windowSize, int overlap) {
        int stepSize = windowSize - overlap;
        int numberOfWindows = (samples.size() - windowSize) / stepSize + 1;
        double[][] spectrogram = new double[numberOfWindows][windowSize / 2];
        double[] window = new double[windowSize];

        // Hamming window function
        for(int i = 0; i < windowSize; i++) {
            window[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (windowSize - 1));
        }

        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

        for(int i = 0; i < numberOfWindows; i++) {
            double[] segment = new double[windowSize];

            //Get samples for the current window and apply the Hamming window
            Iterator<Short> iterator = samples.iterator();
            for(int j = 0; j < i * stepSize; j++) iterator.next(); // skip to start of the current window
            for(int j = 0; j < windowSize; j++) {
                if(iterator.hasNext()) {
                    segment[j] = iterator.next() * window[j];
                } else {
                    segment[j] = 0;
                }
            }

            // Perform FFT
            Complex[] result = transformer.transform(segment, TransformType.FORWARD);

            // Compute Magnitude
            for(int j = 0; j < windowSize / 2; j++) {
                spectrogram[i][j] = result[j].abs();
            }
        }
        return spectrogram;
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
}
