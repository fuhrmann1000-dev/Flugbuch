<?php

class DB
{
    public $db;

    public function connect($dbServer, $dbUser, $dbPassword, $dbName)
    {
        try {
            $this->db = new PDO('mysql:host=' . $dbServer . ';dbname=' . $dbName . ';charset=utf8', $dbUser, $dbPassword);
        } catch (PDOException $ex) {
            # Konkrete Fehlermeldung $ex->getMessage() nur auf Testserver ausgeben, nicht "in Produktion":
            die('Die Datenbank ist momentan nicht erreichbar. ');
        }
    }

    public function execute($sql) {
        
    }
}