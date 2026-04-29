# KanuControl

> ⚠️ Hinweis:
> KanuControl befindet sich weiterhin in aktiver Entwicklung.
> Der fachliche Kern des Systems ist jedoch inzwischen funktionsfähig und vollständig integrierbar.

---

# 🚀 Release-Status

## Aktueller Stand: v0.6.0.0

**KanuControl ist jetzt ein funktionsfähiges Fachsystem für:**

- Veranstaltungsverwaltung
- Teilnehmerverwaltung
- Förderlogik (KJFP)
- Finanzplanung
- Abrechnung
- PDF-Generierung
- Dashboard & Kennzahlen

Der aktuelle Fokus verschiebt sich damit von Kernfunktionalität hin zu:
- Stabilisierung
- UX
- Performance
- Produktionsreife

---

# 🎯 Ziel von KanuControl

KanuControl ist eine Webanwendung zur Beantragung, Verwaltung und Abrechnung von Zuschüssen
aus dem Kinder-, Jugend- und Freizeitplan (KJFP) des Landessportbundes NRW (LSB NRW).

Die Anwendung richtet sich an:

- Kanuvereine
- Kanuverbände
- Geschäftsstellen
- Jugendwarte
- Kassierer
- Veranstaltungsleitungen

Ziel ist es, heute häufig manuelle, fehleranfällige und papierbasierte Prozesse
durch eine strukturierte, digitale Lösung zu ersetzen.

---

# 🧭 Fachlicher Ablauf

## 1. Stammdaten

- Vereine verwalten
- Personen verwalten
- Mitgliedschaften pflegen
- Hauptverein-Logik
- Kontoinhaber verwalten
- KiK-Zertifizierung verwalten

---

## 2. Veranstaltungen

- Veranstaltungen anlegen
- FM / JEM / BM / GV
- Veranstaltungsleitung
- Unterkunft / Verpflegung
- Teilnehmerplanung
- Teilnehmergebühren
- Länder / Orte
- Förderfähigkeit automatisch ableitbar

---

## 3. Teilnehmerverwaltung

- Teilnehmer erfassen
- Bulk Add / Remove
- Leiter automatisch Teilnehmer
- Rollenmodell
- Finanzgruppen
- Suche / Filter / Pagination

---

## 4. Finanzplanung

- Planung nach Kategorien
- Kosten & Einnahmen
- Automatische Summenbildung
- Förderfähigkeitsberechnung
- Dashboard-Kennzahlen

Unterstützte Kategorien:

### Kosten
- Unterkunft
- Verpflegung
- Honorare
- Fahrtkosten
- Verbrauchsmaterial
- Kultur
- Miete
- Sonstige Kosten

### Einnahmen
- Teilnehmerbeiträge
- Pfand
- KJFP-Zuschuss
- Sonstige Einnahmen

---

## 5. Förderung (KJFP)

KanuControl enthält eine zentrale Förderlogik:

- Förderfähigkeitsprüfung
- Altersgrenzen je Veranstaltungstyp
- KiK-Zuschläge
- Förderdeckel
- Fördersätze historisiert
- Berechnung pro Teilnehmer / Tag
- Gesamtförderung

### Fachliche Regeln

Für FM/JEM:
- Teilnehmer zwischen 6 und einschließlich 20 Jahren förderfähig
- Mitarbeiter & Leiter nicht förderfähig
- KiK-Zuschläge automatisch berücksichtigt
- Deckel automatisch angewendet

---

## 6. Abrechnung

- Erfassung echter Buchungen
- Einnahmen & Ausgaben
- Finanzgruppen
- Automatische Summierung
- Eigenleistungsberechnung
- KJFP-Abgleich
- Dashboard-Auswertung

---

## 7. PDF-Generierung

Bereits integriert:

- Teilnehmerliste
- FM/JEM Anmeldung
- FM/JEM Abrechnung

Eigenschaften:
- editierbare PDF-Formulare
- automatische Feldbefüllung
- mehrseitige PDFs
- dynamische Berechnungen
- automatische Förderwerte

---

# 📊 Dashboard

Das Finanzdashboard enthält:

- Plan-Kosten
- Ist-Kosten
- Plan-Einnahmen
- Ist-Einnahmen
- Plan-Saldo
- Ist-Saldo
- Abweichungen
- Förderung
- Kosten nach Kategorien
- Einnahmen nach Kategorien

Alle Werte werden serverseitig aggregiert.

---

# 🧱 Technische Architektur

KanuControl ist eine Client-Server-Webanwendung.

## Überblick

```text
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

---

## 🌐 Mandantenfähigkeit

KanuControl ist mandantenfähig (Multi-Tenant) aufgebaut.

### Eigenschaften

* Schema-per-Tenant
* Datenisolation pro Verein
* Keycloak-basierte Tenant-Erkennung
* JWT / Gruppen-basiert
* Lazy Schema Creation
* Liquibase-basierte Baseline-Struktur

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

Aktueller Stand

* 218 Tests grün
* Controller-Tests
* Service-Tests
* Repository-Tests
* PDF-Tests
* Multi-Tenant-Tests
* Förderlogik-Tests
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
- Axios

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

# 🗺️ Roadmap

## ✅ v0.6.0.0 – Core Complete

Abgeschlossen:

* Stammdaten
* Veranstaltungen
* Teilnehmer
* Finanzgruppen
* Planung
* Abrechnung
* Förderlogik
* Dashboard
* PDF Anmeldung
* PDF Abrechnung
* Teilnehmerliste
* Multi-Tenant Architektur

---

# 🎯 Nächste Meilensteine

## 🔜 v0.7 Fokus

* Abschlusslogik Veranstaltungen
* Plausibilitätsprüfungen
* PDF-Feinschliff
* UX-Optimierung
* Rechte/Rollen
* Stabilisierung

⸻

## 🎯 v1.0 Fokus

* Produktionsreife
* Backup/Restore
* Monitoring
* Docker Deployment
* Performance
* Security Hardening
* Audit Logging

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