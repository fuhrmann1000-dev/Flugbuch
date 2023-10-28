<?php

require_once('DBC.php');
require_once('Flug.php');

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

        $statement = $this->db->prepare('SELECT * FROM flugbuch');
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
            . Flug::BESATZUNG . ', '
            . Flug::GAESTE . ', '
            . Flug::FLUGART . ', '
            . Flug::STARTPLATZ . ', '
            . Flug::ZIELPLATZ . ', '
            . Flug::FLUGLEITER . ', '
            . Flug::GESCHLEPPTER . ', '
            . Flug::SCHLEPPHOEHE . ', '
            . Flug::BETRAG . ', '
            . Flug::BEZAHLT . ', '
            . Flug::BEMERKUNG
            . ') '
            . 'VALUES ('
            . ':datum, '
            . ':startzeit, '
            . ':landezeit, '
            . ':muster, '
            . ':kennzeichen, '
            . ':pilot, '
            . ':besatzung, '
            . ':gaeste, '
            . ':flugart, '
            . ':startplatz, '
            . ':zielplatz, '
            . ':flugleiter, '
            . ':geschleppter, '
            . ':schlepphoehe, '
            . ':betrag, '
            . ':bezahlt, '
            . ':bemerkung '
            . ')';

        $statement = $this->db->prepare($sql);
        $statement->execute([
            Flug::DATUM => $flug->datum,
            Flug::STARTZEIT => $flug->startzeit,
            Flug::LANDEZEIT => $flug->landezeit,
            Flug::MUSTER => $flug->muster,
            Flug::KENNZEICHEN => $flug->kennzeichen,
            Flug::PILOT => $flug->pilot,
            Flug::BESATZUNG => $flug->besatzung,
            Flug::GAESTE => $flug->gaeste,
            Flug::FLUGART => $flug->flugart,
            Flug::STARTPLATZ => $flug->startplatz,
            Flug::ZIELPLATZ => $flug->zielplatz,
            Flug::FLUGLEITER => $flug->flugleiter,
            Flug::GESCHLEPPTER => $flug->geschleppter,
            Flug::SCHLEPPHOEHE => $flug->schlepphoehe,
            Flug::BETRAG => $flug->betrag,
            Flug::BEZAHLT => $flug->bemerkung
        ]);

        $flug = null;
        if ($row = $statement->fetch()) {
            $flug = $this->mapRowToFlug($row);

        }

        return $flug;
    }

    public function updateFlug(Flug $flug)
    {
        $sql = 'UPDATE flugbuch SET '
            . Flug::DATUM . '= :datum, '
            . Flug::STARTZEIT . ', = :startzeit, '
            . Flug::LANDEZEIT . ', = :landezeit, '
            . Flug::MUSTER . ',  = :muster, '
            . Flug::KENNZEICHEN . ', = :kennzeichen, '
            . Flug::PILOT . ', = :pilot, '
            . Flug::BESATZUNG . ', = :besatzung, '
            . Flug::GAESTE . ', = :gaeste, '
            . Flug::FLUGART . ', = :flugart, '
            . Flug::STARTPLATZ . ', = :startplatz, '
            . Flug::ZIELPLATZ . ', = :zielplatz, '
            . Flug::FLUGLEITER . ', = :flugleiter, '
            . Flug::GESCHLEPPTER . ', = :geschleppter, '
            . Flug::SCHLEPPHOEHE . ', = :schlepphoehe, '
            . Flug::BETRAG . ', = :betrag, '
            . Flug::BEZAHLT . ', = :bezahlt, '
            . Flug::BEMERKUNG . ', = :bemerkung '
            . 'WHERE ' . Flug::ID . '= :id';
            
        $statement = $this->db->prepare($sql);
        $statement->execute([
            Flug::DATUM => $flug->datum,
            Flug::STARTZEIT => $flug->startzeit,
            Flug::LANDEZEIT => $flug->landezeit,
            Flug::MUSTER => $flug->muster,
            Flug::KENNZEICHEN => $flug->kennzeichen,
            Flug::PILOT => $flug->pilot,
            Flug::BESATZUNG => $flug->besatzung,
            Flug::GAESTE => $flug->gaeste,
            Flug::FLUGART => $flug->flugart,
            Flug::STARTPLATZ => $flug->startplatz,
            Flug::ZIELPLATZ => $flug->zielplatz,
            Flug::FLUGLEITER => $flug->flugleiter,
            Flug::GESCHLEPPTER => $flug->geschleppter,
            Flug::BETRAG => $flug->betrag,
            Flug::BEZAHLT => $flug->bemerkung, 
            Flug::ID => $flug->id
        ]);

        $flug = null;
        if ($row = $statement->fetch()) {
            $flug = $this->mapRowToFlug($row);

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
        $flug->datum = $row[Flug::DATUM];
        $flug->startzeit = $row[Flug::STARTZEIT];
        $flug->landezeit = $row[Flug::LANDEZEIT];
        $flug->muster = $row[Flug::MUSTER];
        $flug->kennzeichen = $row[Flug::KENNZEICHEN];
        $flug->pilot = $row[Flug::PILOT];
        $flug->besatzung = $row[Flug::BESATZUNG];
        $flug->gaeste = $row[Flug::GAESTE];
        $flug->flugart = $row[Flug::FLUGART];
        $flug->startplatz = $row[Flug::STARTPLATZ];
        $flug->zielplatz = $row[Flug::ZIELPLATZ];
        $flug->flugleiter = $row[Flug::FLUGLEITER];
        $flug->geschleppter = $row[Flug::GESCHLEPPTER];
        $flug->schlepphoehe = $row[Flug::SCHLEPPHOEHE];
        $flug->betrag = $row[Flug::BETRAG];
        $flug->bezahlt = $row[Flug::BEZAHLT];
        $flug->bemerkung = $row[Flug::BEMERKUNG];

        return $flug;
    }
}