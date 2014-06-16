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

import org.apache.commons.io.IOUtils;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jérémie Huchet
 */
public class GenerateTripsCsvTask implements Runnable {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateTripsCsvTask.class);

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
     * @param output
     *            the path to the output directory
     */
    public GenerateTripsCsvTask(final GtfsRelationalDao gtfsDao, final File output) {

        this.outputFile = new File(output, "trips.csv");
        this.gtfsDao = gtfsDao;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
                    CHARSET));

            // output trips count
            out.write(String.valueOf(gtfsDao.getAllTrips().size()));
            out.newLine();

            for (final Trip trip : gtfsDao.getAllTrips()) {

                out.write(trip.getId().toString());
                out.write(';');
                out.write(trip.getRoute().getId().toString());
                out.write(';');
                out.write(trip.getTripHeadsign());
                out.write(';');
                out.write(trip.getDirectionId());
                out.write(';');
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
}
