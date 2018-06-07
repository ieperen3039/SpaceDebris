package Results;

import java.util.Locale;

/**
 * @author Geert van Ieperen created on 12-3-2018.
 */
public class Interval {
    public final double lower;
    public final double upper;

    public Interval(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public Interval(double mean, double variance, int sampleSize) {
        double zAlpha = 1.96;
        double halfwidth = zAlpha * Math.sqrt(variance / sampleSize);
        lower = mean - halfwidth;
        upper = mean + halfwidth;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%1.06f, %1.06f]", lower, upper);
    }
}
