package fr.itinerennes.bundler.cli;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.regex.Pattern;

import javax.management.StringValueExp;

import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.util.StringValueResolver;

import fr.itinerennes.bundler.gtfs.GtfsException;
import fr.itinerennes.bundler.gtfs.GtfsUtils;
import fr.itinerennes.bundler.tasks.framework.AbstractTask;
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

        final GtfsItinerennesBundler main = new GtfsItinerennesBundler();
        main.parseCmdLine(args);

        ClassPathXmlApplicationContext ctx = null;
        try {
        	ctx = new ClassPathXmlApplicationContext("classpath:/application-context.xml");
        	ctx.getBeanFactory().addEmbeddedValueResolver(new ProgramArgumentValueResolver(main));
        	ctx.start();
            final Collection<AbstractTask> tasks = ctx.getBeansOfType(AbstractTask.class).values();
            main.execute(tasks);
        } finally {
        	IOUtils.closeQuietly(ctx);
        }
    }

    private void parseCmdLine(final String[] args) {

        final CmdLineParser parser = new CmdLineParser(this);

        // parse the arguments
        try {
            parser.parseArgument(args);
        } catch (final CmdLineException e) {
            System.err.println(e.getMessage());
            printUsageAndExit(parser, 1);
        }

        // print usage
        if (help) {
            printUsageAndExit(parser, 0);
        }

        // default output folder is current directory
        if (output == null) {
            output = new File(".");
        }
        
        // ensure output is a directory
        if (output.exists() && output.isFile()) {
            System.err.println(output + " already exists and is a regular file");
            printUsageAndExit(parser, 1);
        } else if (!output.exists()) {
            output.mkdirs();
            LOGGER.info("output will be written to {}", output.toURI());
        }
    }

    private void execute(final Collection<AbstractTask> tasks) throws IOException {
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
        System.exit(status);
    }

	private static class ProgramArgumentValueResolver implements StringValueResolver {
		
		private static final Pattern ARG = Pattern.compile("^program\\.args\\.\\w+$");

		private final GtfsItinerennesBundler program;

		public ProgramArgumentValueResolver(final GtfsItinerennesBundler program) {
			this.program = program;
		}
		
		@Override
		public String resolveStringValue(final String key) {
			final String fieldName = key.replaceAll("^program\\.\\w{4}\\.", "");
			if (ARG.matcher(key).matches()) {
				try {
					return String.valueOf(program.getClass().getField(fieldName).get(program));
				} catch (final Exception e) {
					LOGGER.error("Can't resolve property value for key {}", key);
					return null;
				}
			} else {
				return null;
			}
		}
	}
}
