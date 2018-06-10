package Simulation;

import Results.MultiResultCollector;
import Results.MultiSpaceResults;
import Results.MultiSpaceResultsImpl;
import Results.ProgressBar;

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
    public static final int NOF_RUNS = 100;
    /** how many parallel threads may run at once */
    private static final int THREAD_POOL = 10;
    private static final int MAX_YEARS = 100;
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
                collector.add(run.results());
                progress.printUpdate();
                System.out.flush();
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

        System.out.printf("Mean saves in %d years: %1.03f %s%n", MAX_YEARS, results.savesMean(), results.savesConf());
        System.out.printf("Mean lost satellites in %d years: %1.03f %s%n", MAX_YEARS, results.lostSatellitesMean(), results.lostSatellitesConf());
        System.out.printf("Mean total particles after %d years: %1.03f",
                MAX_TIME, hughParticles[MAX_TIME] + largeParticles[MAX_TIME] + smallParticles[MAX_TIME]
        );

        output.println("dayNr;activeSats;launchQueue;hughDebris;largeDebris;smallDebris");
        final int stepSize = (MAX_TIME / 5000) + 1; // at most 5000 values
        for (int i = 0; i < activeSats.length; i += stepSize) {
            output.printf(Locale.US,
                    "%d;%.03f;%.03f;%.02f;%.02f;%.02f\n",
                    i,
                    activeSats[i],
                    launchQueue[i],
                    hughParticles[i],
                    largeParticles[i],
                    smallParticles[i]
            );
        }
        output.close();
    }

}
