package Distributions;

import java.util.concurrent.ThreadLocalRandom;

public class GeometricDistribution extends Distribution {

    protected double p;   // Success probability

    public GeometricDistribution(double p) {
        this.p = p;
    }

    @Override
    public double expectation() {
        return 1 / p;
    }

    @Override
    public double variance() {
        return (1 - p) / (p * p);
    }

    @Override
    public double nextRandom() {
        double U = ThreadLocalRandom.current().nextDouble();
        return 1 + Math.floor(Math.log(U) / Math.log(1 - p));
    }

    public int nextInt() {
        return (int) nextRandom();
    }
}