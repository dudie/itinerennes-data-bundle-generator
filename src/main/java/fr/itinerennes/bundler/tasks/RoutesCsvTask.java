package fr.itinerennes.bundler.tasks;

import java.io.IOException;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.itinerennes.bundler.tasks.framework.AbstractCountedCsvTask;

/**
 * @author Jeremie Huchet
 */
@Component
public class RoutesCsvTask extends AbstractCountedCsvTask {

    private static final String ROUTES_CSV_FILENAME = "routes.csv";

    @Autowired
    private GtfsDao gtfsDao;

    public RoutesCsvTask() {
        super(ROUTES_CSV_FILENAME);
    }

    @Override
    protected void generateLines() throws IOException {
        for (final Route r : gtfsDao.getAllRoutes()) {
            writeLine(r.getId().toString(), r.getShortName(), r.getLongName());
        }
    }

}
