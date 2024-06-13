<?php

require_once(__DIR__ . '/DBC.php');
require_once(__DIR__ . '/DBException.php');
require_once(__DIR__ . '/Flug.php');
require_once(__DIR__ . '/FlugImport.php');

/**
 * FIXME: die Aufrufe noch alle mit try-catch-Blöcken versehen, es kann ja etwas schief gehen
 */
class FlugImportDB extends DBC
{
    public function __construct()
    {
        parent::connect();
    }

    public function insertFlug(FlugImport $flugImport)
    {
        $sql = 'INSERT INTO csv_import ('
            . FlugImport::IMPORT_ID . ', '
            . Flug::DATUM . ', '
            . Flug::PILOT . ', '
            . Flug::KENNZEICHEN . ', '
            . Flug::MUSTER . ', '
            . Flug::STARTZEIT . ', '
            . Flug::LANDEZEIT . ', '
            . Flug::FLUGART . ', '
            . Flug::GESCHLEPPTER . ', '
            . Flug::SCHLEPPHOEHE . ', '
            . Flug::FLUGLEITER . ', '
            . Flug::STARTPLATZ . ', '
            . Flug::ZIELPLATZ . ', '
            . Flug::GAESTE
            . ') '
            . 'VALUES ('
            . ':importId, '
            . ':datum, '
            . ':pilot, '
            . ':kennzeichen, '
            . ':muster, '
            . ':startzeit, '
            . ':landezeit, '
            . ':flugart, '
            . ':geschleppter, '
            . ':schlepphoehe, '
            . ':flugleiter, '
            . ':startplatz, '
            . ':zielplatz, '
            . ':gaeste'
            . ')';

        $statement = $this->db->prepare($sql);

        $statement->bindValue(':importId', $flugImport->importId);
        $dateInDataBaseFormat = $this->convertDateToMySQLFormat($flugImport->datum);
        $statement->bindValue(':datum', $dateInDataBaseFormat);
        $statement->bindValue(':pilot', $flugImport->pilot);
        $statement->bindValue(':kennzeichen', $flugImport->kennzeichen);
        $statement->bindValue(':muster', $flugImport->muster);
        $statement->bindValue(':startzeit', $flugImport->startzeit);
        $statement->bindValue(':landezeit', $flugImport->landezeit);
        $statement->bindValue(':flugart', $flugImport->flugart);
        $statement->bindValue(':geschleppter', $flugImport->geschleppter);
        $statement->bindValue(':schlepphoehe', $flugImport->schlepphoehe);        
        $statement->bindValue(':flugleiter', $flugImport->flugleiter);
        $statement->bindValue(':startplatz', $flugImport->startplatz);
        $statement->bindValue(':zielplatz', $flugImport->zielplatz);
        $statement->bindValue(':gaeste', $flugImport->gaeste);

        try {
            $statement->execute();
        } catch (PDOException $ex) {
            throw new DBException('Beim Speichern der Flugdaten ist ein Fehler aufgetreten.' . $ex->getMessage());
        }

        $id = $this->lastInsertId();
        $flugImport->id = $id;

        return $flugImport;
    }

    public function selectFlugImportByImportId($importId)
    {
        $statement = $this->db->prepare('SELECT * FROM csv_import WHERE import_id = :importId');
        $statement->execute([
            'importId' => $importId
        ]);

        $flugImport = null;
        if ($row = $statement->fetch()) {
            $flugImport = $this->mapRowToFlugImport($row);
        }

        return $flugImport;
    }

    public function selectNonTransferredFlugImports()
    {
        $statement = $this->db->prepare('SELECT * FROM csv_import WHERE transferred = 0');
        $statement->execute();

        $flugImportList = [];
        while ($row = $statement->fetch()) {
            $flugImport = $this->mapRowToFlugImport($row);
            $flugImportList[] = $flugImport;
        }

        return $flugImportList;
    }

    public function setFlugImportTransferred($importId)
    {
        $statement = $this->db->prepare('UPDATE csv_import SET transferred = 1 WHERE import_id = :importId');
        $statement->execute([
            'importId' => $importId
        ]);
    }

    private function mapRowToFlugImport($row)
    {
        $flugImport = new FlugImport();

        $flugImport->id = $row[Flug::ID];
        $flugImport->importId = $row[FlugImport::IMPORT_ID];
        $datum = $this->convertMySQLDateToGermanFormat($row[Flug::DATUM]);
        $flugImport->datum = $datum;
        $flugImport->pilot = $row[Flug::PILOT];
        $flugImport->kennzeichen = $row[Flug::KENNZEICHEN];
        $flugImport->muster = $row[Flug::MUSTER];
        $startzeit = substr($row[Flug::STARTZEIT], 0, 5); // Die Sekunden werfen wir weg
        $flugImport->startzeit = $startzeit;
        $landezeit = substr($row[Flug::LANDEZEIT], 0, 5);
        $flugImport->landezeit = $landezeit;
        $flugImport->flugart = $row[Flug::FLUGART];
        $flugImport->geschleppter = $row[Flug::GESCHLEPPTER];
        $flugImport->schlepphoehe = $row[Flug::SCHLEPPHOEHE];
        $flugImport->flugleiter = $row[Flug::FLUGLEITER];
        $flugImport->startplatz = $row[Flug::STARTPLATZ];
        $flugImport->zielplatz = $row[Flug::ZIELPLATZ];
        $flugImport->gaeste = $row[Flug::GAESTE];

        return $flugImport;
    }

    protected function convertMySQLDateToGermanFormat($date)
    {
        $d = explode('-', $date);
        return sprintf('%02d.%02d.%04d', $d[2], $d[1], $d[0]);
    }

    protected function convertMySQLDateTimeToGermanFormat($dateTime)
    {
        $dateTimeArray = date_parse_from_format("Y-m-d H:i:s", $dateTime);
        return sprintf('%02d.%02d.%04d %02d:%02d:%02d', $dateTimeArray['day'], $dateTimeArray['month'], $dateTimeArray['year'], $dateTimeArray['hour'], $dateTimeArray['minute'], $dateTimeArray['second']);
    }

    // 13.01.2024 2024-01-13
    protected function convertDateToMySQLFormat($date)
    {
        $d = explode('.', $date);
        return sprintf('%04d-%02d-%02d', $d[2], $d[1], $d[0]);
    }

    protected function convertDateTimeToMySQLFormat($dateTime)
    {
        $dateTimeArray = date_parse_from_format("d.m.Y H:i:s", $dateTime);
        return sprintf('%04d-%02d-%02d %02d:%02d:%02d', $dateTimeArray['year'], $dateTimeArray['month'], $dateTimeArray['day'], $dateTimeArray['hour'], $dateTimeArray['minute'], $dateTimeArray['second']);
    }
}