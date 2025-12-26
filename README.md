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

```text
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