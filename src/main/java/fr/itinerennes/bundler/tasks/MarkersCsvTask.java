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
import fr.itinerennes.bundler.tasks.framework.CsvTaskException;
import fr.itinerennes.bundler.tasks.framework.PreExec;
import fr.itinerennes.commons.utils.StringUtils;
import fr.itinerennes.bundler.tasks.MarkersCsvTask.Marker;

/**
 * Generates the markers.csv file.
 * @author Jérémie Huchet
 */
@Component
public class MarkersCsvTask extends AbstractCountedCsvTask<Marker> {

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
    protected List<Marker> getDataList() throws CsvTaskException {
        final List<Marker> data = new ArrayList<Marker>();

        for (final Stop stop : busStops) {
            data.add(new Marker(stop));
        }

        for (final BikeStation bike : bikeStations) {
            data.add(new Marker(bike));
        }

        for (final SubwayStation subway : subwayStations) {
            data.add(new Marker(subway));
        }
        return data;
    }

    @Override
    protected Object[] toCSV(final Marker m) throws CsvTaskException {
        return new Object[] { m.type, m.id, m.lon, m.lat, m.name, m.indexedName };
    }

    static class Marker {

        private final String type;
        private final String id;
        private final int lon;
        private final int lat;
        private final String name;
        private final String indexedName;

        public Marker(final Stop stop) {
            this.type = "BUS";
            this.id = stop.getId().toString();
            this.lon = toIntE6(stop.getLon());
            this.lat = toIntE6(stop.getLat());
            this.name = stop.getName();
            this.indexedName = index(this.name);
        }

        public Marker(final BikeStation bike) {
            this.type = "BIKE";
            this.id = bike.getId();
            this.lon = toIntE6(bike.getLongitude());
            this.lat = toIntE6(bike.getLatitude());
            this.name = bike.getName();
            this.indexedName = index(this.name);
        }

        public Marker(final SubwayStation subway) {
            this.type = "SUBWAY";
            this.id = subway.getId();
            this.lon = toIntE6(subway.getLongitude());
            this.lat = toIntE6(subway.getLatitude());
            this.name = subway.getName();
            this.indexedName = index(this.name);
        }

        private int toIntE6(final double coord) {
            return (int) (coord * 1E6);
        }

        private String index(final String value) {
            return StringUtils.unaccent(value).toLowerCase().replaceAll("[^a-z0-9]", "");
        }
    }
}
