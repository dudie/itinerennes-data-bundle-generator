package fr.itinerennes.bundler.gtfs;

/*
 * [license]
 * Itinerennes data resources generator
 * ----
 * Copyright (C) 2013 - 2014 Dudie
 * ----
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
