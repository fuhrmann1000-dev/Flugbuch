-- phpMyAdmin SQL Dump
-- version 5.1.1deb5ubuntu1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Erstellungszeit: 02. Jan 2024 um 17:03
-- Server-Version: 8.0.35-0ubuntu0.22.04.1
-- PHP-Version: 8.1.2-1ubuntu2.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `flugbuch`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `flugbuch`
--

CREATE TABLE `flugbuch` (
  `id` int NOT NULL,
  `Datum` date NOT NULL,
  `Startzeit` time DEFAULT NULL,
  `Landezeit` time DEFAULT NULL,
  `Muster` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `Kennzeichen` varchar(6) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `Pilot` varchar(100) DEFAULT NULL,
  `Besatzung` int DEFAULT '1',
  `Gaeste` int DEFAULT '0',
  `Flugart` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `Startplatz` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT 'Altes Lager',
  `Zielplatz` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT 'Altes Lager',
  `Flugleiter` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '0',
  `Geschleppter` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `Schlepphoehe` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `Betrag` decimal(5,2) DEFAULT '0.00',
  `Bezahlt` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci,
  `BStart` decimal(6,2) DEFAULT '0.00',
  `BStop` decimal(6,2) DEFAULT '0.00',
  `Bemerkung` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `Anzahl` int DEFAULT '1',
  `Commit` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 PACK_KEYS=0;
