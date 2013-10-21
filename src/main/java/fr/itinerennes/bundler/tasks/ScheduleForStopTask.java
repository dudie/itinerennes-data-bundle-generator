package fr.itinerennes.bundler.tasks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;

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

import fr.dudie.onebusaway.model.ScheduleStopTime;
import fr.dudie.onebusaway.model.StopSchedule;
import fr.dudie.onebusaway.model.Time;
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
            sched.getStopTimes().add(toScheduledStopTime(st));
            final fr.dudie.onebusaway.model.Route route = toRoute(st.getTrip().getRoute());
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
        Collections.sort(sched.getRoutes(), new Comparator<fr.dudie.onebusaway.model.Route>() {
            @Override
            public int compare(fr.dudie.onebusaway.model.Route r1, fr.dudie.onebusaway.model.Route r2) {
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

    private ScheduleStopTime toScheduledStopTime(final StopTime gStopTime) {
        final ScheduleStopTime sst = new ScheduleStopTime();
        sst.setArrivalTime(new Time(gStopTime.getArrivalTime() * 1000));
        sst.setDepartureTime(new Time(gStopTime.getDepartureTime() * 1000));
        sst.setHeadsign(gStopTime.getTrip().getTripHeadsign());
        // routes are set as global entities
        // sst.setRoute(toRoute(gStopTime.getTrip().getRoute()));
        sst.setServiceId(gStopTime.getTrip().getServiceId().toString());
        sst.setTripId(gStopTime.getTrip().getId().toString());
        return sst;
    }

    private fr.dudie.onebusaway.model.Route toRoute(final Route gRoute) {
        final fr.dudie.onebusaway.model.Route route = new fr.dudie.onebusaway.model.Route();
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

    private fr.dudie.onebusaway.model.Stop toStop(final Stop gStop) {
        final fr.dudie.onebusaway.model.Stop stop = new fr.dudie.onebusaway.model.Stop();
        stop.setCode(Integer.valueOf(gStop.getCode()));
        stop.setDirection(gStop.getDirection());
        stop.setId(gStop.getId().toString());
        stop.setLat(gStop.getLat());
        stop.setLon(gStop.getLon());
        stop.setName(gStop.getName());
        return stop;
    }

}
