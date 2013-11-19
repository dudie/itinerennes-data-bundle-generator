package fr.itinerennes.bundler.gtfs;

import java.util.Calendar;

import org.onebusaway.gtfs.model.ServiceCalendar;

public enum DayOfWeek {

    MONDAY(new Handler(Calendar.MONDAY) {

        @Override
        public boolean isServiceActive(final ServiceCalendar service) {
            return service.getMonday() == 1;
        }

    }),

    TUESDAY(new Handler(Calendar.TUESDAY) {

        @Override
        public boolean isServiceActive(final ServiceCalendar service) {
            return service.getTuesday() == 1;
        }

    }),

    WEDNESDAY(new Handler(Calendar.WEDNESDAY) {

        @Override
        public boolean isServiceActive(final ServiceCalendar service) {
            return service.getWednesday() == 1;
        }

    }),

    THURSDAY(new Handler(Calendar.THURSDAY) {

        @Override
        public boolean isServiceActive(final ServiceCalendar service) {
            return service.getThursday() == 1;
        }

    }),

    FRIDAY(new Handler(Calendar.FRIDAY) {

        @Override
        public boolean isServiceActive(final ServiceCalendar service) {
            return service.getFriday() == 1;
        }

    }),

    SATURDAY(new Handler(Calendar.SATURDAY) {

        @Override
        public boolean isServiceActive(final ServiceCalendar service) {
            return service.getSaturday() == 1;
        }

    }),

    SUNDAY(new Handler(Calendar.SUNDAY) {

        @Override
        public boolean isServiceActive(final ServiceCalendar service) {
            return service.getSunday() == 1;
        }

    });

    private final Handler h;

    private DayOfWeek(final Handler h) {
        this.h = h;
    }

    public static boolean isSameDay(final Calendar calendar, final ServiceCalendar service) {
        for (final DayOfWeek dow : values()) {
            if (dow.h.isSameDay(calendar) && dow.h.isServiceActive(service)) {
                return true;
            }
        }
        return false;
    }

    private static abstract class Handler {
        private final int dayOfWeek;

        public Handler(final int dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        abstract boolean isServiceActive(ServiceCalendar service);

        boolean isSameDay(Calendar calendar) {
            return calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek;
        }
    }
}
