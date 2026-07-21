<?php

require_once (__DIR__ . '/File.php');
require_once (__DIR__ . '/FlugImport.php');
require_once (__DIR__ . '/FieldStatus.php');
require_once (__DIR__ . '/FileResult.php');

class FileService
{
    public function __construct()
    {
 
    }

    public function uploadFile()
    {
        $fileResult = new FileResult();
        $file = new File();
        $error = false;

        $targetFile = $file->folder . '/' . basename($_FILES[File::FILE_NAME]["name"]);

        if (move_uploaded_file($_FILES[File::FILE_NAME]["tmp_name"], $targetFile)) {
            $fileResult->resultMessage = 'Die Flugdaten wurden hochgeladen.';
            $fileResult->resultMessageType = FileResult::INFO;

        } else {
            $fileResult->resultMessage = 'Beim hochladen der Datei auf den Server ist ein Fehler aufgetreten.';
            $fileResult->resultMessageType = FileResult::ERROR;
            $error = true;
        }

        $fieldStatus = new FieldStatus();
        $fieldStatus->fieldName = File::FILE_NAME;
        if (!$error) {
            $file->fileName = basename($_FILES[File::FILE_NAME]["name"]);
            $fileResult->file = $file;
        } else {
            $fieldStatus->messageType = FieldStatus::ERROR;
            $fieldStatus->message = 'Es wurde keine Datei angegeben oder das Dateiformat war nicht gültig.';
            $fileResult->resultMessageType = FileResult::ERROR;
        }
        $fileResult->fieldStatusList[File::FILE_NAME] = $fieldStatus;

        return $fileResult;
    }
}
