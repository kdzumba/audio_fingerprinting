package org.kdzumba.gui.panels;

import org.kdzumba.AudioProcessor;
import org.kdzumba.gui.components.ColorBarComponent;
import org.kdzumba.gui.components.FrequencySpectrumComponent;
import org.kdzumba.gui.components.SpectrogramComponent;
import org.kdzumba.gui.components.TimeAmplitudeGraphComponent;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class AudioVisualizerPanel extends JPanel {
    private final TimeAmplitudeGraphComponent audioVisualizer;
    private final SpectrogramComponent spectrogram;
//    private final FrequencySpectrumComponent frequencySpectrum;
    private final AudioProcessor audioProcessor = new AudioProcessor();
    private final ColorBarComponent colorBar;

    public AudioVisualizerPanel() {
        var controlsPanel = getContentPanel();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        audioVisualizer = new TimeAmplitudeGraphComponent(audioProcessor.getSamplesArray());
        this.add(audioVisualizer);
        this.add(Box.createVerticalStrut(10));

//        frequencySpectrum = new FrequencySpectrumComponent();
//        this.add(frequencySpectrum);
//        this.add(Box.createVerticalStrut(10));

        spectrogram = new SpectrogramComponent(audioProcessor.getAudioFormat());

        Color[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};
        colorBar = new ColorBarComponent(colors, 0, 100, 10);

        JPanel spectrogramPanel = new JPanel();
        spectrogramPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
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
            try {
                startCaptureThread();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return captureAudioButton;
    }

    private JButton getStopCaptureButton() {
        JButton stopAudioCapture = new JButton("Stop");
        stopAudioCapture.addActionListener((e) -> audioProcessor.stopCapture());
        return stopAudioCapture;
    }

    private void startCaptureThread() throws IOException {
        SwingWorker<Void, double[][]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                audioProcessor.startCapture();
                while(audioProcessor.capturing) {
                    if(!audioProcessor.getSamples().isEmpty()) {
                        double[][] spectrogramData = audioProcessor.generateSpectrogram(1024, 512);
                        publish(spectrogramData);
                    }
                }
                return null;
            }

            @Override
            protected void process(List<double[][]> chunks) {
                if(!chunks.isEmpty()) {
                    double[][] latestSpectrogramData = chunks.get(chunks.size() - 1);
                    spectrogram.setSpectrogramData(latestSpectrogramData);
                }
            }
        };

//        SwingWorker<Void, double[]> frequencySpectrumWorker = new SwingWorker<>() {
//            @Override
//            protected Void doInBackground() {
//                System.out.println("Frequency spectrum worker background..........");
//                while(audioProcessor.capturing) {
//                    double[] magnitudes = audioProcessor.getSpectrumMagnitudes();
//                    publish(magnitudes);
//                }
//                return null;
//            }
//
//            @Override
//            protected void process(List<double[]> chunks) {
//                if(!chunks.isEmpty()) {
//                    double[] magnitudes = chunks.get(chunks.size() - 1);
//                    frequencySpectrum.setMagnitudes(magnitudes);
//                }
//            }
//        };
//
//        frequencySpectrumWorker.execute();
        worker.execute();

        Timer timer = new Timer(50, event -> {
            audioVisualizer.repaint();
            spectrogram.repaint();
//            frequencySpectrum.repaint();
        });
        timer.start();
    }

    private void toggleGrid() {
        audioVisualizer.setShowGrid(!audioVisualizer.getShowGrid());
        audioVisualizer.repaint();
    }
}
