<?php

class FieldStatus {
    const INFO = 'info';
    const WARNING = 'warning';
    const ERROR = 'error';

    public $fieldName;
    public $messageType;
    public $message;

    public function __construct()
    {
        $this->messageType = self::INFO;
        $this->messageType = '';
    }
}