package Simulation;

import Distributions.BinomialDistribution;
import Distributions.NormalDistribution;
import Distributions.PoissonDistribution;

import static Distributions.Distribution.randomToInt;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * all values are either integer/long or double precision, time is measured in days, distance is measured in meters.
 * Values are derived from - (not) http://www.duncansteel.com/archives/1425
 *                         - (not) http://www.raa-journal.org/raa/index.php/raa/article/viewFile/1587/1442
 *                         - https://www.esa.int/Our_Activities/Operations/Space_Debris/Reentry_and_collision_avoidance
 *                         - https://www.esa.int/Our_Activities/Operations/Space_Debris/Space_debris_by_the_numbers
 *                         - http://spaceflight101.com/2017-space-launch-statistics/
 *                         - http://www.stat.yale.edu/~pollard/Courses/241.fall97/Poisson.pdf
 * @author Geert van Ieperen created on 22-5-2018.
 */
@SuppressWarnings("WeakerAccess")
public class SpaceSimulation extends Thread {
    /** If this is false, poisson distributions may be used for collisions */
    private static final boolean FORCE_NORMAL_DIST = false;
    /** if the error resulting from assuming poisson at binomials falls below this margin, poisson is used instead */
    private static final double POISSON_ERROR_MARGIN = 0.01;

    /** average period for debris to fall into the atmosphere or outer space in days */
    public static final int fallPeriodDebrisSmall = 2000;
    private static final double fallProbSmall = probSplit(0.5, fallPeriodDebrisSmall);
    /** average period for debris to fall into the atmosphere or outer space in days */
    public static final int fallPeriodDebrisLarge = 2000;
    private static final double fallProbLarge = probSplit(0.5, fallPeriodDebrisLarge);
    /** the number of particles a satellite creates when colliding */
    public static final double shreddingFactor = 5000;
    /** the fraction of shredded particles that is smaller than 10 cm */
    public static final double shreddingSmallFraction = 0.75;
    /** max satellite launches per day */
    public static final double launchesPerDay = 90 / 365.25;
    /** number of satellites that we want in the sky */
    public static final int satellitesRequiredInOrbit = 1200;
    /** number of satellites that an observatory can resolve within one day */
    public static final int observatoryCapacity = 2;

    /** chance that one small particle hits one large object in one day */
    // 12 avoidances per year: with 780_000 particles and 1200 satellites, we have 780000 * 1200 * p = 12
    public static final double probDangerSmall = probSplit(100.0 / (satellitesRequiredInOrbit * 780_000), 365);
    /** chance that one large particle hits one other large object (or sat) in one day */
    public static final double probDangerLarge = probDangerSmall * 4; // *4 because of surface
    /** chance of collision when alarm is raised */
    private static final double collisionByDangerRisk = 1.0 / 100;

    /** particles [1 ... 10] cm */
    private long particlesSmall = 750_000;
    /** particles > 10 cm */
    private long particlesLarge = 29_000;
    /** non-functional satellites still in orbit */
    private int particlesHugh = 3600;
    /** operational satellites in orbit */
    private int satellitesInOrbit = satellitesRequiredInOrbit;
    /** number of days before the next satellite is launched */
    private double daysUntilNextLaunch = 0;

    /** results are stored here */
    private final SpaceResults results;
    private final int maxTime;

    public SpaceSimulation(int runTime) {
        results = new SpaceResults(runTime);
        maxTime = runTime - 1;
        results.addResults(particlesSmall, particlesLarge, particlesHugh, satellitesInOrbit);
    }

    /**
     * runs a single simulation run
     */
    public void run() {
        for (int i = 0; i < maxTime; i++) {
            progressOneDay();

            if (satellitesInOrbit < 0 || particlesSmall < 0 || particlesLarge < 0 || particlesHugh < 0) {
                throw new IllegalStateException(String.format(
                        "negative amount detected after %d days:\n" +
                                "sat in orbit: %d\n" +
                                "hugh particles: %d\n" +
                                "large particles: %d\n" +
                                "small particles: %d",
                        i,
                        satellitesInOrbit,
                        particlesHugh,
                        particlesLarge,
                        particlesSmall
                ));
            }

            results.addResults(particlesSmall, particlesLarge, particlesHugh, satellitesInOrbit);
        }
    }

    /** returns the results, but only after the {@link #run()} method has finished */
    public SpaceResults results() throws InterruptedException {
        join();
        return results;
    }

    /** updates and calculates the changes for one day */
    private void progressOneDay() {
        // different types of collisions
        int collSatWithHugh = sampleOptimized((long) particlesHugh * satellitesInOrbit, probDangerLarge);
        int collSatWithLarge = sampleOptimized(particlesLarge * satellitesInOrbit, probDangerSmall);
        int collSatWithSmall = sampleOptimized(particlesSmall * satellitesInOrbit, probDangerSmall);
        int collHughWithLarge = sampleOptimized(particlesLarge * particlesHugh, probDangerSmall * collisionByDangerRisk);
        int collHughWithHugh = sampleOptimized((long) particlesHugh * particlesHugh, probDangerLarge * collisionByDangerRisk);

        // it may theoretically happen for three sats to collide, but then the collision chance should be adjusted
        collHughWithHugh = min(particlesHugh, collHughWithHugh * 2);

        Observatory esa = new Observatory();
        collSatWithHugh = sampleOptimized(esa.save(collSatWithHugh), collisionByDangerRisk);
        collSatWithLarge = sampleOptimized(esa.save(collSatWithLarge), collisionByDangerRisk);
        collSatWithSmall = sampleOptimized(esa.save(collSatWithSmall), collisionByDangerRisk);

        satellitesInOrbit -= collSatWithHugh;
        particlesHugh -= collSatWithHugh;
        shredIntoParticles(collSatWithHugh * 2);

        particlesHugh -= max(collHughWithHugh * 2, 0);
        shredIntoParticles(collHughWithHugh * 2);

        // process effects of collisions
        particlesHugh -= collHughWithLarge;
        shredIntoParticles(collHughWithLarge);

        satellitesInOrbit -= collSatWithLarge;
        shredIntoParticles(collSatWithLarge);

        satellitesInOrbit -= collSatWithSmall;
        particlesHugh += collSatWithSmall;

        // particles falling back into the atmosphere
        int fallenHugh = sampleOptimized(particlesHugh, fallProbLarge);
        particlesHugh -= fallenHugh;
        int fallenLarge = sampleOptimized(particlesLarge, fallProbSmall);
        particlesLarge -= fallenLarge;

        // launching new satellites
        daysUntilNextLaunch = max(0, daysUntilNextLaunch - 1);
        // could have been an if-statement, but this is more stable
        while (daysUntilNextLaunch < 1 && satellitesInOrbit < satellitesRequiredInOrbit) {
            satellitesInOrbit++;
            daysUntilNextLaunch += (1.0 / launchesPerDay); // may be a distribution?
        }
    }

    /** produce and add small and large particles resulting from shredding satellites */
    private void shredIntoParticles(int nOfCollisions) {
        particlesLarge += nOfCollisions * shreddingFactor * (1 - shreddingSmallFraction);
        particlesSmall += nOfCollisions * shreddingFactor * shreddingSmallFraction;
    }

    /** returns a sample of collisions */
    private static int sampleOptimized(long n, double p) {
        if (n < 0) {
            throw new IllegalStateException("n < 0: " + n);

        } else if (n == 0) {
            return 0;

        } else if (FORCE_NORMAL_DIST || n > (9 * (1 - p) / p)) { // if the binomial approaches an normal dist
            double est = NormalDistribution.get(n * p, n * p * (1 - p));
            return (int) min(max(randomToInt(est), 0), n);

        } else if (4 * p < POISSON_ERROR_MARGIN) {
            int est = PoissonDistribution.get(n * p);
            return (int) min(est, n);

        } else {
            return BinomialDistribution.get(n, p);
        }
    }

    /**
     * @param repetitions n
     * @param totalProb   t
     * @return p such that n experiments of p have a chance t on at least one success; (1 - p)^n = (1 - t)
     */
    private static double probSplit(double totalProb, int repetitions) {
        return 1 - Math.pow(1 - totalProb, 1.0 / repetitions);
    }

    // may save a few collisions
    class Observatory {
        private int savesLeft = observatoryCapacity;

        /**
         * try to save the satellites
         * @param number how many dangers there are
         * @return how many dangers are left
         */
        public int save(int number) {
            if (savesLeft > number) {
                savesLeft -= number;
                return 0;

            } else {
                number -= savesLeft;
                savesLeft = 0;
                return number;
            }
        }
    }
}
