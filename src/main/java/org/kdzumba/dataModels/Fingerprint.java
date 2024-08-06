package org.kdzumba.dataModels;

public class Fingerprint {
    public final double time;
    public final double frequency;
    public final double deltaFrequency;
    public final double deltaTime;

    public Fingerprint(double time, double frequency, double deltaFrequency, double deltaTime) {
        this.time = time;
        this.frequency = frequency;
        this.deltaFrequency = deltaFrequency;
        this.deltaTime = deltaTime;
    }

    @Override
    public String toString() {
        return "Fingerprint{" +
                "time=" + time +
                ", frequency=" + frequency +
                ", deltaFrequency=" + deltaFrequency +
                ", deltaTime=" + deltaTime +
                "}";
    }
}
