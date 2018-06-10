package Results;

import Simulation.SpaceSimulation;

/**
 * @author Geert van Ieperen created on 24-5-2018.
 */
public class SpaceResults {
    /** particles over time */
    private final long[] smallParticles;
    private final long[] largeParticles;
    private final int[] hughParticles;
    private final int[] activeSatellites;
    private final int[] spaceFlightsQueued;
    private final int[] lostSatellites;

    private int nOfLostSatellites = 0;
    private int index = 0;
    private int saves = 0;

    public SpaceResults(int nOfResults) {
        smallParticles = new long[nOfResults];
        largeParticles = new long[nOfResults];
        spaceFlightsQueued = new int[nOfResults];
        hughParticles = new int[nOfResults];
        activeSatellites = new int[nOfResults];
        lostSatellites = new int[nOfResults];
    }

    public void addResults(long pSmall, long pLarge, int pHugh, int satsInOrbit) {
        smallParticles[index] = pSmall;
        largeParticles[index] = pLarge;
        hughParticles[index] = pHugh;
        activeSatellites[index] = satsInOrbit;
        spaceFlightsQueued[index] = SpaceSimulation.satellitesRequiredInOrbit - satsInOrbit;
        index++;
    }

    public void addLostSatellites(int n) {
        nOfLostSatellites += n;
        lostSatellites[index] += n;
    }

    public void addSaves(int n) {
        saves += n;
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

    public int[] getHughParticles() {
        return hughParticles;
    }

    public int[] getActiveSatellites() {
        return activeSatellites;
    }

    public int getNumberOfLostSatellites() {
        return nOfLostSatellites;
    }

    public int[] getLostSatellites() {
        return lostSatellites;
    }

    public int getSaves() {
        return saves;
    }
}
