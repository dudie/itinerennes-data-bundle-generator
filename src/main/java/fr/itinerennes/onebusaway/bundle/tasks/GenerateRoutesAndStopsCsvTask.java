package fr.itinerennes.onebusaway.bundle.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.io.IOUtils;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jérémie Huchet
 */
public class GenerateRoutesAndStopsCsvTask implements Runnable {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GenerateRoutesAndStopsCsvTask.class);

    /** The UTF-8 charset. */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /** The list of bus stops. */
    private final GtfsDao gtfsDao;

    /** The output file. */
    private final File outputFile;

    /**
     * Constructor.
     * 
     * @param gtfsDao
     *            the GTFS relational DAO
     * @param outputPath
     *            the path to the output directory
     */
    public GenerateRoutesAndStopsCsvTask(final GtfsRelationalDao gtfsDao, final File output) {

        this.outputFile = new File(output, "routes_stops.csv");
        this.gtfsDao = gtfsDao;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        @SuppressWarnings("unchecked") final Map<String, Set<String>> routesToStops = LazyMap
                .decorate(new HashMap<String, Set<String>>(), new Factory() {

                    @Override
                    public Object create() {

                        return new HashSet<String>();
                    }
                });

        for (final StopTime stopTime : gtfsDao.getAllStopTimes()) {
            final String routeId = stopTime.getTrip().getRoute().getId().toString();
            final String stopId = stopTime.getStop().getId().toString();
            // add or overwrite stopId for the routeId
            routesToStops.get(routeId).add(stopId);
        }

        // count amount of couples (routeId, stopId)
        int count = 0;
        for (final Entry<String, Set<String>> route : routesToStops.entrySet()) {
            if (route.getValue().isEmpty()) {
                throw new IllegalStateException("route " + route.getKey() + " has 0 stops");
            }
            count += route.getValue().size();
        }

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
                    CHARSET));

            // output amount of couples (routeId, stopId)
            out.write(String.valueOf(count));
            out.newLine();

            for (final Entry<String, Set<String>> route : routesToStops.entrySet()) {
                final String routeId = route.getKey();
                for (final String stopId : route.getValue()) {
                    out.write(routeId);
                    out.write(';');
                    out.write(stopId);
                    out.write(';');
                    out.newLine();
                }
            }

        } catch (final FileNotFoundException e) {
            LOGGER.error("output file not found", e);
        } catch (final IOException e) {
            LOGGER.error("can't write to output file", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
