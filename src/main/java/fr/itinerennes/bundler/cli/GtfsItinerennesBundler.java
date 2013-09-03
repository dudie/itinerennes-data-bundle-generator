package fr.itinerennes.bundler.cli;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.itinerennes.bundler.gtfs.GtfsException;
import fr.itinerennes.bundler.gtfs.GtfsUtils;
import fr.itinerennes.onebusaway.bundle.tasks.GenerateMarkersCsvTask;
import fr.itinerennes.onebusaway.bundle.tasks.GenerateRoutesAndStopsCsvTask;
import fr.itinerennes.onebusaway.bundle.tasks.GenerateTripsCsvTask;

/**
 * @author Jérémie Huchet
 */
public class GtfsItinerennesBundler {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsItinerennesBundler.class);

    @Option(name = "-k", aliases = "--keolis-api-key", usage = "Keolis API key", required = true, metaVar = "API_KEY")
    private String keolisApiKey;

    @Option(name = "-o", aliases = "--output", usage = "path to the output directory", required = false, metaVar = "OUTPUT")
    private File output;

    @Option(name = "-am", aliases = "--agency-mapping", usage = "agency mapping: '1=2' means agency id '1' will be translated to '2'", required = false, metaVar = "AGENCY_MAPPING")
    private Map<String, String> agencyMapping;

    @Option(name = "-h", aliases = "--help", usage = "this help", required = false)
    private boolean help;

    @Argument(usage = "path to the GTFS file", required = true, metaVar = "GTFS")
    private File gtfsFile;

    /**
     * @param args
     */
    public static void main(final String[] args) throws IOException {

        new GtfsItinerennesBundler().doMain(args);
    }

    private void doMain(final String[] args) throws IOException {

        final CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            if (help) {
                printUsageAndExit(parser, 0);
            }

            if (output == null) {
                output = new File(".");
            }
            if (output.exists() && output.isFile()) {
                throw new CmdLineException(parser, output + " already exists and is a regular file");
            } else if (!output.exists()) {
                output.mkdirs();
            }
            LOGGER.info("output will be written to {}", output.toURI());

        } catch (final CmdLineException e) {
            System.err.println(e.getMessage());
            printUsageAndExit(parser, 1);
        }

        try {
            // load GTFS data
            final GtfsRelationalDao gtfsDao = GtfsUtils.load(gtfsFile, agencyMapping);

            // execute tasks
            new GenerateMarkersCsvTask(gtfsDao, output, keolisApiKey).run();
            new GenerateRoutesAndStopsCsvTask(gtfsDao, output).run();
            new GenerateRoutesAndStopsCsvTask(gtfsDao, output).run();
            new GenerateTripsCsvTask(gtfsDao, output).run();
        } catch (final GtfsException e) {
            LOGGER.error("Unable to load GTFS data", e);
        }
    }

    private void printUsageAndExit(final CmdLineParser parser, final int status) {

        System.err.println(String.format(
                "java %s <GTFS> -k <API_KEY> [-o <OUTPUT>] [-am <AGENCY_MAPPING>]", this.getClass()
                        .getName()));
        parser.printUsage(System.err);
        System.exit(1);
    }
}
