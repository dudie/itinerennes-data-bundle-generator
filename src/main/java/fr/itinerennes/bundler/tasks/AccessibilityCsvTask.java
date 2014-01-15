package fr.itinerennes.bundler.tasks;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.itinerennes.bundler.gtfs.dao.KeolisGtfsDao;
import fr.itinerennes.bundler.tasks.framework.AbstractCsvTask;
import fr.itinerennes.bundler.tasks.framework.CsvTaskException;
import fr.itinerennes.bundler.tasks.AccessibilityCsvTask.AccessibleItem;

/**
 * Generates the accessibility.csv file.
 * 
 * @author Jeremie Huchet
 */
@Component
public class AccessibilityCsvTask extends AbstractCsvTask<AccessibleItem> {

    @Autowired
    private KeolisGtfsDao gtfs;

    public AccessibilityCsvTask() {
        super("accessibility.csv");
    }

    /**
     * Generates the list of all accessible bus routes and bus stops.
     * 
     * Returns the list of all stops having attribute <i>wheelchairBoarding</i> set to <code>1</code> and all
     * acccessible routes.
     */
    @Override
    protected List<AccessibleItem> getData() throws CsvTaskException {
        final List<AccessibleItem> data = new ArrayList<AccessibleItem>();
        for (final Stop stop : gtfs.getAllStops()) {
            if (stop.getWheelchairBoarding() > 0) {
                data.add(new AccessibleItem(stop));
            }
        }
        for (final Route route : gtfs.getAllAccessibleRoutes()) {
            data.add(new AccessibleItem(route));
        }
        return data;
    }

    @Override
    protected Object[] toCSV(final AccessibleItem d) throws CsvTaskException {
        return new Object[] { d.id, d.type, d.accessible };
    }

    static class AccessibleItem {

        private final String id;
        private final String type;
        private final int accessible = 1;

        public AccessibleItem(final Stop stop) {
            this.id = stop.getId().toString();
            this.type = "BUS";
        }

        public AccessibleItem(final Route route) {
            this.id = route.getId().toString();
            this.type = "BUS_ROUTE";
        }
    }
}
