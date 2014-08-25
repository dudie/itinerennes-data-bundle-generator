package fr.itinerennes.bundler.tasks.framework;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
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
public abstract class AbstractCsvTask<T> extends AbstractTask {

    /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateMarkersCsvTask.class);

    /** The UTF-8 charset. */
    public static final Charset CHARSET = Charset.forName("UTF-8");

    /** The empty string. */
    protected static final String EMPTY = "";

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
        final File outFile = getOutputFile();
        LOGGER.debug("Writing output to {}", outFile);
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), CHARSET));
            final List<T> data = getData();
            LOGGER.debug("Writing {} lines to {}", CollectionUtils.size(data), outFile);
            final List<Object[]> lines = new ArrayList<Object[]>(data.size());
            for (final T d : data) {
                lines.add(toCSV(d));
            }
            Collections.sort(lines, new Comparator<Object[]>() {

                @Override
                public int compare(final Object[] o1, final Object[] o2) {
                    return toString(o1).compareTo(toString(o2));
                }

                private String toString(final Object[] data) {
                    final StringBuilder s = new StringBuilder();
                    for (final Object o : data) {
                        if (o != null) {
                            s.append(o.toString());
                        }
                    }
                    return s.toString();
                }
            });
            for (final Object[] lineData : lines) {
                writeLine(lineData);
            }
        } catch (final FileNotFoundException e) {
            LOGGER.error("output file not found", e);
        } catch (final IOException e) {
            LOGGER.error("can't write to output file", e);
        } catch (final CsvTaskException e) {
            LOGGER.error("can't generate CSV data", e);
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
    private void writeLine(final Object... values) throws IOException {
        final StringBuilder csv = new StringBuilder();
        for (final Object v : values) {
            csv.append(v).append(';');
        }
        out.write(csv.toString());
        out.newLine();
    }

    /**
     * Subclasses should implement this method and return one bean for each CSV line to produce.
     */
    protected abstract List<T> getData() throws CsvTaskException;

    /**
     * Subclasses should implement this method to define how to convert one bean to CSV values.
     */
    protected abstract Object[] toCSV(T data) throws CsvTaskException;
}
