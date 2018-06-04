package Distributions;

public class PoissonDistribution extends Distribution {

    protected double lambda;

    public PoissonDistribution(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public double expectation() {
        return lambda;
    }

    @Override
    public double variance() {
        return lambda;
    }

    @Override
    public double nextRandom() {
        return get(lambda);
    }

    public static int get(double lambda) {
        // Discrete inverse transform method
        double U = random.nextDouble();
        int index = 0;
        double current = Math.exp(-lambda);
        double sum = current;
        while (sum < U) {
            index++;
            current = current * lambda / (1.0 * index);
            sum += current;
        }
        return index;
    }
}