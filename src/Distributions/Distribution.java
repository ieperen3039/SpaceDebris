package Distributions;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author mboon
 */
public abstract class Distribution {

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
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        if (rand.nextDouble() < value) base++;

        return base;
    }
}
