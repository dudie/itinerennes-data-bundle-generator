package fr.itinerennes.bundler.tasks;

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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fr.itinerennes.api.client.model.StopWithRoutes;
import fr.itinerennes.bundler.tasks.framework.AbstractTask;

/**
 * @author Jeremie Huchet
 */
@Component
public class StopTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleForStopTask.class);

    @Value("${program.args.output}")
    private String output;

    @Autowired
    private Gson gson;

    @Autowired
    private GtfsRelationalDao gtfs;

    @Override
    protected void execute() {
        final File dest = mkdir("stop");
        for (final Stop s : gtfs.getAllStops()) {
            generateStop(dest, s);
        }
    }

    private File mkdir(final String entity) {
        final String parent = String.format("%s/%s", output, entity);
        final File dir = new File(parent);
        dir.mkdirs();
        return dir;
    }

    private void generateStop(final File output, final Stop gStop) {
        final StopWithRoutes stop = new StopWithRoutes();
        stop.setCode(Integer.valueOf(gStop.getCode()));
        stop.setDirection(gStop.getDirection());
        stop.setId(gStop.getId().toString());
        stop.setLat(gStop.getLat());
        stop.setLon(gStop.getLon());
        stop.setName(gStop.getName());
        stop.setRoutes(new ArrayList<fr.itinerennes.api.client.model.Route>());

        final Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
        for (final StopTime gStopTime : gtfs.getAllStopTimes()) {
            // if the stop time serves the stop and the route isn't already added
            final AgencyAndId routeId = gStopTime.getTrip().getRoute().getId();
            if (gStop.getId().equals(gStopTime.getStop().getId()) && !routeIds.contains(routeId)) {
                routeIds.add(routeId);
                stop.getRoutes().add(toRoute(gtfs.getRouteForId(routeId)));
            }
        }

        write(new File(output, String.format("%s.json", stop.getId().toString())), stop);
    }

    private void write(final File outputFile, final Object o) {
        Writer w = null;
        try {
            w = new PrintWriter(outputFile, "utf-8");
            w.write(gson.toJson(o));
        } catch (IOException e) {
            LOGGER.error("Can't serialize {} {}", new Object[] { o, outputFile, e });
        } finally {
            IOUtils.closeQuietly(w);
        }
    }

    private fr.itinerennes.api.client.model.Route toRoute(final Route gRoute) {
        final fr.itinerennes.api.client.model.Route route = new fr.itinerennes.api.client.model.Route();
        route.setAgencyId(gRoute.getAgency().getId());
        route.setId(String.valueOf(gRoute.getId()));
        route.setShortName(gRoute.getShortName());
        route.setLongName(gRoute.getLongName());
        route.setDescription(gRoute.getDesc());
        route.setType(gRoute.getType());
        route.setTextColor(gRoute.getTextColor());
        route.setColor(gRoute.getColor());
        return route;
    }

}
