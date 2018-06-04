package Simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 24-5-2018.
 */
public class SpaceResults {
    /** particles over time */
    private final List<Long> smallParticles;
    private final List<Long> largeParticles;
    private final List<Integer> satellitesInOrbit;
    private final List<Integer> activeSatellites;
    private final List<Integer> spaceFlightsQueued;
    private final List<Long> totalParticles;
    private int lostSattellites = 0;

    public SpaceResults(int nOfResults) {
        smallParticles = new ArrayList<>(nOfResults);
        largeParticles = new ArrayList<>(nOfResults);
        spaceFlightsQueued = new ArrayList<>(nOfResults);
        satellitesInOrbit = new ArrayList<>(nOfResults);
        activeSatellites = new ArrayList<>(nOfResults);
        totalParticles = new ArrayList<>(nOfResults);
    }

    public void addResults(long pSmall, long pLarge, int pHugh, int satsInOrbit) {
        smallParticles.add(pSmall);
        largeParticles.add(pLarge);
        satellitesInOrbit.add(pHugh + satsInOrbit);
        activeSatellites.add(satsInOrbit);
        spaceFlightsQueued.add(SpaceSimulation.satellitesRequiredInOrbit - satsInOrbit);
        totalParticles.add(pSmall + pLarge + pHugh);
    }

    public void addLostSatellites(int n) {
        lostSattellites += n;
    }

    public List<Long> getLargeParticles() {
        return Collections.unmodifiableList(largeParticles);
    }

    public List<Long> getSmallParticles() {
        return Collections.unmodifiableList(smallParticles);
    }

    public List<Integer> getSpaceFlightsQueued() {
        return Collections.unmodifiableList(spaceFlightsQueued);
    }

    public List<Long> getTotalParticles() {
        return Collections.unmodifiableList(totalParticles);
    }

    public int lostSatellitesMean() {
        return lostSattellites;
    }
}
