/**
 * 
 */
package fr.itinerennes.bundler.tasks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fr.itinerennes.api.client.model.Time;
import fr.itinerennes.api.client.model.TripSchedule;
import fr.itinerennes.api.client.model.TripStopTime;
import fr.itinerennes.bundler.gtfs.GtfsAdvancedDao;
import fr.itinerennes.bundler.tasks.framework.AbstractTask;

/**
 * @author Jeremie Huchet
 */
@Component
public class TripDetailsTask extends AbstractTask {

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
        final File dest = mkdir("trip-details");
        for (final Trip t : gtfs.getAllTrips()) {
            generateScheduleForTrip(dest, t);
        }
    }

    private File mkdir(final String entity) {
        final String parent = String.format("%s/%s", output, entity);
        final File dir = new File(parent);
        dir.mkdirs();
        return dir;
    }

    private void generateScheduleForTrip(File output, Trip t) {
        final TripSchedule sched = new TripSchedule();
        sched.setHeadsign(t.getTripHeadsign());
        sched.setRoute(toRoute(t.getRoute()));
        for (final StopTime st : gtfs.getStopTimesForTrip(t)) {
            sched.getStopTimes().add(toTripStopTime(st));
        }
        write(new File(output, String.format("%s.json", t.getId().toString())), sched);
    }

    private TripStopTime toTripStopTime(StopTime st) {
        final Calendar a = Calendar.getInstance(xGtfs.getTimeZone(st.getStop().getId().getAgencyId()));
        final Calendar d = Calendar.getInstance(xGtfs.getTimeZone(st.getStop().getId().getAgencyId()));
        a.add(Calendar.SECOND, st.getArrivalTime());
        d.add(Calendar.SECOND, st.getDepartureTime());
        
        final TripStopTime tst = new TripStopTime();
        tst.setArrivalTime(new Time(st.getArrivalTime() * 1000));
        tst.setDepartureTime(new Time(st.getDepartureTime() * 1000));
        tst.setStop(toStop(st.getStop()));
        tst.setStopHeadsign(st.getStopHeadsign());
        return tst;
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
