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

    /** transforms a double to an int, by drawing a random variable for the remainder */
    public static int randomToInt(double value) {
        int base = (int) value;

        value -= base;
        if (random.nextDouble() < value) base++;

        return base;
    }
}
