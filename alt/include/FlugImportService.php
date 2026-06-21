<?php

require_once (__DIR__ . '/File.php');
require_once (__DIR__ . '/FlugImport.php');
require_once (__DIR__ . '/Flug.php');
require_once (__DIR__ . '/FlugImportResult.php');
require_once (__DIR__ . '/FieldStatus.php');
require_once (__DIR__ . '/FlugImportDB.php');
require_once (__DIR__ . '/FlugService.php');
require_once (__DIR__ . '/FlugResult.php');


class FlugImportService
{

    private $flugImportDB;

    private $flugService;

    public function __construct()
    {
        $this->flugImportDB = new FlugImportDB();
        $this->flugService = new FlugService();
    }

    public function importData($fileName)
    {
        $flugImportResult = new FlugImportResult();
        $file = new File();

        $row = 1;
        if (($handle = fopen($file->folder . '/' . $fileName, "r")) !== FALSE) {

            while (($data = fgetcsv($handle, null, ";")) !== FALSE) {
                if (1 == $row) { // Kopfzeile überspringen
                    $row++;
                    continue;
                }
                $flugImport = new FlugImport();

                $flugImport->importId = $data[0];
                $flugImport->datum = $data[1];
                $flugImport->pilot = $data[2];
                $flugImport->kennzeichen = $data[3];
                $flugImport->muster = $data[4];
                $flugImport->startzeit = $data[5];
                $flugImport->landezeit = $data[6];
                $flugImport->flugart = $data[7];
                $flugImport->geschleppter = $data[8];
                $flugImport->schlepphoehe = $data[9];
                $flugImport->flugleiter = $data[10];
                $flugImport->startplatz = $data[11];
                $flugImport->zielplatz = $data[12];
                $flugImport->gaeste = $data[13];

                $existingFlugImport = $this->flugImportDB->selectFlugImportByImportId($flugImport->importId);
                if (null == $existingFlugImport) {
                    $this->flugImportDB->insertFlug($flugImport);
                }

                $row++;
            }
            fclose($handle);
        }

        return $flugImportResult;
    }

    public function pullImportedData()
    {
        $flugResult = new FlugResult();

        $flugImportList = $this->flugImportDB->selectNonTransferredFlugImports();
        foreach ($flugImportList as $flugImport) {
            $flug = $this->mapFlugImportToFlug($flugImport);
            $flugResult = $this->flugService->insertFlug($flug);
            $this->flugImportDB->setFlugImportTransferred($flugImport->importId);
        }

        return $flugResult;
    }

    private function mapFlugImportToFlug(FlugImport $flugImport)
    {
        $flug = new Flug();

        $flug->datum = $flugImport->datum;
        $flug->pilot = $flugImport->pilot;
        $flug->kennzeichen = $flugImport->kennzeichen;
        $flug->muster = $flugImport->muster;
        $flug->startzeit = $flugImport->startzeit;
        $flug->landezeit = $flugImport->landezeit;
        $flug->flugart = $flugImport->flugart;
        $flug->geschleppter = $flugImport->geschleppter;
        $flug->schlepphoehe = $flugImport->schlepphoehe;
        $flug->flugleiter = $flugImport->flugleiter;
        $flug->startplatz = $flugImport->startplatz;
        $flug->zielplatz = $flugImport->zielplatz;
        $flug->gaeste = $flugImport->gaeste;

        return $flug;
    }
}
