package org.kdzumba.dataModels;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class FingerprintHash implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public double frequency1;
    public double frequency2;
    public int timeDifference;
    public int anchorTime;

    public FingerprintHash(Peak peak1, Peak peak2) {
        this.frequency1 = peak1.actualFrequency;
        this.frequency2 = peak2.actualFrequency;
        this.timeDifference = peak2.time - peak1.time;
        this.anchorTime = peak1.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequency1, frequency2, timeDifference);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FingerprintHash that = (FingerprintHash) obj;

        boolean isEqual = Double.compare(that.frequency1, frequency1) == 0 &&
                Double.compare(that.frequency2, frequency2) == 0 &&
                timeDifference == that.timeDifference;
        System.out.println("This: " + this + " that: " + that + " IsEqual: " + isEqual);
        return isEqual;
    }

    @Override
    public String toString() {
        return String.format("Hash[f1=%.2f, f2=%.2f, dt=%d, t=%d]", frequency1, frequency2, timeDifference, anchorTime);
    }
}
