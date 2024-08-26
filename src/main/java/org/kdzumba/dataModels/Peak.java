package org.kdzumba.dataModels;

public class Peak {
    public int timeIndex;
    public int frequencyBin;
    public double magnitude;
    public double band;
    public double actualFrequency;
    public double timeOffset;

    public Peak(int time, int frequency, double band, double magnitude, double actualFrequency, double timeOffset) {
        this.timeIndex = time;
        this.frequencyBin = frequency;
        this.magnitude = magnitude;
        this.band = band;
        this.actualFrequency = actualFrequency;
        this.timeOffset = timeOffset;
    }
}

