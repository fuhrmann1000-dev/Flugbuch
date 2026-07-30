package de.windenshelter.flugbuch.service.ftp;

import java.nio.file.Path;

import de.windenshelter.flugbuch.service.FtpDownloadException;

/**
 * Fetches the daily flight log CSV from wherever it lives (FTP today,
 * possibly FTPS/SFTP later) and saves it to the local import volume.
 *
 * Kept as an interface, separate from the real network call, specifically
 * so the scheduler that uses this can be unit-tested with a fake
 * implementation instead of a real FTP server.
 */
public interface FtpFileFetcher {

    /**
     * Downloads the current flight log CSV to the local import directory
     * and returns the path it was saved to.
     *
     * @throws FtpDownloadException if the connection, login, or download fails
     */
    Path fetchLatestFile();
}
