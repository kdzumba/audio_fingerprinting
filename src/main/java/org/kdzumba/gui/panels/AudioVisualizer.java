package org.kdzumba.gui.panels;

import org.kdzumba.AudioProcessor;
import org.kdzumba.gui.AudioVisualiserComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.PipedInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioVisualizer extends JPanel {
    private final Logger LOGGER = LoggerFactory.getLogger(AudioVisualizer.class);
    private final AudioVisualiserComponent audioVisualizer;
    private final AudioProcessor audioProcessor = new AudioProcessor();
    private final PipedInputStream inputStream = new PipedInputStream();

    public AudioVisualizer() {
        JRadioButton showGrid = new JRadioButton();
        showGrid.addActionListener((e) -> toggleGrid());

        JLabel gridLabel = new JLabel("Show Grid Lines");
        JPanel buttonLabelGroup = new JPanel();

        buttonLabelGroup.add(gridLabel);
        buttonLabelGroup.add(showGrid);

        JButton captureAudioButton = new JButton("Capture Audio");
        captureAudioButton.addActionListener((e) -> new Thread(() -> {
            try {
                audioProcessor.captureAudioDataFromMicrophone(inputStream);
            } catch(IOException exception) {
                System.out.println("An IOException occurred");
            }
        }).start());


        audioVisualizer = new AudioVisualiserComponent();

        captureAudioButton.addActionListener((e) -> new Thread(() -> {
            try { 
                byte[] readBuffer = new byte[audioProcessor.getStreamBufferSize()];
                while(true) {
                    int bytesRead = inputStream.read(readBuffer, 0, readBuffer.length);
                    if(bytesRead > 0) {
                        short[] samples = new short[bytesRead / 2];
                        ByteBuffer.wrap(readBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);

                        audioVisualizer.setSamples(samples);
                        for(short sample : samples) { System.out.println("Sample: " + sample); }
                        audioVisualizer.repaint();
                    }
                }
            } catch(IOException exception) {
                System.out.println("An IOException occurred");
            }
        }).start());

        var controlsPanel = new JPanel();

        buttonLabelGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        captureAudioButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        controlsPanel.add(buttonLabelGroup);
        controlsPanel.add(captureAudioButton);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(audioVisualizer);
        this.add(controlsPanel);
    }

    private void toggleGrid() {
        System.out.println("Toggling grid visibility");
        LOGGER.debug("Toggling grid visibility");
        audioVisualizer.setShowGrid(!audioVisualizer.getShowGrid());
        audioVisualizer.repaint();
    }
}
