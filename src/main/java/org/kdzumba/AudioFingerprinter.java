package org.kdzumba;

import org.kdzumba.dataModels.Fingerprint;
import org.kdzumba.dataModels.Landmark;

import java.util.ArrayList;
import java.util.List;

public class AudioFingerprinter {
    private final double[][] spectrogram;
    private final int sampleRate;
    private final int windowSize;

    public AudioFingerprinter(double[][] spectrogram, int sampleRate, int windowSize) {
        this.spectrogram = spectrogram;
        this.sampleRate = sampleRate;
        this.windowSize = windowSize;
    }

    public List<Landmark> extractLandmarks() {
        List<Landmark> landmarks = new ArrayList<>();
        for(int t = 0; t < spectrogram.length; t++) {
            for(int f = 1; f < spectrogram[t].length - 1; f++) {
                if(isLocalPeak(t, f)) {
                    landmarks.add(new Landmark(t, f));
                }
            }
        }
        return landmarks;
    }

    public List<Fingerprint> generateFingerprints(List<Landmark> landmarks) {
        List<Fingerprint> fingerprints = new ArrayList<>();
        for(int i = 0; i < landmarks.size(); i++) {
            for(int j = i + 1; j < Math.min(i + 10, landmarks.size()); j++) {
                Landmark first = landmarks.get(i);
                Landmark second = landmarks.get(j);
                double dt = second.time - first.time;
                double df = second.frequency - first.frequency;
                fingerprints.add(new Fingerprint(first.time, first.frequency, df, dt));
            }
        }
        return fingerprints;
    }

    private boolean isLocalPeak(int t, int f) {
        return spectrogram[t][f] > spectrogram[t][f - 1] && spectrogram[t][f] > spectrogram[t][f + 1];
    }
}
