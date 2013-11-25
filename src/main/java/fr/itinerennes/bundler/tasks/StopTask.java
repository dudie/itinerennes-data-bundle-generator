package fr.itinerennes.bundler.tasks;

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

        final Set<AgencyAndId> added = new HashSet<AgencyAndId>();
        for (final StopTime gStopTime : gtfs.getAllStopTimes()) {
            // if the stop time serves the stop
            final AgencyAndId stopId = gStopTime.getStop().getId();
            if (stopId.equals(gStop.getId()) && !added.contains(stopId)) {
                stop.getRoutes().add(toRoute(gStopTime.getTrip().getRoute()));
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
