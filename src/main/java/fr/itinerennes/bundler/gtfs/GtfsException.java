package fr.itinerennes.bundler.gtfs;

/**
 * @author Jérémie Huchet
 */
public class GtfsException extends Exception {

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    public GtfsException(final String message, final Exception cause) {

        super(message, cause);
    }
}
