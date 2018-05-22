package Distributions;

import java.util.Random;

/**
 * @author mboon
 */
public abstract class Distribution {

    protected final static Random random = new Random();

    public abstract double expectation();

    public abstract double variance();

    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    public abstract double nextRandom();
}
