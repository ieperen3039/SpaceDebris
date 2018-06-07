package Simulation;

import Results.MultiResultCollector;
import Results.MultiSpaceResults;
import Results.MultiSpaceResultsImpl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class Main {
    /** number of simulations run */
    public static final int NOF_RUNS = 10;
    /** how many parallel threads may run at once */
    private static final int THREAD_POOL = 3;
    private static final int MAX_YEARS = 25;
    /** number of days for one simulation */
    public static final int MAX_TIME = 365 * MAX_YEARS;

    // run NOF_RUNS simulations and save everything in a csv file
    public static void main(String[] args) throws Exception {
        PrintWriter output = new PrintWriter("data.csv");
//        PrintStream output = System.out;
        long startTime = System.currentTimeMillis();

        List<SpaceSimulation> runs = new ArrayList<>(THREAD_POOL);
        MultiResultCollector collector = MultiSpaceResultsImpl.getCollector(NOF_RUNS, MAX_TIME + 1);
        for (int i = 0; i < NOF_RUNS; i += THREAD_POOL) {
            System.out.printf("\rRunning: %d to %d of %d", i, Math.min(i + THREAD_POOL, NOF_RUNS), NOF_RUNS);

            for (int j = 0; (j < THREAD_POOL) && (i + j < NOF_RUNS); j++) {
                SpaceSimulation suite = new SpaceSimulation(MAX_TIME + 1);
                suite.start(); // in parallel
                runs.add(suite);
            }

            for (SpaceSimulation run : runs) {
                collector.add(run.results());
            }

            runs.clear();
        }
        System.out.println();
        System.out.println("Simulation time: " + (System.currentTimeMillis() - startTime) + " ms\n");

        MultiSpaceResults results = collector.wrapUp();
        System.out.println("Mean saves in " + MAX_YEARS + " years: " + results.savesMean() + " " + results.savesConf());
        System.out.println("Mean lost satellites in " + MAX_YEARS + " years: " + results.lostSatellitesMean() + " " + results.lostSatellitesConf());

        double[] totals = results.totals();
        double[] activeSats = results.getActiveSatellites();
        double[] launchQueue = results.getSpaceFlightsQueued();
        double[] hughDebris = results.getTotalSatellites();
        output.println("totals,activeSats,launchQueue,hughDebris");

        final int stepSize = (MAX_TIME / 5000) + 1;
        for (int i = 0; i < totals.length; i += stepSize) {
            output.printf(Locale.US,
                    "%.03f,%.03f,%.03f,%.03f\n",
                    totals[i],
                    activeSats[i],
                    launchQueue[i],
                    hughDebris[i]
            );
        }
        output.close();
    }

}
