package org.kdzumba.dataModels;

import java.util.Objects;

public class FingerprintHash {
    public double anchorFreq;
    public double pointFreq;
    public double timeDifference;
    public double anchorTimeOffset;

    public FingerprintHash(Peak anchor, Peak point) {
        this.anchorFreq = anchor.actualFrequency;
        this.pointFreq = point.actualFrequency;
        this.timeDifference = point.timeIndex - anchor.timeIndex;
        this.anchorTimeOffset = anchor.timeOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(anchorFreq, pointFreq, timeDifference);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FingerprintHash that = (FingerprintHash) obj;

        return Double.compare(that.anchorFreq, anchorFreq) == 0 &&
                Double.compare(that.pointFreq, pointFreq) == 0 &&
                timeDifference == that.timeDifference;
    }

    @Override
    public String toString() {
        return String.format("Hash[f1=%.2f, f2=%.2f, t=%.2f]", anchorFreq, pointFreq, timeDifference);
    }
}
