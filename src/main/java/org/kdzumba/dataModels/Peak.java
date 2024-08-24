package org.kdzumba.dataModels;

public class Peak {
    public int time;
    public int frequency;
    public double magnitude;
    public double band;
    public double actualFrequency;

    public Peak(int time, int frequency, double band, double magnitude, double actualFrequency) {
        this.time = time;
        this.frequency = frequency;
        this.magnitude = magnitude;
        this.band = band;
        this.actualFrequency = actualFrequency;
    }
}

