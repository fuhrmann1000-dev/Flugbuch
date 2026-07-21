<?php

require_once (__DIR__ . '/Flug.php');

class FlugImport extends Flug
{
    const IMPORT_ID = 'import_id';
    
    public $importId;
    
    public function __construct()
    {
    }
}