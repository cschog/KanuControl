# KanuControl

> **Die moderne Vereinsverwaltung für Kanuvereine und Kanuverbände**

KanuControl ist eine vollständig webbasierte Vereinsverwaltung zur Planung, Durchführung und Abrechnung von Kanuveranstaltungen. Der Schwerpunkt liegt auf der Verwaltung von Ferienfreizeiten und Jugendmaßnahmen nach den Förderrichtlinien des KJFP (Kinder- und Jugendförderplan).

**Version:** 1.0  
**Status:** ✅ Funktional abgeschlossen

---

# Inhaltsverzeichnis

- [Funktionen](#funktionen)
- [Technologie](#technologie)
- [Architektur](#architektur)
- [Module](#module)
- [Workflow](#workflow)
- [Installation](#installation)
- [Entwicklung](#entwicklung)
- [Roadmap](#roadmap)

---

# Funktionen

## Vereinsverwaltung

- Vereine
- Mitglieder
- Ansprechpartner
- Benutzerverwaltung
- Rollen und Berechtigungen

## Veranstaltungen

- Planung
- Teilnehmerverwaltung
- Mitarbeitende
- Unterkünfte
- Verpflegung
- Reisekosten
- Dokumente

## Fördermittel

Unterstützung des kompletten KJFP-Prozesses

- Simulation
- Finanzplanung
- Förderberechnung
- Planungsverwaltung
- Förderantrag
- Zuschüsse
- Eigenanteil

## Finanzen

- Belege
- Buchungen
- Finanzgruppen
- Einnahmen
- Ausgaben
- Automatische Berechnungen

## Abrechnung

- Abrechnung einer Veranstaltung
- KJFP-Abrechnung
- Teilnehmerbeiträge
- Zuschüsse
- PDF-Auswertungen

## Stammdaten

Verwaltung aller zentralen Stammdaten

- Beitragsstrukturen
- Fördersätze
- Unterkunftsarten
- Verpflegungsmodelle
- Finanzgruppen
- Postleitzahlen
- Vereine

---

# Technologie

## Backend

- Java 17
- Spring Boot
- Spring Security
- Hibernate / JPA
- Liquibase
- PostgreSQL

## Frontend

- React
- TypeScript
- Material UI
- TanStack Table
- React Router
- Axios

## Infrastruktur

- Docker
- Keycloak
- Nginx
- Prometheus
- Grafana
- Loki
- Uptime Kuma

---

# Architektur

```
React (Frontend)
        │
        ▼
Spring Boot REST API
        │
        ▼
PostgreSQL
```

Authentifizierung erfolgt über **Keycloak** mittels OpenID Connect.

Mehrere Vereine können über eine Multi-Tenant-Architektur unabhängig voneinander verwaltet werden.

---

# Module

## Dashboard

Übersicht über

- Veranstaltungen
- Teilnehmer
- Finanzen
- Kennzahlen

---

## Personen

Verwaltung von

- Mitgliedern
- Mitarbeitenden
- Referenten
- Leitungen

---

## Veranstaltungen

Verwaltung kompletter Veranstaltungen inklusive

- Teilnehmer
- Mitarbeitende
- Unterkunft
- Verpflegung
- Planung
- Förderung
- Abrechnung

---

## Simulation

Berechnung einer Veranstaltung bereits vor der Beantragung.

Berücksichtigt u.a.

- Teilnehmerzahl
- Mitarbeitende
- Unterkunft
- Verpflegung
- KJFP-Förderung
- KiK-Zuschlag
- Eigenanteil

---

## Planung

Erstellung der Jahresplanung.

- Förderplanung
- Kostenplanung
- Finanzierungsübersicht

---

## Abrechnung

Erfassung aller

- Belege
- Buchungen
- Einnahmen
- Ausgaben

Automatische Berechnung von

- Zuschüssen
- Eigenanteil
- Teilnehmerbeiträgen

---

## PDF-Erzeugung

Automatische Erstellung verschiedener Dokumente.

Beispiele

- Förderantrag
- Teilnehmerlisten
- Finanzübersichten
- Abrechnungen

---

# Workflow

Der typische Lebenszyklus einer Veranstaltung:

```text
Veranstaltung
      │
      ▼
Simulation
      │
      ▼
Planung
      │
      ▼
Förderantrag
      │
      ▼
Durchführung
      │
      ▼
Abrechnung
      │
      ▼
Archiv
```

---

# Installation

## Voraussetzungen

- Java 17
- Node.js
- PostgreSQL
- Docker (optional)

## Backend

```bash
mvn spring-boot:run
```

## Frontend

```bash
yarn install
yarn dev
```

---

# Entwicklung

## Backend

```
Spring Boot
 ├── Controller
 ├── Services
 ├── Repository
 ├── Entities
 └── DTOs
```

## Frontend

```
React
 ├── Pages
 ├── Components
 ├── Dialoge
 ├── Tabellen
 ├── API
 └── Theme
```

---

# Roadmap

## Version 1.1

- Erweiterte Statistiken
- Weitere PDF-Ausgaben
- Verbesserte Dashboards
- Performanceoptimierungen

## Version 1.2

- Mobile Optimierungen
- Erweiterte Auswertungen
- API-Erweiterungen

---

# Lizenz

Dieses Projekt wurde für die Verwaltung von Kanuvereinen und Kanuverbänden entwickelt.

---

# Status

| Bereich | Status |
|----------|--------|
| Vereinsverwaltung | ✅ |
| Veranstaltungen | ✅ |
| Teilnehmer | ✅ |
| Simulation | ✅ |
| Planung | ✅ |
| KJFP | ✅ |
| Förderung | ✅ |
| Finanzen | ✅ |
| Abrechnung | ✅ |
| PDF-Ausgaben | ✅ |
| Mehrmandantenfähigkeit | ✅ |
| Keycloak | ✅ |

---

# KanuControl 1.0

**KanuControl 1.0 markiert den ersten funktional vollständigen Meilenstein des Projekts.**

Die Anwendung unterstützt den kompletten Lebenszyklus einer Veranstaltung – von der Planung über die Förderung bis hin zur vollständigen finanziellen Abrechnung.