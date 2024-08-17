package org.kdzumba;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.kdzumba.dataModels.FingerprintHash;
import org.kdzumba.dataModels.Peak;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import org.kdzumba.interfaces.*;

public class AudioProcessor implements Publisher{
    private final AudioFormat audioFormat;
    public final List<Short> samples;
    private final short[] samplesArray;
    public boolean capturing;
    private int numberOfBytesRead;
    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;
    private TargetDataLine line;
    private final List<Subscriber> subscribers;
    public Set<FingerprintHash> toMatch;
    public boolean shouldPerformMatch = false;
    private double[][] cummulativeSpectrogram;

    private static final int BUFFER_SIZE = 1024; // Max number of samples to store for spectrogram generation
    private static final int PIPED_STREAM_BUFFER_SIZE = 2048; // Number of bytes to read off the Target Data Line's buffer

    public AudioProcessor() {
        float SAMPLE_RATE = 8192;
        int SAMPLE_SIZE_IN_BITS = 16;

        int CHANNELS = 1;
        boolean SIGNED = true;
        boolean BIG_ENDIAN = true;

        audioFormat = new AudioFormat(SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN);

        samples = new LinkedList<>();
        samplesArray = new short[PIPED_STREAM_BUFFER_SIZE/(SAMPLE_SIZE_IN_BITS / 8)];
        capturing = false;
        subscribers = new ArrayList<>();
    }

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
                exception.printStackTrace();
            }
        }, "Capture Thread");

        Thread processThread = new Thread(() -> {
            try {
                processCapturedSamples(inputStream);
            } catch(Exception exception) {
                System.out.println("An IOException occurred when processing captured samples");
                exception.printStackTrace();
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

            if(shouldPerformMatch) {
                Set<FingerprintHash> storedFingerprints = loadFingerprints("fingerprints.ser");
                System.out.println("Number of hashes stored: " + storedFingerprints.size());
                System.out.println("Number of hashes matched: " + storedFingerprints.size());
                int matchScore = matchFingerprints(storedFingerprints, toMatch);

                if (matchScore > 75) {
                    System.out.println("MatchScore: " + matchScore);
                    System.out.println("Match found!");
                } else {
                    System.out.println("MatchScore: " + matchScore);
                    System.out.println("No match.");
                }
            }
        } catch(IOException exception) {
            System.out.println("An IOException occurred when closing streams");
        }
    }

    public short[] getSamplesArray() { return samplesArray; }

    public void captureAudioDataFromMicrophone(PipedOutputStream outputStream) throws IOException {
        line = getTargetDataLine();
        byte[] microphoneDataBuffer = new byte[PIPED_STREAM_BUFFER_SIZE];

        // Start Capturing Audio
        Objects.requireNonNull(line).start();
        try (outputStream;) {
            while (capturing) {
                numberOfBytesRead = line.read(microphoneDataBuffer, 0, microphoneDataBuffer.length);
                outputStream.write(microphoneDataBuffer, 0, numberOfBytesRead);
            }
            line.flush();
        } finally {
            line.stop();
            line.close();
        }
    }

    public void processCapturedSamples(PipedInputStream inputStream) throws IOException {
        byte[] pipedOutputStreamBuffer = new byte[PIPED_STREAM_BUFFER_SIZE];

        while (capturing) {
            int totalBytesRead = 0;
            while (totalBytesRead < numberOfBytesRead) {
                int bytesRead = inputStream.read(pipedOutputStreamBuffer, totalBytesRead, numberOfBytesRead - totalBytesRead);
                if (bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;
            }

            //This conversion from bytes to shorts means we have half the number of samples as 
            //the bytes that were read (1 short = 2 bytes), so the samples array always contains 
            //half the read bytes size
            ByteBuffer.wrap(pipedOutputStreamBuffer).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(samplesArray);

            if (totalBytesRead > 0) {
                if (samples.size() >= BUFFER_SIZE) { // when we get BUFFER_SIZE samples, we have 1 second worth of audio data
                    this.notifySamplesChanged(new ArrayList<>(samples));
                    samples.clear();
                }
                for (short sample : samplesArray) {
                  samples.add(sample);
                }   
            }   
        }   
    }

    public double[][] generateSpectrogram(int windowSize, int hopSize, List<Short> samples) {
        int numberOfTimeBlocks = (samples.size() - windowSize) / hopSize + 1;
        int numberOfFrequencyBins = (windowSize / 2);
        double[][] spectrogram = new double[numberOfTimeBlocks][numberOfFrequencyBins];
        double[] windowFunction = new double[windowSize];

        // Hamming window function
        for(int i = 0; i < windowSize; i++) {
            windowFunction[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (windowSize - 1));
        }

        for(int i = 0; i < numberOfTimeBlocks; i++) {
            double[] timeBlock = new double[windowSize];

            Iterator<Short> iterator = samples.iterator();
            // skip to start of the current window
            for(int j = 0; j < i * hopSize; j++) iterator.next();

            for(int k = 0; k < windowSize; k++) {
                if(iterator.hasNext()) {
                    // Applying the window function to each of the samples within the current timeBlock
                    timeBlock[k] = iterator.next() * windowFunction[k];
                } else {
                    // Zero-padding if we run out of samples before reaching window size
                    timeBlock[k] = 0;
                }
            }

            // Perform Short Time Fourier Transform (stft) on the timeBlock
            FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
            Complex[] stft = transformer.transform(timeBlock, TransformType.FORWARD);

            
            for(int j = 0; j < numberOfFrequencyBins; j++) {
                double magnitude = stft[j].abs();
                spectrogram[i][j] = 20 * Math.log10(magnitude);
            }
        }

        this.updateCummulativeSpectrogram(spectrogram);
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

    private TargetDataLine getTargetDataLine() {
        if(line != null) {
            line.stop();
            line.close();
        }

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, this.audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Specified DataLine type not supported");
            return null;
        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(this.audioFormat);
        } catch(LineUnavailableException exception) {
            System.out.println("LineUnavailableException");
        }
        return line;
    }

    public void notifySamplesChanged(List<Short> samples) {
        new Thread(() -> {
          notifySubscribers(samples);
        }).start();
    }

    public Set<Peak> extractPeaks(double[][] spectrogram, double threshold) {
        Set<Peak> peaks = new HashSet<>();

        for (int time = 1; time < spectrogram.length - 1; time++) {
            for (int frequency = 1; frequency < spectrogram[time].length - 1; frequency++) {
                double magnitude = spectrogram[time][frequency];
                if (magnitude > threshold &&
                        magnitude > spectrogram[time - 1][frequency] &&
                        magnitude > spectrogram[time + 1][frequency] &&
                        magnitude > spectrogram[time][frequency - 1] &&
                        magnitude > spectrogram[time][frequency + 1]) {

                    peaks.add(new Peak(time, frequency, magnitude));
                }
            }
        }
        return peaks;
    }

    public Set<FingerprintHash> generateHashes(Set<Peak> peaks, int fanOut) {
        Set<FingerprintHash> hashes = new HashSet<>();
        List<Peak> peakList = new ArrayList<>(peaks);

        for (int i = 0; i < peakList.size(); i++) {
            Peak anchor = peakList.get(i);

            for (int j = 1; j <= fanOut && i + j < peakList.size(); j++) {
                Peak point = peakList.get(i + j);
                FingerprintHash hash = new FingerprintHash(anchor, point);
                hashes.add(hash);
            }
        }
        return hashes;
    }

    public Set<FingerprintHash> generateAudioFingerprint(double[][] spectrogram, double peakThreshold, int fanOut) {
        Set<Peak> peaks = extractPeaks(spectrogram, peakThreshold);
        return generateHashes(peaks, fanOut);
    }

    public void saveFingerprints(Set<FingerprintHash> fingerprints, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(fingerprints);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<FingerprintHash> loadFingerprints(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Set<FingerprintHash>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int matchFingerprints(Set<FingerprintHash> storedFingerprints, Set<FingerprintHash> newFingerprints) {
        Set<FingerprintHash> intersection = new HashSet<>(storedFingerprints);
        intersection.retainAll(newFingerprints);
        return intersection.size();
    }

    private void updateCummulativeSpectrogram(double[][] newSpectrogramData) {
        if(cummulativeSpectrogram == null) {
            cummulativeSpectrogram = newSpectrogramData;
        } else {
            // Append the new spectrogram data to the existing cumulative data
            int existingLength = cummulativeSpectrogram.length;
            int newLength = newSpectrogramData.length;
            int totalLength = existingLength + newLength;

            double[][] updatedSpectrogramData = new double[totalLength][];
            System.arraycopy(cummulativeSpectrogram, 0, updatedSpectrogramData, 0, existingLength);
            System.arraycopy(newSpectrogramData, 0, updatedSpectrogramData, existingLength, newLength);
            cummulativeSpectrogram = updatedSpectrogramData;
        }
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
    public void notifySubscribers(List<Short> samples) {
        subscribers.forEach(s -> s.update(samples));
    }
}
