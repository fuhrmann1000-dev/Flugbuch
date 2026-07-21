<?php

require_once (__DIR__ . '/Flug.php');
require_once (__DIR__ . '/FlugService.php');
require_once (__DIR__ . '/FlugResult.php');


class FlugExportService
{
    
    private $flugService;

    public function __construct()
    {
        $this->flugService = new FlugService();
    }

    public function exportData()
    {
        $flugresult = $this->flugService->filterFluege();
        $fluege = $flugresult->fluege;
        
        $fluegeAsArray = [];

        $headline = array('Datum',
                        'Startzeit',
                        'Landezeit',
                        'Muster',
                        'Kennzeichen',
                        'Pilot',
                        'Gäste',
                        'Flugart',
                        'Startplatz',
                        'Zielplatz',
                        'Flugleiter',
                        'Geschleppter',
                        'Schlepphöhe',
                        'Betrag',
                        'Bemerkung',
                        'Fluganzahl');

        $fluegeAsArray [] = $headline;

        foreach($fluege as $flug){
            $flugAsArray = [];

            $flugAsArray[] = $flug->datum;
            $flugAsArray[] = $flug->startzeit;
            $flugAsArray[] = $flug->landezeit;
            $flugAsArray[] = $flug->muster;
            $flugAsArray[] = $flug->kennzeichen;
            $flugAsArray[] = $flug->pilot;
            $flugAsArray[] = $flug->gaeste;
            $flugAsArray[] = $flug->flugart;
            $flugAsArray[] = $flug->startplatz;
            $flugAsArray[] = $flug->zielplatz;
            $flugAsArray[] = $flug->flugleiter;
            $flugAsArray[] = $flug->geschleppter;
            $flugAsArray[] = $flug->schlepphoehe;
            $flugAsArray[] = $flug->betrag;
            $flugAsArray[] = $flug->bemerkung;
            $flugAsArray[] = $flug->fluganzahl;

            $fluegeAsArray[] = $flugAsArray;
        }

        $this->arrayToCSVDownload($fluegeAsArray);
    }

    function arrayToCSVDownload($array, $filename = "export.csv", $delimiter=";") {
        // open raw memory as file so no temp files needed, you might run out of memory though
        $f = fopen('php://memory', 'w'); 
        // loop over the input array
        foreach ($array as $line) { 
            // generate csv lines from the inner arrays
            fputcsv($f, $line, $delimiter); 
        }
        // reset the file pointer to the start of the file
        fseek($f, 0);
        // tell the browser it's going to be a csv file
        header('Content-Type: text/csv');
        // tell the browser we want to save it instead of displaying it
        header('Content-Disposition: attachment; filename="'.$filename.'";');
        // make php send the generated csv lines to the browser
        fpassthru($f);
    }

}
