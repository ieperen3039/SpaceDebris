package Simulation;

import Results.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static Simulation.SpaceSimulation.YEARS;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class Main {
    /** number of simulations run */
    public static final int NOF_RUNS = 250;
    /** how many parallel threads may run at once */
    private static final int THREAD_POOL = 10;
    private static final int MAX_YEARS = 300;
    /** number of days for one simulation */
    public static final int MAX_TIME = MAX_YEARS * YEARS;

    // run NOF_RUNS simulations and save everything in a csv file
    public static void main(String[] args) throws Exception {
        PrintWriter output = new PrintWriter("data.csv");
//        PrintStream output = System.out;
        ProgressBar progress = new ProgressBar(NOF_RUNS);

        List<SpaceSimulation> runs = new ArrayList<>(THREAD_POOL);
        MultiResultCollector collector = MultiSpaceResultsImpl.getCollector(NOF_RUNS, MAX_TIME + 1);
        for (int i = 0; i < NOF_RUNS; i += THREAD_POOL) {

            for (int j = 0; (j < THREAD_POOL) && (i + j < NOF_RUNS); j++) {
                SpaceSimulation suite = new SpaceSimulation(MAX_TIME + 1);
                suite.start(); // in parallel
                runs.add(suite);
            }

            for (SpaceSimulation run : runs) {
                SpaceResults results = run.results();
                collector.add(results);
                progress.printUpdate();
            }

            runs.clear();
        }
        System.out.print("\n\n");

        MultiSpaceResults results = collector.wrapUp();
        double[] activeSats = results.getActiveSatellites();
        double[] launchQueue = results.getSpaceFlightsQueued();
        double[] hughParticles = results.getHughParticles();
        double[] largeParticles = results.getLargeParticles();
        double[] smallParticles = results.getSmallParticles();
        double[] lostSatellites = results.getLostSatellites();

        System.out.printf(Locale.US, "Mean saves in %d years: %1.01f %s%n", MAX_YEARS, results.savesMean(), results.savesConf());
        System.out.printf(Locale.US, "Mean lost satellites in %d years: %1.03f %s%n", MAX_YEARS, results.lostSatellitesMean(), results.lostSatellitesConf());
        System.out.printf(Locale.US, "Small particles: %1.01f %s%n", results.smallParticleMean(), results.smallParticleConf());
        System.out.printf(Locale.US, "Large particles: %1.01f %s%n", results.largeParticleMean(), results.largeParticleConf());
        System.out.printf(Locale.US, "Hugh particles: %1.01f %s%n", results.hughParticleMean(), results.hughParticleConf());

        System.out.printf(Locale.US, "Mean total particles after %d years: %1.00f",
                MAX_YEARS, hughParticles[MAX_TIME] + largeParticles[MAX_TIME] + smallParticles[MAX_TIME]
        );

        output.println("dayNr;activeSats;launchQueue;hughDebris;largeDebris;smallDebris;lostSatellites");
        final int stepSize = (MAX_TIME / 5000) + 1; // at most 5000 values
        for (int i = 0; i < activeSats.length; i += stepSize) {
            output.printf(Locale.US,
                    "%d;%.03f;%.04f;%.02f;%.02f;%.02f;%.05f\n",
                    i,
                    activeSats[i],
                    launchQueue[i],
                    hughParticles[i],
                    largeParticles[i],
                    smallParticles[i],
                    lostSatellites[i]
            );
        }
        output.close();
    }

}
