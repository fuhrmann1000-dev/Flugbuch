<?php

require_once('Flug.php');
require_once('FieldStatus.php');

class FlugResult
{

    const INFO = 'info';
    const WARNING = 'warning';
    const ERROR = 'error';

    public $flug;
    public $fluege;
    public $resultMessage;
    public $resultMessageType;

    public $fieldStatusList;

    public function __construct()
    {
        $this->flug = new Flug();
        $this->resultMessage = '';
        $this->resultMessageType = self::INFO;

        $this->fieldStatusList = [];

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::DATUM] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::STARTZEIT] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::LANDEZEIT] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::MUSTER] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::KENNZEICHEN] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::PILOT] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::FLUGANZAHL] =$fieldStatus;
        
        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::GAESTE] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::FLUGART] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::STARTPLATZ] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::ZIELPLATZ] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::FLUGLEITER] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::GESCHLEPPTER] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::SCHLEPPHOEHE] =$fieldStatus;

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[Flug::BEMERKUNG] =$fieldStatus;
    }
}