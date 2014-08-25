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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fr.itinerennes.api.client.model.ScheduleStopTime;
import fr.itinerennes.api.client.model.StopSchedule;
import fr.itinerennes.bundler.gtfs.GtfsAdvancedDao;
import fr.itinerennes.bundler.tasks.framework.AbstractTask;

@Component
public class ScheduleForStopTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleForStopTask.class);

    @Value("${program.args.output}")
    private String output;

    @Autowired
    private Gson gson;

    @Autowired
    private GtfsRelationalDao gtfs;

    @Autowired
    private GtfsAdvancedDao xGtfs;

    @Override
    protected void execute() {
        for (final ServiceDate d : xGtfs.getAllServiceDates()) {
            LOGGER.info("generating {}", d);
            final File scheduleForStopOutput = mkdir(d, "schedule-for-stop");
            for (final Stop s : gtfs.getAllStops()) {
                generateScheduleForStop(scheduleForStopOutput, s, d);
            }
        }
    }

    private File mkdir(final ServiceDate sd, final String entity) {
        final String parent = String.format("%s/%s/%04d/%02d/%02d", output, entity, sd.getYear(), sd.getMonth(), sd.getDay());
        final File dir = new File(parent);
        dir.mkdirs();
        return dir;
    }

    private void generateScheduleForStop(File output, Stop s, ServiceDate sd) {

        final StopSchedule sched = new StopSchedule();
        sched.setDate(sd.getAsDate());
        sched.setStop(toStop(s));

        for (final StopTime st : xGtfs.getStopTimes(s, sd)) {
            sched.getStopTimes().add(toScheduledStopTime(st, sd));
            final fr.itinerennes.api.client.model.Route route = toRoute(st.getTrip().getRoute());
            if (!sched.getRoutes().contains(route)) {
                sched.getRoutes().add(route);
            }
        }

        Collections.sort(sched.getStopTimes(), new Comparator<ScheduleStopTime>() {
            @Override
            public int compare(ScheduleStopTime st1, ScheduleStopTime st2) {
                return st1.getDepartureTime().compareTo(st2.getDepartureTime());
            }
        });
        Collections.sort(sched.getRoutes(), new Comparator<fr.itinerennes.api.client.model.Route>() {
            @Override
            public int compare(fr.itinerennes.api.client.model.Route r1, fr.itinerennes.api.client.model.Route r2) {
                final String id1 = r1.getAgencyId() + r1.getId();
                final String id2 = r2.getAgencyId() + r2.getId();
                return id1.compareTo(id2);
            }
        });

        write(new File(output, String.format("%s.json", s.getId())), sched);
    }

    private void write(File outputFile, Object o) {
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

    private ScheduleStopTime toScheduledStopTime(final StopTime gStopTime, final ServiceDate gServiceDate) {
        final ScheduleStopTime sst = new ScheduleStopTime();
        
        final Calendar a = gServiceDate.getAsCalendar(TimeZone.getDefault());
        final Calendar d = gServiceDate.getAsCalendar(TimeZone.getDefault());
        a.add(Calendar.SECOND, gStopTime.getArrivalTime());
        d.add(Calendar.SECOND, gStopTime.getDepartureTime());
        
        sst.setArrivalTime(new Date(a.getTimeInMillis()));
        sst.setDepartureTime(new Date(d.getTimeInMillis()));
        sst.setHeadsign(gStopTime.getTrip().getTripHeadsign());
        sst.setRoute(toRoute(gStopTime.getTrip().getRoute()));
        sst.setRouteId(sst.getRoute().getId());
        sst.setServiceId(gStopTime.getTrip().getServiceId().toString());
        sst.setTripId(gStopTime.getTrip().getId().toString());
        return sst;
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

    private fr.itinerennes.api.client.model.Stop toStop(final Stop gStop) {
        final fr.itinerennes.api.client.model.Stop stop = new fr.itinerennes.api.client.model.Stop();
        stop.setCode(Integer.valueOf(gStop.getCode()));
        stop.setDirection(gStop.getDirection());
        stop.setId(gStop.getId().toString());
        stop.setLat(gStop.getLat());
        stop.setLon(gStop.getLon());
        stop.setName(gStop.getName());
        return stop;
    }

}
