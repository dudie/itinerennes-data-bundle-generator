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
import static org.fest.assertions.Assertions.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import fr.itinerennes.bundler.gtfs.GtfsException;
import fr.itinerennes.bundler.gtfs.GtfsUtils;
import fr.itinerennes.bundler.gtfs.keolis.KeolisGtfsDaoImpl;

public final class GtfsUtilsTest {

    private static File directory;

    private static File invalidFile;

    private static File gtfsFile;

    @BeforeClass
    public static void prepare() throws IOException {

        directory = File.createTempFile("junit-", "-itr.tmp");
        directory.delete();
        directory.mkdir();

        invalidFile = File.createTempFile("junit-", "-itr.tmp");

        gtfsFile = File.createTempFile("junit-", "-itr.tmp.zip");
        final InputStream gtfsIn = GtfsUtilsTest.class.getResourceAsStream("gtfs.zip");
        IOUtils.copy(gtfsIn, new FileOutputStream(gtfsFile));
    }

    @AfterClass
    public static void finish() throws IOException {

        directory.delete();
        invalidFile.delete();
        gtfsFile.delete();
    }

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = GtfsException.class)
    public void testDirectory() throws GtfsException {

        GtfsUtils.load(directory);
    }

    @Test(expected = GtfsException.class)
    public void testInvalidFile() throws GtfsException {

        GtfsUtils.load(invalidFile);
    }

    @Test
    public void testGtfsFile() throws GtfsException {

        final KeolisGtfsDaoImpl gtfsDao = GtfsUtils.load(gtfsFile);

        assertEquals(1, gtfsDao.getAllAgencies().size());
        assertEquals(14, gtfsDao.getAllCalendarDates().size());
        assertEquals(28, gtfsDao.getAllCalendars().size());
        assertEquals(0, gtfsDao.getAllFareAttributes().size());
        assertEquals(0, gtfsDao.getAllFareRules().size());
        assertEquals(0, gtfsDao.getAllFeedInfos().size());
        assertEquals(0, gtfsDao.getAllFrequencies().size());
        assertEquals(0, gtfsDao.getAllPathways().size());
        assertEquals(68, gtfsDao.getAllRoutes().size());
        assertEquals(0, gtfsDao.getAllShapeIds().size());
        assertEquals(0, gtfsDao.getAllShapePoints().size());
        assertEquals(1365, gtfsDao.getAllStops().size());
        assertEquals(182666, gtfsDao.getAllStopTimes().size());
        assertEquals(0, gtfsDao.getAllTransfers().size());
        assertEquals(6809, gtfsDao.getAllTrips().size());
        assertEquals(23, gtfsDao.getAllAccessibleRoutes().size());

    }

    @Test
    public void testGtfsFileWithAgencyMapping() throws GtfsException {

        final Map<String, String> am = new HashMap<String, String>();
        am.put("1", "2");
        final GtfsRelationalDao gtfsDao = GtfsUtils.load(gtfsFile, am);

        assertThat(gtfsDao.getAgencyForId("1")).isNull();

        final Agency a2 = gtfsDao.getAgencyForId("2");
        assertThat(a2).isNotNull();
        assertThat(a2.getName()).isEqualTo("STAR");
    }
}
