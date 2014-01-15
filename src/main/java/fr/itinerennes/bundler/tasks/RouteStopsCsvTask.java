package fr.itinerennes.bundler.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.itinerennes.bundler.tasks.RouteStopsCsvTask.Item;
import fr.itinerennes.bundler.tasks.framework.AbstractCountedCsvTask;
import fr.itinerennes.bundler.tasks.framework.CsvTaskException;
import fr.itinerennes.bundler.tasks.framework.PostExec;
import fr.itinerennes.bundler.tasks.framework.PreExec;

@Component
public class RouteStopsCsvTask extends AbstractCountedCsvTask<Item> {

    @Autowired
    private GtfsDao gtfsDao;

    @SuppressWarnings("unchecked")
    private final Map<String, Set<String>> routesToStops = LazyMap.decorate(new HashMap<String, Set<String>>(), new HashSetFactory());

    public RouteStopsCsvTask() {
        super("routes_stops.csv");
    }

    @PreExec
    public void loadData() {
        for (final StopTime stopTime : gtfsDao.getAllStopTimes()) {
            final String routeId = stopTime.getTrip().getRoute().getId().toString();
            final String stopId = stopTime.getStop().getId().toString();
            // add or overwrite stopId for the routeId
            routesToStops.get(routeId).add(stopId);
        }
    }

    @PostExec
    public void release() {
        routesToStops.clear();
    }

    @Override
    protected List<Item> getDataList() throws CsvTaskException {
        final List<Item> relations = new ArrayList<Item>();
        for (final Entry<String, Set<String>> route : routesToStops.entrySet()) {
            final String routeId = route.getKey();
            for (final String stopId : route.getValue()) {
                relations.add(new Item(routeId, stopId));
            }
        }
        return relations;
    }

    @Override
    protected Object[] toCSV(final Item item) throws CsvTaskException {
        return new Object[] { item.route, item.stop };
    }

    private static class HashSetFactory implements Factory {

        @Override
        public Object create() {
            return new HashSet<String>();
        }

    }

    static class Item {
        private String route;
        private String stop;

        public Item(final String route, final String stop) {
            this.route = route;
            this.stop = stop;
        }
    }
}
