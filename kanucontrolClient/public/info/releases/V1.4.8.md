# KanuControl V1.4.8

**Release Notes**\
**Veröffentlichung:** 11.09.2026

Mit **KanuControl V1.4.8** erhält die Anwendung das bislang größte Update
im Bereich **Finanzen**. Neben einer umfassend überarbeiteten
Finanzverwaltung wurden zahlreiche Verbesserungen an Benutzeroberfläche,
Datenmodell und Backend umgesetzt. Die neue Architektur bildet
gleichzeitig die Grundlage für den zukünftigen **automatischen
Finanzausgleich**.

# KanuControl V1.4.8

## Highlights

### 📎 Digitale Belegverwaltung

Mit Version 1.1 können Belege vollständig digital verwaltet werden.

- Upload von Fotos und PDF-Dokumenten
- Automatische Bildoptimierung vor dem Upload
- Vorschau direkt im Browser
- Download von Belegdokumenten
- Löschen von Dokumenten
- Sofortige Aktualisierung der Belegliste
- Optimierte Upload-Performance

------------------------------------------------------------------------

# 🚀 Highlights

-   Neuer Finanzbereich mit Kosten- und Einnahmenverwaltung
-   Einführung von Konten
-   Neues Finanz-Dashboard
-   Vorbereitung des automatischen Finanzausgleichs
-   Zahlreiche Verbesserungen an Oberfläche, Backend und Performance

------------------------------------------------------------------------

# 💰 Finanzen

Der Finanzbereich wurde grundlegend erweitert.

## Neu

-   Einführung von Finanzkategorien
-   Trennung von **Kosten** und **Einnahmen**
-   Verbesserte Buchungserfassung
-   Speicherung der Herkunft einer Buchung
-   Vorbereitung zukünftiger Auswertungen
-   Grundlage für transparente Veranstaltungsabrechnungen

------------------------------------------------------------------------

## 💳 Teilnehmerbeiträge: Überweisung und Quittung

Bei der Erfassung von Teilnehmerbeiträgen unterscheidet KanuControl zwischen
**Überweisung** und **Quittung**. Die beiden Zahlungswege haben unterschiedliche
Auswirkungen auf das **Vereinskonto (VK)**, das **Teilnehmerkonto (TN-Konto)**
und den späteren **Finanzausgleich**.

### Überweisung

Bei einer **Überweisung** wird der Teilnehmerbeitrag auf das
**Vereinskonto (VK)** überwiesen. Der Betrag steht damit dem Vereinskonto für
die Abrechnung der Veranstaltung zur Verfügung.

### Quittung

Bei einer **Quittung** wird der Teilnehmerbeitrag grundsätzlich als
**Guthaben auf dem Teilnehmerkonto (TN-Konto)** erfasst.

Von diesem Guthaben werden zunächst die für die Veranstaltung zu zahlenden
**Teilnehmerbeiträge** abgezogen. Zusätzlich werden die Ausgaben berücksichtigt,
die Mitglieder des TN-Kontos für die Veranstaltung bezahlt oder verauslagt haben,
zum Beispiel:

- Lebensmittel
- Eintrittskarten
- sonstige für die Veranstaltung verauslagte Kosten

Der Saldo des TN-Kontos zeigt damit, ob die eingezahlten Teilnehmerbeiträge und
die verauslagten Ausgaben ausgeglichen sind oder ob dem TN-Konto noch Geld
zusteht.

**Beispiel:**

- Quittungen / Guthaben (Teilnehmerbeiträge) auf dem TN-Konto: **800 €**
- zu zahlende Teilnehmerbeiträge: **800 €**
- Ausgaben eines Mitglieds des TN-Kontos, z. B. für Lebensmittel: **150 €**
- **Saldo des TN-Kontos: -150 €**

In diesem Beispiel gleichen die Quittungen zunächst genau die zu zahlenden
Teilnehmerbeiträge aus. Das Mitglied des TN-Kontos hat darüber hinaus
**150 € für die Veranstaltung ausgegeben**.

Der negative Saldo von **-150 €** bedeutet daher, dass dem TN-Konto noch
**150 € zustehen**. Dieser Betrag wird später im Rahmen des
**Finanzausgleichs** vom **Vereinskonto (VK)** an das TN-Konto erstattet.

Hat das TN-Konto keine zusätzlichen Ausgaben, beträgt der Saldo bei einem
vollständig ausgeglichenen Teilnehmerbeitrag **0 €**.

> **Merksatz:** Quittungen dienen zunächst dazu, die geschuldeten
> Teilnehmerbeiträge auszugleichen. Erst zusätzliche Ausgaben, die Mitglieder
> des TN-Kontos für die Veranstaltung verauslagt haben, führen zu einem
> negativen Saldo und damit zu einem Erstattungsanspruch gegenüber dem
> Vereinskonto.

### Sonderfall: Quittung mit Geldübergabe an die Leitung

Wenn ein Teilnehmer zwar eine **Quittung** erhält, das Geld aber physisch an
die Veranstaltungsleitung übergibt, wird dieser Teilnehmerbeitrag finanziell
wie eine **Überweisung** behandelt. Das Geld befindet sich in diesem Fall nicht
mehr beim Teilnehmer bzw. auf dessen TN-Konto, sondern ist dem
**Vereinskonto (VK)** zuzurechnen.

------------------------------------------------------------------------

# 👨‍👩‍👧 Konten

Mit Konten können Teilnehmer gemeinsamen Abrechnungen zugeordnet
werden.

-   Zuordnung beliebiger Teilnehmer zu einem Konto
-   Buchungen werden Konten zugewiesen
-   Grundlage für die automatische Verteilung von Kosten und Beiträgen
-   Zuordnung muss spätestens vor dem Finanzausgleich erfolgen

------------------------------------------------------------------------

# 📊 Finanz-Dashboard

Das neue Dashboard bietet einen schnellen Überblick über die finanzielle
Situation einer Veranstaltung.

-   KPI-Karten
-   Einnahmen und Ausgaben auf einen Blick
-   Saldenübersicht
-   Modernisierte Navigation

------------------------------------------------------------------------

# 📄 Belege & Auswertungen

-   Überarbeitete Belegverwaltung
-   Optimierte Tabellen
-   Schnellere Navigation
-   Vorbereitung neuer Excel-Auswertungen

------------------------------------------------------------------------

# 🏕️ Veranstaltungen

-   Überarbeitung des Veranstaltungsmodells
-   Verbesserungen bei Unterkunfts- und Verpflegungsarten
-   Optimierungen der Planung und Simulation

------------------------------------------------------------------------

# ⚙️ Verwaltung

-   Erweiterte Stammdaten
-   Zusätzliche Validierungen
-   Verbesserte Datenintegrität

------------------------------------------------------------------------

# 🖥️ Benutzeroberfläche

-   Modernisierte Formulare
-   Verbesserte Tabellen
-   Optimierte Navigation
-   Zahlreiche Detailverbesserungen im Layout

------------------------------------------------------------------------

# 🔧 Backend & API

-   Neue Finanz-Entitäten
-   Neue Enums für Finanztypen und Finanzkategorien
-   Erweiterte Validierungen
-   Aussagekräftigere Fehlermeldungen
-   Verbesserte Datenkonsistenz

------------------------------------------------------------------------

# 🐞 Fehlerkorrekturen

-   Zahlreiche kleinere Fehler behoben
-   Verbesserte Stabilität
-   Optimierungen im Frontend
-   Optimierungen im Backend
-   Performanceverbesserungen

------------------------------------------------------------------------

# 🔜 Ausblick

Die in Version 1.01 eingeführten Konten bilden die technische
Grundlage für den kommenden **automatischen Finanzausgleich**.

Geplante Erweiterungen:

-   Automatische Kostenverteilung
-   Weitere Excel- und PDF-Auswertungen
-   Erweiterte Finanzanalysen

