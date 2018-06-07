package Results;

/**
 * @author Geert van Ieperen created on 7-6-2018.
 */
public class MeanDoubleCollector {
    private double total = 0;
    private double secondMoment = 0;
    private int nOf = 0;

    public void add(double newValue) {
        total += newValue;
        secondMoment += newValue * newValue;
        nOf++;
    }

    public double getMean() {
        return total / nOf;
    }

    public Interval getConfidence() {
        double mean = getMean();
        double variance = secondMoment - mean * mean;
        return new Interval(mean, variance, nOf);
    }
}
