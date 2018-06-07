package Results;

import Distributions.Interval;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class MultiSpaceResultImpl implements MultiSpaceResults, MultiResultCollector {
    private final int nOfRuns;
    private long[] meanParticles;
    private double meanLostSatellites;
    private double meanLostSatellites2;
    private boolean isWrappedUp = false;

    private MultiSpaceResultImpl(int runs, int runLength) {
        if (runs == 0 || runLength == 0)
            throw new IllegalArgumentException("runs = " + runs + ", length = " + runLength);
        this.nOfRuns = runs;

        meanParticles = new long[runLength];
        meanLostSatellites = 0;
        meanLostSatellites2 = 0;
    }

    public static MultiResultCollector getCollector(int runs, int runLength) {
        return new MultiSpaceResultImpl(runs, runLength);
    }

    @Override
    public synchronized void add(Simulation.SpaceResults results) {
        int lostSatellitesMean = results.lostSatellitesMean();
        meanLostSatellites += lostSatellitesMean;
        meanLostSatellites2 += lostSatellitesMean * lostSatellitesMean;

        long[] particles = results.getTotalParticles();
        addAll(particles, meanParticles);
    }

    private static void addAll(long[] source, long[] total) {
        int runLength = total.length;
        for (int k = 0; k < runLength; k++) {
            total[k] += source[k];
        }
    }

    @Override
    public MultiSpaceResults wrapUp() {
        if (isWrappedUp) throw new IllegalStateException("wrapUp is called twice");

        for (int i = 0; i < meanParticles.length; i++) {
            meanParticles[i] /= nOfRuns;
        }

        meanLostSatellites /= nOfRuns;
        meanLostSatellites2 /= nOfRuns;

        isWrappedUp = true;
        return this;
    }

    @Override
    public long[] totals() {
        return meanParticles;
    }

    @Override
    public Interval lostSatellitesConf() {
        // TODO check this
        double variance = meanLostSatellites2 - meanLostSatellites * meanLostSatellites;
        return new Interval(variance, meanLostSatellites, nOfRuns);
    }

    @Override
    public double lostSatellitesMean() {
        return meanLostSatellites;
    }
}
