package de.windenshelter.flugbuch.service;

public class WinchImportException extends RuntimeException {

    public WinchImportException(String message) {
        super(message);
    }

    public WinchImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
