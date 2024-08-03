package org.kdzumba.gui.panels;

import org.kdzumba.AudioProcessor;
import org.kdzumba.gui.AudioVisualiserComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class AudioVisualizer extends JPanel {
    private final Logger LOGGER = LoggerFactory.getLogger(AudioVisualizer.class);
    private final AudioVisualiserComponent audioVisualizer;
    private final AudioProcessor audioProcessor = new AudioProcessor();

    public AudioVisualizer() {
        JRadioButton showGrid = new JRadioButton();
        showGrid.addActionListener((e) -> toggleGrid());

        JLabel gridLabel = new JLabel("Show Grid Lines");
        JPanel buttonLabelGroup = new JPanel();

        buttonLabelGroup.add(gridLabel);
        buttonLabelGroup.add(showGrid);

        JButton captureAudioButton = getCaptureButton();
        audioVisualizer = new AudioVisualiserComponent(audioProcessor.getSamples());
        var controlsPanel = new JPanel();
        controlsPanel.add(buttonLabelGroup);
        controlsPanel.add(captureAudioButton);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(audioVisualizer);
        this.add(controlsPanel);
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
