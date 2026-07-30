package de.windenshelter.flugbuch.service;

/** Thrown when connecting to, logging into, or downloading from the FTP server fails. */
public class FtpDownloadException extends RuntimeException {

    public FtpDownloadException(String message) {
        super(message);
    }

    public FtpDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
