package Simulation;

import Distributions.BinomialDistribution;
import Distributions.GeometricDistribution;
import Distributions.NormalDistribution;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * all values are either integer or double precision, time is measured in days, distance is measured in meters.
 * @author Geert van Ieperen created on 22-5-2018.
 */
public class SpaceSimulation {
    // number of simulations run
    private static final int NOF_RUNS = 100;
    // number of days for one simulation
    private static final int MAX_TIME = 1000;
    /** chance that one particle hits one specific satellite in one year */
    private static final double probDebrisCollision = 0;
    /** average period for debris to fall into the atmosphere or outer space in days */
    private static final double fallProbDebrisSmall = 0;
    /** average period for debris to fall into the atmosphere or outer space in days */
    private static final double fallProbDebrisLarge = 0;
    /** the number of particles a satellite creates when colliding */
    private static final int shreddingFactor = 1000;
    /** max satellite launches per day */
    private static final double launchesPerDay = 10.0 / 356;
    /** number of satellites that we want in the sky */
    private static final int satellitesRequiredInOrbit = 0;

    /** particles smaller than a whole satellite */
    private long particlesSmall = 0;
    /** non-functional satellites still in orbit */
    private long particlesLarge = 0;
    /** operational satellites in orbit */
    private int satellitesInOrbit = 0;

    /**
     * runs the simulation
     */
    private List<Long> simulation() {
        double daysUntilNextLaunch = 0;
        List<Long> particlesTotal = new ArrayList<>();

        for (int i = 0; i < MAX_TIME; i++) {
            // large debris collision with satellite
            int largeWithSatColl = sampleCollisions(particlesLarge * satellitesInOrbit);
            satellitesInOrbit -= largeWithSatColl;
            particlesLarge -= largeWithSatColl;
            particlesSmall += largeWithSatColl * shreddingFactor * 2;

            // small debris collision with satellite
            int smallWithSatColl = sampleCollisions(particlesSmall * satellitesInOrbit);
            satellitesInOrbit -= smallWithSatColl;
            particlesSmall += smallWithSatColl * shreddingFactor;

            // small debris collision with large debris
            int smallWithLargeColl = sampleCollisions(particlesSmall * particlesLarge);
            particlesLarge -= smallWithLargeColl;
            particlesSmall += smallWithLargeColl * shreddingFactor;

            // particles falling back into the atmosphere
            int fallenLarge = new GeometricDistribution(fallProbDebrisLarge).nextInt();
            particlesLarge -= fallenLarge;
            int fallenSmall = new GeometricDistribution(fallProbDebrisSmall).nextInt();
            particlesSmall -= fallenSmall;

            // launching new satellites
            daysUntilNextLaunch = Math.max(0, daysUntilNextLaunch - 1);
            // could have been an if-statement, but this is more stable
            while (daysUntilNextLaunch < 1 && satellitesInOrbit < satellitesRequiredInOrbit) {
                satellitesInOrbit++;
                daysUntilNextLaunch += 1 / launchesPerDay; // may be a distribution?
            }

            particlesTotal.add(particlesSmall + particlesLarge);
        }

        return particlesTotal;
    }

    /** returns a sample of collisions */
    private static int sampleCollisions(long n) {
        double p = SpaceSimulation.probDebrisCollision;

        if (n > (9 * (1 - p) / p)) { // if the binomial approaches an normal dist
            NormalDistribution dist = new NormalDistribution(n * p, n * p * (1 - p));
            return (int) (dist.nextRandom() + 0.5); // +0.5 for rounding

        } else {
            return new BinomialDistribution(n, p).nextInt();
        }
    }

    // run NOF_RUNS simulations and save everything in a csv file
    public static void main(String[] args) throws Exception {
        PrintWriter output = new PrintWriter("data.csv");

        // run sims
        for (int i = 0; i < NOF_RUNS; i++) {
            System.out.print("\rProgress: " + (i + 1) + "/" + NOF_RUNS);
            List<Long> result = new SpaceSimulation().simulation();
            output.println(result.toString());
        }

        output.close();
    }
}
