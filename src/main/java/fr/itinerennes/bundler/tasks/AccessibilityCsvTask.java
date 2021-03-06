package fr.itinerennes.bundler.tasks;

/*
 * [license]
 * Itinerennes data resources generator
 * ~~~~
 * Copyright (C) 2013 - 2014 Dudie
 * ~~~~
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * [/license]
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.itinerennes.bundler.tasks.AccessibilityCsvTask.AccessibleItem;
import fr.itinerennes.bundler.tasks.framework.AbstractCsvTask;
import fr.itinerennes.bundler.tasks.framework.CsvTaskException;

/**
 * Generates the accessibility.csv file.
 * 
 * @author Jeremie Huchet
 */
@Component
public class AccessibilityCsvTask extends AbstractCsvTask<AccessibleItem> {

    private static final int WHEELCHAIR_ACCESSIBLE = 1;

    @Autowired
    private GtfsDao gtfs;

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
        final Map<Route, Boolean> accessibleRoutes = new HashMap<Route, Boolean>();
        for (final Trip trip : gtfs.getAllTrips()) {
            if (WHEELCHAIR_ACCESSIBLE == trip.getWheelchairAccessible()) {
                if (!accessibleRoutes.containsKey(trip.getRoute())) {
                    accessibleRoutes.put(trip.getRoute(), true);
                }
            } else {
                accessibleRoutes.put(trip.getRoute(), false);
            }
        }
        for (final Entry<Route, Boolean> r : accessibleRoutes.entrySet()) {
            if (r.getValue()) {
                data.add(new AccessibleItem(r.getKey()));
            }
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
