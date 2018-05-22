package Distributions;

public class BinomialDistribution extends Distribution {

    protected long nOfExperiments;      // Number of experiments
    protected double success;   // Success probability

    public BinomialDistribution(long nOfExperiments, double success) {
        this.nOfExperiments = nOfExperiments;
        this.success = success;
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
        // A binomial rv is the sum of n Bernoulli rv's with parameter p
        BernoulliDistribution bd = new BernoulliDistribution(success);
        int sum = 0;
        for (int i = 0; i < nOfExperiments; i++) {
            if (bd.nextBoolean()) sum++;
        }
        return sum;
    }
}