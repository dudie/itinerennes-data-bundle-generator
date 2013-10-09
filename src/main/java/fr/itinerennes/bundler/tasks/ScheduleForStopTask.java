package fr.itinerennes.bundler.tasks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fr.dudie.onebusaway.gson.OneBusAwayGsonFactory;
import fr.dudie.onebusaway.model.ScheduleStopTime;
import fr.dudie.onebusaway.model.StopSchedule;
import fr.dudie.onebusaway.model.Time;
import fr.dudie.onebusaway.model.TripSchedule;
import fr.dudie.onebusaway.model.TripStopTime;
import fr.itinerennes.bundler.gtfs.GtfsAdvancedDao;
import fr.itinerennes.bundler.tasks.framework.AbstractTask;

@Component
public class ScheduleForStopTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleForStopTask.class);

    private final Gson gson = OneBusAwayGsonFactory.newInstance();

    @Value("${program.args.output}")
    private String output;

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
            final File scheduleForTripOutput = mkdir(d, "schedule-for-trip");
            for (final Trip t : gtfs.getAllTrips()) {
                generateScheduleForTrip(scheduleForTripOutput, t, d);
            }
        }
    }

    private void generateScheduleForTrip(File output, Trip t, ServiceDate sd) {
        final TripSchedule sched = new TripSchedule();
        final Trip prev = xGtfs.getPreviousTrip(t, sd);
        if (null != prev) {
            sched.setPreviousTripId(prev.getId().toString());
        }
        final Trip next = xGtfs.getNextTrip(t, sd);
        if (null != next) {
            sched.setNextTripId(next.getId().toString());
        }
        for (final StopTime st : gtfs.getStopTimesForTrip(t)) {
            sched.getStopTimes().add(toStripStopTime(st));
        }
        write(new File(output, t.getId().toString()), sched);
    }

    private TripStopTime toStripStopTime(StopTime st) {
        final TripStopTime tst = new TripStopTime();
        tst.setArrivalTime(new Time(st.getArrivalTime()));
        tst.setDepartureTime(new Time(st.getDepartureTime()));
        tst.setDistanceAlongTrip(null);
        tst.setStop(toStop(st.getStop()));
        tst.setStopHeadsign(st.getStopHeadsign());
        return tst;
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

    private ScheduleStopTime toScheduledStopTime(StopTime gStopTime) {
        final ScheduleStopTime sst = new ScheduleStopTime();
        sst.setArrivalTime(new Date(gStopTime.getArrivalTime()));
        sst.setDepartureTime(new Date(gStopTime.getDepartureTime()));
        sst.setHeadsign(gStopTime.getTrip().getTripHeadsign());
        // routes are set as global entities
        // sst.setRoute(toRoute(gStopTime.getTrip().getRoute()));
        sst.setServiceId(gStopTime.getTrip().getServiceId().toString());
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
