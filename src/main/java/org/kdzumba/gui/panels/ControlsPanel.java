package org.kdzumba.gui.panels;

import org.kdzumba.AudioProcessor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

@Component
public class ControlsPanel extends JPanel{
    private final AudioProcessor audioProcessor;
    private final JButton stopCaptureButton;
    private final JButton fingerprintButton;

    public ControlsPanel(AudioProcessor audioProcessor) {
        this.audioProcessor = audioProcessor;

        this.fingerprintButton = fingerprintButton();
        this.stopCaptureButton = getStopCaptureButton();

        this.setLayout(new FlowLayout(FlowLayout.LEFT));

        this.add(fingerprintButton);
        this.add(stopCaptureButton);
    }

    private JButton fingerprintButton() {
        JButton fingerprintButton = new JButton("Fingerprint");
        fingerprintButton.addActionListener((e) -> {
            try {
                this.stopCaptureButton.setEnabled(true);
                audioProcessor.startCapture();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return fingerprintButton;
    }

    private JButton getStopCaptureButton() {
        JButton stopAudioCapture = new JButton("Stop");
        stopAudioCapture.setEnabled(false);
        stopAudioCapture.addActionListener((e) -> {
            audioProcessor.setSongMetaData();
            audioProcessor.stopCapture();
        });
        return stopAudioCapture;
    }
}
