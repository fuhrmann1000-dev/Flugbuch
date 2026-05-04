package de.windenshelter.flugbuch.service;

public class ExcelImportException extends RuntimeException {
    public ExcelImportException(String nachricht, Throwable ursache) {
        super(nachricht, ursache);
    }
}