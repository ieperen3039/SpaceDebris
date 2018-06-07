package Simulation;

import Distributions.Interval;

import java.util.List;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class MultiSpaceResults {
    private final int nOfRuns;
    private final int runLength;
    private long[] meanParticles;
    private double meanLostSatellites;
    private double meanLostSatellites2;
    private boolean isWrappedUp = false;

    public MultiSpaceResults(int runs, int runLength) {
        if (runs == 0 || runLength == 0)
            throw new IllegalArgumentException("runs = " + runs + ", length = " + runLength);
        this.nOfRuns = runs;
        this.runLength = runLength;

        meanParticles = new long[runLength];
        meanLostSatellites = 0;
        meanLostSatellites2 = 0;
    }

    public synchronized void add(SpaceResults results) {
        int lostSatellitesMean = results.lostSatellitesMean();
        meanLostSatellites += lostSatellitesMean;
        meanLostSatellites2 += lostSatellitesMean * lostSatellitesMean;

        List<Long> particles = results.getTotalParticles();
        for (int k = 0; k < runLength; k++) {
            meanParticles[k] += particles.get(k);
        }
    }

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

    public long[] totals() {
        return meanParticles;
    }

    public Interval lostSatellitesConf() {
        // TODO check this
        double variance = meanLostSatellites2 - meanLostSatellites * meanLostSatellites;
        return new Interval(variance, meanLostSatellites, nOfRuns);
    }

    public double lostSatellitesMean() {
        return meanLostSatellites;
    }
}
