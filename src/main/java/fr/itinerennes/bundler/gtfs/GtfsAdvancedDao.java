package fr.itinerennes.bundler.gtfs;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GtfsAdvancedDao {

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

                final List<ServiceCalendarDate> exceptions = gtfs.getCalendarDatesForServiceId(serviceId);
                final List<ServiceDate> additions = new ArrayList<ServiceDate>();
                final List<ServiceDate> removals = new ArrayList<ServiceDate>();
                for (final ServiceCalendarDate scd : exceptions) {
                    if (1 == scd.getExceptionType()) {
                        additions.add(scd.getDate());
                    } else if (2 == scd.getExceptionType()) {
                        removals.add(scd.getDate());
                    }
                }

                if (start.compareTo(date) <= 0 && end.compareTo(date) >= 0 && (!removals.contains(date) || additions.contains(date))) {
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
}
