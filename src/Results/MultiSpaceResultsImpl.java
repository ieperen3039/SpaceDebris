package Results;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class MultiSpaceResultsImpl implements MultiSpaceResults, MultiResultCollector {
    private final int nOfRuns;
    private final int runLength;
    private boolean isWrappedUp = false;

    private MeanDoubleCollector lostSatellitesTotal;
    private MeanDoubleCollector esaSaves;
    private MeanDoubleCollector smallTotal;
    private MeanDoubleCollector largeTotal;
    private MeanDoubleCollector hughTotal;

    private MeanDoubleCollector removalLaunches;

    private double[] activeSatellites;
    private double[] spaceFlightsQueued;
    private double[] smallParticles;
    private double[] largeParticles;
    private double[] hughParticles;
    private double[] lostSatellites;

    private MultiSpaceResultsImpl(int runs, int runLength) {
        if (runs == 0 || runLength == 0)
            throw new IllegalArgumentException("runs = " + runs + ", length = " + runLength);
        this.nOfRuns = runs;
        this.runLength = runLength;

        lostSatellitesTotal = new MeanDoubleCollector();
        esaSaves = new MeanDoubleCollector();
        smallTotal = new MeanDoubleCollector();
        largeTotal = new MeanDoubleCollector();
        hughTotal = new MeanDoubleCollector();
        removalLaunches = new MeanDoubleCollector();

        activeSatellites = new double[runLength];
        spaceFlightsQueued = new double[runLength];
        hughParticles = new double[runLength];
        smallParticles = new double[runLength];
        largeParticles = new double[runLength];
        lostSatellites = new double[runLength];
    }

    public static MultiResultCollector getCollector(int runs, int runLength) {
        return new MultiSpaceResultsImpl(runs, runLength);
    }

    @Override
    public synchronized void add(SpaceResults results) {
        lostSatellitesTotal.add(results.getNumberOfLostSatellites());
        esaSaves.add(results.getSaves());
        smallTotal.add(results.getSmallParticles()[runLength - 1]);
        largeTotal.add(results.getLargeParticles()[runLength - 1]);
        hughTotal.add(results.getHughParticles()[runLength - 1]);
        removalLaunches.add(results.getRemovalLaunch());

        addAll(results.getLostSatellites(), lostSatellites);
        addAll(results.getActiveSatellites(), activeSatellites);
        addAll(results.getSpaceFlightsQueued(), spaceFlightsQueued);
        addAll(results.getSmallParticles(), smallParticles);
        addAll(results.getLargeParticles(), largeParticles);
        addAll(results.getHughParticles(), hughParticles);
    }

    @Override
    public MultiSpaceResults wrapUp() {
        if (isWrappedUp) throw new IllegalStateException("wrapUp is called twice"); // not threadsafe

        for (int i = 0; i < runLength; i++) {
            activeSatellites[i] /= nOfRuns;
            spaceFlightsQueued[i] /= nOfRuns;
            hughParticles[i] /= nOfRuns;
            largeParticles[i] /= nOfRuns;
            smallParticles[i] /= nOfRuns;
            lostSatellites[i] /= nOfRuns;
        }

        isWrappedUp = true;
        return this;
    }

    @Override
    public double lostSatellitesMean() {
        return lostSatellitesTotal.getMean();
    }

    @Override
    public Interval lostSatellitesConf() {
        return lostSatellitesTotal.getConfidence();
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
    public double smallParticleMean() {
        return smallTotal.getMean();
    }

    @Override
    public Interval smallParticleConf() {
        return smallTotal.getConfidence();
    }

    @Override
    public double largeParticleMean() {
        return largeTotal.getMean();
    }

    @Override
    public Interval largeParticleConf() {
        return largeTotal.getConfidence();
    }

    @Override
    public double hughParticleMean() {
        return hughTotal.getMean();
    }

    @Override
    public Interval hughParticleConf() {
        return hughTotal.getConfidence();
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

    @Override
    public double[] getLostSatellites() {
        return lostSatellites;
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


    @Override
    public MeanDoubleCollector getRemovalLaunches() {
        return removalLaunches;
    }

}
