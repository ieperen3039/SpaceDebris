package Simulation;

import Distributions.Interval;

import java.util.Arrays;
import java.util.List;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class MultiSpaceResults {
    private final int nOfRuns;
    private final int runLength;
    private long[] meanParticles;
    private int meanLostSatellites;
    private int meanLostSatellites2;
    private boolean isWrappedUp = false;

    public MultiSpaceResults(int runs, int runLength) {
        this.nOfRuns = runs;
        this.runLength = runLength;

        meanParticles = new long[runLength];
        meanLostSatellites = 0;
        meanLostSatellites2 = 0;
    }

    public void add(SpaceResults results) {
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

        isWrappedUp = true;
        return this;
    }

    public String totals() {
        return Arrays.toString(meanParticles);
    }

    public Interval lostSatellites() {
        // TODO check this
        double variance = meanLostSatellites2 - meanLostSatellites * meanLostSatellites;
        return new Interval(variance, meanLostSatellites, nOfRuns);
    }
}
