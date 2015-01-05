package fr.itinerennes.bundler.gtfs;

/*
 * [license]
 * Itinerennes data resources generator
 * ~~~~
 * Copyright (C) 2013 - 2014 Dudie
 * ~~~~
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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.onebusaway.csv_entities.ZipFileCsvInputSource;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jérémie Huchet
 */
public final class GtfsUtils {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsUtils.class);

    /**
     * Avoid instantiation.
     */
    private GtfsUtils() {

    }

    /**
     * Load a GTFS from a file.
     * 
     * @param gtfsFile
     *            the path to the GTFS file to load
     * @return a GTFS DAO
     * @throws GtfsException
     *             unable to load the GTFS data
     */
    public static GtfsRelationalDao load(final File gtfsFile) throws GtfsException {

        return load(gtfsFile, null);
    }

    /**
     * Load a GTFS from a file and override agency identifiers.
     * 
     * @param gtfsFile
     *            the path to the GTFS file to load
     * @param agencyMappings
     *            a mapping for agency identifiers
     * @return a GTFS DAO
     * @throws GtfsException
     *             unable to load the GTFS data
     */
    public static GtfsRelationalDao load(final File gtfsFile, final Map<String, String> agencyMappings) throws GtfsException {

        try {
            final ZipFile file = new ZipFile(gtfsFile);
            final ZipFileCsvInputSource source = new ZipFileCsvInputSource(file);

            final GtfsRelationalDaoImpl gtfsDao = new GtfsRelationalDaoImpl();

            final GtfsReader reader = new GtfsReader();
            reader.setInputSource(source);
            reader.setEntityStore(gtfsDao);

            if (null != agencyMappings) {
                for (final Entry<String, String> mapping : agencyMappings.entrySet()) {
                    final String fromAgencyId = mapping.getKey();
                    final String toAgencyId = mapping.getValue();
                    reader.addAgencyIdMapping(fromAgencyId, toAgencyId);
                    LOGGER.debug("Mapping agency '{}' to '{}'", fromAgencyId, toAgencyId);
                }
            }
            reader.run();

            return gtfsDao;
        } catch (final ZipException e) {
            throw new GtfsException("Unable to open zip file" + e.getMessage(), e);
        } catch (final IOException e) {
            throw new GtfsException("Unable to read GTFS data" + e.getMessage(), e);
        }
    }
}
