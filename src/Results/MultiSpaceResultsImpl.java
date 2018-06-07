package Results;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class MultiSpaceResultsImpl implements MultiSpaceResults, MultiResultCollector {
    private final int nOfRuns;
    private MeanIntCollector lostSatellites;
    private MeanIntCollector esaSaves;
    private boolean isWrappedUp = false;

    private double[] activeSatellites;
    private double[] spaceFlightsQueued;
    private double[] smallParticles;
    private double[] largeParticles;
    private double[] hughParticles;

    private MultiSpaceResultsImpl(int runs, int runLength) {
        if (runs == 0 || runLength == 0)
            throw new IllegalArgumentException("runs = " + runs + ", length = " + runLength);
        this.nOfRuns = runs;

        activeSatellites = new double[runLength];
        spaceFlightsQueued = new double[runLength];
        hughParticles = new double[runLength];
        smallParticles = new double[runLength];
        largeParticles = new double[runLength];
        lostSatellites = new MeanIntCollector();
        esaSaves = new MeanIntCollector();
    }

    public static MultiResultCollector getCollector(int runs, int runLength) {
        return new MultiSpaceResultsImpl(runs, runLength);
    }

    @Override
    public synchronized void add(SpaceResults results) {
        lostSatellites.add(results.getLostSatellites());
        esaSaves.add(results.getSaves());
        addAll(results.getActiveSatellites(), activeSatellites);
        addAll(results.getSpaceFlightsQueued(), spaceFlightsQueued);
        addAll(results.getHughParticles(), hughParticles);
        addAll(results.getSmallParticles(), smallParticles);
        addAll(results.getLargeParticles(), largeParticles);
    }

    @Override
    public MultiSpaceResults wrapUp() {
        if (isWrappedUp) throw new IllegalStateException("wrapUp is called twice"); // not threadsafe

        for (int i = 0; i < activeSatellites.length; i++) {
            activeSatellites[i] /= nOfRuns;
            spaceFlightsQueued[i] /= nOfRuns;
            hughParticles[i] /= nOfRuns;
            largeParticles[i] /= nOfRuns;
            smallParticles[i] /= nOfRuns;
        }

        isWrappedUp = true;
        return this;
    }

    @Override
    public double lostSatellitesMean() {
        return lostSatellites.getMean();
    }

    @Override
    public Interval lostSatellitesConf() {
        return lostSatellites.getConfidence();
    }

    @Override
    public double savesMean() {
        return esaSaves.getMean();
    }

    @Override
    public Interval savesConf() {
        return esaSaves.getConfidence();
    }

    @Override
    public double[] getActiveSatellites() {
        return activeSatellites;
    }

    @Override
    public double[] getSpaceFlightsQueued() {
        return spaceFlightsQueued;
    }

    @Override
    public double[] getHughParticles() {
        return hughParticles;
    }

    @Override
    public double[] getLargeParticles() {
        return largeParticles;
    }

    @Override
    public double[] getSmallParticles() {
        return smallParticles;
    }


    private static void addAll(long[] source, double[] total) {
        int runLength = total.length;
        for (int k = 0; k < runLength; k++) {
            total[k] += source[k];
        }
    }

    private static void addAll(int[] source, double[] total) {
        int runLength = total.length;
        for (int k = 0; k < runLength; k++) {
            total[k] += source[k];
        }
    }

}
