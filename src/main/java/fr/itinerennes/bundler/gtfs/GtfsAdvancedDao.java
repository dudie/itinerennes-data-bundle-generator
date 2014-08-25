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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GtfsAdvancedDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsAdvancedDao.class);

    private GtfsRelationalDao gtfs;

    @Autowired
    public GtfsAdvancedDao(final GtfsRelationalDao gtfsDao) {
        this.gtfs = gtfsDao;
    }

    public ServiceDate getStartDate() {
        final List<ServiceDate> startDates = new ArrayList<ServiceDate>();
        for (final ServiceCalendar sc : gtfs.getAllCalendars()) {
            startDates.add(sc.getStartDate());
        }
        for (final ServiceCalendarDate scd : gtfs.getAllCalendarDates()) {
            startDates.add(scd.getDate());
        }
        Collections.sort(startDates);
        return startDates.get(0);
    }

    public ServiceDate getEndDate() {
        final List<ServiceDate> endDates = new ArrayList<ServiceDate>();
        for (final ServiceCalendar sc : gtfs.getAllCalendars()) {
            endDates.add(sc.getEndDate());
        }
        for (final ServiceCalendarDate scd : gtfs.getAllCalendarDates()) {
            endDates.add(scd.getDate());
        }
        Collections.sort(endDates);
        return endDates.get(endDates.size() - 1);
    }

    public Iterable<ServiceDate> getAllServiceDates() {
        final ServiceDate start = getStartDate();
        final ServiceDate end = getEndDate();
        return new Iterable<ServiceDate>() {

            @Override
            public Iterator<ServiceDate> iterator() {
                return new Iterator<ServiceDate>() {

                    private ServiceDate next = start;

                    @Override
                    public boolean hasNext() {
                        return next.compareTo(end) <= 0;
                    }

                    @Override
                    public ServiceDate next() {
                        final ServiceDate current = next;
                        next = next.next(TimeZone.getDefault());
                        return current;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public List<StopTime> getStopTimes(final Stop stop, final ServiceDate date) {

        final List<StopTime> stopTimes = new ArrayList<StopTime>(gtfs.getStopTimesForStop(stop));
        CollectionUtils.filter(stopTimes, new Predicate() {

            @Override
            public boolean evaluate(Object object) {
                final StopTime st = (StopTime) object;
                final AgencyAndId serviceId = st.getTrip().getServiceId();
                final ServiceCalendar sc = gtfs.getCalendarForServiceId(serviceId);
                final ServiceDate start = sc.getStartDate();
                final ServiceDate end = sc.getEndDate();

                if (isAdditionalException(date, st.getTrip())) {
                    return true;
                } else if (isRemovalException(date, sc)) {
                    return false;
                } else if (start.compareTo(date) <= 0 && end.compareTo(date) >= 0) {
                    final String agencyId = stop.getId().getAgencyId();
                    final TimeZone tz = GtfsAdvancedDao.this.getTimeZone(agencyId);
                    return DayOfWeek.isSameDay(date.getAsCalendar(tz), sc);
                } else {
                    return false;
                }
            }
        });
        return stopTimes;
    }

    public Trip getPreviousTrip(final Trip current, final ServiceDate date) {
        final StopTime tripFirst = gtfs.getStopTimesForTrip(current).get(0);
        final List<StopTime> stopTimes = getStopTimes(tripFirst.getStop(), date);
        final Iterator<StopTime> i = stopTimes.iterator();
        StopTime prev = null, result = null;
        while (result == null && i.hasNext()) {
            final StopTime st = i.next();
            if (st.equals(tripFirst)) {
                result = prev;
            } else {
                prev = st;
            }
        }
        return null == result ? null : result.getTrip();
    }

    public Trip getNextTrip(Trip current, ServiceDate date) {
        final StopTime tripFirst = gtfs.getStopTimesForTrip(current).get(0);
        final List<StopTime> stopTimes = getStopTimes(tripFirst.getStop(), date);
        final Iterator<StopTime> i = stopTimes.iterator();
        StopTime next = null;
        while (next == null && i.hasNext()) {
            final StopTime st = i.next();
            if (st.equals(tripFirst)) {
                if (i.hasNext()) {
                    next = i.next();
                }
            }
        }
        return null == next ? null : next.getTrip();
    }

    public TimeZone getTimeZone(final String agencyId) {
        final Agency agency = gtfs.getAgencyForId(agencyId);
        return TimeZone.getTimeZone(agency.getTimezone());
    }

    public List<ServiceCalendarDate> getAdditionalExceptionsForDate(final ServiceDate date) {
        final List<ServiceCalendarDate> exceptions = new ArrayList<ServiceCalendarDate>();
        for (final ServiceCalendarDate scd : gtfs.getAllCalendarDates()) {
            if (1 == scd.getExceptionType() && scd.getDate().equals(date)) {
                exceptions.add(scd);
            }
        }
        return exceptions;
    }

    public boolean isRemovalException(final ServiceDate date, final ServiceCalendar calendar) {
        return isException(2, date, calendar.getServiceId());
    }

    public boolean isAdditionalException(final ServiceDate date, final Trip trip) {
        return isException(1, date, trip.getServiceId());
    }

    private boolean isException(final int exceptionType, final ServiceDate date, final AgencyAndId serviceId) {
        final List<ServiceDate> exceptions = new ArrayList<ServiceDate>();
        for (final ServiceCalendarDate scd : gtfs.getCalendarDatesForServiceId(serviceId)) {
            if (exceptionType == scd.getExceptionType()) {
                exceptions.add(scd.getDate());
            }
        }
        return exceptions.contains(date);
    }
}
