package Distributions;

import java.util.concurrent.ThreadLocalRandom;

public class BernoulliDistribution extends Distribution {

    private final ThreadLocalRandom rand = ThreadLocalRandom.current();
    protected double success;  // Success probability

    public BernoulliDistribution(double success) {
        this.success = success;
    }

    @Override
    public double expectation() {
        return success;
    }

    @Override
    public double variance() {
        return success * (1 - success);
    }

    @Override
    public double nextRandom() {
        return nextBoolean() ? 1 : 0;
    }

    public boolean nextBoolean() {
        return rand.nextDouble() < success;
    }

    public int nextInt() {
        return nextBoolean() ? 1 : 0;
    }
}