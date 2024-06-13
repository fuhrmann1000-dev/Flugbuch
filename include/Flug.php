<?php

class Flug
{
    const ID = 'id';
    const DATUM = 'Datum';
    const STARTZEIT = 'Startzeit';
    const LANDEZEIT = 'Landezeit';
    const MUSTER = 'Muster';
    const KENNZEICHEN = 'Kennzeichen';
    const PILOT = 'Pilot';
    const FLUGANZAHL = 'Fluganzahl';
    const GAESTE = 'Gaeste';
    const FLUGART = 'Flugart';
    const STARTPLATZ = 'Startplatz';
    const ZIELPLATZ = 'Zielplatz';
    const FLUGLEITER = 'Flugleiter';
    const GESCHLEPPTER = 'Geschleppter';
    const ANZAHL = 'Anzahl';
    const SCHLEPPHOEHE = 'Schlepphoehe';
    const BETRAG = 'Betrag';
    const BEZAHLT = 'Bezahlt';
    const BEMERKUNG = 'Bemerkung';

    public $id;
    public $datum;
    public $startzeit;
    public $landezeit;
    public $muster;
    public $kennzeichen;
    public $pilot;
    public $fluganzahl;
    public $gaeste;
    public $flugart;
    public $startplatz;
    public $zielplatz;
    public $flugleiter;
    public $geschleppter;
    public $anzahl;
    public $schlepphoehe;
    public $betrag;
    public $bezahlt;
    public $bstart;
    public $bstop;
    public $bemerkung;

    public function __construct()
    {
        $this->datum = '';
        $this->startzeit = '';
        $this->landezeit = '';
        $this->muster = '';
        $this->kennzeichen = '';
        $this->pilot = '';
        $this->fluganzahl = 1;
        $this->gaeste = '0';
        $this->flugart = '';
        $this->startplatz = '';
        $this->zielplatz = '';
        $this->flugleiter = '';
        $this->geschleppter = '';
        $this->anzahl = '0';
        $this->schlepphoehe = '';
        $this->betrag = '0.0';
        $this->bezahlt = '0.0';
        $this->bstart = '';
        $this->bstop = '';
        $this->bemerkung = '';
    }
}