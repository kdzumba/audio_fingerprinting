package org.kdzumba.gui.panels;

import javax.swing.*;
import java.awt.*;

public class MatchResultsPanel extends JPanel {
    private static  JLabel artistNameResultLabel;
    private static JLabel songNameResultLabel;
    private static JLabel songYearResultLabel;

    public MatchResultsPanel() {
        artistNameResultLabel = new JLabel();
        songNameResultLabel = new JLabel();
        songYearResultLabel = new JLabel();

        this.setBorder(BorderFactory.createTitledBorder("Match Results"));

        this.setLayout(new GridLayout(3,3));

        JLabel artistLabel = new JLabel("Artist:");
        this.add(artistLabel);
        this.add(artistNameResultLabel);

        JLabel songNameLabel = new JLabel("Name:");
        this.add(songNameLabel);
        this.add(songNameResultLabel);

        JLabel songYear = new JLabel("Year:");
        this.add(songYear);
        this.add(songYearResultLabel);
    }

    public static void updateMatchResults(String songName, String artistName, int year) {
        songNameResultLabel.setText(songName);
        artistNameResultLabel.setText(artistName);
        songYearResultLabel.setText(Integer.toString(year));
    }
}
