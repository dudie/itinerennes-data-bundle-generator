package fr.itinerennes.bundler.cli;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import fr.itinerennes.bundler.gtfs.GtfsException;
import fr.itinerennes.bundler.gtfs.GtfsUtils;

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

        final GtfsRelationalDao gtfsDao = GtfsUtils.load(gtfsFile);

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

    }
}
