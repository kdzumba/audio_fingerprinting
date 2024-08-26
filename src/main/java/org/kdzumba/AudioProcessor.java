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

import org.kdzumba.database.entities.FingerprintHashEntity;
import org.kdzumba.database.entities.SongMetaData;
import org.kdzumba.database.repositories.FingerprintHashRepository;
import org.kdzumba.database.repositories.SongMetaDataRepository;
import org.kdzumba.interfaces.*;
import org.kdzumba.utils.MathUtils;

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
    private double[][] cumulativeSpectrogram;
    private final FingerprintHashRepository fingerprintHashRepository;
    private final SongMetaDataRepository songMetaDataRepository;

    private static final int BUFFER_SIZE = 49152; // Max number of samples to store for spectrogram generation
    private static final int PIPED_STREAM_BUFFER_SIZE = 2048; // Number of bytes to read off the Target Data Line's buffer

    public AudioProcessor(FingerprintHashRepository fingerprintHashRepository, SongMetaDataRepository songMetaDataRepository) {
        this.fingerprintHashRepository = fingerprintHashRepository;
        this.songMetaDataRepository = songMetaDataRepository;

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
            this.generateFingerprints();
            if(shouldPerformMatch) {
                Set<FingerprintHashEntity> hashEntitiesToMatch = new HashSet<>();

                for(FingerprintHash fingerprint : toMatch) {
                    FingerprintHashEntity dbHash = new FingerprintHashEntity();
                    dbHash.setHash(fingerprint.hashCode());
                    dbHash.setAnchorTimeOffset(fingerprint.anchorTimeOffset);
                    hashEntitiesToMatch.add(dbHash);
                }

                SongMetaData matchedSong = findBestMatch(hashEntitiesToMatch);

                shouldPerformMatch = false;
                cumulativeSpectrogram = null;
                toMatch = null;
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
                int bytesRead = inputStream.read(pipedOutputStreamBuffer,
                        totalBytesRead,
                        numberOfBytesRead - totalBytesRead);
                if (bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;
            }

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

    private void normalizeMagnitudes(double[][] spectrogram) {
        MathUtils.Range spectrogramRange = getSpectrogramRange(spectrogram);
        MathUtils.Range normalizedRange = new MathUtils.Range(0, 100);

        for(int i = 0; i < spectrogram.length; i++) {
            for(int j = 0; j < spectrogram[0].length; j++) {
                spectrogram[i][j] = MathUtils.convertToRange(spectrogram[i][j], spectrogramRange, normalizedRange);
            }
        }
    }

    private static MathUtils.Range getSpectrogramRange(double[][] spectrogram) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (double[] doubles : spectrogram) {
            for (int j = 0; j < spectrogram[0].length; j++) {
                double current = doubles[j];
                if (current > max) {
                    max = current;
                }
                if (current < min) {
                    min = current;
                }
            }
        }
        return new MathUtils.Range(min, max);
    }

    public double[][] generateSpectrogram(int windowSize, int hopSize, List<Short> samples) {
        int numberOfTimeBlocks = (samples.size() - windowSize) / hopSize + 1;
        int numberOfFrequencyBins = (windowSize / 2);
        double[][] spectrogram = new double[numberOfTimeBlocks][numberOfFrequencyBins];
        double[] windowFunction = new double[windowSize];

        for(int i = 0; i < windowSize; i++) {
            windowFunction[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (windowSize - 1));
        }

        for(int i = 0; i < numberOfTimeBlocks; i++) {
            double[] timeBlock = new double[windowSize];
            Iterator<Short> iterator = samples.iterator();
            for(int j = 0; j < i * hopSize; j++) iterator.next();

            for(int k = 0; k < windowSize; k++) {
                if(iterator.hasNext()) {
                    timeBlock[k] = iterator.next() * windowFunction[k];
                } else {
                    timeBlock[k] = 0;
                }
            }

            performSTFT(spectrogram, timeBlock, numberOfFrequencyBins, i);
        }

        normalizeMagnitudes(spectrogram);
        new Thread(() -> this.updateCumulativeSpectrogram(spectrogram)).start();
        return spectrogram;
    }

    private void performSTFT(double[][] spectrogram, double[] timeBlock, int numberOfFrequencyBins, int currentWindow) {
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] stft = transformer.transform(timeBlock, TransformType.FORWARD);

        for(int j = 0; j < numberOfFrequencyBins; j++) {
            double magnitudedB = 20 * Math.log10(stft[j].abs());
            spectrogram[currentWindow][j] = magnitudedB;
        }
    }

    private TargetDataLine getTargetDataLine() {
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

    public List<Peak> extractBandPeaks(double[][] spectrogram, double threshold, double hopSize) {
        List<Peak> bandPeaks = new ArrayList<>();
        int numberOfBands = 12;
        int numberOfWindows = spectrogram.length;
        double nyquistFreq = this.audioFormat.getSampleRate() / 2.0;
        double[] bandBoundaries = new double[numberOfBands + 1];

        for(int i = 0; i <= numberOfBands; i++) {
            bandBoundaries[i] = nyquistFreq / Math.pow(2, numberOfBands - i);
        }

        for(int window = 0; window < numberOfWindows; window++) {
            double[] windowData = spectrogram[window];
            double timeOffset = window * hopSize / this.audioFormat.getSampleRate();
            for(int bin = 0; bin < windowData.length; bin++) {
                double frequency = (bin * nyquistFreq) / (windowData.length - 1);
                if(windowData[bin] > threshold && frequency > 300) {
                    int band = getBandForFrequency(bandBoundaries, frequency);
                    bandPeaks.add(new Peak(window, bin, band, windowData[bin], frequency, timeOffset));
                }
            }
        }
        return bandPeaks;
    }

    private int getBandForFrequency(double[] bandBoundaries, double frequency) {
        for (int i = 0; i < bandBoundaries.length - 1; i++) {
            if (frequency >= bandBoundaries[i + 1] && frequency < bandBoundaries[i]) {
                return i;
            }
        }
        return bandBoundaries.length - 1; // Frequency is in the highest band
    }

    public Set<FingerprintHash> generateHashes(List<Peak> peaks, int fanOut) {
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
        List<Peak> peaks = extractBandPeaks(spectrogram, peakThreshold, 100);
        return generateHashes(peaks, fanOut);
    }

    public void saveFingerprints(Set<FingerprintHash> fingerprints, String filename) {
        SongMetaData metaData = new SongMetaData();
        metaData.setYear(2024);
        metaData.setSong("3001");
        metaData.setArtist("J. Cole");
        songMetaDataRepository.save(metaData);

        for(FingerprintHash fingerprint : fingerprints) {
            FingerprintHashEntity dbHash = new FingerprintHashEntity();
            dbHash.setHash(fingerprint.hashCode());
            dbHash.setSongMetaData(metaData);
            dbHash.setAnchorTimeOffset(fingerprint.anchorTimeOffset);
            fingerprintHashRepository.save(dbHash);
        }
    }

    public SongMetaData findBestMatch(Set<FingerprintHashEntity> sampleFingerprints) {
        List<FingerprintHashEntity> storedFingerprints = fingerprintHashRepository.findAllByOrderByHashAsc();;
        Map<Long, List<Double>> timeDiffMap = new HashMap<>();

        for (FingerprintHashEntity sampleFingerprint : sampleFingerprints) {
            for (FingerprintHashEntity storedFingerprint : storedFingerprints) {
                if (sampleFingerprint.equals(storedFingerprint)) {
                    Long songId = storedFingerprint.getSongMetaData().getId();
                    double timeDifference = sampleFingerprint.getAnchorTimeOffset() - storedFingerprint.getAnchorTimeOffset();

                    timeDiffMap.computeIfAbsent(songId, k -> new ArrayList<>()).add(timeDifference);
                }
            }
        }

        // Scoring based on time difference histogram
        Long bestMatchSongId = null;
        int maxHistogramPeak = 0;

        for (Map.Entry<Long, List<Double>> entry : timeDiffMap.entrySet()) {
            List<Double> timeDiffs = entry.getValue();
            int histogramPeak = calculateHistogramPeak(timeDiffs);

            if (histogramPeak > maxHistogramPeak) {
                maxHistogramPeak = histogramPeak;
                bestMatchSongId = entry.getKey();
            }
        }

        if (bestMatchSongId != null) {
            return songMetaDataRepository.findById(bestMatchSongId).orElse(null);
        }
        return null;
    }

    private int calculateHistogramPeak(List<Double> timeDifferences) {
        Map<Double, Integer> histogram = new HashMap<>();

        for (double timeDiff : timeDifferences) {
            histogram.put(timeDiff, histogram.getOrDefault(timeDiff, 0) + 1);
        }

        return histogram.values().stream().max(Integer::compareTo).orElse(0);
    }


    private void updateCumulativeSpectrogram(double[][] newSpectrogramData) {
        if(cumulativeSpectrogram == null) {
            cumulativeSpectrogram = newSpectrogramData;
        } else {
            // Append the new spectrogram data to the existing cumulative data
            int existingLength = cumulativeSpectrogram.length;
            int newLength = newSpectrogramData.length;
            int totalLength = existingLength + newLength;

            double[][] updatedSpectrogramData = new double[totalLength][];
            System.arraycopy(cumulativeSpectrogram, 0, updatedSpectrogramData, 0, existingLength);
            System.arraycopy(newSpectrogramData, 0, updatedSpectrogramData, existingLength, newLength);
            cumulativeSpectrogram = updatedSpectrogramData;
        }
    }

    public void generateFingerprints() {
        double peakThreshold = 98;
        int fanOut = 10;
        Set<FingerprintHash> fingerprints = this.generateAudioFingerprint(cumulativeSpectrogram, peakThreshold, fanOut);

        if (!shouldPerformMatch) {
            this.saveFingerprints(fingerprints, "fingerprints.ser");
            System.out.println("Done fingerprinting...");
        } else {
            this.toMatch = fingerprints;
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
