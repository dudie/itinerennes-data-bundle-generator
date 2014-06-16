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

import java.io.File;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class GtfsDaoTest {

    private static GtfsRelationalDao gtfsDao;

    @BeforeClass
    public static void loadGtfs() throws GtfsException {
        gtfsDao = GtfsUtils.load(new File("src/integration/resources/GTFS-20120618.zip"));
    }

    @Test
    public void whatIsTheFormOfStopIds() {
        System.out.println(ToStringBuilder.reflectionToString(gtfsDao.getAllStops().iterator().next(), ToStringStyle.MULTI_LINE_STYLE));
        // org.onebusaway.gtfs.model.Stop@309cbd4b[
        // id=1_1422
        // name=Merlin
        // lat=48.11628685
        // lon=-1.711464025
        // code=1422
        // desc=Rennes
        // zoneId=<null>
        // url=<null>
        // locationType=0
        // parentStation=<null>
        // wheelchairBoarding=0
        // direction=<null>
        // ]
    }

    @Test
    public void sampleFeedInfoOutput() {

        System.out.println("==== FEED_INFO ====");
        for (final FeedInfo fi : gtfsDao.getAllFeedInfos()) {
            System.out.println(ToStringBuilder.reflectionToString(fi, ToStringStyle.MULTI_LINE_STYLE));
        }
    }
}
