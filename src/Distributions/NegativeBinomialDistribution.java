package Distributions;

public class NegativeBinomialDistribution extends Distribution {

    protected int n;      // Number of successes required
    protected double p;   // Success probability

    public NegativeBinomialDistribution(int n, double p) {
        this.n = n;
        this.p = p;
    }

    @Override
    public double expectation() {
        return 1.0 * n / p;
    }

    @Override
    public double variance() {
        return 1.0 * n * (1 - p) / (p * p);
    }

    @Override
    public double nextRandom() {
        // A negative binomial rv is the sum of n geometric rv's with parameter p
        double sum = 0;
        GeometricDistribution gd = new GeometricDistribution(p);
        for (int i = 0; i < n; i++) {
            sum += gd.nextRandom();
        }
        return sum;
    }
}


    
    