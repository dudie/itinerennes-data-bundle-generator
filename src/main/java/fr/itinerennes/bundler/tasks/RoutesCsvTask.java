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
