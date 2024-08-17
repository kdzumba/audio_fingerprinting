package org.kdzumba.gui.panels;

import org.kdzumba.AudioProcessor;
import org.kdzumba.dataModels.FingerprintHash;
import org.kdzumba.gui.components.ColorBarComponent;
import org.kdzumba.gui.components.SpectrogramComponent;
import org.kdzumba.gui.components.TimeAmplitudeGraphComponent;
import org.kdzumba.interfaces.Subscriber;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.*;
import java.io.IOException;
import java.util.Set;
import java.util.List;

public class AudioVisualizerPanel extends JPanel implements Subscriber {
    private final TimeAmplitudeGraphComponent audioVisualizer;
    private final SpectrogramComponent spectrogram;
    private final AudioProcessor audioProcessor = new AudioProcessor();
    private final ColorBarComponent colorBar;
    private boolean shouldPerformMatch = false;
    private double[][] cumulativeSpectrogramData = null;

    public AudioVisualizerPanel() {
        var controlsPanel = getContentPanel();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        audioVisualizer = new TimeAmplitudeGraphComponent(audioProcessor.getSamplesArray());
        this.add(audioVisualizer);
        this.add(Box.createVerticalStrut(10));

        spectrogram = new SpectrogramComponent(audioProcessor.getAudioFormat());

        Color[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};
        colorBar = new ColorBarComponent(colors, 0, 100, 10);

        JPanel spectrogramPanel = new JPanel();
        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        spectrogramPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        spectrogramPanel.setLayout(new BoxLayout(spectrogramPanel, BoxLayout.X_AXIS));
        spectrogramPanel.add(spectrogram);
        spectrogramPanel.add(Box.createHorizontalStrut(10));
        spectrogramPanel.add(colorBar);

        this.add(spectrogramPanel);
        this.add(controlsPanel);

        this.audioProcessor.addSubscriber(this);
    }

    private JPanel getContentPanel() {
        JRadioButton showGrid = new JRadioButton();
        showGrid.setSelected(true);
        showGrid.addActionListener((e) -> toggleGrid());

        JLabel gridLabel = new JLabel("Show Grid Lines");
        JPanel buttonLabelGroup = new JPanel();

        buttonLabelGroup.add(gridLabel);
        buttonLabelGroup.add(showGrid);

        JButton fingerprintButton = fingerprintButton();
        JButton matchButton = matchButton();
        JButton stopCaptureButton = getStopCaptureButton();
        var controlsPanel = new JPanel();
        controlsPanel.add(buttonLabelGroup);
        controlsPanel.add(fingerprintButton);
        controlsPanel.add(matchButton);
        controlsPanel.add(stopCaptureButton);
        return controlsPanel;
    }

    private JButton fingerprintButton() {
        JButton fingerprintButton = new JButton("Fingerprint");
        fingerprintButton.addActionListener((e) -> {
            try {
                audioProcessor.startCapture();
                    Timer timer = new Timer(50, (event) -> {
                        audioVisualizer.repaint();
                    });
                    timer.start();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return fingerprintButton;
    }

    private JButton matchButton() {
        JButton matchButton = new JButton("Match");
        matchButton.addActionListener((e) -> {
            shouldPerformMatch = true;
            try {
                audioProcessor.startCapture();
                Timer timer = new Timer(50, (event) -> {
                    audioVisualizer.repaint();
                });
                timer.start();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return matchButton;
    }

    private JButton getStopCaptureButton() {
        JButton stopAudioCapture = new JButton("Stop");
        stopAudioCapture.addActionListener((e) -> audioProcessor.stopCapture());
        return stopAudioCapture;
    }

    private void toggleGrid() {
        audioVisualizer.setShowGrid(!audioVisualizer.getShowGrid());
        audioVisualizer.repaint();
    }
    
    @Override 
    public void update(List<Short> samples) {
        this.onGenerateSpectrogram(samples);
    }

    private void onGenerateSpectrogram(List<Short> samples) {

        //TODO: This guy is doing too much. Should only be receiving latest spectrogram data
        //and rendering that

        double[][] newSpectrogramData = audioProcessor.generateSpectrogram(1024, 992, samples);
        if(cumulativeSpectrogramData == null) {
            cumulativeSpectrogramData = newSpectrogramData;
        } else {
            // Append the new spectrogram data to the existing cumulative data
            int existingLength = cumulativeSpectrogramData.length;
            int newLength = newSpectrogramData.length;
            int totalLength = existingLength + newLength;

            double[][] updatedSpectrogramData = new double[totalLength][];
            System.arraycopy(cumulativeSpectrogramData, 0, updatedSpectrogramData, 0, existingLength);
            System.arraycopy(newSpectrogramData, 0, updatedSpectrogramData, existingLength, newLength);
            cumulativeSpectrogramData = updatedSpectrogramData;
        }

        //We are passing the newly generated spectrogram data for visualization
        spectrogram.setSpectrogramData(newSpectrogramData);

        double peakThreshold = 10.0;
        int fanOut = 10;
        Set<FingerprintHash> fingerprints = audioProcessor.generateAudioFingerprint(cumulativeSpectrogramData, peakThreshold, fanOut);

        if (!shouldPerformMatch) {
            audioProcessor.shouldPerformMatch = false;
            audioProcessor.saveFingerprints(fingerprints, "fingerprints.ser");
        } else {
            audioProcessor.toMatch = fingerprints;
            audioProcessor.shouldPerformMatch = true;
        }
    }
}
