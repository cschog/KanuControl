# KanuControl

> ⚠️ **Hinweis:**  
> KanuControl befindet sich aktuell **in aktiver Entwicklung** und ist **noch nicht produktiv einsetzbar**.

---

## 🎯 Ziel von KanuControl

**KanuControl** ist eine Webanwendung zur **Beantragung, Verwaltung und Abrechnung von Zuschüssen**
aus dem **Kinder-, Jugend- und Freizeitplan (KJFP)** des **Landessportbundes NRW (LSB NRW)**.

Die Anwendung richtet sich an:

- Kanuvereine
- Kanuverbände (z. B. KVNRW)
- Geschäftsstellen, die Förderanträge prüfen und abrechnen

Ziel ist es, die heute oft **manuellen, fehleranfälligen und papierbasierten Prozesse**
durch eine **strukturierte, digitale Lösung** zu ersetzen.

---

## 🧭 Fachlicher Ablauf

### Grunddaten

- Anlegen eines Vereins (falls noch nicht vorhanden)
- Anlegen von Personen
- Zuordnung von Personen zu Vereinen (Mitgliedschaften)

---

### Antragstellung

- Anlegen einer **Jugend-Veranstaltung**
- Erfassen der relevanten Eckdaten
- Erzeugung von **Antragsformularen** (später als PDF)

---

### Durchführung der Veranstaltung

- Erfassen der **Teilnehmer**
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

> 📄 **PDF-Erzeugung ist ein späterer Ausbauschritt**  
> Fokus liegt zunächst auf stabilen Datenmodellen und Prozessen.

---

## 🧱 Technische Architektur

KanuControl ist eine **Client-Server-Webanwendung**.

### Überblick

```
Browser (React)
   │
   ▼
Spring Boot REST API
   │
Service Layer
   │
Mapper (DTO ↔ Entity)
   │
Repository (JPA)
   │
MySQL (Schema-per-Tenant)
```
---

flowchart LR
    FE[React Frontend] --> C[REST Controller]
    C --> S[Service Layer]
    S --> M[Mapper]
    M --> R[Repository]
    R --> DB[(MySQL)]

    subgraph Tenancy
        TF[TenantFilter]
        TC[TenantContext]
        TS[TenantSchemaService]
    end

    FE --> TF
    TF --> TC
    TS --> DB

## 🌐 Mandantenfähigkeit

KanuControl ist mandantenfähig aufgebaut.
	•	Jeder Verein sieht nur seine eigenen Daten
	•	Trennung erfolgt über Schema-per-Tenant
	•	Tenant wird ermittelt über:
	•	JWT (Keycloak)
	•	HTTP-Header


## Datenbankstruktur

```
Database
├── kanu                (System / Default)
├── tenant_verein_1
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

Schemas werden lazy erstellt und gecacht.

---

## 🔐 Authentifizierung & Autorisierung

Zur Benutzerverwaltung wird Keycloak eingesetzt.
	•	Open-Source IAM
	•	OAuth2 / OpenID Connect
	•	Rollen- & Rechteverwaltung
	•	Saubere Trennung von Authentifizierung und Fachlogik

🧪 Qualität & Tests
	•	Integrationstests für:
	•	REST-Endpoints
	•	Mandanten-Initialisierung
	•	Liquibase-Migrationen
	•	Smoke-Tests für Systemstart & Grundfunktionen
	•	Fokus auf:
	•	Stabilität
	•	Nachvollziehbarkeit
	•	Erweiterbarkeit

## 📦 Backend

Technologien
	•	Java 17
	•	Spring Boot 3.2.2
	•	Spring Data JPA
	•	Liquibase
	•	MySQL
	•	Maven

Architekturprinzipien
	•	Kein Business-Code im Controller
	•	Kein Datenbankzugriff im Controller
	•	DTO ↔ Entity strikt getrennt
	•	Zentrale Fehlerbehandlung (@RestControllerAdvice)
	•	Mandantenlogik außerhalb der Fachlogik

## 🎨 Frontend

Technologien
	•	React
	•	Vite
	•	TypeScript
	•	Tailwind CSS
	•	VS Code

Das Frontend kommuniziert ausschließlich über die REST-API
und enthält keine Businesslogik.

## 🗺️ Roadmap

### Phase 0 – Fundament (aktuell)
	•	Backend-Grundstruktur
	•	Mandantenfähigkeit
	•	Keycloak-Integration
	•	CRUD für Verein, Person, Mitglied
	•	Integrationstests

### Phase 1 – Frontend-Basis
	•	Login via Keycloak
	•	CRUD-Oberflächen
	•	Basis-Navigation

### Phase 2 – Veranstaltungen
	•	Veranstaltungen & Typen
	•	Teilnehmerverwaltung
	•	Fachliche Validierungen

### Phase 3 – Abrechnung
	•	Einnahmen / Ausgaben
	•	Reisekosten
	•	Plausibilitätsprüfungen

### Phase 4 – Dokumente
	•	PDF-Erzeugung
	•	Editierbare Formulare
	•	Archivierung


## 🌍 Open-Source & Contributions

KanuControl ist von Beginn an als Open-Source-Projekt gedacht.

Ziele
	•	Nachvollziehbarer Code
	•	Klare Struktur
	•	Einsteigerfreundlich
	•	Fachlich verständlich (auch für Nicht-Informatiker)

Contributions sind willkommen
	•	Bugfixes
	•	Refactorings
	•	Tests
	•	Dokumentation
	•	Feature-Vorschläge

Contribution-Guidelines folgen in einer späteren Version.

## 🛠️ Lokale Entwicklung (Mac)

Voraussetzungen
	•	Docker
	•	Java 17
	•	Maven
	•	Node.js

### Keycloak starten

docker run -d --name keycloak \
  -p 9080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v /Volumes/Merlin_Daten/Apps/keyCloak-Data:/opt/keycloak/data \
  quay.io/keycloak/keycloak:24.0.2 start-dev
  
### Keycloak Admin UI
	•	URL: http://localhost:9080
	•	User: admin
	•	Passwort: admin
	•	Realm: KanuControl
	
## 📄 Lizenz

Die Lizenz wird zu einem späteren Zeitpunkt festgelegt
(vorgesehen: Open-Source, z. B. MIT oder Apache 2.0).

## ❤️ Motivation

KanuControl entsteht aus der Praxis für die Praxis –
mit dem Ziel, ehrenamtliche Arbeit zu entlasten
und Verwaltungsprozesse einfacher, transparenter und sicherer zu machen.