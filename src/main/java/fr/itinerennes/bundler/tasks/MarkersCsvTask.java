package fr.itinerennes.bundler.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.dudie.keolis.client.KeolisClient;
import fr.dudie.keolis.model.BikeStation;
import fr.dudie.keolis.model.SubwayStation;
import fr.itinerennes.bundler.tasks.framework.AbstractCountedCsvTask;
import fr.itinerennes.bundler.tasks.framework.PreExec;
import fr.itinerennes.commons.utils.StringUtils;

/**
 * @author Jérémie Huchet
 */
@Component
public class MarkersCsvTask extends AbstractCountedCsvTask {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkersCsvTask.class);

    private static final String MARKERS_CSV_FILENAME = "markers.csv";

    @Autowired
    private GtfsDao gtfsDao;

    @Autowired
    private KeolisClient keolis;

    private final List<Stop> busStops = new ArrayList<Stop>();

    /** The list of bike stations. */
    private final List<BikeStation> bikeStations = new ArrayList<BikeStation>();

    /** The list of subway stations. */
    private final List<SubwayStation> subwayStations = new ArrayList<SubwayStation>();

    public MarkersCsvTask() {
        super(MARKERS_CSV_FILENAME);
    }

    @PreExec
    public void loadBusStations() {
        busStops.addAll(gtfsDao.getAllStops());
        LOGGER.info("loaded {} bus stations", busStops.size());
    }

    @PreExec
    public void loadBikeStations() throws IOException {
        bikeStations.addAll(keolis.getAllBikeStations());
        LOGGER.info("loaded {} bike stations", bikeStations.size());
    }

    @PreExec
    public void loadSubwayStations() throws IOException {
        subwayStations.addAll(keolis.getAllSubwayStations());
        LOGGER.info("loaded {} subway stations", subwayStations.size());
    }

    @Override
    protected void generateLines() throws IOException {
        for (final Stop stop : busStops) {
            writeLine("BUS", stop.getId().toString(), stop.getLon(), stop.getLat(), stop.getName(), index(stop.getName()), stop.getDesc());
        }

        for (final BikeStation bike : bikeStations) {
            final String name = StringUtils.capitalize(bike.getName());
            writeLine("BIKE", bike.getId(), (int) (bike.getLatitude() * 1E6), (int) (bike.getLongitude() * 1E6), name, index(name), "");
        }

        for (final SubwayStation subway : subwayStations) {
            final String name = StringUtils.capitalize(subway.getName());
            writeLine("SUBWAY", subway.getId(), subway.getLongitude(), subway.getLatitude(), name, index(name), "");
        }
    }

    private String index(final String value) {
        return StringUtils.unaccent(value).toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
