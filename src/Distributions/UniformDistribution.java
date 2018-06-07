package Distributions;

import java.util.concurrent.ThreadLocalRandom;

public class UniformDistribution extends Distribution {

    protected double a; // lower bound
    protected double b; // upper bound 

    public UniformDistribution(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public double expectation() {
        return (a + b) / 2;
    }

    @Override
    public double variance() {
        return (b - a) * (b - a) / 12;
    }

    @Override
    public double nextRandom() {
        double U = ThreadLocalRandom.current().nextDouble();
        return a + U * (b - a);
    }
}