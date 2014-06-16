package fr.itinerennes.bundler.tasks.framework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeremie Huchet
 */
public abstract class AbstractCountedCsvTask<T> extends AbstractCsvTask<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCountedCsvTask.class);
    
    private int lineCount = 0;

    public AbstractCountedCsvTask(String filename) {
        super(filename);
    }

    @PostExec
    public void prependLineCount() throws IOException {

        LOGGER.debug("Inserting line count at file head: {}", lineCount);

        final File output = getOutputFile();
        final File source = File.createTempFile("itr-", output.getName(), output.getParentFile());
        source.delete();
        FileUtils.moveFile(output, source);

        InputStream from = null;
        BufferedWriter to = null;
        try {
            from = new FileInputStream(source);
            to = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), CHARSET));
            to.write(String.valueOf(lineCount));
            to.newLine();
            final LineIterator i = IOUtils.lineIterator(from, CHARSET.name());
            while (i.hasNext()) {
                to.write(i.next());
                to.newLine();
            }
        } finally {
            IOUtils.closeQuietly(from);
            IOUtils.closeQuietly(to);
            FileUtils.deleteQuietly(source);
        }
    }

    @Override
    protected final List<T> getData() throws CsvTaskException {
        final List<T> data = getDataList();
        this.lineCount = CollectionUtils.size(data);
        return data;
    }

    protected abstract List<T> getDataList() throws CsvTaskException;
}
