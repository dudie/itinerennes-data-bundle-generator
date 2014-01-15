package fr.itinerennes.bundler.tasks;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.itinerennes.bundler.tasks.framework.AbstractCountedCsvTask;
import fr.itinerennes.bundler.tasks.framework.CsvTaskException;

/**
 * @author Jeremie Huchet
 */
@Component
public class RoutesCsvTask extends AbstractCountedCsvTask<Route> {

    private static final String ROUTES_CSV_FILENAME = "routes.csv";

    @Autowired
    private GtfsDao gtfsDao;

    public RoutesCsvTask() {
        super(ROUTES_CSV_FILENAME);
    }

    @Override
    protected List<Route> getDataList() throws CsvTaskException {
        return new ArrayList<Route>(gtfsDao.getAllRoutes());
    }

    @Override
    protected Object[] toCSV(final Route r) throws CsvTaskException {
        return new Object[] { r.getId().toString(), r.getShortName(), r.getLongName() };
    }

}
