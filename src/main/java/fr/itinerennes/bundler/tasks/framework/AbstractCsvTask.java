package fr.itinerennes.bundler.tasks.framework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import fr.itinerennes.onebusaway.bundle.tasks.GenerateMarkersCsvTask;

/**
 * Helper to generate a CSV file.
 * 
 * @author Jeremie Huchet
 */
public abstract class AbstractCsvTask extends AbstractTask {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateMarkersCsvTask.class);

    /** The UTF-8 charset. */
    public static final Charset CHARSET = Charset.forName("UTF-8");

    /** The name of the generated file. */
    private final String filename;

    /** The output file. */
    @Value("${program.args.output}")
    private String outputDir;

    /** Writer to the output file. */
    private BufferedWriter out = null;

    /**
     * @param filename
     *            the final name of the generated file
     */
    public AbstractCsvTask(final String filename) {
        final Pattern csv = Pattern.compile("\\.csv$", Pattern.CASE_INSENSITIVE);
        if (csv.matcher(filename).find()) {
            this.filename = filename;
        } else {
            this.filename = String.format("%s.csv", filename);
        }
    }

    /**
     * <ol>
     * <li>opens the output file</li>
     * <li>write the total line count</li>
     * <li>invokes {@link #generateLines()} to let subclasses write lines, see {@link #writeLine(Object...)}</li>
     * </ol>
     * 
     * @see fr.itinerennes.bundler.tasks.framework.AbstractTask#execute()
     */
    @Override
    protected final void execute() {
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getOutputFile()), CHARSET));
            generateLines();
        } catch (final FileNotFoundException e) {
            LOGGER.error("output file not found", e);
        } catch (final IOException e) {
            LOGGER.error("can't write to output file", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    protected final File getOutputFile() {
        return new File(outputDir, filename);
    }

    /**
     * Subclasses should use this to write a line to the output file.
     * 
     * @param values
     *            the values to be written
     * @throws IOException
     */
    protected void writeLine(final Object... values) throws IOException {
        final StringBuilder csv = new StringBuilder();
        for (final Object v : values) {
            csv.append(v).append(';');
        }
        out.write(csv.toString());
        out.newLine();
    }

    /**
     * Subclasses should invoke {@link #writeLine(Object...)}.
     */
    protected abstract void generateLines() throws IOException;
}