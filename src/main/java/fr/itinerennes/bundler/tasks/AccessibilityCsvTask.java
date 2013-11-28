package fr.itinerennes.bundler.tasks;

import java.io.IOException;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.itinerennes.bundler.gtfs.dao.KeolisGtfsDao;
import fr.itinerennes.bundler.tasks.framework.AbstractCsvTask;

/**
 * @author Jeremie Huchet
 */
@Component
public class AccessibilityCsvTask extends AbstractCsvTask {

    @Autowired
    private KeolisGtfsDao gtfs;

    public AccessibilityCsvTask() {
        super("accessibility.csv");
    }

    @Override
    protected void generateLines() throws IOException {
        for (final Stop stop : gtfs.getAllStops()) {
            if (stop.getWheelchairBoarding() > 0) {
                writeLine(stop.getId(), "BUS", 1);
            }
        }
        for (final Route route : gtfs.getAllAccessibleRoutes()) {
            writeLine(route.getId().toString(), "BUS_ROUTE", 1);
        }
    }

}
