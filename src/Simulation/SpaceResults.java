package Simulation;

/**
 * @author Geert van Ieperen created on 24-5-2018.
 */
public class SpaceResults {
    /** particles over time */
    private final long[] smallParticles;
    private final long[] largeParticles;
    private final int[] satellitesInOrbit;
    private final int[] activeSatellites;
    private final int[] spaceFlightsQueued;
    private final long[] totalParticles;

    private int lostSattellites = 0;
    private int index = 0;

    public SpaceResults(int nOfResults) {
        smallParticles = new long[nOfResults];
        largeParticles = new long[nOfResults];
        spaceFlightsQueued = new int[nOfResults];
        satellitesInOrbit = new int[nOfResults];
        activeSatellites = new int[nOfResults];
        totalParticles = new long[nOfResults];
    }

    public void addResults(long pSmall, long pLarge, int pHugh, int satsInOrbit) {
        smallParticles[index] = pSmall;
        largeParticles[index] = pLarge;
        satellitesInOrbit[index] = pHugh + satsInOrbit;
        activeSatellites[index] = satsInOrbit;
        spaceFlightsQueued[index] = SpaceSimulation.satellitesRequiredInOrbit - satsInOrbit;
        totalParticles[index] = pSmall + pLarge + pHugh;
        index++;
    }

    public void addLostSatellites(int n) {
        lostSattellites += n;
    }

    public long[] getLargeParticles() {
        return largeParticles;
    }

    public long[] getSmallParticles() {
        return smallParticles;
    }

    public int[] getSpaceFlightsQueued() {
        return spaceFlightsQueued;
    }

    public int[] getSatellitesInOrbit() {
        return satellitesInOrbit;
    }

    public int[] getActiveSatellites() {
        return activeSatellites;
    }

    public long[] getTotalParticles() {
        return totalParticles;
    }

    public int lostSatellitesMean() {
        return lostSattellites;
    }
}
