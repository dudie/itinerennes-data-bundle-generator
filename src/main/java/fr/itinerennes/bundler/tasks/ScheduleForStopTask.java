package fr.itinerennes.bundler.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

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
import com.google.gson.GsonBuilder;

import fr.dudie.onebusaway.model.ScheduleStopTime;
import fr.dudie.onebusaway.model.StopSchedule;
import fr.itinerennes.bundler.gtfs.GtfsAdvancedDao;
import fr.itinerennes.bundler.tasks.framework.AbstractTask;

@Component
public class ScheduleForStopTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleForStopTask.class);

    private final Gson gson = new Gson();

    @Value("${program.args.output}")
    private String output;

    @Autowired
    private GtfsRelationalDao gtfs;

    @Autowired
    private GtfsAdvancedDao xGtfs;

    @Override
    protected void execute() {
        LOGGER.info("starting SST task");
        for (final ServiceDate d : xGtfs.getAllServiceDates()) {
            for (final Stop s : gtfs.getAllStops()) {
                LOGGER.info("generating schedule-for-stop {} {}", d.toString(), s.toString());
                generateScheduleForStop(s, d);
            }
            // for (final Trip t : gtfs.getAllTrips()) {
            //
            // }
        }
    }

    private void generateScheduleForStop(Stop s, ServiceDate sd) {
        final String parent = String.format("%s/schedule-for-stop/%04d/%02d/%02d", output, sd.getYear(), sd.getMonth(), sd.getDay());
        new File(parent).mkdirs();

        final StopSchedule sched = new StopSchedule();
        sched.setDate(sd.getAsDate());
        sched.setStop(toStop(s));
        for (final StopTime st : xGtfs.getStopTimes(s, sd)) {
            sched.getStopTimes().add(toScheduledStopTime(st));
        }
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(parent, s.getId().getId()))));
            w.write(gson.toJson(sched));
        } catch (IOException e) {
            LOGGER.error("Can't serialize {}", new Object[] { parent, e });
        } finally {
            IOUtils.closeQuietly(w);
        }
    }

    private ScheduleStopTime toScheduledStopTime(StopTime gStopTime) {
        final ScheduleStopTime sst = new ScheduleStopTime();
        sst.setArrivalTime(new Date(gStopTime.getArrivalTime()));
        sst.setDepartureTime(new Date(gStopTime.getDepartureTime()));
        sst.setHeadsign(gStopTime.getTrip().getTripHeadsign());
        sst.setRoute(toRoute(gStopTime.getTrip().getRoute()));
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
