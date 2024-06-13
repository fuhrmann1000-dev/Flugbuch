<?php

require_once(__DIR__ . '/Flug.php');
require_once(__DIR__ . '/FieldStatus.php');
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
            $flugResult->flug = $this->flugDB->updateFlug($flug);
            $flugResult->resultMessageType = FlugResult::INFO;
            $flugResult->resultMessage = 'Die Flugdaten wurden aktualisiert.';
        } else {
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
        if (!isset($flug->fluganzahl) || 0 == strlen($flug->fluganzahl)) {
            $flug->fluganzahl = '0';
        }


        $datum = $flug->datum;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::DATUM;
        if (isset($datum) && $this->validateDateFormat($datum, 'd.m.Y')) {
            $fieldStatus->messageType = FieldStatus::INFO;
            $fieldStatus->message = 'Das Datum wurde richtig eingegeben.';
            $datumIsValid = true;
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde kein g&uuml;ltiges Datum angegeben.';
            $datumIsValid = false;
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::DATUM] = $fieldStatus;

        // teste, ob das Flugdatum kleiner gleich heute ist
        if ($datumIsValid) {
            $now = new DateTime();
            $heute = $now->format('Ymd');
            $timestamp = strtotime($datum); // 13.01.2024 -> 2024-01-13
            $flugTag = date('Ymd', $timestamp);
            $fieldStatus = new FieldStatus();
            if ($flugTag <= $heute) {
                $fieldStatus->messageType = FieldStatus::INFO;
                $fieldStatus->message = 'Das Datum wurde richtig eingegeben.';
            } else {
                $fieldStatus->messageType = FieldStatus::ERROR;
                $fieldStatus->message = 'Das angegebene Flugdatum liegt in der Zukunft.';
                $flugResult->resultMessageType = FlugResult::ERROR;
            }
            $flugResult->fieldStatusList[Flug::DATUM] = $fieldStatus;
        }

        $startzeit = $flug->startzeit;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::STARTZEIT;
        if (isset($startzeit) && $this->validateDateFormat($startzeit, 'H:i')) {
            $fieldStatus->messageType = FieldStatus::INFO;
            $fieldStatus->message = 'Die Startzeit wurde richtig eingegeben.';
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde keine g&uuml;ltige Startzeit angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::STARTZEIT] = $fieldStatus;

        $landezeit = $flug->landezeit;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::LANDEZEIT;
        if (isset($landezeit) && $this->validateDateFormat($landezeit, 'H:i')) {
            $fieldStatus->messageType = FieldStatus::INFO;
            $fieldStatus->message = 'Die Landezeit wurde richtig eingegeben.';
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde keine g&uuml;ltige Landezeit angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::LANDEZEIT] = $fieldStatus;

        //if (startzeit == gültig und landezeite == gültig) {
        $start = DateTime::createFromFormat('H:i', $startzeit);
        $landung = DateTime::createFromFormat('H:i', $landezeit);

        // teste, ob das Startzeit kleiner ist als Landezeit
        if ($start <= $landung) {
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Die Landezeit ist vor der Startzeit.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        //  }

        $muster = $flug->muster;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::MUSTER;
        if (isset($muster) && 0 < strlen($muster)) {
            $fieldStatus->messageType = FieldStatus::INFO;
            $fieldStatus->message = 'Das Muster wurde richtig eingegeben.';
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde keine g&uuml;ltiges Muster angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::MUSTER] = $fieldStatus;

        $kennzeichen = $flug->kennzeichen;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::KENNZEICHEN;
        if (isset($kennzeichen) && 6 == strlen($kennzeichen)) {
            $fieldStatus->messageType = FieldStatus::INFO;
            $fieldStatus->message = 'Das Kennzeichen wurde richtig eingegeben.';
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde kein g&uuml;ltiges Kennzeichen angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::KENNZEICHEN] = $fieldStatus;


        $pilot = $flug->pilot;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::PILOT;
        if (isset($pilot) && 0 < strlen($pilot)) {
            $fieldStatus->messageType = FieldStatus::INFO;
            $fieldStatus->message = 'Der Pilot wurde richtig eingegeben.';
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde kein g&uuml;ltiger Pilot angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::PILOT] = $fieldStatus;

        //Fluganzahl und Gaeste abfrage?
        $fluganzahl = $flug->fluganzahl;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::FLUGANZAHL;
        if ($fluganzahl > 0) {
            $fieldStatus->messageType = FieldStatus::INFO;
            $fieldStatus->message = 'Die Fluganzahl wurde richtig eingegeben.';
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde keine g&uuml;ltige Fluganzahl angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::FLUGANZAHL] = $fieldStatus;

        $gaeste = $flug->gaeste;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::GAESTE;
        if ($gaeste < 3) {
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde kein g&uuml;ltiger Gast angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::GAESTE] = $fieldStatus;

        $flugart = $flug->flugart;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::FLUGART;
        if (isset($flugart) && 0 < strlen($flugart)) {
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde kein g&uuml;ltige Flugart angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::FLUGART] = $fieldStatus;

        $startplatz = $flug->startplatz;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::STARTPLATZ;
        if (isset($startplatz) && 0 < strlen($startplatz)) {
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde kein g&uuml;ltiger Startplatz angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::STARTPLATZ] = $fieldStatus;

        $zielplatz = $flug->zielplatz;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::ZIELPLATZ;
        if (isset($zielplatz) && 0 < strlen($zielplatz)) {
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde kein g&uuml;ltiger Zielplatz angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::ZIELPLATZ] = $fieldStatus;

        $flugleiter = $flug->flugleiter;
        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = Flug::FLUGLEITER;
        if (isset($flugleiter) && 0 < strlen($flugleiter)) {
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde kein g&uuml;ltiger FLUGLEITER angegeben.';
            $flugResult->resultMessageType = FlugResult::ERROR;
        }
        $flugResult->fieldStatusList[Flug::FLUGLEITER] = $fieldStatus;




        return $flugResult;
    }

    private function validateDateFormat($date, $format = 'Y-m-d H:i:s')
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
