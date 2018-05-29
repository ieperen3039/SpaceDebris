package Distributions;

public class BinomialDistribution extends Distribution {

    protected long nOfExperiments;      // Number of experiments
    protected double success;   // Success probability
    private BernoulliDistribution bernoulli;

    // A binomial rv is the sum of n Bernoulli rv's with parameter p
    public BinomialDistribution(long nOfExperiments, double success) {
        this.nOfExperiments = nOfExperiments;
        this.success = success;
        this.bernoulli = new BernoulliDistribution(success);
    }

    @Override
    public double expectation() {
        return nOfExperiments * success;
    }

    @Override
    public double variance() {
        return nOfExperiments * success * (1 - success);
    }

    @Override
    public double nextRandom() {
        return nextInt();
    }

    public int nextInt() {
        int sum = 0;
        for (int i = 0; i < nOfExperiments; i++) {
            sum += bernoulli.nextInt();
        }
        return sum;
    }

    public static int next(long nOfExperiments, double success) {
        int sum = 0;
        for (int i = 0; i < nOfExperiments; i++) {
            if (random.nextDouble() < success) sum++;
        }
        return sum;
    }
}