package de.windenshelter.flugbuch.service;

public class SchleppbetriebImportException extends RuntimeException {

    public SchleppbetriebImportException(String message) {
        super(message);
    }

    public SchleppbetriebImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
