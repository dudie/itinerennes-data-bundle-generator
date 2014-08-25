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
