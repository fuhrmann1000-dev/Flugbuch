<?php

require_once(__DIR__ . '/DBC.php');
require_once(__DIR__ . '/DBException.php');
require_once(__DIR__ . '/Flug.php');

/**
 * FIXME: die Aufrufe noch alle mit try-catch-Blöcken versehen, es kann ja etwas schief gehen
 */
class FlugDB extends DBC
{
    public function __construct()
    {
        parent::connect();
    }

    /**
     * hier noch die Filter einfügen, Paging und Sorting als Parameter hinzufügen
     */
    public function filterFluege()
    {
        /**
         * Beispielaufruf
         * $statement = $db->prepare("SELECT * FROM users WHERE vorname = :vorname AND nachname = :nachname");
         * $statement->execute(array(':vorname' => 'Max', ':nachname' => 'Mustermann'));
         * while ($row = $statement->fetch()) {
         * echo $row['vorname'] . " " . $row['nachname'] . "<br />";
         * echo "E-Mail: " . $row['email'] . "<br /><br />";
         * }
         */
        $sql = 'SELECT * FROM flugbuch '
            . 'WHERE Datum >= DATE_ADD(CURRENT_DATE, INTERVAL -1 MONTH) '
            . 'ORDER BY Datum DESC';

        $statement = $this->db->prepare($sql);
        $statement->execute();


        $fluege = [];
        while ($row = $statement->fetch()) {
            $flug = $this->mapRowToFlug($row);

            $fluege[] = $flug;
        }

        return $fluege;
    }

    public function insertFlug(Flug $flug)
    {
        $sql = 'INSERT INTO flugbuch ('
            . Flug::DATUM . ', '
            . Flug::STARTZEIT . ', '
            . Flug::LANDEZEIT . ', '

            . Flug::MUSTER . ', '
            . Flug::KENNZEICHEN . ', '
            . Flug::PILOT . ', '

            . Flug::FLUGANZAHL . ', '
            . Flug::GAESTE . ', '
            . Flug::FLUGART . ', '

            . Flug::STARTPLATZ . ', '
            . Flug::ZIELPLATZ . ', '
            . Flug::FLUGLEITER . ', '

            . Flug::GESCHLEPPTER . ', '
            //            . Flug::ANZAHL . ', '
            . Flug::SCHLEPPHOEHE . ', '

            . Flug::BETRAG . ', '
            //            . Flug::BEZAHLT . ', '
            . Flug::BEMERKUNG
            . ') '
            . 'VALUES ('
            . ':datum, '
            . ':startzeit, '
            . ':landezeit, '

            . ':muster, '
            . ':kennzeichen, '
            . ':pilot, '

            . ':fluganzahl, '
            . ':gaeste, '
            . ':flugart, '

            . ':startplatz, '
            . ':zielplatz, '
            . ':flugleiter, '

            . ':geschleppter, '
            //            . ':anzahl, '
            . ':schlepphoehe, '

            . ':betrag, '
            //. ':bezahlt, '
            . ':bemerkung '
            . ')';

        $statement = $this->db->prepare($sql);

        $dateInDataBaseFormat = $this->convertDateToMySQLFormat($flug->datum);
        $statement->bindValue(':datum', $dateInDataBaseFormat);
        $statement->bindValue(':startzeit', $flug->startzeit);
        $statement->bindValue(':landezeit', $flug->landezeit);

        $statement->bindValue(':muster', $flug->muster);
        $statement->bindValue(':kennzeichen', $flug->kennzeichen);
        $statement->bindValue(':pilot', $flug->pilot);

        $statement->bindValue(':fluganzahl', $flug->fluganzahl);
        $statement->bindValue(':gaeste', $flug->gaeste);
        $statement->bindValue(':flugart', $flug->flugart);

        $statement->bindValue(':startplatz', $flug->startplatz);
        $statement->bindValue(':zielplatz', $flug->zielplatz);
        $statement->bindValue(':flugleiter', $flug->flugleiter);

        $statement->bindValue(':geschleppter', $flug->geschleppter);
        //        $statement->bindValue(':anzahl', $flug->anzahl);
        $statement->bindValue(':schlepphoehe', $flug->schlepphoehe);

        $statement->bindValue(':betrag', $flug->betrag);
        //$statement->bindValue(':bezahlt', $flug->bezahlt);
        $statement->bindValue(':bemerkung', $flug->bemerkung);

        try {
            $statement->execute();
        } catch (PDOException $ex) {
            throw new DBException('Beim Speichern der Flugdaten ist ein Fehler aufgetreten.' . $ex->getMessage());
        }

        $id = $this->lastInsertId();
        $flug->id = $id;

        return $flug;
    }

    public function updateFlug(Flug $flug)
    {
        $sql = 'UPDATE flugbuch SET '
            . Flug::DATUM . '= :datum, '
            . Flug::STARTZEIT . ' = :startzeit, '
            . Flug::LANDEZEIT . ' = :landezeit, '
            . Flug::MUSTER . ' = :muster, '
            . Flug::KENNZEICHEN . ' = :kennzeichen, '
            . Flug::PILOT . ' = :pilot, '
            . Flug::FLUGANZAHL . ' = :fluganzahl, '
            . Flug::GAESTE . ' = :gaeste, '
            . Flug::FLUGART . ' = :flugart, '
            . Flug::STARTPLATZ . ' = :startplatz, '
            . Flug::ZIELPLATZ . ' = :zielplatz, '
            . Flug::FLUGLEITER . ' = :flugleiter, '
            . Flug::GESCHLEPPTER . ' = :geschleppter, '
            //            . Flug::ANZAHL . ', = :anzahl, '
            . Flug::SCHLEPPHOEHE . ' = :schlepphoehe, '
            . Flug::BETRAG . ' = :betrag, '
            . Flug::BEZAHLT . ' = :bezahlt, '
            . Flug::BEMERKUNG . ' = :bemerkung '
            . 'WHERE ' . Flug::ID . '= :id';



        $statement = $this->db->prepare($sql);
        $dateInDataBaseFormat = $this->convertDateToMySQLFormat($flug->datum);
        $statement->bindValue(':datum', $dateInDataBaseFormat);
        $statement->bindValue(':startzeit', $flug->startzeit);
        $statement->bindValue(':landezeit', $flug->landezeit);

        $statement->bindValue(':muster', $flug->muster);
        $statement->bindValue(':kennzeichen', $flug->kennzeichen);
        $statement->bindValue(':pilot', $flug->pilot);

        $statement->bindValue(':fluganzahl', $flug->fluganzahl);
        $statement->bindValue(':gaeste', $flug->gaeste);
        $statement->bindValue(':flugart', $flug->flugart);

        $statement->bindValue(':startplatz', $flug->startplatz);
        $statement->bindValue(':zielplatz', $flug->zielplatz);
        $statement->bindValue(':flugleiter', $flug->flugleiter);

        $statement->bindValue(':geschleppter', $flug->geschleppter);
        //        $statement->bindValue(':anzahl', $flug->anzahl);
        $statement->bindValue(':schlepphoehe', $flug->schlepphoehe);

        $statement->bindValue(':betrag', $flug->betrag);
        $statement->bindValue(':bezahlt', $flug->bezahlt);
        $statement->bindValue(':bemerkung', $flug->bemerkung);

        $statement->bindValue(':id', $flug->id);
        
        try {
            $statement->execute();
        } catch (PDOException $ex) {
            throw new DBException('Beim Speicher der Flugdaten ist ein Fehler aufgetreten.' . $ex->getMessage());
        }

        return $flug;
    }

    public function selectFlugById($id)
    {
        $statement = $this->db->prepare('SELECT * FROM flugbuch WHERE id = :id');
        $statement->execute([
            'id' => $id
        ]);

        $flug = null;
        if ($row = $statement->fetch()) {
            $flug = $this->mapRowToFlug($row);
        }

        return $flug;
    }

    public function deleteFlugById($id)
    {
        $statement = $this->db->prepare('DELETE FROM flugbuch WHERE id = :id');
        $statement->execute([
            'id' => $id
        ]);
    }

    private function mapRowToFlug($row)
    {
        $flug = new Flug();

        $flug->id = $row[Flug::ID];
        $datum = $this->convertMySQLDateToGermanFormat($row[Flug::DATUM]);
        $flug->datum = $datum;
        $startzeit = substr($row[Flug::STARTZEIT], 0, 5); // Die Sekunden werfen wir weg
        $flug->startzeit = $startzeit;
        $landezeit = substr($row[Flug::LANDEZEIT], 0, 5);
        $flug->landezeit = $landezeit;
        $flug->muster = $row[Flug::MUSTER];
        $flug->kennzeichen = $row[Flug::KENNZEICHEN];
        $flug->pilot = $row[Flug::PILOT];
        $flug->fluganzahl = $row[Flug::FLUGANZAHL];
        $flug->gaeste = $row[Flug::GAESTE];
        $flug->flugart = $row[Flug::FLUGART];
        $flug->startplatz = $row[Flug::STARTPLATZ];
        $flug->zielplatz = $row[Flug::ZIELPLATZ];
        $flug->flugleiter = $row[Flug::FLUGLEITER];
        $flug->geschleppter = $row[Flug::GESCHLEPPTER];
        $flug->anzahl = 0;
        $flug->schlepphoehe = $row[Flug::SCHLEPPHOEHE];
        $flug->betrag = $row[Flug::BETRAG];
        $flug->bezahlt = $row[Flug::BEZAHLT];
        $flug->bemerkung = $row[Flug::BEMERKUNG];

        return $flug;
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