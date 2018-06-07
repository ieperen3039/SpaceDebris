package Simulation;

import Results.MultiResultCollector;
import Results.MultiSpaceResultImpl;
import Results.MultiSpaceResults;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 4-6-2018.
 */
public class Main {
    /** number of simulations run */
    public static final int NOF_RUNS = 10;
    /** how many parallel threads may run at once */
    private static final int THREAD_POOL = 5;
    private static final int MAX_YEARS = 20;
    /** number of days for one simulation */
    public static final int MAX_TIME = 365 * MAX_YEARS;

    // run NOF_RUNS simulations and save everything in a csv file
    public static void main(String[] args) throws Exception {
//        PrintWriter output = new PrintWriter("data.csv");
        PrintStream output = System.out;
        long startTime = System.currentTimeMillis();

        List<SpaceSimulation> runs = new ArrayList<>(THREAD_POOL);
        MultiResultCollector collector = MultiSpaceResultImpl.getCollector(NOF_RUNS, MAX_TIME + 1);
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
        MultiSpaceResults results = collector.wrapUp();
        System.out.println();

        System.out.println("Simulation time: " + (System.currentTimeMillis() - startTime) + " ms");

        System.out.println("Mean lost satellites in " + MAX_YEARS + " years: " + results.lostSatellitesMean() + " " + results.lostSatellitesConf());

        long[] totals = results.totals();
        for (int i = 0; i < totals.length; i += 200) {
            output.print(totals[i]);
            output.print(", ");
        }
        output.close();
    }

}
