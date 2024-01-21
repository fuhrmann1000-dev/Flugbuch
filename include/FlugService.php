<?php

require_once(__DIR__ . '/Flug.php');
require_once(__DIR__ . '/FlugResult.php');
require_once(__DIR__ . '/FlugDB.php');


class FlugService
{

    private $flugDB;

    public function __construct()
    {
        $this->flugDB = new FlugDB();
        $this->flugDB->connect();
    }

    /**
     * hier noch die Filter einfügen, Paging und Sorting als Parameter hinzufügen
     */
    public function filterFluege()
    {
        $flugResult = new FlugResult();
        $flugResult->resultMessage = 'Die Flugdaten wurden geladen.';
        $flugResult->resultMessageType = FlugResult::INFO;

        $flugResult->fluege = $this->flugDB->filterFluege();
        return $flugResult;
    }

    public function insertFlug(Flug $flug)
    {
        $flugResult = $this->validateFlug($flug);
        $flugResult->flug = $flug;

        if (FlugResult::ERROR != $flugResult->resultMessageType) {

            $flugResult->flug = $this->flugDB->insertFlug($flug);
            $flugResult->resultMessage = 'Die Flugdaten wurden gespeichert.';
            $flugResult->resultMessageType = FlugResult::INFO;
        } else {
            $flugResult->resultMessage .= 'Die Flugdaten konnten nicht gespeichert werden.';
        }

        return $flugResult;
    }

    public function updateFlug(Flug $flug)
    {
        $flugResult = $this->validateFlug($flug);
        $flugResult->flug = $flug;

        if (FlugResult::ERROR != $flugResult->resultMessageType) {
            try {
                $flugResult->flug = $this->flugDB->updateFlug($flug);
                $flugResult->resultMessageType = FlugResult::INFO;
            $flugResult->resultMessage = 'Die Flugdaten wurden aktualisiert.';
            } catch (DBException $ex) {
                // Hier mache ich jetzt meine Fehlerbehandlung weil die DB nicht da war
                $flugResult->resultMessageType = FlugResult::ERROR;
                $flugResult->resultMessage = $ex->getMessage();
            }
            
            
        } else {
            $flugResult->resultMessageType = FlugResult::ERROR;
            $flugResult->resultMessage .= 'Die Flugdaten konnten nicht aktualisiert werden.';
        }

        return $flugResult;
    }

    private function validateFlug(Flug $flug)
    {
        $flugResult = new FlugResult();

        if (!isset($flug->gaeste) || 0 == strlen($flug->gaeste)) {
            $flug->gaeste = '0';
        }
        if (0 == strlen($flug->betrag)) {
            $flug->betrag = '0.0';
        }
        if (!isset($flug->besatzung) || 0 == strlen($flug->besatzung)) {
            $flug->besatzung = '0';
        }
        

        $datum = $flug->datum;
        if (isset($datum) && $this->validateDate($datum, 'd.m.Y')) {

        } else {
            $flugResult->resultMessage .= 'Es wurde kein g&uuml;ltiges Datum angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        $startzeit = $flug->startzeit;
        if (isset($startzeit) && $this->validateDate($startzeit, 'H:i')) {

        } else {
            $flugResult->resultMessage .= 'Es wurde keine g&uuml;ltige Startzeit angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        $landezeit = $flug->landezeit;
        if (isset($landezeit) && $this->validateDate($landezeit, 'H:i')) {

        } else {
            $flugResult->resultMessage .= 'Es wurde keine g&uuml;ltige Landezeit angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        $muster = $flug->muster;
        if (isset($muster) && 0 < strlen($muster)) {

        } else {
            $flugResult->resultMessage .= 'Es wurde kein g&uuml;ltiges Muster angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        $kennzeichen = $flug->kennzeichen;
        if (isset($kennzeichen) && 0 < strlen($kennzeichen)) {

        } else {
            $flugResult->resultMessage .= 'Es wurde kein g&uuml;ltiges Kennzeichen angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        $pilot = $flug->pilot;
        if (isset($pilot) && 0 < strlen($pilot)) {

        } else {
            $flugResult->resultMessage .= 'Es wurde kein g&uuml;ltiger Pilot angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        $flugart = $flug->flugart;
        if (isset($flugart) && 0 < strlen($flugart)) {

        } else {
            $flugResult->resultMessage .= 'Es wurde keine g&uuml;ltige Flugart angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        $startplatz = $flug->startplatz;
        if (isset($startplatz) && 0 < strlen($startplatz)) {

        } else {
            $flugResult->resultMessage .= 'Es wurde kein g&uuml;ltiger Startplatz angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        $zielplatz = $flug->zielplatz;
        if (isset($zielplatz) && 0 < strlen($zielplatz)) {

        } else {
            $flugResult->resultMessage .= 'Es wurde kein g&uuml;ltiger Zielplatz angegeben.<br/>';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }

        return $flugResult;
    }

    private function validateDate($date, $format = 'Y-m-d H:i:s')
    {
        $d = DateTime::createFromFormat($format, $date);
        return $d && $d->format($format) == $date;
    }

    public function selectFlugById($id)
    {
        $flugResult = new FlugResult();
        $flugResult->resultMessage = 'Die Flugdaten wurden aus der Datenbank abgerufen.';
        $flugResult->resultMessageType = FlugResult::INFO;

        $flugResult->flug = $this->flugDB->selectFlugById($id);

        return $flugResult;
    }

    public function deleteFlugById($id)
    {
        $flugResult = new FlugResult();
        $flugResult->resultMessage = 'Die Flugdaten wurden entfernt.';
        $flugResult->resultMessageType = FlugResult::INFO;

        $this->flugDB->deleteFlugById($id);

        return $flugResult;
    }

}