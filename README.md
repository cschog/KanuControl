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

- Java 21
- Spring Boot 3.5.10
- Spring Data JPA (Hibernate 6)
- Liquibase
- PostgreSQL
- Maven
- Apache pdfbox V3.02

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
- node v22.x
- TypeScript
- Material UI ab V7.x
- VS Code

Das Frontend kommuniziert ausschließlich über die REST-API
und enthält keine Businesslogik.

---

# 🗺️ KanuControl – Roadmap

Stand: **Release v0.5.9**  
Status: **Finanzgruppen integriert – Übergang zur Abrechnung**

---

# 📊 Gesamtfortschritt

**≈ 75 % bis v1.0**

- 🟢 Architektur stabil
- 🟢 Stammdaten vollständig
- 🟢 Veranstaltungen & Teilnehmer vollständig
- 🟢 Finanzgruppen (Kürzel) integriert
- 🟡 Abrechnung in Aufbau
- 🟡 Import & Datenqualität teilweise
- ⚪ Stabilisierung & Produktionsreife offen

---

# Phase 0 – Fundament → Release 0.1.x ✅

**Ziel:** stabile Architektur & Stammdaten  
**Status:** abgeschlossen

## Backend
- [x] Spring Boot 3 Basis
- [x] Multi-Tenant (Schema-per-Tenant)
- [x] Keycloak Integration
- [x] Liquibase Migration
- [x] Hibernate Runtime Schema Switching

## Domäne
- [x] Verein CRUD
- [x] Person CRUD
- [x] Mitglied (Join Entity)
- [x] Hauptverein-Logik
- [x] Fachliche Validierungen

## Frontend
- [x] Vereinsverwaltung
- [x] Personenverwaltung
- [x] Mitgliedszuordnung

## Import
- [x] CSV Import Personen
- [x] Mapping-Datei
- [x] Dry-Run
- [x] Fehleranzeige
- [x] Upload-Infrastruktur

---

# Phase 1 – Frontend & Nutzung → Release 0.2.x ✅

**Ziel:** komfortable Bedienung  
**Status:** weitgehend abgeschlossen

## UI
- [x] Navigation
- [x] Pagination
- [x] Sortierung
- [x] Filter
- [ ] Keycloak UX verbessern

## Technik
- [ ] Multipart Handling finalisieren
- [ ] Upload Limits
- [ ] Global Error Handling verbessern

---

# Phase 2 – Import & Datenqualität → Release 0.3.x 🟡

**Ziel:** robuste Massendaten  
**Status:** teilweise abgeschlossen

## CSV Import Mitglieder
- [x] Upload
- [x] Dry-Run
- [ ] Pflichtfeld Validierung
- [ ] Fachliche Regeln
- [ ] Dublettenprüfung
- [ ] Fehlerreport (Zeile + Ursache)

## Mapping
- [ ] Person ↔ Mitglied ↔ Verein

## Stabilität
- [ ] Idempotenter Import

---

# Phase 3 – Veranstaltungen & Teilnehmer → Release 0.4.x ✅

**Ziel:** Veranstaltungsmanagement vollständig  
**Status:** abgeschlossen

## Veranstaltung
- [x] CRUD
- [x] Aktiv-Status
- [x] Leiter Domain-Regel

## Teilnehmer
- [x] Join-Entity
- [x] Rollen (L / M)
- [x] Bulk Add / Remove
- [x] Leiter immer Teilnehmer
- [x] Sortierung (Leiter zuerst)
- [x] Search (Server-seitig)
- [x] Debounced Suche im FE

## UI
- [x] Teilnehmerverwaltung
- [x] Dual-List Auswahl
- [x] Multi-Select stabil
- [x] SelectAll stabil
- [x] Filter Reset
- [x] Confirm Dialog bei Entfernen

## Teilnehmerliste PDF
- [x] Mehrseitig
- [x] Editierbares Formular
- [x] Alter zum Veranstaltungsbeginn
- [x] Header / Footer / Zeitraum
- [x] Checkbox Mapping (JEM/FM/BM)
- [x] Sortierung (Leiter → Verein → Name)
- [x] Dynamischer Dateiname
- [x] Download im FE

---

# Phase 4 – Finanzgruppen & Abrechnung → Release 0.5.x 🟡

**Ziel:** finanzielle Struktur & Vorbereitung Abrechnung  
**Status:** Finanzgruppen abgeschlossen

## Finanzgruppen (Kürzel)
- [x] Entity FinanzGruppe
- [x] 1:n Beziehung Teilnehmer → FinanzGruppe
- [x] Unique Constraint (Veranstaltung + Kürzel)
- [x] Bulk-Zuweisung
- [x] Entfernen aus Gruppe
- [x] Search mit Debounce
- [x] Confirm Dialog
- [x] 201 Integrationstests grün

## Abrechnung – Basis
- [ ] Einnahmen
- [ ] Ausgaben
- [ ] Teilnehmergebühren
- [ ] Reisekosten
- [ ] Berechnung pro Finanzgruppe
- [ ] Abschlusslogik Veranstaltung

---

# Phase 5 – Dokumente & Formulare → Release 0.6.x 🟡

**Ziel:** vollständige Verwaltungsdokumente

## PDF Engine
- [x] Formular-basierte PDFs
- [x] Mehrseitig
- [x] Editierbar
- [ ] Template-System

## Dokumente
- [x] Teilnehmerliste
- [ ] Anmeldung
- [ ] Erhebungsbogen
- [ ] Abrechnung
- [ ] Reisekosten

---

# Phase 6 – Stabilisierung → Release 1.0.0 ⚪

## Qualität
- [ ] Performance
- [ ] Memory / PDF Optimierung
- [ ] Query Optimierung
- [ ] Testabdeckung > 90 %

## Sicherheit
- [ ] Rollen & Rechte fein granular
- [ ] Audit Logging
- [ ] CSRF / Hardening Review

## Betrieb
- [ ] Monitoring
- [ ] Backup & Restore
- [ ] Docker Deployment
- [ ] Produktionsdokumentation

---

# 🎯 Nächste Meilensteine

## → v0.6 Fokus
- Abrechnung starten
- Einnahmen / Ausgaben Domain
- Finanzgruppen-Berechnung
- Erhebungsbogen PDF
- Anmeldung PDF

## → v0.7 Fokus
- Abschlusslogik Veranstaltung
- Abrechnungs-PDF
- Plausibilitätsprüfungen

## → v1.0 Fokus
- Stabilität
- Performance
- Security
- Produktivbetrieb
---

# 🧭 Projektstatus

**KanuControl ist jetzt ein funktionsfähiges Fachsystem.**  
Die nächsten Releases bringen Vollständigkeit, Stabilität und Produktionsreife.

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


## License

This project is licensed under the **Apache License 2.0** – see the [LICENSE](LICENSE) file for details.

### Trademark and Branding

The name **KanuControl**, the KanuControl logo, and the KanuControl icon are **not covered by the Apache License** and remain the exclusive property of the project owner.

You may use, modify, and distribute the software under the terms of the Apache 2.0 license, but you may **not use the KanuControl name, logo, or icon** in redistributed or modified versions without explicit permission.

If you distribute a modified version of this software, you must remove all KanuControl branding.

For trademark permission requests contact:
**chris.schog@ekc-home.de**

---

## ❤️ Motivation

KanuControl entsteht aus der Praxis für die Praxis –
mit dem Ziel, ehrenamtliche Arbeit zu entlasten
und Verwaltungsprozesse einfacher, transparenter und sicherer zu machen.