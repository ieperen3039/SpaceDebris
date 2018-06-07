package Results;

/**
 * @author Geert van Ieperen created on 7-6-2018.
 */
public class MeanIntCollector {
    private int total = 0;
    private int secondMoment = 0;
    private int nOf = 0;

    public void add(int newValue) {
        total += newValue;
        secondMoment += newValue * newValue;
        nOf++;
    }

    public double getMean() {
        return (double) total / nOf;
    }

    public Interval getConfidence() {
        double mean = getMean();
        double msm = (double) secondMoment / nOf;
        double variance = msm - (mean * mean);
        return new Interval(mean, variance, nOf);
    }
}
