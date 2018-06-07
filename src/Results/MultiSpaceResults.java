package Results;

/**
 * @author Geert van Ieperen created on 7-6-2018.
 */
public interface MultiSpaceResults {
    double[] totals();

    Interval lostSatellitesConf();

    double lostSatellitesMean();

    double savesMean();

    Interval savesConf();

    double[] getActiveSatellites();

    double[] getSpaceFlightsQueued();

    double[] getTotalSatellites();
}
