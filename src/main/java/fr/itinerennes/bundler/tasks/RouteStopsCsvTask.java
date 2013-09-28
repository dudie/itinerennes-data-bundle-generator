package fr.itinerennes.bundler.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.itinerennes.bundler.tasks.framework.AbstractCountedCsvTask;
import fr.itinerennes.bundler.tasks.framework.PostExec;
import fr.itinerennes.bundler.tasks.framework.PreExec;

@Component
public class RouteStopsCsvTask extends AbstractCountedCsvTask {

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
    protected void generateLines() throws IOException {
        for (final Entry<String, Set<String>> route : routesToStops.entrySet()) {
            final String routeId = route.getKey();
            for (final String stopId : route.getValue()) {
                writeLine(routeId, stopId);
            }
        }
    }

    private static class HashSetFactory implements Factory {

        @Override
        public Object create() {
            return new HashSet<String>();
        }

    }
}
