package fr.itinerennes.bundler.gtfs;

import static org.fest.assertions.Assertions.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class GtfsAdvancedDaoTest {

    private static GtfsRelationalDao gtfsDao;
    private GtfsAdvancedDao advancedDao;

    @BeforeClass
    public static void loadGtfs() throws GtfsException, URISyntaxException {
        final URL u = GtfsAdvancedDaoTest.class.getResource("/fr/itinerennes/bundler/cli/gtfs.zip");
        gtfsDao = GtfsUtils.load(new File(u.toURI()));
    }

    @Before
    public void setupDao() {
        advancedDao = new GtfsAdvancedDao(gtfsDao);
    }

    @Test
    public void testGetStartDate() {
        final Calendar cal = Calendar.getInstance();
        cal.set(2012, 4, 21);
        assertThat(advancedDao.getStartDate()).isEqualTo(new ServiceDate(cal));
    }

    @Test
    public void testGetEndDate() {
        final Calendar cal = Calendar.getInstance();
        cal.set(2012, 5, 17);
        assertThat(advancedDao.getEndDate()).isEqualTo(new ServiceDate(cal));
    }
    
    @Test
    public void testGetAllServiceDates() {
        int i = 0;
        for (final ServiceDate sd : advancedDao.getAllServiceDates()) {
            i++;
        }
        assertThat(i).isEqualTo(28);
    }
    
    @Test
    public void testGetStopTimesForStop() {
        final Calendar cal = Calendar.getInstance();
        cal.set(2012, 4, 21);
        final Stop stop = gtfsDao.getStopForId(new AgencyAndId("1", "1001"));
        final ServiceDate date = new ServiceDate(cal);
        final List<StopTime> stoptimes = advancedDao.getStopTimes(stop, date);
        assertThat(stoptimes).isNotEmpty();
        System.out.println(stop);
        System.out.println(stoptimes.size());
        for (final StopTime st : stoptimes) {
            System.out.println(st);
        }
        
    }
    
    @Test
    public void testGetTimeZone() {
        assertThat(advancedDao.getTimeZone("1")).isEqualTo(TimeZone.getTimeZone("Europe/Paris"));
    }
}
