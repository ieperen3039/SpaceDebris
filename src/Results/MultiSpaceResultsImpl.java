package Results;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class MultiSpaceResultsImpl implements MultiSpaceResults, MultiResultCollector {
    private final int nOfRuns;
    private MeanIntCollector lostSatellites;
    private MeanIntCollector esaSaves;
    private boolean isWrappedUp = false;

    private double[] meanParticles;
    private double[] activeSatellites;
    private double[] spaceFlightsQueued;
    private double[] totalSatellites;


    private MultiSpaceResultsImpl(int runs, int runLength) {
        if (runs == 0 || runLength == 0)
            throw new IllegalArgumentException("runs = " + runs + ", length = " + runLength);
        this.nOfRuns = runs;

        meanParticles = new double[runLength];
        activeSatellites = new double[runLength];
        spaceFlightsQueued = new double[runLength];
        totalSatellites = new double[runLength];
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
        addAll(results.getTotalParticles(), meanParticles);
        addAll(results.getActiveSatellites(), activeSatellites);
        addAll(results.getSpaceFlightsQueued(), spaceFlightsQueued);
        addAll(results.getSatellitesInOrbit(), totalSatellites);
    }

    @Override
    public MultiSpaceResults wrapUp() {
        if (isWrappedUp) throw new IllegalStateException("wrapUp is called twice"); // not threadsafe

        for (int i = 0; i < meanParticles.length; i++) {
            meanParticles[i] /= nOfRuns;
            activeSatellites[i] /= nOfRuns;
            spaceFlightsQueued[i] /= nOfRuns;
            totalSatellites[i] /= nOfRuns;
        }

        isWrappedUp = true;
        return this;
    }

    @Override
    public double[] totals() {
        return meanParticles;
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
    public double[] getTotalSatellites() {
        return totalSatellites;
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
