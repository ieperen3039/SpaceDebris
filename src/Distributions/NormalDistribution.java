package Distributions;

public class NormalDistribution extends Distribution {

    protected double mu;
    protected double sigma;

    public NormalDistribution(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public double expectation() {
        return mu;
    }

    @Override
    public double variance() {
        return sigma * sigma;
    }

    @Override
    public double nextRandom() {
        double U = random.nextGaussian();
        return mu + sigma * U;
    }
}