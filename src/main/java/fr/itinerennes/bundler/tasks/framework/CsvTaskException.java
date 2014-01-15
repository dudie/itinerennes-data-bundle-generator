package fr.itinerennes.bundler.tasks.framework;

/**
 * @author Jeremie Huchet
 */
public class CsvTaskException extends Exception {

    private static final long serialVersionUID = -5798584179460164952L;

    public CsvTaskException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
