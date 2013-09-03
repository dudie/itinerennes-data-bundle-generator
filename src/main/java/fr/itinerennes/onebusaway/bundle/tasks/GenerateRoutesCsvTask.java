package fr.itinerennes.onebusaway.bundle.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jérémie Huchet
 */
public class GenerateRoutesCsvTask implements Runnable {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateRoutesCsvTask.class);

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
    public GenerateRoutesCsvTask(final GtfsRelationalDao gtfsDao, final String output) {

        this.outputFile = new File(output, "routes.csv");
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

            // output routes count
            out.write(String.valueOf(gtfsDao.getAllRoutes().size()));
            out.newLine();

            for (final Route route : gtfsDao.getAllRoutes()) {

                out.write(route.getId().toString());
                out.write(';');
                out.write(route.getShortName());
                out.write(';');
                out.write(route.getLongName());
                out.write(';');
                out.write(route.getTextColor());
                out.write(';');
                out.write(route.getColor());
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
