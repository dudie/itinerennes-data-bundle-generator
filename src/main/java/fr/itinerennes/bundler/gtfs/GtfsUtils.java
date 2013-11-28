package fr.itinerennes.bundler.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.onebusaway.csv_entities.ZipFileCsvInputSource;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.itinerennes.bundler.gtfs.keolis.KeolisExtensionsReader;
import fr.itinerennes.bundler.gtfs.keolis.KeolisGtfsDaoImpl;

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
    public static KeolisGtfsDaoImpl load(final File gtfsFile) throws GtfsException {

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
    public static KeolisGtfsDaoImpl load(final File gtfsFile, final Map<String, String> agencyMappings) throws GtfsException {

        try {
            final ZipFile file = new ZipFile(gtfsFile);
            final ZipFileCsvInputSource source = new ZipFileCsvInputSource(file);

            final KeolisGtfsDaoImpl gtfsDao = new KeolisGtfsDaoImpl();

            final KeolisExtensionsReader reader = new KeolisExtensionsReader(gtfsDao);
            reader.setInputSource(source);

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
