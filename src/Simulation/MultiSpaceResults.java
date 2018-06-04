package Simulation;

import java.util.Arrays;
import java.util.List;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class MultiSpaceResults {
    private final int nOfRuns;
    private long[] meanParticles;
    private boolean isWrappedUp = false;

    public MultiSpaceResults(int runs, int runLength) {
        nOfRuns = runs;
        meanParticles = new long[runLength];
    }

    public void add(SpaceResults results) {
        List<Long> particles = results.getTotalParticles();

        for (int k = 0; k < particles.size(); k++) {
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
}
