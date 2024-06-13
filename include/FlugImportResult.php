<?php

require_once('FlugImport.php');
require_once('FieldStatus.php');

class FlugImportResult
{

    const INFO = 'info';
    const WARNING = 'warning';
    const ERROR = 'error';

    public $flugImport;
    public $flugImportList;
    public $resultMessage;
    public $resultMessageType;

    public $fieldStatusList;

    public function __construct()
    {
        $this->flugImport = new FlugImport();
        $this->resultMessage = '';
        $this->resultMessageType = self::INFO;

        $this->fieldStatusList = [];

        
    }
}