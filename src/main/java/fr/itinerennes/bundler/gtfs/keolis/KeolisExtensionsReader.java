package fr.itinerennes.bundler.gtfs.keolis;

/*
 * [license]
 * Itinerennes data resources generator
 * ----
 * Copyright (C) 2013 - 2014 Dudie
 * ----
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsDao;

import fr.itinerennes.bundler.gtfs.keolis.model.RouteExt;

/**
 * @author Jeremie Huchet
 */
public class KeolisExtensionsReader extends GtfsReader {

    private final GtfsDao gtfsDao;
    
    private final List<Route> accessibleRoutes;

    public KeolisExtensionsReader(final KeolisGtfsDaoImpl targetDao) throws IOException {
        super();
        this.gtfsDao = targetDao;
        this.accessibleRoutes = new ArrayList<Route>();
        targetDao.setAllAccessibleRoutes(accessibleRoutes);
        setEntityStore(targetDao);

        getEntityClasses().add(RouteExt.class);
        setEntitySchemaFactory(new KeolisExtensionsEntitySchemaFactory());
        addEntityHandler(new KeolisExtensionsEntityHandler());
    }

    private class KeolisExtensionsEntityHandler implements EntityHandler {

        @Override
        public void handleEntity(final Object bean) {
            if (bean instanceof RouteExt) {
                final RouteExt routeExt = (RouteExt) bean;
                if (routeExt.isAccessible()) {
                    final String agencyId = getAgencyForEntity(Route.class, routeExt.getId());
                    accessibleRoutes.add(gtfsDao.getRouteForId(new AgencyAndId(agencyId, routeExt.getId())));
                }
            }
        }

    }
}
