package Distributions;

import java.util.concurrent.ThreadLocalRandom;

public class DiscreteUniformDistribution extends Distribution {

    protected int m; // lower bound
    protected int n; // upper bound

    public DiscreteUniformDistribution(int m, int n) {
        this.m = m;
        this.n = n;
    }

    @Override
    public double expectation() {
        return (m + n) / 2.0;
    }

    @Override
    public double variance() {
        return 1.0 * ((n - m + 1) * (n - m + 1) - 1) / 12;
    }

    @Override
    public double nextRandom() {
        double U = ThreadLocalRandom.current().nextDouble();
        return m + Math.floor((n - m + 1) * U);
    }


}