package fr.itinerennes.bundler.cli;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public final class GtfsFileOptionHandlerTest {

    private static File directory;

    private static File invalidFile;

    private static File gtfsFile;

    @Mock
    private CmdLineParser parser;

    @Mock
    private OptionDef option;

    @Mock
    private Setter<? super GtfsRelationalDao> setter;

    @Mock
    private Parameters params;

    private GtfsFileOptionHandler handler;

    @BeforeClass
    public static void prepare() throws IOException {

        directory = File.createTempFile("junit-", "-itr.tmp");
        directory.delete();
        directory.mkdir();

        invalidFile = File.createTempFile("junit-", "-itr.tmp");

        gtfsFile = File.createTempFile("junit-", "-itr.tmp.zip");
        final InputStream gtfsIn = GtfsFileOptionHandlerTest.class.getResourceAsStream("gtfs.zip");
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
        handler = new GtfsFileOptionHandler(parser, option, setter);
    }

    @Test(expected = CmdLineException.class)
    public void testNullParamValue() throws CmdLineException {

        when(params.getParameter(anyInt())).thenReturn(null);
        handler.parseArguments(params);
    }

    @Test(expected = CmdLineException.class)
    public void testUnexistingFile() throws CmdLineException {

        when(params.getParameter(anyInt())).thenReturn(
                "/this/file/shouln't/exist/on/the/filesystem");
        handler.parseArguments(params);
    }

    @Test(expected = CmdLineException.class)
    public void testDirectory() throws CmdLineException {

        when(params.getParameter(anyInt())).thenReturn(directory.getAbsolutePath());
        handler.parseArguments(params);
    }

    @Test(expected = CmdLineException.class)
    public void testInvalidFile() throws CmdLineException {

        when(params.getParameter(anyInt())).thenReturn(invalidFile.getAbsolutePath());
        handler.parseArguments(params);
    }

    @Test
    public void testGtfsFile() throws CmdLineException {

        when(params.getParameter(anyInt())).thenReturn(gtfsFile.getAbsolutePath());
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {

                final GtfsRelationalDao gtfsDao = (GtfsRelationalDao) invocation.getArguments()[0];

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

                return null;
            }
        }).when(setter).addValue(any(GtfsRelationalDao.class));

        handler.parseArguments(params);

        verify(setter, times(1));
    }
}
