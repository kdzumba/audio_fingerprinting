package org.kdzumba.gui.panels;

import org.kdzumba.AudioProcessor;
import org.kdzumba.gui.common.Constants;
import org.kdzumba.gui.components.ColorBarComponent;
import org.kdzumba.gui.components.SpectrogramComponent;
import org.kdzumba.gui.components.TimeAmplitudeGraphComponent;
import org.kdzumba.interfaces.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

@Component
public class AudioVisualizerPanel extends JPanel implements Subscriber {
    private final TimeAmplitudeGraphComponent audioVisualizer;
    private final SpectrogramComponent spectrogram;
    private final AudioProcessor audioProcessor;
    private final ColorBarComponent colorBar;

    @Autowired
    public AudioVisualizerPanel(AudioProcessor audioProcessor) {
        this.audioProcessor= audioProcessor;
        var controlsPanel = getContentPanel();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        audioVisualizer = new TimeAmplitudeGraphComponent(audioProcessor.getSamplesArray());
        this.add(audioVisualizer);
        this.add(Box.createVerticalStrut(10));

        spectrogram = new SpectrogramComponent(audioProcessor.getAudioFormat(), 1024);

        colorBar = new ColorBarComponent(Constants.SPECTROGRAM_GRADIENT, 0, 100, 10);

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

        JButton matchButton = matchButton();
        var controlsPanel = new JPanel();
        controlsPanel.add(buttonLabelGroup);
        controlsPanel.add(matchButton);
        return controlsPanel;
    }

    private JButton matchButton() {
        JButton matchButton = new JButton("Match");
        matchButton.addActionListener((e) -> {
            audioProcessor.shouldPerformMatch = true;
            try {
                if(!audioProcessor.capturing)
                    audioProcessor.startCapture();
                // Start a timer for 30 seconds
                java.util.Timer timer = new java.util.Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Perform match after 30 seconds of capture
                        audioProcessor.performMatch();
                    }
                }, 30000); // 30,000 milliseconds = 30 seconds
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return matchButton;
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
        double[][] newSpectrogramData = audioProcessor.generateSpectrogram(1024, 100, samples);
        spectrogram.setSpectrogramData(newSpectrogramData);
    }
}
