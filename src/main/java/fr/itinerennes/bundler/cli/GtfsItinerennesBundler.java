package fr.itinerennes.bundler.cli;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.itinerennes.bundler.tasks.framework.AbstractTask;

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
    private Map<String, String> agencyMapping = new HashMap<String, String>();

    @Option(name = "-t", aliases = "--tasks", usage = "tasks to run", required = false, metaVar = "TASKS")
    private List<String> tasks = new ArrayList<String>();

    @Option(name = "-l", aliases = "--list-tasks", usage = "list available tasks", required = false)
    private boolean listTasks;

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

        ClassPathXmlApplicationContext bootCtx = null;
        ClassPathXmlApplicationContext ctx = null;
        try {
            LOGGER.info("Initialization...");
            // pre-context dynamically initialize some beans depending on program args
            bootCtx = new ClassPathXmlApplicationContext("classpath:/boot-context.xml");
            main.loadArguments(bootCtx.getBean("programArgs", Properties.class));
            bootCtx.getBean("agencyMapping", Map.class).putAll(main.agencyMapping);

            ctx = new ClassPathXmlApplicationContext(new String[] { "classpath:/application-context.xml" }, bootCtx);
            LOGGER.info("Application context initialization finished");

            if (main.listTasks) {
                main.listTasksAndExit(ctx);
            }

            final List<AbstractTask> selectedTasks = main.verifySelectedTasksExists(ctx);
            LOGGER.info("Start tasks execution...");
            main.execute(selectedTasks);
            LOGGER.info("Tasks execution finished");
        } finally {
            IOUtils.closeQuietly(ctx);
            IOUtils.closeQuietly(bootCtx);
        }
    }

    private void loadArguments(final Properties p) {
        for (final Field f : getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Option.class) || f.isAnnotationPresent(Argument.class)) {
                try {
                    final String name = String.format("program.args.%s", f.getName());
                    final String value = String.valueOf(f.get(this));
                    p.setProperty(name, value);
                    LOGGER.info("Adding property {}={} to application context", name, value);
                } catch (final Exception e) {
                    throw new RuntimeException("unable to retrieve field values", e);
                }
            }
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

    private void listTasksAndExit(final ClassPathXmlApplicationContext context) {
        System.out.println("Available tasks:");
        for (final String tName : context.getBeanNamesForType(AbstractTask.class)) {
            System.out.printf(" - %s\n", tName);
        }
        System.exit(0);
    }

    private List<AbstractTask> verifySelectedTasksExists(final ApplicationContext ctx) {
        if (tasks.isEmpty()) {
            tasks.addAll(Arrays.asList(ctx.getBeanNamesForType(AbstractTask.class)));
        }
        final List<AbstractTask> selectedTasks = new ArrayList<AbstractTask>();
        final List<String> missingTasks = new ArrayList<String>();
        for (final String tName : tasks) {
            if (ctx.containsBean(tName)) {
                selectedTasks.add(ctx.getBean(tName, AbstractTask.class));
                LOGGER.error("Added task '{}' in execution queue", tName);
            } else {
                missingTasks.add(tName);
                LOGGER.error("Task '{}' doesn't exist", tName);
            }
        }
        if (!missingTasks.isEmpty()) {
            System.exit(1);
        }
        return selectedTasks;
    }

    private <T extends Runnable> void execute(final Collection<T> tasks) throws IOException {
        // execute tasks
        for (final Runnable t : tasks) {
            LOGGER.info("Running task {}...", t.getClass().getSimpleName());
            t.run();
            LOGGER.info("Task {} finished", t.getClass().getSimpleName());
        }
    }

    private void printUsageAndExit(final CmdLineParser parser, final int status) {

        System.err.println(String.format("java %s <GTFS> -k <API_KEY> [-t TASKS] [-o <OUTPUT>] [-am <AGENCY_MAPPING>]", this.getClass().getName()));
        parser.printUsage(System.err);
        System.exit(status);
    }
}
