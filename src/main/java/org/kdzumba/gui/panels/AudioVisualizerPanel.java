package org.kdzumba.gui.panels;

import org.kdzumba.AudioProcessor;
import org.kdzumba.gui.components.ColorBarComponent;
import org.kdzumba.gui.components.SpectrogramComponent;
import org.kdzumba.gui.components.TimeAmplitudeGraphComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class AudioVisualizerPanel extends JPanel {
    private final Logger LOGGER = LoggerFactory.getLogger(AudioVisualizerPanel.class);
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
        var controlsPanel = new JPanel();
        controlsPanel.add(buttonLabelGroup);
        controlsPanel.add(captureAudioButton);
        return controlsPanel;
    }

    private JButton getCaptureButton() {
        JButton captureAudioButton = new JButton("Capture Audio");
        captureAudioButton.addActionListener((e) -> {
            try {
                PipedOutputStream outputStream = new PipedOutputStream();
                PipedInputStream inputStream = new PipedInputStream(outputStream);

                startCaptureThread(outputStream);
                startSamplesProcessingThread(inputStream);

                Timer timer = new Timer(50, event -> audioVisualizer.repaint());
                timer.start();
            } catch(IOException exception) {
                System.out.println("An IOException occurred when setting up streams");
            }
        });
        return captureAudioButton;
    }

    private void startCaptureThread(PipedOutputStream outputStream) {
        new Thread(() -> {
            try {
                audioProcessor.capturing = !audioProcessor.capturing;
                audioProcessor.captureAudioDataFromMicrophone(outputStream);
            } catch(IOException exception) {
                System.out.println("An IOException occurred when capturing samples");
            }
        }).start();
    }

    private void startSamplesProcessingThread(PipedInputStream inputStream) {
        new Thread(() -> {
            try {
                audioProcessor.processCapturedSamples(inputStream);
                audioVisualizer.repaint();

                // Generate spectrogram data and update the SpectrogramComponent
                double[][] spectrogramData = audioProcessor.generateSpectrogram(1024, 512);
                spectrogram.setSpectrogramData(spectrogramData);
            } catch(IOException exception) {
                System.out.println("An IO Exception occurred when processing samples");
            }
        }).start();
    }
    private void toggleGrid() {
        System.out.println("Toggling grid visibility");
        LOGGER.debug("Toggling grid visibility");
        audioVisualizer.setShowGrid(!audioVisualizer.getShowGrid());
        audioVisualizer.repaint();
    }
}
