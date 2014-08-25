package fr.itinerennes.bundler.cli;

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

import java.io.IOException;
import java.util.zip.ZipFile;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.onebusaway.csv_entities.ZipFileCsvInputSource;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import fr.itinerennes.commons.utils.StringUtils;

/**
 * @author Jérémie Huchet
 */
public final class GtfsFileOptionHandler extends OptionHandler<GtfsRelationalDao> {

    public GtfsFileOptionHandler(final CmdLineParser parser, final OptionDef option,
            final Setter<? super GtfsRelationalDao> setter) {

        super(parser, option, setter);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.kohsuke.args4j.spi.OptionHandler#parseArguments(org.kohsuke.args4j.spi.Parameters)
     */
    @Override
    public int parseArguments(final Parameters params) throws CmdLineException {

        final String gtfsFilePath = params.getParameter(0);
        if (StringUtils.isBlank(gtfsFilePath)) {
            throw new CmdLineException(owner, option.metaVar() + " can't be empty");
        }
        try {
            final ZipFile file = new ZipFile(gtfsFilePath);
            final ZipFileCsvInputSource source = new ZipFileCsvInputSource(file);

            final GtfsRelationalDaoImpl gtfsDao = new GtfsRelationalDaoImpl();

            final GtfsReader reader = new GtfsReader();
            reader.setInputSource(source);
            reader.setEntityStore(gtfsDao);
            reader.run();

            setter.addValue(gtfsDao);
            return 1;
        } catch (final IOException e) {
            throw new CmdLineException(owner, "Can't read GTFS data from file " + gtfsFilePath, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.kohsuke.args4j.spi.OptionHandler#getDefaultMetaVariable()
     */
    @Override
    public String getDefaultMetaVariable() {

        return "GTFS_FILE";
    }

}
