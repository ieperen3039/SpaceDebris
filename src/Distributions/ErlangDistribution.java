package Distributions;

public class ErlangDistribution extends Distribution {

    protected int n;
    protected double lambda;

    public ErlangDistribution(int n, double lambda) {
        this.n = n;
        this.lambda = lambda;
    }

    @Override
    public double expectation() {
        return 1.0 * n / lambda;
    }

    @Override
    public double variance() {
        return 1.0 * n / (lambda * lambda);
    }

    @Override
    public double nextRandom() {
        // A Erlang rv is the sum of n exponential rv's with parameter lambda
        ExponentialDistribution ed = new ExponentialDistribution(lambda);
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += ed.nextRandom();
        }
        return sum;
    }


}