package de.windenshelter.flugbuch.configuration.ftp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import de.windenshelter.flugbuch.service.FtpDownloadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Plain-FTP implementation of {@link FtpFileFetcher} using Apache Commons
 * Net. If the upstream server ever turns out to speak FTPS or SFTP instead
 * of plain FTP, only this class needs to change (or be swapped for a
 * sibling implementation) — everything else depends on the
 * {@link FtpFileFetcher} interface, not on Commons Net directly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonsNetFtpFileFetcher implements FtpFileFetcher {

    private static final DateTimeFormatter LOCAL_FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FtpProperties ftpProperties;

    /** Connects, logs in, downloads the configured remote file, then always disconnects. */
    @Override
    public Path fetchLatestFile() {
        FTPClient ftpClient = new FTPClient();
        try {
            connectAndLogin(ftpClient);
            return downloadToLocalFile(ftpClient);
        } catch (IOException e) {
            throw new FtpDownloadException(
                    "Could not fetch flight log CSV from FTP server " + ftpProperties.getHost(), e);
        } finally {
            disconnectQuietly(ftpClient);
        }
    }

    /** Opens the FTP connection, logs in, and switches to passive/binary mode. */
    private void connectAndLogin(FTPClient ftpClient) throws IOException {
        ftpClient.connect(ftpProperties.getHost(), ftpProperties.getPort());
        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            throw new FtpDownloadException(
                    "FTP server " + ftpProperties.getHost() + " refused the connection (reply code " + replyCode + ")");
        }

        boolean loggedIn = ftpClient.login(ftpProperties.getUsername(), ftpProperties.getPassword());
        if (!loggedIn) {
            throw new FtpDownloadException("FTP login failed for user '" + ftpProperties.getUsername() + "'");
        }

        // Passive mode: the client opens the data connection instead of the
        // server, which is what makes FTP work through NAT/firewalls in
        // practice. Almost always what you want unless told otherwise.
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
    }

    /** Downloads the configured remote file into the local import directory, named with today's date. */
    private Path downloadToLocalFile(FTPClient ftpClient) throws IOException {
        Path localDirectory = Path.of(ftpProperties.getLocalDirectory());
        Files.createDirectories(localDirectory);

        String localFileName = "flugbuch_" + LocalDate.now().format(LOCAL_FILE_DATE_FORMAT) + ".csv";
        Path localFile = localDirectory.resolve(localFileName);

        try (OutputStream out = Files.newOutputStream(localFile)) {
            boolean success = ftpClient.retrieveFile(ftpProperties.getRemoteFilePath(), out);
            if (!success) {
                throw new FtpDownloadException(
                        "FTP server did not return " + ftpProperties.getRemoteFilePath()
                                + " (reply: " + ftpClient.getReplyString().trim() + ")");
            }
        }

        log.info("Downloaded flight log CSV from FTP ({}) to {}", ftpProperties.getRemoteFilePath(), localFile);
        return localFile;
    }

    /** Logs out and disconnects, only logging (not throwing) if that fails. */
    private void disconnectQuietly(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                log.warn("Error while disconnecting from FTP server {}: {}", ftpProperties.getHost(), e.getMessage());
            }
        }
    }
}
