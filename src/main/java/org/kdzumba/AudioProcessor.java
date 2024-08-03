package org.kdzumba;

import javax.sound.sampled.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class AudioProcessor {
    private AudioFormat audioFormat;
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioProcessor.class);
    public double[] capturedInputAmplitudes;
    private int streamBufferSize;

    public AudioProcessor() {
        // The audio format tells Java how to interpret and handle the bits of information
        // in the incoming sound stream.

        float SAMPLE_RATE = 44100f;     // Rate at which an audio file is sampled (Hz)
        int SAMPLE_SIZE_IN_BITS = 16;   // Number of bits used to represent each sample of the audio. Determines
                                        // the bit depth for the audio signal
        int CHANNELS = 1;               // Mono or Stereo Sound
        boolean SIGNED = true;
        boolean BIG_ENDIAN = false;

        audioFormat = new AudioFormat(SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN);
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    // The captured data is written to a PipedOutputStream, which gets connected to the passed in 
    // PipedInputStream object from which the data will be read in a separate thread
    public void captureAudioDataFromMicrophone(PipedInputStream inputStream) throws IOException {
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);
        int numberOfBytesRead;
        TargetDataLine line = getTargetDataLine();

        // 1 Frame = 1/8 the size of the line's buffer
        // This ensures the program's success in sharing the line's buffer with the mixer
        this.streamBufferSize = Objects.requireNonNull(line).getBufferSize() / 8;
        byte[] data = new byte[this.streamBufferSize];

        // Start Capturing Audio
        line.start();
        while (true) {
            numberOfBytesRead = line.read(data, 0, data.length);
            // We are writing all the data that we read from the data line to the outputStream
            // This data might not fill up the whole streamBufferSize, hence numberOfBytesRead
            outputStream.write(data, 0, numberOfBytesRead);
        }
        // Remove any remaining bytes from the Line's buffer
        //line.flush();
        //outputStream.close();
    }

    public int getStreamBufferSize() { return this.streamBufferSize; }

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

    private static int[] byteToIntArray(byte[] bytes) {
        ByteBuffer wrappedBytes = ByteBuffer.wrap(bytes);
        int[] ints = new int[bytes.length / 4]; // 4 here is the size of an integer in bytes
        for(int i = 0; i < ints.length; i++) {
            ints[i] = wrappedBytes.getInt();
        }
        return ints;
    }

    private double normalize(byte value) {
        int sampleSizeInBits = this.audioFormat.getSampleSizeInBits();
        int maxSampleValue = 1 << sampleSizeInBits; // Effectively doing 2^sampleSizeInBits
        double desiredAmplitudeRange = 1.0; // Our audio amplitudes will be in the range [-1.0 and 1.0]
        double scalingFactor = desiredAmplitudeRange / maxSampleValue;
        return value * scalingFactor;
    }

    public void setAudioFormat(AudioFormat format) {
        audioFormat = format;
    }
}
