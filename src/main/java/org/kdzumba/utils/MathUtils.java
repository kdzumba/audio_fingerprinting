package org.kdzumba.utils;

public class MathUtils {
  public static double convertToRange(double num, Range fromRange, Range toRange) {
      return toRange.min + ((num - fromRange.min) * (toRange.max - toRange.min)) / (fromRange.max - fromRange.min);
  }

  public static class Range {
    public double min;
    public double max;

    public Range(double min, double max) {
      this.min = min;
      this.max = max;
    }
  }
}
