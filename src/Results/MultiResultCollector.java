package Results;

import Simulation.SpaceResults;

/**
 * @author Geert van Ieperen created on 7-6-2018.
 */
public interface MultiResultCollector {
    void add(SpaceResults results);

    MultiSpaceResults wrapUp();
}
