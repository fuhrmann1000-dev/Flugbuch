<?php

require_once('Flug.php');

class FlugResult
{

    const INFO = 'info';
    const WARNING = 'warning';
    const ERROR = 'error';

    public $flug;
    public $fluege;
    public $resultMessage;
    public $resultMessageType;

    public function __construct()
    {
        $this->flug = new Flug();
        $this->fluege = [];
        $this->resultMessage = '';
        $this->resultMessageType = self::INFO;
    }
}