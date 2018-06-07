package Results;

import Distributions.Interval;

/**
 * @author Geert van Ieperen created on 7-6-2018.
 */
public interface MultiSpaceResults {
    long[] totals();

    Interval lostSatellitesConf();

    double lostSatellitesMean();
}
