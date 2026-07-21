# Flugbuch – Digitales Flughauptbuch

**Drachenflieger-Club Berlin (DCB) – Flugplatz Altes Lager**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.java.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![H2](https://img.shields.io/badge/H2-Dev%20%26%20Test-lightgrey.svg)](https://www.h2database.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

---

> **Status (Stand 2026-06):** Frühe Entwicklungsphase. Das Spring-Boot-Backend
> existiert mit **zwei Import-Pfaden** (Flugschul-Excel, Schleppkladde-CSV) auf
> **Staging-Tabellen**. Es gibt **noch keine** `Flug`-Haupttabelle, **keinen**
> REST-Controller und **keine** Authentifizierung. Dieses README trennt bewusst
> **[Implementiert](#-implementiert)** von **[Geplant](#-geplant)**, damit klar
> ist, was heute lauffähig ist und was Roadmap ist.

---

## 📋 Projektbeschreibung

Das **Flugbuch** ist ein digitales Flughauptbuch für den Drachenflieger-Club
Berlin (DCB) am Flugplatz Altes Lager. Es soll das bestehende PHP-System
(Legacy-Frontend im Repo unter `index.php`, `include/`, `assets/`) ablösen und
die Daten mehrerer Quellen in einem revisionssicheren Hauptbuch zusammenführen.

### Ziel

- Digitale Erfassung aller Flüge am Flugplatz Altes Lager
- Integration mehrerer Datenquellen (Hauptflugbuch, Schleppkladde, Flugschule)
- Revisionssichere Speicherung gemäß **GoBD** (Aufbewahrung über mehrere Jahre)
- Exportfunktionen für die **LuBB** (Gemeinsame Obere Luftfahrtbehörde
  Berlin-Brandenburg, Sitz Schönefeld)
- Rollenbasierter Zugriff (Pilot, Flugleiter, Admin)

### Rechtlicher Rahmen

Das System stützt sich auf drei Ebenen. Die genaue Ausgestaltung (insbesondere
die Genehmigungsauflagen des Platzes) ist vor Produktivbetrieb mit der **LuBB**
abzustimmen.

**1. Pflicht zur Führung des Flughauptbuchs – § 53 LuftVZO**
> § 53 LuftVZO ("Anzuwendende Vorschriften") erklärt für Landeplätze – und über
> weitere Verweise auch für Segelfluggelände – die Flughafen-Vorschriften für
> anwendbar (u. a. die Pflichten des Landeplatzhalters nach § 41 Abs. 1,
> § 43 Abs. 1, §§ 44, 45 LuftVZO). Das operative Führen und die Nachweispflicht
> des Flughauptbuchs ergibt sich daraus i. V. m. den **Genehmigungen des
> Platzes durch die Landesbehörde (LuBB)** sowie den Richtlinien des Bundes und
> der Länder über die Führung von Flughauptbüchern.

**2. Geforderte Inhalte – NfL-Richtlinien / Genehmigungsauflagen**
> Welche Felder erfasst werden müssen, geben die standardisierten Vorgaben der
> Länder vor (historisch basierend auf den **Nachrichten für Luftfahrer, NfL**),
> konkretisiert durch die Genehmigung des jeweiligen Platzes. Siehe
> [geforderte Pflichtfelder](#geforderte-pflichtfelder-pro-flug-haupttabelle-flugbuch).

**3. Digitale Führung & Revisionssicherheit – GoBD (analog)**
> Die LuftVZO definiert "Revisionssicherheit" nicht mathematisch. Die
> Anerkennung digitaler Systeme stützt sich analog auf die **GoBD** (Grundsätze
> zur ordnungsgemäßen Führung und Aufbewahrung von Büchern … in elektronischer
> Form). Die Luftfahrtbehörden nutzen diese Kriterien als Maßstab für
> "ordnungsgemäße digitale Buchführung". Daraus folgen die drei Kernforderungen:
> - **Nachvollziehbarkeit & Nachprüfbarkeit** → Verfahrensdokumentation
> - **Wahrheit, Klarheit, fortlaufende Aufzeichnung** → direktes Loggen beim Flug
> - **Unveränderbarkeit** → Einträge dürfen nicht rückwirkend spurlos
>   überschrieben werden (Audit-Trail + Hash-Verkettung, siehe
>   [Compliance & GoBD](#-compliance--gobd-geplant))

> ℹ️ *Hinweis zur Historie:* Frühere Entwürfe zitierten **LuftVO §17/§31**
> (Luftsperrgebiete bzw. Flugverkehrskontrolle) und **LuftVZO §21c**
> (UAS-/Drohnenzulassung) – beide regeln **nicht** die Flughauptbuchführung und
> sind hiermit korrigiert.

<a id="geforderte-pflichtfelder-pro-flug-haupttabelle-flugbuch"></a>
### Geforderte Pflichtfelder pro Flug (Haupttabelle `flugbuch`)

> **Noch nicht als Entity umgesetzt** – Zielbild für die geplante `Flug`-Entity.
> Felder nach NfL-Richtlinien / Genehmigungsauflagen (siehe
> [Rechtlicher Rahmen](#rechtlicher-rahmen)).

| Feld | Beschreibung | Beispiel |
|------|--------------|----------|
| `laufendeNummer` | Laufende Nummer des Fluges (fortlaufend) | `1`, `2`, … |
| `datum` | Flugdatum | `2026-03-17` |
| `kennzeichen` | Luftfahrzeug: Kennzeichen | `D-MVBO` |
| `muster` | Luftfahrzeug: Typ/Muster | `ASK 21`, `Ultraleicht` |
| `pilot` | Verantwortlicher Pilot (ggf. Fluglehrer/Fluggast) | `Mustermann, Max` |
| `startart` | Startart | `Winde`, `F-Schlepp`, `Eigenstart` |
| `startzeit` | Startzeit (gesetzliche Zeit, hier Ortszeit) | `13:40` |
| `landezeit` | Landezeit (≥ Startzeit) | `13:50` |
| `startplatz` | Startplatz | `Altes Lager` |
| `zielplatz` | Landeplatz (falls Überlandflug) | `Altes Lager`, `EDCS` |
| `anzahl` | Anzahl der Landungen (Landegebühren/Lärmschutz) | `1` |
| `bemerkung` | Besondere Vorkommnisse | `Starker Wind` |

Weitere geplante Felder (vereinsspezifisch / Schlepp): `besatzung`, `gaeste`,
`flugleiter`, `geschleppter`, `schlepphoehe`, `betrag`, `bezahlt`,
`barogrammStart`, `barogrammStop`, `createdAt`, `updatedAt`.

> ℹ️ **Zeitzone:** Die NfL-Vorgaben nennen "gesetzliche Zeit" (UTC oder
> Ortszeit). Dieses System verwendet durchgängig **Ortszeit** (kein UTC) – diese
> Festlegung muss in der Verfahrensdokumentation eindeutig dokumentiert werden.

---

## ✅ Implementiert

Was heute im Backend lauffähig und getestet ist:

### Datenquellen (Import auf Staging-Tabellen)

| Quelle | Format | Service | Staging-Entity |
|--------|--------|---------|----------------|
| **Schleppkladde** | CSV (`;`-separiert) von [schleppbetrieb.de](https://schleppbetrieb.de) | `SchleppbetriebImportService` | `StagingSchleppbetriebEintrag` |
| **Flugschule** | Excel (`.xlsm`, mit Makros) | `ExcelImportService` (Apache POI) | `StagingSchleppkladdeEintrag` |

**Schleppkladde-CSV-Import** (`SchleppbetriebImportService`):
- Dependency-freier Parser für das `;`-separierte Format mit
  Anführungszeichen-Behandlung (z. B. Pilotenname `"Nachname, Vorname"`).
- Deutsches Datumsformat `dd.MM.yyyy HH:mm` → `LocalDateTime`.
- Robust gegen Eigenheiten echter Exporte: **UTF-8-BOM**, gequotetes
  `Zeitpunkt`-Feld, internationale Namen (UTF-8 end-to-end).
- **Idempotenter** Re-Import über `external_id` (UNIQUE): eine Massenabfrage
  `findExistingExternalIds(IN :ids)` je Chunk statt einer Existenzabfrage pro
  Zeile (kein N+1), In-Memory-Dedup innerhalb derselben Eingabe.
- Neue Eintraege erhalten `status = PENDING`.

**Flugschul-Excel-Import** (`ExcelImportService`):
- Parst die Datenblätter der `.xlsm`-Datei via Apache POI; Makros werden
  ignoriert.
- Re-Import über `importiereMitUeberschreiben` (delete-then-save).

> Beide Import-Pfade sind aktuell **nur auf Service-Ebene** vorhanden und werden
> über Tests aufgerufen – es gibt **noch keinen** REST-Endpoint, der sie über
> HTTP erreichbar macht.

### Tooling / Infrastruktur

- **Docker Compose** (`backend/docker-compose.yml`): MySQL 8.0 + Spring-Boot-App.
- **Tests**: Unit (Mockito) + Integration (`@SpringBootTest` gegen **H2
  in-memory**) – kein Docker für die Tests selbst nötig.
- **JaCoCo** (0.8.13): Coverage-Check in der `verify`-Phase, Schwelle **80 %**.

---

## 🧭 Geplant

Roadmap (grob nach Priorität, vgl. `CLAUDE.md` und `next-steps.md`):

1. **REST-Endpoints für die Importe** (`POST /api/import/csv`,
   `POST /api/import/excel`).
2. **JPA-Entity `Flug`** (Haupttabelle `flugbuch`) + Merge Staging → `flugbuch`
   mit Duplikat-Check (Status `MERGED`).
3. **Manuelle-Eingabe-CRUD** (`/api/fluege`) – POC liegt auf Branch
   `manuelle-eingabe-poc`, noch nicht auf `main`.
4. **REST-Controller + GlobalExceptionHandler** (Validierung → 422, nicht
   gefunden → 404).
5. **Flyway** einrichten und von `ddl-auto: update` auf `validate` umstellen.
6. **Audit-Trail** (Wer/Wann/Feld/Alt/Neu) als eigene Tabelle.
7. **Tagesabschluss** (Festschreibung eines Flugtages, danach nur markierte
   Korrekturbuchungen).
8. **Kryptografische Verkettung** (SHA-256-Chain pro Eintrag, Blockchain-Prinzip).
9. **Authentifizierung** (Spring Security) mit Rollenkonzept Pilot / Flugleiter /
   Admin.
10. **Export** für die LuBB: PDF (Papier-Flughauptbuch-Stil) + CSV/Excel.
11. **Produktions-DB auf MariaDB** umstellen (Treiber/URL; Import-Code ist
    bereits dialekt-neutral gebaut).
12. **Frontend**: Entscheidung SPA (z. B. Angular) vs. Thymeleaf, dann Anbindung
    an die API.

> Die früher in diesem README dokumentierten Endpunkte, Spring Security, Flyway,
> Testcontainers und eine `audit_log`/`users`-Tabelle existieren **noch nicht** –
> sie sind Teil dieser Roadmap.

---

## 🏗️ Architektur

### Überblick

Klassische geschichtete Spring-Boot-Architektur. **Fett** = vorhanden,
*kursiv* = geplant:

```
        CSV / Excel                              HTTP (geplant)
            │                                         │
            ▼                                         ▼
  ┌───────────────────┐                     ┌──────────────────┐
  │   **Service**     │ ◀────────────────── │  *Controller*    │
  │  (Import-Logik)   │                     │   (REST API)     │
  └─────────┬─────────┘                     └──────────────────┘
            │   ▲
   *Mapper* │   │ *DTO*  (MapStruct vorhanden, noch ungenutzt)
            ▼   │
  ┌───────────────────┐        ┌────────────────────────────────┐
  │  **Repository**   │ ─────▶ │   MySQL 8.0 (prod) / H2 (dev)   │
  │ (Spring Data JPA) │        │  staging_schleppbetrieb_eintrag │
  └─────────┬─────────┘        │  staging_schleppkladde_eintrag  │
            │                  │  *flugbuch* (Haupttabelle, gepl.)│
            ▼                  │  *audit_log* (geplant)          │
  ┌───────────────────┐        └────────────────────────────────┘
  │   **Model**       │
  │  (JPA-Entities)   │
  └───────────────────┘
```

### Package-Struktur (`de.windenshelter.flugbuch`)

```
backend/src/main/java/de/windenshelter/flugbuch/
├── FlugbuchApplication.java     # Spring-Boot-Entry-Point
├── model/                       # JPA-Staging-Entities (vorhanden)
│   ├── StagingSchleppbetriebEintrag   # Schleppkladde-CSV
│   └── StagingSchleppkladdeEintrag    # Flugschul-Excel
├── repository/                  # Spring Data JPA Repositories (vorhanden)
├── service/                     # Import-Logik (vorhanden)
│   ├── SchleppbetriebImportService / -Exception
│   └── ExcelImportService / -Exception
├── mapper/                      # MapStruct-Mapper (Gerüst)
└── dto/                         # DTOs (Gerüst)

# geplant: controller/ (REST), entity/ (Flug-Haupttabelle)
```

> Die im alten README erwähnten Verzeichnisse `controller/`, `entity/`,
> `config/`, `exception/`, `db/migration/` sowie `application-dev.yml`/
> `application-prod.yml` existieren **noch nicht**.

### Architektur-Prinzipien

- **Staging-First-Import:** Rohdaten landen zunächst unverändert in
  quellbezogenen Staging-Tabellen (Status `PENDING` → `VALIDATED` → `MERGED` /
  `QUARANTINED`). Das Zusammenführen in die `flugbuch`-Haupttabelle ist ein
  separater, prüfbarer Schritt (geplant) – nicht der Import selbst.
- **Idempotenz über stabile Schlüssel:** Wo die Quelle eine stabile ID liefert
  (Schleppkladde `external_id`, UNIQUE), wird der Re-Import über eine
  Existenzprüfung idempotent gehalten statt über delete-then-save.
- **Dialekt-neutrale Persistenz:** Idempotenz via JPQL + App-Logik, **kein**
  MySQL-spezifisches `INSERT ... ON DUPLICATE KEY`. So betrifft der spätere
  Wechsel der Produktions-DB auf MariaDB nur Treiber/URL, nicht den Code.
- **Revisionssicherheit als Querschnitt (geplant):** Audit-Trail, Festschreibung
  (Tagesabschluss) und Hash-Verkettung werden als zentrale, nicht umgehbare
  Mechanismen ergänzt, nicht als Feature einzelner Endpunkte.

---

## 🎨 Coding Style & Konventionen

- **Sprachmix (bewusst):** Geschäfts-/Import-Logik verwendet **deutsche**
  Methoden- und Feldnamen (`importiereAusStream`, `verarbeiteZeile`,
  `zeitpunkt`), weil die Fachdomäne deutsch ist. **Klassennamen** sind
  englisch/quellbeschreibend (`SchleppbetriebImportService`). Deutsche
  Feldnamen im Schema sind historisch bedingt und werden beibehalten.
- **Lombok:** Boilerplate über `@Getter/@Setter`, `@Builder`,
  `@RequiredArgsConstructor` (Constructor-Injection), `@Slf4j` für Logging.
- **Konstruktor-Injection** statt Field-Injection (`@RequiredArgsConstructor`
  über `final`-Felder).
- **Eigene Exceptions je Quelle** (`SchleppbetriebImportException`,
  `ExcelImportException`) mit aussagekräftigen, zeilengenauen Meldungen.
- **Konstanten statt Magic Numbers:** Spaltenindizes, Trennzeichen, Status- und
  Chunk-Größen sind benannte `static final`-Konstanten.
- **Tests / TDD:** Neue Funktionalität entsteht test-getrieben
  (rot → grün → refactor). Parsing-/Geschäftslogik als **Unit-Test** mit
  gemocktem Repository; voller Import-/Persistenz-Durchlauf als
  `@SpringBootTest` + `@Transactional` gegen H2 (rollt pro Test zurück).
- **Zeiten:** durchgängig **Ortszeit**, niemals UTC im Schema.

### Git-Konventionen

- **Branches:** `<issue#>-<slug>`, z. B. `5-daten-von-der-schleppkladde-importieren`.
  `main` ist Source of Truth.
- **Commit-Messages:** Deutsch, mit Ticket-Prefix, z. B.
  `Flugbuch-5, Schleppkladde-CSV-Import implementiert`.

---

## 🚀 Schnellstart

### Voraussetzungen

- [Docker](https://www.docker.com/) + [Docker Compose](https://docs.docker.com/compose/)
- Optional für lokale Entwicklung: **Java 21** und **Maven 3.9+**

> **Wichtig:** Das Projekt setzt `java.version = 21`. Ein Java-17-Image schlägt
> beim Kompilieren fehl.

### Backend + DB via Docker Compose

Die Compose-Datei liegt unter `backend/docker-compose.yml` (Kommandos daher aus
`backend/` heraus):

```bash
# Stack starten (MySQL 8.0 + Spring-Boot-App)
docker compose -f backend/docker-compose.yml up --build

# Stack stoppen
docker compose -f backend/docker-compose.yml down

# Stack stoppen und DB-Volume löschen (clean start)
docker compose -f backend/docker-compose.yml down -v
```

- App erreichbar unter `http://localhost:8080`
- MySQL: DB `flugbuch`, User `flugbuch_user` / `flugbuch_password`
- H2-Console (nur bei H2-Datasource): `http://localhost:8080/h2-console`
  (JDBC-URL `jdbc:h2:mem:flugbuchdb`)

### Lokale Entwicklung (ohne Docker)

```bash
cd backend
mvn spring-boot:run     # startet gegen H2 in-memory (Default)
```

---

## 🧪 Tests

Tests laufen gegen **H2 in-memory** – **kein Docker für die Tests selbst nötig**
(Testcontainers ist nicht eingebunden).

```bash
# Alle Tests (lokal, Java 21)
cd backend && mvn test

# Einzelne Testklasse
mvn test -Dtest=SchleppbetriebImportServiceTest

# Mit Coverage-Check (JaCoCo, Schwelle 80 %)
mvn verify
```

Falls kein lokales Java/Maven vorhanden ist, als Wegwerf-Build-Umgebung:

```bash
docker run --rm -v "$(pwd)/backend":/app \
  -w /app maven:3.9-eclipse-temurin-21 mvn test
```

### Test-Schichten

| Schicht | Testet | DB |
|---|---|---|
| **Unit** (`service/*Test`) | Parsing-/Import-Logik (Mockito-Repository) | nein |
| **Integration** (`integration/*Test`) | Voller Import gegen H2 (`@SpringBootTest`) | H2 |
| **Context** (`FlugbuchApplicationTests`) | Application-Context lädt | H2 |

---

## 🗄️ Datenbank

| Umgebung | DB | Konfiguration |
|---|---|---|
| **Entwicklung / Tests** | H2 in-memory | Default in `application.yml` |
| **Produktion** | MySQL 8.0 | via `backend/docker-compose.yml` |
| **Produktion (geplant)** | MariaDB | Treiber/URL-Swap, Code bereits portabel |

- Datasource wird über Umgebungsvariablen überschrieben
  (`SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` / `_DRIVER`); Default = H2.
- Schema entsteht aktuell über Hibernate `ddl-auto: update`.
  **Flyway** und die Umstellung auf `ddl-auto: validate` sind geplant (siehe
  [Geplant](#-geplant)).

### Vorhandene Staging-Tabellen

- `staging_schleppbetrieb_eintrag` – Rohdaten aus dem Schleppkladde-CSV
  (`external_id` UNIQUE für idempotente Re-Imports).
- `staging_schleppkladde_eintrag` – Rohdaten aus dem Flugschul-Excel.

---

## 📦 Technologie-Stack

| Komponente | Technologie | Version | Status |
|------------|-------------|---------|--------|
| Framework | Spring Boot | 4.0.6 | ✅ |
| Sprache | Java | 21 | ✅ |
| Build | Maven | 3.9+ | ✅ |
| DB (prod) | MySQL | 8.0 | ✅ |
| DB (dev/test) | H2 | – | ✅ |
| ORM | Spring Data JPA + Hibernate | – | ✅ |
| Validation | Spring Validation | – | ✅ (Starter eingebunden) |
| Mapping | MapStruct | 1.6.3 | ✅ (Gerüst) |
| Excel | Apache POI (`poi-ooxml`) | 5.4.0 | ✅ |
| Testing | JUnit 5 + Mockito + AssertJ | – | ✅ |
| Coverage | JaCoCo | 0.8.13 | ✅ (80 % Schwelle) |
| Migrationen | Flyway | – | 🧭 geplant |
| Sicherheit | Spring Security | – | 🧭 geplant |
| Integrationstests | Testcontainers | – | 🧭 geplant |
| DB (prod, künftig) | MariaDB | – | 🧭 geplant |
| Frontend | Angular / Thymeleaf | – | 🧭 offen |

---

## 📝 Compliance & GoBD (geplant)

Anforderungen aus der Abstimmung mit LuBB/GoBD – **noch nicht implementiert**:

| Anforderung | Beschreibung | Status |
|-------------|--------------|--------|
| **Audit Trail** | Protokollierung aller Änderungen (Wer, Wann, Feld, Alt, Neu) | 🧭 geplant |
| **Tagesabschluss** | Festschreibung eines Flugtages, danach nur markierte Korrekturen | 🧭 geplant |
| **Hash-Verkettung** | SHA-256-Chain pro Eintrag (Blockchain-Prinzip) | 🧭 geplant |
| **Export-Integrität** | Prüfbare Integrität der Exporte | 🧭 geplant |
| **Rollenkonzept** | Pilot / Flugleiter / Admin mit persönlichen Logins | 🧭 geplant |
| **Archivierung** | Lesbarkeit über die gesetzliche Aufbewahrungsfrist | 🧭 geplant |

### Verfahrensdokumentation (für LuBB)

Separates Dokument (kein Code), das vor Produktivbetrieb mit der LuBB
abzustimmen ist: Systemarchitektur, Datensicherungskonzept,
Berechtigungskonzept, Revisionssicherheits-Mechanik.

---

## 📜 Lizenz

Eigentum des **Drachenflieger-Club Berlin (DCB)**; interne Nutzungsbestimmungen.

---

*Dieses README beschreibt den realen Stand des Backends und trennt Implementiertes
von Geplantem. Bei Abweichungen gilt der Code als Quelle der Wahrheit – bitte das
README mitpflegen.*
