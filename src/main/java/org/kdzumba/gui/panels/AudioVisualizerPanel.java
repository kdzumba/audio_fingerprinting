package org.kdzumba.gui.panels;

import org.kdzumba.AudioProcessor;
import org.kdzumba.gui.components.ColorBarComponent;
import org.kdzumba.gui.components.SpectrogramComponent;
import org.kdzumba.gui.components.TimeAmplitudeGraphComponent;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;

public class AudioVisualizerPanel extends JPanel {
    private final TimeAmplitudeGraphComponent audioVisualizer;
    private final SpectrogramComponent spectrogram;
    private final AudioProcessor audioProcessor = new AudioProcessor();
    private final ColorBarComponent colorBar;

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
        spectrogramPanel.setLayout(new BoxLayout(spectrogramPanel, BoxLayout.X_AXIS));
        spectrogramPanel.add(spectrogram);
        spectrogramPanel.add(Box.createHorizontalStrut(10));
        spectrogramPanel.add(colorBar);

        this.add(spectrogramPanel);
        this.add(controlsPanel);
    }

    private JPanel getContentPanel() {
        JRadioButton showGrid = new JRadioButton();
        showGrid.setSelected(true);
        showGrid.addActionListener((e) -> toggleGrid());

        JLabel gridLabel = new JLabel("Show Grid Lines");
        JPanel buttonLabelGroup = new JPanel();

        buttonLabelGroup.add(gridLabel);
        buttonLabelGroup.add(showGrid);

        JButton captureAudioButton = getCaptureButton();
        JButton stopCaptureButton = getStopCaptureButton();
        var controlsPanel = new JPanel();
        controlsPanel.add(buttonLabelGroup);
        controlsPanel.add(captureAudioButton);
        controlsPanel.add(stopCaptureButton);
        return controlsPanel;
    }

    private JButton getCaptureButton() {
        JButton captureAudioButton = new JButton("Capture Audio");
        captureAudioButton.addActionListener((e) -> {
            startCaptureThread();
            Timer timer = new Timer(50, event -> audioVisualizer.repaint());
            timer.start();
        });
        return captureAudioButton;
    }

    private JButton getStopCaptureButton() {
        JButton stopAudioCapture = new JButton("Stop");
        stopAudioCapture.addActionListener((e) -> {
            audioProcessor.stopCapture();
        });
        return stopAudioCapture;
    }

    private void startCaptureThread() {
        new Thread(() -> {
            try {
                audioProcessor.startCapture();
                audioVisualizer.repaint();
                // Generate spectrogram data and update the SpectrogramComponent
                if(!audioProcessor.getSamples().isEmpty()) {
                    double[][] spectrogramData = audioProcessor.generateSpectrogram(1024, 512);
                    spectrogram.setSpectrogramData(spectrogramData);
                }
                spectrogram.repaint();
            } catch(IOException exception) {
                System.out.println("An IOException occurred when capturing samples");
            }
        }).start();
    }

    private void toggleGrid() {
        audioVisualizer.setShowGrid(!audioVisualizer.getShowGrid());
        audioVisualizer.repaint();
    }
}
