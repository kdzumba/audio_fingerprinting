package org.kdzumba.dataModels;

public class Peak {
    public int time;
    public int frequency;
    public double magnitude;

    public Peak(int time, int frequency, double magnitude) {
        this.time = time;
        this.frequency = frequency;
        this.magnitude = magnitude;
    }
}
