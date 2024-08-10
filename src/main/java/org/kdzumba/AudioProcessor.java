package org.kdzumba;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.ArrayList;

import org.kdzumba.utils.MathUtils;
import org.kdzumba.interfaces.*;

public class AudioProcessor implements Publisher{
    //region private fields
    private final AudioFormat audioFormat;
    public final ConcurrentLinkedQueue<Short> samples;
    private final short[] samplesArray;
    public boolean capturing;
    public boolean generatingSpectrogram;
    private int numberOfBytesRead;
    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;
    private TargetDataLine line;
    private final List<Subscriber> subscribers;
    //endregion

    //region static fields
    private static final int BUFFER_SIZE = 44100; // Max number of samples to store
    private static final int PIPED_STREAM_BUFFER_SIZE = 4096;
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioProcessor.class);
    //endregion

    //region ctor
    public AudioProcessor() {
        // The audio format tells the application how to interpret and handle the bits of information
        // in the incoming sound stream.

        float SAMPLE_RATE = 44_100;     // Rate at which an audio file is sampled (Hz)
        int SAMPLE_SIZE_IN_BITS = 16;   // Number of bits used to represent each sample of the audio. Determines
                                        // the bit depth for the audio signal
        int CHANNELS = 2;               // Stereo sound = 2 samples per frame (2 * 16 bits = 32 bits per frame)
        boolean SIGNED = true;
        boolean BIG_ENDIAN = true;

        audioFormat = new AudioFormat(SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN);

        samples = new ConcurrentLinkedQueue<>();
        samplesArray = new short[PIPED_STREAM_BUFFER_SIZE/(SAMPLE_SIZE_IN_BITS / 8)];
        capturing = false;
        subscribers = new ArrayList<>();
    }
    //endregion

    //region public methods
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void startCapture() throws IOException {
        capturing = true;
        inputStream = new PipedInputStream();   // Data read from the PipedOutput stream is read from here
        outputStream = new PipedOutputStream(inputStream); // Data read from the data line is written here

        Thread captureThread = new Thread(() -> {
            try{
                captureAudioDataFromMicrophone(outputStream);
            } catch(IOException exception){
                System.out.println("An IOException occurred when capturing audio samples");
            }
        }, "Capture Thread");

        Thread processThread = new Thread(() -> {
            try {
                processCapturedSamples(inputStream);
            } catch(Exception exception) {
                System.out.println("An IOException occurred when processing captured samples");
            }
        }, "Process Thread");

        captureThread.start();
        processThread.start();
    }

    public void stopCapture() {
        capturing = false;
        if(line != null) {
            line.stop();
            line.close();
        }
        try {
            if(outputStream != null) outputStream.close();
            if(inputStream != null) inputStream.close();
        } catch(IOException exception) {
            System.out.println("An IOException occurred when closing streams");
        }
    }

    public ConcurrentLinkedQueue<Short> getSamples() { return samples; }
    public short[] getSamplesArray() { return samplesArray; }

    public void captureAudioDataFromMicrophone(PipedOutputStream outputStream) throws IOException {
        line = getTargetDataLine();
        byte[] writeBuffer = new byte[PIPED_STREAM_BUFFER_SIZE];

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
        byte[] readBuffer = new byte[PIPED_STREAM_BUFFER_SIZE];

        while (capturing) {
            int totalBytesRead = 0;
            while (totalBytesRead < numberOfBytesRead) {
                int bytesRead = inputStream.read(readBuffer, totalBytesRead, numberOfBytesRead - totalBytesRead);
                if (bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;
            }

            ByteBuffer.wrap(readBuffer).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(samplesArray);

//            this.generateSinusoidData(13180);

            if (totalBytesRead > 0) {
                if (samples.size() >= BUFFER_SIZE) {
                    this.setGenerateSpectrogram();
                }

                if(!this.generatingSpectrogram) {
                    for (short sample : samplesArray) {
                        samples.add(sample);
                    }
                }
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
                // Square of the magnitude here to get the power of the frequency j at time i
                spectrogram[i][j] = Math.pow(result[j].abs(), 2);
            }
        }

        return spectrogram;
    }

    public double[] getSpectrumMagnitudes() {
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        double[] doubleSamples = new double[samplesArray.length];
        double[] magnitudes = new double[samplesArray.length / 2];

        for(int i = 0; i < samplesArray.length; i++) {
            doubleSamples[i] = samplesArray[i];
        }

        Complex[] fft = transformer.transform(doubleSamples, TransformType.FORWARD);
        for(int i = 0; i < fft.length / 2; i++) {
            magnitudes[i] = Math.pow(fft[i].abs(), 2);
        }
        return magnitudes;
    }
    //endregion

    //region private methods
    private TargetDataLine getTargetDataLine() {
        if(line != null) {
            line.stop();
            line.close();
        }

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, this.audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
            LOGGER.debug("Failed to access the DataLine with given type and format as it's not supported in this system");
            return null;
        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(this.audioFormat);
        } catch(LineUnavailableException exception) {
            LOGGER.debug("There was no DataLine available for the application to acquire");
        }
        return line;
    }


    public void setGenerateSpectrogram() {
        this.generatingSpectrogram = true;
        notifySubscribers();
        this.generatingSpectrogram = false;
        samples.clear();
    }

    @Override 
    public void addSubscriber(Subscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        this.subscribers.remove(subscriber);
    }

    @Override 
    public void notifySubscribers() {
        subscribers.forEach(Subscriber::update);
    }

    //endregion
}
