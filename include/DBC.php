<?php

require_once('config.php');


class DBC
{
    public $db;

    public function connect()
    {
        try {
            $dbHost = 'localhost';
            //$dbName = 'd03e79ea';
            //$dbUser = 'd03e79ea';

            $dbName = 'flugbuch';
            $dbUser = 'flugbuch';
            $dbPassword = 'TYVKjodHNkFYNV7wjnsB';
            $this->db = new PDO('mysql:host=' . $dbHost . ';dbname=' . $dbName . ';charset=utf8', $dbUser, $dbPassword);
        } catch (PDOException $ex) {
            # Konkrete Fehlermeldung $ex->getMessage() nur auf Testserver ausgeben, nicht "in Produktion":
            die('Die Datenbank ist momentan nicht erreichbar. ' . $ex->getMessage());
        }
    }

}