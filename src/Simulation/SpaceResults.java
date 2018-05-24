package Simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Geert van Ieperen created on 24-5-2018.
 */
public class SpaceResults {
    /** particles over time */
    private final List<Long> smallParticles;
    private final List<Integer> largeParticles;
    private final List<Integer> spaceFlightsQueued;
    private int nOfResults;

    public SpaceResults() {
        smallParticles = new ArrayList<>();
        largeParticles = new ArrayList<>();
        spaceFlightsQueued = new ArrayList<>();
    }

    public void addResults(long pSmall, int pLarge, int satsInOrbit) {
        smallParticles.add(pSmall);
        largeParticles.add(pLarge);
        spaceFlightsQueued.add(SpaceSimulation.satellitesRequiredInOrbit - satsInOrbit);
        nOfResults++;
    }

    public List<Integer> getLargeParticles() {
        return Collections.unmodifiableList(largeParticles);
    }

    public List<Long> getSmallParticles() {
        return Collections.unmodifiableList(smallParticles);
    }

    public List<Integer> getSpaceFlightsQueued() {
        return Collections.unmodifiableList(spaceFlightsQueued);
    }

    public List<Long> getTotalParticles() {
        Iterator<Integer> larges = largeParticles.iterator();

        List<Long> list = new ArrayList<>(nOfResults);
        for (Long small : smallParticles) {
            Long total = small + larges.next();
            list.add(total);
        }

        return list;
    }
}
