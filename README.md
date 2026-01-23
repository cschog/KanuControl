# KanuControl

> ⚠️ Hinweis:
> KanuControl befindet sich aktuell in aktiver Entwicklung und ist noch nicht produktiv einsetzbar.

---

## 🎯 Ziel von KanuControl

KanuControl ist eine Webanwendung zur Beantragung, Verwaltung und Abrechnung von Zuschüssen
aus dem Kinder-, Jugend- und Freizeitplan (KJFP) des Landessportbundes NRW (LSB NRW).

Die Anwendung richtet sich an:

- Kanuvereine
- Kanuverbände (z. B. KVNRW)
- Geschäftsstellen, die Förderanträge prüfen und abrechnen

Ziel ist es, die heute oft manuellen, fehleranfälligen und papierbasierten Prozesse
durch eine strukturierte, digitale Lösung zu ersetzen.

---

## 🧭 Fachlicher Ablauf

### Grunddaten

- Anlegen eines Vereins (falls noch nicht vorhanden)
- Anlegen von Personen
- Zuordnung von Personen zu Vereinen (Mitgliedschaften)

---

### Antragstellung

- Anlegen einer Jugend-Veranstaltung
- Erfassen der relevanten Eckdaten
- Erzeugung von Antragsformularen (später als PDF)

---

### Durchführung der Veranstaltung

- Erfassen der Teilnehmer
  - Personen werden bei Bedarf automatisch angelegt
  - Zuordnung zur Veranstaltung erfolgt direkt
- Pflege von Teilnehmerlisten

---

### Abrechnung

- Erfassen von:
  - Einnahmen
  - Ausgaben
  - optional Reisekosten
- Automatische Berechnung relevanter Summen
- Ausgabe der Abrechnungsunterlagen als PDF:
  - Deckblatt
  - Erhebungsbogen
  - Teilnehmerliste
  - ggf. Reisekostenübersicht

Hinweis:
PDF-Erzeugung ist ein späterer Ausbauschritt.
Fokus liegt zunächst auf stabilen Datenmodellen und Prozessen.

---

## 🧱 Technische Architektur

KanuControl ist eine Client-Server-Webanwendung.

### Überblick
```
Browser (React)
   |
   v
Spring Boot REST API
   |
Service Layer
   |
Mapper (DTO <-> Entity)
   |
Repository (JPA)
   |
PostgreSQL (Schema-per-Tenant)
```
---

## 🌐 Mandantenfähigkeit

KanuControl ist mandantenfähig (Multi-Tenant) aufgebaut.

- Jeder Verein sieht ausschließlich seine eigenen Daten
- Trennung erfolgt strikt über Schema-per-Tenant
- Tenant-Ermittlung erfolgt über:
  - JWT (Keycloak)
  - HTTP-Header
- Ein neutrales Default-Schema (kanu) wird ausschließlich für Systemzwecke genutzt

Schemas werden lazy erzeugt.

- Das Baseline-Schema (`kanu`) wird ausschließlich über Liquibase verwaltet
- Neue Tenant-Schemas werden zur Laufzeit aus dieser Baseline abgeleitet
- Die Struktur der Tenant-Schemas ist damit garantiert identisch
- Schema-Initialisierung erfolgt kontrolliert und idempotent

---

## 🗄️ Datenbankstruktur (PostgreSQL)

```
Database: kanu
├── kanu                (System / Default-Schema)
├── ekc_test
│   ├── person
│   ├── verein
│   ├── mitglied
│   └── …
├── tenant_verein_2
│   ├── person
│   ├── verein
│   ├── mitglied
│   └── …
```

- Das Schema `kanu` dient als technisches Baseline- und System-Schema
- Tenant-Schemas enthalten ausschließlich fachliche Daten
- Änderungen an der Struktur erfolgen immer zuerst im Baseline-Schema

---

## 🔐 Authentifizierung & Autorisierung

Zur Benutzerverwaltung wird Keycloak eingesetzt.

- Open-Source IAM
- OAuth2 / OpenID Connect
- Rollen- & Rechteverwaltung
- Saubere Trennung von Authentifizierung und Fachlogik
- Mandantenzuordnung über Gruppen / Claims

---

## 🧪 Qualität & Tests

- Controller-Tests (@WebMvcTest)
- Integrationstests für:
  - REST-Endpunkte
  - Mandanten-Initialisierung
  - Liquibase-Migrationen
- Smoke-Tests für Systemstart & Grundfunktionen

### Besonderer Fokus liegt auf der Absicherung der Mandantenarchitektur:

- Baseline-Migrationstests (Liquibase)
- Smoke-Tests für Tenant-Schema-Provisionierung
- Verifikation der Schema-Isolation

---

## 📦 Backend

### Technologien

- Java 17
- Spring Boot 3.2.x
- Spring Data JPA (Hibernate 6)
- Liquibase
- PostgreSQL
- Maven

### Architekturprinzipien

- Kein Business-Code im Controller
- Kein Datenbankzugriff im Controller
- DTO <-> Entity strikt getrennt
- Zentrale Fehlerbehandlung (@RestControllerAdvice)
- Mandantenlogik strikt außerhalb der Fachlogik

---

## 🎨 Frontend

### Technologien

- React
- Vite
- TypeScript
- Material UI ab V7.x
- VS Code

Das Frontend kommuniziert ausschließlich über die REST-API
und enthält keine Businesslogik.

---

## 🗺️ Roadmap

### Phase 0 – Fundament (aktuell)

- [x] Backend-Grundstruktur
- [x] Mandantenfähigkeit (Schema-per-Tenant)
- [x] Keycloak-Integration
- [x] CRUD für:
  - [x] Verein
  - [x] Person
  - [x] Mitglied
  - [x] Veranstaltung
  - [x] Teilnehmer
- [x] Teilnehmerverwaltung
- [x] Controller- & Integrationstests
- [x] Fachliche Validierungen
- [ ] Technische Basis für Datei-Uploads

---

### Phase 1 – Frontend-Basis

- [ ] Login via Keycloak
- [ ] CRUD-Oberflächen
- [ ] Basis-Navigation
- [ ] Tabellen mit Pagination & Sortierung
- [ ] CSV-Export (read-only)
  - [ ] Mitgliederliste
  - [ ] Personenliste
  - [ ] Filterabhängiger Export
	- Multipart-Handling
	- Größenlimits

---

### Phase 2 – Mitglieder & Datenimport

- [ ] CSV-Import Mitglieder
  - [ ] Datei-Upload
  - [ ] Vorschau / Dry-Run
  - [ ] Validierung
    - Pflichtfelder
    - Fachliche Regeln
    - Dubletten
  - [ ] Fehlerbericht (Zeile + Ursache)
- [ ] Mapping
  - [ ] Person ↔ Mitglied ↔ Verein
- [ ] Idempotenter Import
- [ ] Import-Historie / Protokoll

---

### Phase 3 – Veranstaltungen

- [ ] Veranstaltungsverwaltung
- [ ] Teilnehmerlisten
- [ ] Anmeldestatus

---

### Phase 4 – Abrechnung

- [ ] Einnahmen / Ausgaben
- [ ] Reisekosten
- [ ] Plausibilitätsprüfungen

---

### Phase 5 – Dokumente

- [ ] PDF-Erzeugung
- [ ] Editierbare Formulare
- [ ] Dokument-Templates
- [ ] Archivierung
---

## 🌍 Open-Source & Contributions

KanuControl ist von Beginn an als Open-Source-Projekt gedacht.

Ziele:
- Nachvollziehbarer Code
- Klare Struktur
- Einsteigerfreundlich
- Fachlich verständlich (auch für Nicht-Informatiker)

Contributions sind willkommen:
- Bugfixes
- Refactorings
- Tests
- Dokumentation
- Feature-Vorschläge

Contribution-Guidelines folgen in einer späteren Version.

---

## 🛠️ Lokale Entwicklung (Mac)

### Voraussetzungen

- Docker
- Java 17
- Maven
- Node.js

---

## 📄 Lizenz

Die Lizenz wird zu einem späteren Zeitpunkt festgelegt
(vorgesehen: Open-Source, z. B. MIT oder Apache 2.0).

---

## ❤️ Motivation

KanuControl entsteht aus der Praxis für die Praxis –
mit dem Ziel, ehrenamtliche Arbeit zu entlasten
und Verwaltungsprozesse einfacher, transparenter und sicherer zu machen.