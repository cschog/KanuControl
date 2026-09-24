# KanuControl – Bedienungsanleitung V1.6.3

**Version:** 1.6.3  
**Stand:** 21.09.2026

## 1. Überblick

KanuControl begleitet eine Veranstaltung über den gesamten Lebenszyklus – von der Erfassung der Stammdaten und der finanziellen Vorbereitung über Teilnehmer und laufende Abrechnung bis zur Auswertung und zum Finanzausgleich.

Die Anwendung ist mandantenfähig. Im Startbereich wird der aktuell aktive Mandant angezeigt. Die allgemeinen Module und die Finanzmodule sind getrennt strukturiert.

### Der grundsätzliche Ablauf

```text
Verein / Personen
      │
      ▼
Veranstaltung
      │
      ├── Teilnehmer
      │
      ▼
Finanzen
 ┌───────────────┐
 │ Vorbereitung  │ → Simulation → Planung
 ├───────────────┤
 │ Durchführung  │ → Beiträge → Abrechnung → Fahrkosten → Konten
 ├───────────────┤
 │ Auswertung    │ → Dashboard → Finanzausgleich
 └───────────────┘
      │
      ▼
Dokumente / PDFs
```

## 2. Startmenü und Navigation

Nach der Anmeldung stehen im Startmenü die allgemeinen Module zur Verfügung:

- **Vereine**
- **Mitglieder** (nachdem es mind. einen Verein gibt)
- **Veranstaltungen** (nachdem es mindestens eine Person gibt)
- **Teilnehmer**
- **Dokumente**
- **Verwaltung**

Zusätzlich stehen – sobald eine Veranstaltung vorhanden ist – die drei Finanzbereiche zur Verfügung:

- **Vorbereitung**
- **Durchführung**
- **Auswertung**

Die Finanzbereiche beziehen sich auf die aktuell ausgewählte Veranstaltung.

Im Kopfbereich zeigt KanuControl außerdem den aktiven Mandanten sowie die Frontend- und Backend-Version an.

## 3. Vereine

### Verein anlegen

Menü: **Vereine → Neuer Verein**

Hier werden die Stammdaten des Vereins erfasst, insbesondere:

- Vereinsname
- Anschrift
- Kontaktdaten
- Bankverbindung
- Kontoinhaber

Ein Verein bildet die Grundlage für Personen, Veranstaltungen und die zugehörigen Stammdaten.

### Verein bearbeiten

Bestehende Vereine können geöffnet, geändert, kopiert und gelöscht werden. Über die Vereinsbearbeitung steht außerdem der **CSV-Import** zur Verfügung.

## 4. Personen und Mitglieder

Menü: **Mitglieder**

Personen können einzeln angelegt und anschließend einem oder mehreren Vereinen zugeordnet werden.

Typische Personendaten sind:

- Name und Vorname
- Geburtsdatum
- Geschlecht
- Anschrift
- Postleitzahl und Ort
- Kontaktdaten

Für Veranstaltungen sind insbesondere Geburtsdatum und Anschrift wichtig, da diese Daten unter anderem für Teilnehmerlisten und Förderunterlagen benötigt werden.

### CSV-Import

Über die Vereinsbearbeitung können größere Personenbestände per CSV importiert werden.

Der Import unterstützt eine Mapping-Datei. Damit können die Spalten einer vorhandenen CSV-Datei den von KanuControl erwarteten Feldern zugeordnet werden.

Empfohlener Ablauf:

1. Verein öffnen.
2. **CSV importieren** auswählen.
3. CSV-Datei auswählen.
4. Mapping-Datei auswählen bzw. die Zuordnung anpassen.
5. Import starten.
6. Importbericht und eventuelle Fehler prüfen.

## 5. Veranstaltungen

Menü: **Veranstaltungen → Neue Veranstaltung**

Eine Veranstaltung enthält die organisatorischen und finanziellen Rahmendaten einer Maßnahme.

Dazu gehören unter anderem:

- Veranstaltungsname
- Veranstaltungsart
- Beginn und Ende
- Veranstaltungsort
- Leitung
- Unterkunftsart
- Verpflegungsmodell
- Beitragsstruktur bzw. individuelle Gebühren
- weitere veranstaltungsbezogene Angaben

### Veranstaltung kopieren

Bestehende Veranstaltungen können als Vorlage kopiert werden. Das erleichtert die Anlage wiederkehrender Veranstaltungen.

### Aktive Veranstaltung

KanuControl ist immer eine Veranstaltung als aktiv gekennzeichnet. Alle Funktionen – insbesondere Teilnehmer und Dokumente – beziehen sich auf die aktuell aktive Veranstaltung.

## 6. Teilnehmer

Menü: **Teilnehmer** bzw. Teilnehmerverwaltung innerhalb einer Veranstaltung

Personen werden einer Veranstaltung als Teilnehmer oder Mitarbeiter zugeordnet.

Bei der Teilnehmerverwaltung können insbesondere Rollen und veranstaltungsbezogene Daten gepflegt werden.

Die Teilnehmerdaten bilden die Grundlage für:

- Teilnehmerlisten
- Förderunterlagen
- Teilnehmerbeiträge
- Finanzgruppen/Konten
- spätere Abrechnung und Auswertung

Fehlende oder unvollständige Personendaten sollten möglichst früh ergänzt werden.

## 7. Finanzen – neue Struktur

Die Finanzverwaltung von KanuControl V1.6.3 ist in drei Bereiche gegliedert.

### Vorbereitung

- **Simulation**
- **Planung**

### Durchführung

- **Beiträge**
- **Abrechnung**
- **Fahrkosten**
- **Konten**

### Auswertung

- **Dashboard**
- **Finanzausgleich**

Die Planung wird im Menü nur angezeigt, wenn für die Veranstaltung bereits eine Planung vorhanden ist bzw. die entsprechende Funktion verfügbar ist.

---

# 8. Finanzen – Vorbereitung

## 8.1 Simulation

Die Simulation dient dazu, die Finanzierung einer Veranstaltung vor der verbindlichen Planung durchzuspielen.

Verändert werden können – abhängig von der Veranstaltung – beispielsweise:

- Teilnehmer- und Mitarbeiterzahlen
- Teilnehmerbeiträge
- Unterkunft
- Verpflegung
- Fahrkosten
- Honorare
- Verbrauchsmaterial
- Kulturprogramm
- Miete
- Pfand
- sonstige Kosten
- sonstige Einnahmen
- KJFP-Förderung
- KiK-Zuschlag

Die Simulation berechnet die finanziellen Auswirkungen unmittelbar neu.

### KiK

Die Simulation berücksichtigt das KiK-Zertifikat des Vereins. Das Vorhandensein eines gültigen KiK-Zertifikats kann in der Simulation berücksichtigt bzw. simuliert werden.

Der KiK-Zuschlag wird anhand der in KanuControl hinterlegten gültigen Daten berechnet.

## 8.2 Planung

Die Planung übernimmt die Ergebnisse der Simulation in die dauerhaft gespeicherte Veranstaltungsplanung.

Die Planung enthält unter anderem:

- Teilnehmer- und Mitarbeiterzahlen
- Teilnehmerbeiträge
- Unterkunftskosten
- Verpflegungskosten
- Fahrkosten
- Honorare
- Verbrauchsmaterial
- Kulturprogramm
- Miete
- Pfand
- sonstige Kosten und Einnahmen
- KJFP-Förderung
- KiK-Zuschlag, sofern vorhanden
- Eigenanteil
- Gesamtkosten und Gesamteinnahmen

### Simulation und Planung

| Simulation | Planung |
|---|---|
| Zum Ausprobieren verschiedener Szenarien | Verbindliche gespeicherte Planung |
| Kann beliebig verändert werden | Wird dauerhaft zur Veranstaltung gespeichert |
| Keine Änderung bereits gespeicherter Planung | Grundlage für die weitere Verarbeitung |

Die gespeicherte Planung wird im weiteren Ablauf unter anderem für Förderunterlagen und die finanzielle Weiterverarbeitung verwendet.

---

# 9. Finanzen – Durchführung

## 9.1 Beiträge

Menü: **Finanzen → Durchführung → Beiträge**

Hier werden die Teilnehmerbeiträge der Veranstaltung verwaltet.

KanuControl kann Beiträge auf Basis eines festen Beitrags oder einer Beitragsstruktur berechnen.

### Beitragsstrukturen

Eine Beitragsstruktur besteht aus Regeln. Eine Regel kann insbesondere festlegen:

- Altersbereich
- Rolle
- Beitrag

Damit können beispielsweise unterschiedliche Beiträge für Teilnehmer und Mitarbeiter oder für verschiedene Altersgruppen hinterlegt werden.

### Zahlungsnachweise

Zahlungseingänge werden über **Zahlungsnachweise** dokumentiert.

Bei einem Zahlungsnachweis werden unter anderem Zahlungsweg, Betrag und die zugehörigen Teilnehmer bzw. Zahlungspositionen erfasst. Zu einem Zahlungsnachweis können Dokumente hinterlegt werden.

### Überweisung und Quittung

KanuControl unterscheidet zwischen zwei Zahlungswegen:

**Überweisung:** Der Beitrag wird dem Vereinskonto (VK) zugerechnet.

**Quittung:** Der Beitrag wird grundsätzlich als Guthaben dem Teilnehmerkonto/TN-Konto zugerechnet.

Bei einer Quittung werden zunächst die geschuldeten Teilnehmerbeiträge berücksichtigt. Weitere vom TN-Konto verauslagte Veranstaltungskosten können zu einem negativen Saldo und damit zu einem Erstattungsanspruch führen.

**Wichtig:** Übergibt ein Teilnehmer seinen Beitrag gegen Quittung an die Veranstaltungsleitung, wird der Betrag finanziell als Quittung der Veranstaltungsleitung zugerechnet.

### Überzahlungen

KanuControl erkennt offene Überzahlungen. Diese können geprüft und – sofern erforderlich – über die Funktion **Rückzahlung** verarbeitet werden.

### Rückzahlungen

Eine Rückzahlung eines Teilnehmerbeitrags wird mit dem ursprünglichen Zahlungsnachweis verknüpft. Dadurch bleibt nachvollziehbar, auf welchen Zahlungseingang sich die Rückzahlung bezieht.

Rückzahlungen werden bei der Finanzberechnung berücksichtigt und vermindern den ursprünglich eingegangenen Betrag entsprechend dem Zahlungsweg.

## 9.2 Abrechnung

Menü: **Finanzen → Durchführung → Abrechnung**

Die Abrechnung dient zur Erfassung der tatsächlichen Kosten und Einnahmen.

### Belege

Für einen Beleg können unter anderem erfasst werden:

- Datum
- Belegnummer
- Aussteller
- Beschreibung
- Summe
- Finanzgruppe/Konto
- Positionen
- Dokumente

Dokumente können Fotos und PDF-Dateien sein. Ein Foto einer Rechnung muss **IMMER** im Querformat aufgenommen werden.

### Buchungen

Eine Buchung wird einer Finanzkategorie und einem Konto bzw. einer Finanzgruppe zugeordnet. Die Herkunft der Buchung wird gespeichert, sodass automatisch erzeugte und manuell erfasste Buchungen unterschieden werden können.

Die Finanzkategorien trennen **Kosten** und **Einnahmen**.

Beispiele für Kosten:

- Unterkunft
- Verpflegung
- Fahrkosten
- Honorare
- Verbrauchsmaterial
- Kultur
- Miete
- sonstige Kosten

Beispiele für Einnahmen:

- Teilnehmerbeiträge
- Zuschüsse
- Pfand
- sonstige Einnahmen

## 9.3 Fahrkosten

Menü: **Finanzen → Durchführung → Fahrkosten**

Fahrkosten werden separat erfasst und können anschließend in die finanzielle Auswertung einfließen.

Eine Reisekostenabrechnung kann insbesondere enthalten:

- Fahrer
- Mitfahrer
- Fahrtabschnitte
- Strecke
- Kilometer
- wurde ein Vereins-Hänger gezogen
- Erstattungsbetrag

Für Reisekostenabrechnungen können PDFs erzeugt werden.

## 9.4 Konten / Finanzgruppen

Menü: **Finanzen → Durchführung → Konten**

Konten werden in KanuControl als **Finanzgruppen** geführt.

Eine Finanzgruppe fasst Teilnehmer zusammen, deren Zahlungen und Ausgaben gemeinsam betrachtet werden sollen, beispielsweise:

- Familien
- Geschwister
- Sammelzahler
- sonstige Teilnehmergruppen

Teilnehmer können einer Finanzgruppe zugeordnet werden. Buchungen, Zahlungsnachweise und weitere Finanzdaten können einer Finanzgruppe zugeordnet sein.

Für den späteren Finanzausgleich ist eine korrekte Zuordnung besonders wichtig.

Das Systemkonto **VK** steht für das Vereinskonto. Es wird für vereinsbezogene bzw. systemisch zugeordnete Finanzbewegungen verwendet.

---

# 10. Finanzen – Auswertung

## 10.1 Dashboard

Menü: **Finanzen → Auswertung → Dashboard**

Das Finanz-Dashboard stellt Planung und Ist-Zahlen gegenüber.

Es zeigt unter anderem:

- geplante Kosten
- tatsächliche Kosten
- Differenz Kosten
- geplante Einnahmen
- tatsächliche Einnahmen
- Differenz Einnahmen
- geplanter Saldo
- tatsächlicher Saldo
- Differenz Saldo

Zusätzlich werden Kosten und Einnahmen nach Finanzkategorien aufgeschlüsselt.

Damit lässt sich schnell erkennen, wo die tatsächlichen Werte von der Planung abweichen.

## 10.2 Finanzausgleich

Menü: **Finanzen → Auswertung → Finanzausgleich**

Der Finanzausgleich stellt die finanzielle Situation der einzelnen Finanzgruppen dem Vereinskonto gegenüber.

Für eine Finanzgruppe werden unter anderem angezeigt:

- Soll-Beiträge
- Überweisungen
- Quittungen
- Ausgaben
- Fahrkosten
- Ausgleich/Erstattung

Das VK-Konto wird gesondert betrachtet.

### Prüfung vor dem Ausgleich

KanuControl prüft insbesondere:

- ob Finanzgruppen konsistent sind
- ob Teilnehmer korrekt Finanzgruppen zugeordnet sind
- ob Beiträge fehlen
- ob Überzahlungen vorhanden sind

Mögliche Gesamtstatus sind:

- **OK**
- **FINANZGRUPPEN_ABWEICHUNG**
- **BEITRAEGE_FEHLEN**
- **UEBERZAHLUNG**

Bei einer Abweichung sollte zunächst die Ursache korrigiert werden, bevor der Finanzausgleich als abgeschlossen betrachtet wird.

### Fachliche Zuordnung der Zahlungen

Bei **Quittungen** ist die Finanzgruppe des Zahlungsnachweises maßgeblich.

Bei **Überweisungen** wird die Finanzgruppe des zugehörigen Teilnehmers berücksichtigt.

Rückzahlungen werden beim jeweiligen Zahlungsweg berücksichtigt.

### Nachweis

Für den Finanzausgleich kann ein Nachweis erzeugt und zugehörige Dokumentation verwaltet werden.

---

# 11. Dokumente und PDF-Erzeugung

Menü: **Dokumente**

KanuControl stellt abhängig von der Veranstaltung und den vorhandenen Daten verschiedene Dokumente bereit.

Dazu gehören unter anderem:

- Anmeldung FM/JEM
- Abrechnung FM/JEM
- Teilnehmerliste
- Erhebungsbogen
- Reisekostenabrechnungen
- Zahlungsnachweise
- Nachweis zum Finanzausgleich

Vor der PDF-Erzeugung werden die erforderlichen Daten geprüft. Fehlende Angaben werden angezeigt und müssen vor der Erstellung ergänzt werden.

## Digitale Belege

Belegdokumente können als Foto oder PDF gespeichert werden. Dokumente können angezeigt, heruntergeladen und gelöscht werden.

## Zahlungsnachweis-Dokumente

Auch Zahlungsnachweise können mit Dokumenten versehen werden. Dadurch lassen sich beispielsweise Überweisungsbelege oder Quittungen direkt beim Zahlungsvorgang dokumentieren.

---

# 12. Verwaltung

Menü: **Verwaltung**

Hier werden vereinsbezogene bzw. allgemeine Stammdaten gepflegt.

Dazu gehören insbesondere:

- Beitragsstrukturen
- Unterkunftsarten
- Verpflegungsmodelle

Zentrale Verwaltungsdaten wie Förder- und KiK-Daten sowie Postleitzahlen werden über die Administration gepflegt.

## 12.1 Administration

Administratoren haben zusätzliche Bereiche zur Verfügung, unter anderem:

- Postleitzahlen
- Fördersätze
- KiK-Zuschläge
- Reisekosten-Konfiguration
- Audit / Sitzungen
- Audit-Historie

Diese Funktionen sind nicht für die normale Veranstaltungsbearbeitung erforderlich.

---

# 13. Empfohlene Reihenfolge

Für eine neue Veranstaltung empfiehlt sich folgende Reihenfolge:

1. Verein anlegen bzw. prüfen.
2. Personen/Mitglieder erfassen oder per CSV importieren.
3. Stammdaten wie Beitragsstrukturen, Unterkunft und Verpflegung prüfen.
4. Veranstaltung anlegen.
5. Teilnehmer und Mitarbeiter zuordnen.
6. Finanzielle **Simulation** durchführen.
7. Simulation in die **Planung** übernehmen und speichern.
8. Teilnehmerbeiträge prüfen.
9. Für die Durchführung die **Konten/Finanzgruppen** anlegen und Teilnehmer zuordnen.
10. Zahlungsnachweise erfassen.
11. Belege und tatsächliche Buchungen erfassen.
12. Fahrkosten erfassen.
13. Finanz-Dashboard kontrollieren.
14. Vor dem Finanzausgleich die Finanzgruppen und Beiträge prüfen.
15. **Finanzausgleich** durchführen bzw. kontrollieren.
16. Benötigte PDFs und Nachweise erzeugen.

---

# 14. Wichtige Grundregeln

### Planung und Ist-Abrechnung trennen

Die Planung beschreibt die erwartete Finanzierung. Die Abrechnung und das Dashboard zeigen die tatsächlichen Werte.

### Finanzgruppen frühzeitig zuordnen

Teilnehmer sollten vor der finanziellen Auswertung einer passenden Finanzgruppe zugeordnet sein.

### Zahlungsweg korrekt erfassen

Die Unterscheidung zwischen Überweisung und Quittung beeinflusst die Zuordnung zum VK bzw. TN-Konto und damit den späteren Finanzausgleich.

### Belege digital hinterlegen

Wenn möglich sollten Belege direkt beim Erfassen als PDF oder Bild hinterlegt werden. Dadurch bleiben Buchung und Nachweis zusammen.

### Überzahlungen prüfen

Offene Überzahlungen sollten vor dem Abschluss der Veranstaltung kontrolliert und gegebenenfalls über Rückzahlungen verarbeitet werden.

---

# 15. Kurzfassung des Workflows

```text
Stammdaten
   ↓
Veranstaltung
   ↓
Teilnehmer
   ↓
Simulation
   ↓
Planung
   ↓
Beiträge / Zahlungsnachweise
   ↓
Konten / Finanzgruppen
   ↓
Belege / Buchungen / Fahrkosten
   ↓
Dashboard
   ↓
Finanzausgleich
   ↓
PDFs und Nachweise
```

KanuControl V1.6.3 verbindet damit die Planung, Durchführung und finanzielle Auswertung einer Veranstaltung in einem durchgängigen Ablauf.
