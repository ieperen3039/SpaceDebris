package Results;

/**
 * @author Geert van Ieperen created on 7-6-2018.
 */
public class MeanIntCollector {
    private int total = 0;
    private int secondMoment = 0;
    private double nOf = 0;

    public void add(int newValue) {
        total += newValue;
        secondMoment += newValue * newValue;
        nOf++;
    }

    public double getMean() {
        return total / nOf;
    }

    public Interval getConfidence() {
        double mean = getMean();
        double msm = secondMoment / nOf;
        double variance = msm - (mean * mean);
        return new Interval(mean, variance, (int) nOf);
    }
}
