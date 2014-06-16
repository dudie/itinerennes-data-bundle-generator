package fr.itinerennes.onebusaway.bundle.tasks;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import fr.dudie.keolis.client.JsonKeolisClient;
import fr.dudie.keolis.client.KeolisClient;
import fr.dudie.keolis.model.BikeStation;
import fr.dudie.keolis.model.SubwayStation;
import fr.itinerennes.commons.utils.StringUtils;

/**
 * @author Jérémie Huchet
 */
public class GenerateMarkersCsvTask implements Runnable {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateMarkersCsvTask.class);

    /** The UTF-8 charset. */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /** The list of bus stops. */
    private final GtfsDao gtfsDao;

    /** The list of bike stations. */
    private final List<BikeStation> bikeStations;

    /** The list of subway stations. */
    private final List<SubwayStation> subwayStations;

    /** The output file. */
    private final File outputFile;

    @Value("${keolisApiUrl}")
    private String keolisApiUrl;

    /**
     * Constructor.
     * 
     * @param gtfsDao
     *            the GTFS relational DAO
     * @param output
     *            the path to the output directory
     * @param keolisApiKey
     *            the Keolis API key
     * @throws IOException
     *             unable to retrieve data from Keolis API
     */
    public GenerateMarkersCsvTask(final GtfsRelationalDao gtfsDao, final File output,
            final String keolisApiKey) throws IOException {

        outputFile = new File(output, "markers.csv");

        this.gtfsDao = gtfsDao;
        final HttpClient http = new DefaultHttpClient();
        final KeolisClient keolis = new JsonKeolisClient(http, keolisApiUrl, keolisApiKey);
        bikeStations = keolis.getAllBikeStations();
        subwayStations = keolis.getAllSubwayStations();

        LOGGER.info("having {} bike stations", bikeStations.size());
        LOGGER.info("having {} subway stations", subwayStations.size());
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        final Collection<Stop> busStops = gtfsDao.getAllStops();
        LOGGER.info("having {} bus stop", busStops.size());

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
                    CHARSET));

            // output stop and stations count
            out.write(String.valueOf(busStops.size() + bikeStations.size() + subwayStations.size()));
            out.newLine();

            for (final Stop stop : busStops) {
                out.write(toCsvLine("BUS", stop.getId().toString(), stop.getLon(), stop.getLat(),
                        stop.getName(), stop.getDesc()));
                out.newLine();
            }

            for (final BikeStation bike : bikeStations) {
                final String name = StringUtils.capitalize(bike.getName());
                out.write(toCsvLine("BIKE", bike.getId(), bike.getLongitude(), bike.getLatitude(),
                        name, ""));
                out.newLine();
            }

            for (final SubwayStation subway : subwayStations) {
                final String name = StringUtils.capitalize(subway.getName());
                out.write(toCsvLine("SUBWAY", subway.getId(), subway.getLongitude(),
                        subway.getLatitude(), name, ""));
                out.newLine();
            }

        } catch (final FileNotFoundException e) {
            LOGGER.error("output file not found", e);
        } catch (final IOException e) {
            LOGGER.error("can't write to output file", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Generates a CSV line from the given values
     * 
     * @param type
     *            the marker type (BUS, BIKE or SUBWAY)
     * @param id
     *            the marker identifier (unique for a given type)
     * @param lon
     *            the longitude
     * @param lat
     *            the latitude
     * @param name
     *            the name
     * @param city
     *            the city
     * @return a CSV line
     */
    private String toCsvLine(final String type, final String id, final double lon,
            final double lat, final String name, final String city) {

        final StringBuilder csv = new StringBuilder();
        csv.append(type).append(';');
        csv.append(id).append(';');
        csv.append((int) (lat * 1E6)).append(';');
        csv.append((int) (lon * 1E6)).append(';');
        csv.append(name).append(';');
        final String index = StringUtils.unaccent(name).toLowerCase().replaceAll("[^a-z0-9]", "");
        csv.append(index).append(';');
        csv.append(city).append(';');
        return csv.toString();
    }
}
