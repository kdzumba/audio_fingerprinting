package org.kdzumba.utils;

public class MathUtils {
  public static float convertToRange(float num, Range fromRange, Range toRange) {
    return (num - fromRange.min) * (toRange.max - toRange.min) / (fromRange.max - fromRange.min) + toRange.min;
  }

  public static class Range {
    public float min;
    public float max;
    
    public Range() {
      this.min = Float.MIN_VALUE;
      this.max = Float.MAX_VALUE;
    }

    public Range(float min, float max) {
      this.min = min;
      this.max = max;
    }
  }
}
