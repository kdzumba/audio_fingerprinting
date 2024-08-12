package org.kdzumba.dataModels;

import java.util.Objects;

public class FingerprintHash {
    public int frequency1;
    public int frequency2;
    public int timeDifference;
    public int anchorTime;

    public FingerprintHash(Peak peak1, Peak peak2) {
        this.frequency1 = peak1.frequency;
        this.frequency2 = peak2.frequency;
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
        return frequency1 == that.frequency1 &&
                frequency2 == that.frequency2 &&
                timeDifference == that.timeDifference;
    }

    @Override
    public String toString() {
        return String.format("Hash[f1=%d, f2=%d, dt=%d, t=%d]", frequency1, frequency2, timeDifference, anchorTime);
    }
}
