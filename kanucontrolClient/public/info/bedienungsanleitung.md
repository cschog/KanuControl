# KanuControl (KC) – Kurzanleitung

## 1. Verein anlegen

Bevor mit KanuControl gearbeitet werden kann, muss zunächst ein Verein angelegt werden.

Menü:
**Vereine → Neu**

Erfassen Sie alle erforderlichen Stammdaten des Vereins, insbesondere:

- Vereinsname
- Anschrift
- Bankverbindung
- Ansprechpartner

Nach dem Speichern steht der Verein für weitere Arbeitsschritte zur Verfügung.

---

## 2. Mitglieder erfassen

Für Veranstaltungen müssen zunächst Personen bzw. Mitglieder angelegt werden.

Hierfür gibt es zwei Möglichkeiten:

### Direkte Erfassung

Menü:
**Mitglieder**

Neue Personen können einzeln erfasst und gespeichert werden.

### CSV-Import

Menü:

**Vereine → Verein bearbeiten → CSV-Import**

Über den CSV-Import können größere Mitgliederbestände schnell übernommen werden. Dazu kann eine Beispiel CSV -Mapping Datei heruntergeladen werden, die auch angepasst werden kann.

Nach dem Import stehen die Personen sofort für Veranstaltungen zur Verfügung.

---

## 3. Veranstaltung anlegen

Menü:
**Veranstaltungen**

Für jede Maßnahme oder Fahrt wird eine Veranstaltung angelegt.

Erfasst werden unter anderem:

- Veranstaltungsname
- Zeitraum
- Veranstaltungsort
- Veranstaltungsleitung
- Verein

Anschließend können Teilnehmer hinzugefügt werden.

---

## 4. Teilnehmer verwalten

Teilnehmer werden einer Veranstaltung zugeordnet.

Für die Erstellung von Teilnehmerlisten und Förderunterlagen sollten die Personendaten möglichst vollständig gepflegt sein.

Insbesondere sind Geburtsdaten und Postleitzahlen erforderlich.

---

## 5. Planung

### Planung einer Veranstaltung

Die Planung dient dazu, die Ergebnisse einer Simulation dauerhaft für eine Veranstaltung zu übernehmen. Sie bildet die Grundlage für den späteren Förderantrag sowie für die Erstellung der Anmeldeunterlagen.

#### Ablauf

1. Wählen Sie zunächst die gewünschte Veranstaltung aus.
2. Führen Sie eine Simulation durch und passen Sie alle relevanten Werte an (Teilnehmerzahl, Beiträge, Unterkunft, Verpflegung, Fahrtkosten usw.).
3. Übernehmen Sie die Simulation in die Planung.
4. Speichern Sie die Planung.

Alle Planungsdaten werden dauerhaft zur Veranstaltung gespeichert.

> **Hinweis:** Bereits gespeicherte Planungen werden beim erneuten Öffnen wieder geladen. Die eingegebenen Werte bleiben somit erhalten und werden nicht durch Standardwerte ersetzt.

### Gespeicherte Planungsdaten

Die Planung speichert unter anderem:

- Anzahl der Teilnehmer und Mitarbeiter
- Teilnehmerbeiträge
- Unterkunftskosten
- Verpflegungskosten
- Fahrtkosten
- Honorare
- Verbrauchsmaterial
- Kulturprogramm
- Miete
- Pfand
- Sonstige Kosten
- Sonstige Einnahmen
- Fördermittel (KJFP)
- KiK-Zuschlag (falls vorhanden)
- Eigenanteil
- Gesamtkosten und Gesamteinnahmen

Damit entspricht die Planung dem zum Zeitpunkt der Antragstellung erwarteten Finanzierungsplan der Veranstaltung.

### Zusammenhang zwischen Simulation und Planung

Simulation und Planung erfüllen unterschiedliche Aufgaben:

| Simulation | Planung |
|------------|----------|
| Beliebig oft veränderbar | Dauerhaft gespeichert |
| Dient zum Ausprobieren verschiedener Szenarien | Enthält die beschlossene Planung |
| Änderungen wirken sich nicht auf gespeicherte Daten aus | Grundlage für die weitere Bearbeitung |

Die Simulation dient somit als Planungswerkzeug, während die Planung die verbindlichen Daten der Veranstaltung enthält.

### Verwendung der Planungsdaten

Die gespeicherten Planungsdaten werden im weiteren Verlauf des Arbeitsablaufs mehrfach verwendet.

Sie bilden unter anderem die Grundlage für:

- die Erstellung der Förderanmeldung,
- die automatische Erzeugung der Anmelde-PDFs,
- die Berechnung der Fördermittel,
- die spätere finanzielle Abrechnung der Veranstaltung.

Dadurch müssen die Daten nur einmal erfasst werden und stehen während des gesamten Lebenszyklus einer Veranstaltung konsistent zur Verfügung.

## 6. Kürzel für Kosten und Einnahmen anlegen

Menü:
**Finanzen**

Alle Ist-Kosten und Ist-Einnahmen werden über Kürzel erfasst.

Vor der Erfassung von Belegen muss daher mindestens ein Kürzel angelegt werden.

Ein Kürzel dient der Zuordnung von Kosten und Einnahmen zu einer bestimmten Personengruppe oder Kostenstelle.

Jedem Kürzel können eine oder mehrere Personen zugeordnet werden.

---

## 7. Abrechnung

Menü:
**Finanzen → Abrechnung**

Der Menuepunkt ist noch in Vorbereitung

---

## 8. Fahrkosten erfassen

Menü:
**Finanzen → Fahrkosten**

Fahrtkosten können separat erfasst werden.

Hierzu werden unter anderem gespeichert:

- Fahrer
- Mitfahrer
- Strecke
- Kilometer
- Erstattungsbetrag

Die berechneten Fahrkosten werden summiert im Finanz-Dashboard und bei den Buchungen berücksichtigt.

Für jede Fahrkostenabrechnung kann später ein PDF erzeugt werden.

---

## 9. Beitragsstrukturen verwalten

Menü:  
**Verwaltung → Beitragsstrukturen**

Mit Beitragsstrukturen können individuelle Teilnehmerbeiträge für Veranstaltungen erstellt werden.

Eine Beitragsstruktur besteht aus einer oder mehreren Regeln. Jede Regel definiert einen Beitrag für eine bestimmte Altersgruppe und optional für eine bestimmte Rolle (z. B. Teilnehmer oder Mitarbeiter).

Für jede Regel werden folgende Angaben festgelegt:

- Alter von
- Alter bis
- Rolle
- Beitrag

Die Regeln werden bei der Berechnung der Teilnehmerbeiträge automatisch ausgewertet. KanuControl ordnet jedem Teilnehmer anhand seines Alters und seiner Rolle den passenden Beitrag zu.

Eine Beitragsstruktur kann beliebig viele Regeln enthalten und für mehrere Veranstaltungen verwendet werden.

### Beitragsstruktur einer Veranstaltung zuordnen

Menü:  
**Veranstaltungen → Veranstaltung bearbeiten**

Im Feld **Beitragsstruktur** kann eine zuvor angelegte Struktur ausgewählt werden, wenn die Option **Individuelle Gebühren** aktiviert worden ist.

Nach der Zuordnung berechnet KanuControl die Teilnehmerbeiträge automatisch für alle der Veranstaltung zugeordneten Personen.

### Teilnehmerbeiträge verwalten

Menü:  
**Finanzen → Beiträge**

Im Bereich **Beiträge** werden alle Teilnehmer mit ihrem festgelegten Beitrag angezeigt. Das sind entweder ein Beitrag, der für jeden Teilnehmer gilt oder die zugeordnete Beitragsstruktur.

Zusätzlich können hier:

- Zahlungseingänge erfasst
- offene Beträge überwacht
- Gesamtsummen kontrolliert

werden.

KanuControl berechnet automatisch die Summe der Teilnehmerbeiträge sowie die offenen und bereits bezahlten Beträge der Veranstaltung.

---

## 10. PDF-Dokumente erzeugen

Menü:
**Dokumente**

KanuControl kann verschiedene PDF-Dokumente erzeugen.

Verfügbare Dokumente:

- Anmeldung (FM/JEM)
- Abrechnung (FM/JEM)
- Teilnehmerliste
- Erhebungsbogen
- Reisekostenabrechnung(en)

Vor der Erstellung prüft KanuControl, ob alle erforderlichen Daten vorhanden sind.

Fehlende Angaben werden angezeigt und müssen zunächst ergänzt werden bevor das entsprechende PDF erzeugt werden kann.

---

## Empfohlene Reihenfolge

1. Verein anlegen
2. Mitglieder erfassen oder importieren
3. Veranstaltung anlegen
4. Teilnehmer zuordnen
5. Finanzplanung erfassen
6. Kürzel anlegen
7. Ist-Kosten und Ist-Einnahmen erfassen
8. Reisekosten erfassen
9. PDF-Dokumente erzeugen