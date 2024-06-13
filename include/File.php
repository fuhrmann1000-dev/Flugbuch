<?php

class File
{
    
    const FOLDER = 'folder';
    const FILE_NAME = 'fileName';

    public $folder;
    public $fileName;

    public function __construct()
    {
        $this->folder = './imports';
        $this->fileName = '';
    }
}