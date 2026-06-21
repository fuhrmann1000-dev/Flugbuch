<?php

require_once('File.php');
require_once('FieldStatus.php');

class FileResult
{

    const INFO = 'info';
    const WARNING = 'warning';
    const ERROR = 'error';

    public $file;
    public $files;
    public $resultMessage;
    public $resultMessageType;

    public $fieldStatusList;

    public function __construct()
    {
        $this->file = new File();
        $this->resultMessage = '';
        $this->resultMessageType = self::INFO;

        $this->fieldStatusList = [];

        $fieldStatus = new FieldStatus();
        $fieldStatus->messageType = FieldStatus::INFO;
        $fieldStatus->message = '';
        $this->fieldStatusList[File::FILE_NAME] =$fieldStatus;
    }
}