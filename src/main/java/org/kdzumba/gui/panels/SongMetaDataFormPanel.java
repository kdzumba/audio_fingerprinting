package org.kdzumba.gui.panels;

import javax.swing.*;
import java.awt.*;

public class SongMetaDataFormPanel extends JPanel {
    public SongMetaDataFormPanel() {

        int hgap = 10;
        int vgap = 10;
        this.setBorder(BorderFactory.createEmptyBorder(hgap, hgap, hgap, hgap));
        this.setLayout(new BorderLayout(hgap, vgap));

        JPanel fieldsPanel = new JPanel();
        JTextField artistNameField = new JTextField();
        JLabel artistNameLabel = new JLabel("Artist Name:");

        JTextField songNameField = new JTextField();
        JLabel songNameLabel = new JLabel("Song Name:");

        JTextField songYearField = new JTextField();
        JLabel songYearLabel = new JLabel("Year:");

        fieldsPanel.setLayout(new GridLayout(3, 2));

        fieldsPanel.add(artistNameLabel);
        fieldsPanel.add(artistNameField);

        fieldsPanel.add(songNameLabel);
        fieldsPanel.add(songNameField);

        fieldsPanel.add(songYearLabel);
        fieldsPanel.add(songYearField);

        fieldsPanel.setBorder(BorderFactory.createTitledBorder("Fingerprint Details"));

        this.add(fieldsPanel);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(450, 130);
    }
}

