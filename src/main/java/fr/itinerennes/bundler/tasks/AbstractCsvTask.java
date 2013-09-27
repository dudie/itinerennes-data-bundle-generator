package fr.itinerennes.bundler.tasks;

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

import fr.itinerennes.bundler.tasks.framework.AbstractTask;
import fr.itinerennes.onebusaway.bundle.tasks.GenerateMarkersCsvTask;

public abstract class AbstractCsvTask extends AbstractTask {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateMarkersCsvTask.class);

    /** The UTF-8 charset. */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final String filename;

    /** The output file. */
    @Value("${program.args.output}")
    private String outputDir;

    private BufferedWriter out = null;

    public AbstractCsvTask(final String filename) {
        final Pattern csv = Pattern.compile("\\.csv", Pattern.CASE_INSENSITIVE);
        if (csv.matcher(filename).matches()) {
            this.filename = filename;
        } else {
            this.filename = String.format("%s.csv", filename);
        }
    }

    @Override
    protected final void execute() {
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputDir, filename)), CHARSET));
            out.write(String.valueOf(getLineCount()));
            out.newLine();
            generateLines();
        } catch (final FileNotFoundException e) {
            LOGGER.error("output file not found", e);
        } catch (final IOException e) {
            LOGGER.error("can't write to output file", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    protected void writeLine(final Object... values) throws IOException {
        final StringBuilder csv = new StringBuilder();
        for (final Object v : values) {
            csv.append(v).append(';');
        }
        out.write(csv.toString());
        out.newLine();
    }

    protected abstract int getLineCount();

    /**
     * Subclasses should invoke {@link #writeLine(Object...)}.
     */
    protected abstract void generateLines() throws IOException;
}
