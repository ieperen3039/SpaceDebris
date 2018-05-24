package Simulation;

import Distributions.BinomialDistribution;
import Distributions.GeometricDistribution;
import Distributions.NormalDistribution;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * all values are either integer or double precision, time is measured in days, distance is measured in meters.
 * @author Geert van Ieperen created on 22-5-2018.
 */
public class SpaceSimulation extends Thread {
    /** number of simulations run */
    public static final int NOF_RUNS = 100;
    /** number of days for one simulation */
    public static final int MAX_TIME = 100;
    /** chance that one particle hits one specific satellite in one year */
    public static final double probDebrisCollision = 0;
    /** average period for debris to fall into the atmosphere or outer space in days */
    public static final double fallProbDebrisSmall = 0;
    /** average period for debris to fall into the atmosphere or outer space in days */
    public static final double fallProbDebrisLarge = 0;
    /** the number of particles a satellite creates when colliding */
    public static final int shreddingFactor = 0;
    /** max satellite launches per day */
    public static final double launchesPerDay = 1 / 356.0;
    /** number of satellites that we want in the sky */
    public static final int satellitesRequiredInOrbit = 0;

    // note that every simulation is a new SpaceSimulation object
    /** particles smaller than a whole satellite */
    private long particlesSmall = 0;
    /** non-functional satellites still in orbit */
    private int particlesLarge = 0;
    /** operational satellites in orbit */
    private int satellitesInOrbit = 0;
    /** number of days before the next satellite is launched */
    private double daysUntilNextLaunch = 0;

    /** results are stored here */
    private final SpaceResults results = new SpaceResults();
    /** ensures that run() has completed before results can be queried */
    private CountDownLatch completion = new CountDownLatch(2);

    /** returns the results, but only after the {@link #run()} method has finished */
    public SpaceResults results() throws InterruptedException {
        completion.await();
        return results;
    }

    /**
     * runs a single simulation run
     */
    public void run() {
        completion.countDown();
        if (completion.getCount() != 1) throw new IllegalStateException("Simulation can only be run once!");

        for (int i = 0; i < MAX_TIME; i++) {
            simulateCollisions();

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
                daysUntilNextLaunch += (1.0 / launchesPerDay); // may be a distribution?
            }

            results.addResults(particlesSmall, particlesLarge, satellitesInOrbit);
        }

        completion.countDown();
    }

    /** calculates the effect of collisions, and changes the state of the run with */
    private void simulateCollisions() {
        // large debris collision with satellite
        int largeWithSatColl = sampleCollisions((long) particlesLarge * satellitesInOrbit);
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

        // large debris collision with large debris
        int largeWithLarge = sampleCollisions((long) particlesLarge * particlesLarge);
        particlesLarge -= largeWithLarge * 2;
        particlesSmall += largeWithLarge * shreddingFactor * 2;
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

            SpaceSimulation suite = new SpaceSimulation();
            suite.run();
            List<Long> particles = suite.results().getTotalParticles();

            output.println(particles.toString());
        }

        output.close();
    }
}
