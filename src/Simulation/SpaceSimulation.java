package Simulation;

import Distributions.*;
import Results.SpaceResults;

import static Distributions.Distribution.randomToInt;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * all values are either integer/long or double precision, time is measured in days, distance is measured in meters.
 * Values are derived from - (not) http://www.raa-journal.org/raa/index.php/raa/article/viewFile/1587/1442
 *                         - https://www.esa.int/Our_Activities/Operations/Space_Debris/Reentry_and_collision_avoidance
 *                         - https://www.esa.int/Our_Activities/Operations/Space_Debris/Space_debris_by_the_numbers
 *                         - https://www.livescience.com/62113-how-much-space-junk-hits-earth.html
 *                         - http://spaceflight101.com/2017-space-launch-statistics/
 *                         - https://www.esa.int/Our_Activities/Operations/Space_Debris/About_space_debris
 *
 * poisson distribution:   - http://www.stat.yale.edu/~pollard/Courses/241.fall97/Poisson.pdf
 * @author Geert van Ieperen created on 22-5-2018.
 */
@SuppressWarnings("WeakerAccess")
public class SpaceSimulation extends Thread {
    /** If this is false, poisson distributions may be used for collisions */
    public static final boolean FORCE_NORMAL_DIST = false;
    /** if the error resulting from assuming poisson at binomials falls below this margin, poisson is used instead */
    public static final double POISSON_ERROR_MARGIN = 0.001;

    /** Conversion factors */
    public static final int YEARS = 365; // in days
    public static final int NOF_TRACKED_OBJECTS = 38_700; // a few values are based on this

    /** number of satellites that we want in the sky */
    public static final int satellitesRequiredInOrbit = 1200;

    /** average period for debris to fall into the atmosphere or outer space in days */
    public static final double fallProbSmall = probSplit(300.0 / NOF_TRACKED_OBJECTS, YEARS);
    public static final double fallProbLarge = fallProbSmall;
    /** the number of particles a satellite creates when colliding */
    public static final double shreddingFactor = 10_000;
    /** the fraction of shredded particles that is smaller than 10 cm */
    public static final double shreddingSmallFraction = 0.75;
    /** distributions for the shredding values */
    public static final Distribution shreddingDistLarge =
            new ExponentialDistribution(1.0 / (shreddingFactor * (1 - shreddingSmallFraction)));
    public static final Distribution shreddingDistSmall =
            new ExponentialDistribution(1.0 / (shreddingFactor * shreddingSmallFraction));

    /** number of hugh particles generated upon launching a new satellite */
    public static final int launchStages = 2;
    public static final Distribution launchPartDistLarge = new ExponentialDistribution(1.0 / 50);
    /** probability of a dangerous situation per satellite - particle pair per day */
    // 12 avoidances per year: with 38_700 tracked particles and 19 satellites, we have 38_700 * 19 * p = 12
    public static final double probDangerPerParticle = probSplit(12.0 / (19 * NOF_TRACKED_OBJECTS), YEARS);
    /** average chance of collision when alarm is raised */
    public static final double collisionByDangerRisk = 1.0 / 25_000;
    /** breakdown probability per satellite per day. */
    // Even though this would result in an exponential breakdown, this holds when the number of satellites in orbit is constant
    public final static double satBreakdownProb = probSplit(0.1, YEARS);

    /** max satellite launches per day */
    public static final double launchesPerDay = 2 * 90.0 / YEARS;
    /** number of satellites that an observatory can resolve within 24 hours */
    public static final double observatoryCapacity = 0.25;
    // the ESA has 19 satellites by itself
    public static final int nOfObservatories = satellitesRequiredInOrbit / 19;
    public static final double observatorySavesPerDay = nOfObservatories * observatoryCapacity;

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
        Observatories esa = new Observatories();
        for (int i = 0; i < maxTime; i++) {
            progressOneDay(esa);
            esa.nextDay();

            if (satellitesInOrbit < 0 || particlesSmall < 0 || particlesLarge < 0 || particlesHugh < 0) {
                throw new IllegalStateException(String.format(
                        "Negative amount detected after %d days:\n" +
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

    /**
     * updates and calculates the changes for one day
     * @param obs an observatory capable of saving satellites from collisions
     */
    private void progressOneDay(Observatories obs) {
        // different types of collisions
        int collSatWithHugh = sampleOptimized((long) particlesHugh * satellitesInOrbit, probDangerPerParticle);
        int collSatWithLarge = sampleOptimized(particlesLarge * satellitesInOrbit, probDangerPerParticle);
        int collSatWithSmall = sampleOptimized(particlesSmall * satellitesInOrbit, probDangerPerParticle);
        int collHughWithLarge = sampleOptimized(particlesLarge * particlesHugh, probDangerPerParticle * collisionByDangerRisk);
        int collHughWithHugh = sampleOptimized((long) particlesHugh * (particlesHugh - 1), probDangerPerParticle * collisionByDangerRisk);

        // it may theoretically happen for three sats to collide, but then the collision chance should be adjusted
        collHughWithHugh = min(particlesHugh, collHughWithHugh * 2);

        // the observatory tries to resolve plausible collisions. upon failing, there is still only little chance on collision
        collSatWithHugh = sampleOptimized(obs.save(collSatWithHugh), collisionByDangerRisk);
        collSatWithLarge = sampleOptimized(obs.save(collSatWithLarge), collisionByDangerRisk);
        collSatWithSmall = sampleOptimized(collSatWithSmall, collisionByDangerRisk);

        // process effects of collisions
        satellitesInOrbit -= collSatWithHugh;
        particlesHugh -= collSatWithHugh;
        shredIntoParticles(collSatWithHugh * 2);
        results.addLostSatellites(collSatWithHugh);

        particlesHugh -= max(collHughWithHugh * 2, 0);
        shredIntoParticles(collHughWithHugh * 2);

        particlesHugh -= collHughWithLarge;
        shredIntoParticles(collHughWithLarge);

        satellitesInOrbit -= collSatWithLarge;
        shredIntoParticles(collSatWithLarge);
        results.addLostSatellites(collSatWithLarge);

        satellitesInOrbit -= collSatWithSmall;
        particlesHugh += collSatWithSmall;
        results.addLostSatellites(collSatWithSmall);

        int satBreakdown = sampleOptimized(satellitesInOrbit, satBreakdownProb);
        satellitesInOrbit -= satBreakdown;
        particlesHugh += satBreakdown;

        // particles falling back into the atmosphere
        particlesHugh -= sampleOptimized(particlesHugh, fallProbLarge);
        particlesLarge -= sampleOptimized(particlesLarge, fallProbSmall);
        particlesSmall -= sampleOptimized(particlesSmall, fallProbSmall);

        // launching new satellites
        daysUntilNextLaunch = max(0, daysUntilNextLaunch - 1);
        // could have been an if-statement, but this is more stable
        while (daysUntilNextLaunch < 1 && this.satellitesInOrbit < satellitesRequiredInOrbit) {
            this.satellitesInOrbit++;
            particlesHugh += launchStages;
            particlesLarge += launchPartDistLarge.nextRandom();
            daysUntilNextLaunch += (1.0 / launchesPerDay);
        }
    }

    /** produce and add small and large particles resulting from shredding satellites */
    private void shredIntoParticles(int nOfCollisions) {
        for (int i = 0; i < nOfCollisions; i++) {
            this.particlesLarge += shreddingDistLarge.nextRandom();
            this.particlesSmall += shreddingDistSmall.nextRandom();
        }
    }

    /** returns a sample of collisions */
    private static int sampleOptimized(long n, double p) {
        if (n <= 0) {
            if (n == 0) {
                return 0;
            } else {
                throw new IllegalStateException("n < 0: " + n);
            }

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
        // the chance of (NOT t) is equal to the chance of (NOT p after n times) : (1 - t) = (1 - p)^n
        return 1 - Math.pow(1 - totalProb, 1.0 / repetitions);
    }

    // may save a few collisions
    class Observatories {
        private int savesLeft = nOfObservatories;
        private double daysUntilRefresh = 0;

        /**
         * try to save the satellites
         * @param number how many dangers there are
         * @return how many dangers are left
         */
        public int save(int number) {
            if (savesLeft > number) {
                savesLeft -= number;
                results.addSaves(number);
                return 0;

            } else {
                number -= savesLeft;
                results.addSaves(savesLeft);
                savesLeft = 0;
                return number;
            }
        }

        public void nextDay() {
            daysUntilRefresh = max(0, daysUntilRefresh - 1);
            while (daysUntilRefresh < 1 && savesLeft < nOfObservatories) {
                savesLeft++;
                daysUntilRefresh += (1.0 / observatorySavesPerDay);
            }
        }
    }
}
