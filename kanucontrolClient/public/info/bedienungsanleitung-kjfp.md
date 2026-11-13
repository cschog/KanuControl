# Bedienungsanleitung KJFP

**Anwendung:** KanuControl 2.2.2 · **Dokumentationsstand:** 13. November 2026

Diese Anleitung beschreibt das Modul **KJFP**: Veranstaltungen, Teilnehmer, Förderung und Veranstaltungsfinanzen. Die [Vermietung hat eine eigene Anleitung](/info/bedienungsanleitung-vermietung.md). Über das Fragezeichen im Kopfbereich kannst du beide Anleitungen jederzeit öffnen.

## Inhalt

- [1. Überblick](#1-uberblick)
- [2. Startmenü und Navigation](#2-startmenu-und-navigation)
- [3. Datenstatus und kontextabhängige Validierung](#3-datenstatus-und-kontextabhangige-validierung)
- [4. Vereine](#4-vereine)
- [5. Personen und Mitglieder](#5-personen-und-mitglieder)
- [6. Veranstaltungen](#6-veranstaltungen)
- [7. Teilnehmer](#7-teilnehmer)
- [8. Finanzen – Bereiche](#8-finanzen-bereiche)
- [9. Finanzen – Vorbereitung](#9-finanzen-vorbereitung)
- [10. Finanzen – Durchführung](#10-finanzen-durchfuhrung)
- [11. Finanzen – Auswertung](#11-finanzen-auswertung)
- [12. Dokumente und PDF-Erzeugung](#12-dokumente-und-pdf-erzeugung)
- [13. Verwaltung](#13-verwaltung)
- [14. Sitzung und automatische Abmeldung](#14-sitzung-und-automatische-abmeldung)
- [15. Empfohlene Reihenfolge](#15-empfohlene-reihenfolge)
- [16. Wichtige Grundregeln](#16-wichtige-grundregeln)
- [17. Kurzfassung des Workflows](#17-kurzfassung-des-workflows)
- [18. Häufige Fragen](#18-haufige-fragen)


## 1. Überblick

KanuControl begleitet eine Veranstaltung über den gesamten Lebenszyklus
-- von der Erfassung der Stammdaten und der finanziellen Vorbereitung
über Teilnehmer und laufende Abrechnung bis zur Auswertung und zum
Finanzausgleich.

Die Anwendung ist mandantenfähig. Im Startbereich wird der aktuell
aktive Mandant angezeigt. Die allgemeinen Module und die Finanzmodule
sind getrennt strukturiert.

### Der grundsätzliche Ablauf

``` text
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

Nach der Anmeldung wählst du **KJFP** in der Modulauswahl. Ist nur dieses Modul freigeschaltet, wird es direkt geöffnet. Im KJFP-Startmenü stehen die folgenden Bereiche zur Verfügung:

-   **Vereine**
-   **Mitglieder** (nachdem es mind. einen Verein gibt)
-   **Veranstaltungen** (nachdem es mindestens eine Person gibt)
-   **Teilnehmer**
-   **Dokumente**
-   **Verwaltung**

Zusätzlich stehen – sobald eine Veranstaltung vorhanden ist – die drei
Finanzbereiche zur Verfügung:

-   **Vorbereitung**
-   **Durchführung**
-   **Auswertung**

Die Finanzbereiche beziehen sich auf die aktuell ausgewählte
Veranstaltung.

Im Kopfbereich zeigt KanuControl außerdem den aktiven Mandanten sowie
die Frontend- und Backend-Version an.

## 3. Datenstatus und kontextabhängige Validierung

KanuControl unterscheidet zwischen der Frage, ob ein Datensatz gespeichert
werden darf, und der Frage, ob er für eine konkrete Verwendung
vollständig genug ist.

### Speichervalidierung

Die Speichervalidierung beantwortet:

> **Darf dieser Datensatz gespeichert werden?**

Die technischen und fachlichen Voraussetzungen für das Speichern bleiben
bestehen.

### Dynamischer Datenstatus

Zusätzlich bewertet KanuControl abhängig von der aktuellen Verwendung,
welche Angaben benötigt werden.

-   🔴 **Rot – erforderlich:** Eine für die aktuelle Verwendung
    benötigte Angabe fehlt.
-   🟡 **Gelb – empfohlen:** Die Angabe ist für spätere Vorgänge oder
    Dokumente sinnvoll, blockiert den aktuellen Vorgang aber nicht.
-   **Kein Status:** Aktuell besteht kein Handlungsbedarf.

Die Statusanzeige ist dynamisch. Ein Feld kann zunächst ohne
Kennzeichnung sein und später – beispielsweise wenn eine Person als
Veranstaltungsleiter eingesetzt wird – erforderlich werden.

### Wo werden Datenstatus angezeigt?

Relevante Status werden insbesondere angezeigt in:

-   Personenlisten
-   Teilnehmerlisten
-   Vereinslisten
-   Veranstaltungslisten
-   Detailformularen

Es gibt bewusst **kein grünes Statussymbol**. Vollständige Datensätze
benötigen keine zusätzliche Kennzeichnung.

### Vorgangsvalidierung

Bei einem konkreten Vorgang, insbesondere bei der PDF-Erstellung, prüft
KanuControl erneut die tatsächlich benötigten Daten.

-   🔴 Fehlende erforderliche Daten können den Vorgang bzw. die
    PDF-Erstellung blockieren.
-   🟡 Fehlende empfohlene Daten blockieren den Vorgang nicht.
-   Optionale fehlende Angaben können bei geeigneten Dokumenten
    gegebenenfalls manuell ergänzt werden.

### Tooltips

Bei einem Datenstatus kann ein Tooltip erklären, warum eine Angabe
relevant ist.

Beispiel:

> „Erforderlich für die Verwendung als Veranstaltungsleiter."

Bei empfohlenen Angaben wird erläutert, für welchen späteren Vorgang die
Information hilfreich ist.

------------------------------------------------------------------------

## 4. Vereine

### Verein anlegen

Menü: **Vereine → Neuer Verein**

Hier werden die Stammdaten des Vereins erfasst, insbesondere:

-   Vereinsname
-   Anschrift
-   Kontaktdaten
-   Bankverbindung
-   Kontoinhaber

Ein Verein bildet die Grundlage für Personen, Veranstaltungen und die
zugehörigen Stammdaten.

### Datenstatus beim Verein

Nicht alle Vereinsdaten müssen bereits bei der Anlage vollständig
vorhanden sein.

Insbesondere

-   Kontoinhaber
-   Bankname
-   IBAN
-   BIC

müssen nicht pauschal bei jeder Neuanlage vollständig sein.

Wird der Verein als **Veranstalter** verwendet, können bestimmte Angaben
für den weiteren Prozess erforderlich werden. Bankdaten können weiterhin
als 🟡 empfohlen angezeigt werden, wenn sie für spätere Dokumente oder
Vorgänge sinnvoll sind.

### Verein bearbeiten

Bestehende Vereine können geöffnet, geändert, kopiert und gelöscht
werden. Über die Vereinsbearbeitung steht außerdem der **CSV-Import**
zur Verfügung.

## 5. Personen und Mitglieder

Menü: **Mitglieder**

Personen können einzeln angelegt und anschließend einem oder mehreren
Vereinen zugeordnet werden.

Typische Personendaten sind:

-   Name und Vorname
-   Geburtsdatum
-   Geschlecht
-   Anschrift
-   Postleitzahl und Ort
-   Kontaktdaten

Die Bedeutung einzelner Personendaten hängt von der
aktuellen Verwendung ab.

### Datenstatus bei Personen

Eine Person kann zunächst mit Grunddaten angelegt werden. Werden
zusätzliche Funktionen verwendet, können weitere Angaben erforderlich
werden.

Ein wichtiger Anwendungsfall ist die Verwendung als
**Veranstaltungsleiter**. Dann werden insbesondere folgende Angaben
relevant:

-   Geburtsdatum
-   Straße
-   Postleitzahl
-   Ort
-   E-Mail-Adresse
-   Telefonnummer

Fehlende erforderliche Angaben werden 🔴 gekennzeichnet und über einen
Tooltip erläutert. Empfohlene Angaben können 🟡 angezeigt werden.

Die Person kann auch bei fehlenden kontextabhängigen Angaben weiterhin
gespeichert und bearbeitet werden.

### CSV-Import

Über die Vereinsbearbeitung können größere Personenbestände per CSV
importiert werden.

Der Import unterstützt eine Mapping-Datei. Damit können die Spalten
einer vorhandenen CSV-Datei den von KanuControl erwarteten Feldern
zugeordnet werden.

Empfohlener Ablauf:

1.  Verein öffnen.
2.  **CSV importieren** auswählen.
3.  CSV-Datei auswählen.
4.  Mapping-Datei auswählen bzw. die Zuordnung anpassen.
5.  Import starten.
6.  Importbericht und eventuelle Fehler prüfen.

### CSV-Export und Personenauswahl

In der Personenverwaltung können Personen gezielt ausgewählt und für
einen **CSV-Export** zusammengestellt werden.

Der Export eignet sich insbesondere für die Weiterverarbeitung von
Personendaten außerhalb von KanuControl.

## 6. Veranstaltungen

Menü: **Veranstaltungen → Neue Veranstaltung**

Eine Veranstaltung enthält die organisatorischen und finanziellen
Rahmendaten einer Maßnahme.

Dazu gehören unter anderem:

-   Veranstaltungsname
-   KJFP- und Fahrten-Aktivierung; bei KJFP wird FM/JEM aus der Dauer ermittelt
-   Beginn und Ende
-   Veranstaltungsort
-   Leitung
-   Unterkunftsart
-   Verpflegungsmodell
-   Beitragsstruktur bzw. individuelle Gebühren
-   weitere veranstaltungsbezogene Angaben

### Dynamische Anforderungen

Für eine KJFP-Veranstaltung werden Name, Verein, Leitung, Beginn und Ende mit Uhrzeit benötigt. Die Art **FM** (1–4 Kalendertage) bzw. **JEM** (ab 5 Kalendertagen) wird automatisch aus der Dauer einschließlich An- und Abreisetag ermittelt.

Weitere Angaben müssen nicht vollständig bei der Anlage vorliegen.

Mit zunehmender Verwendung können zusätzliche Anforderungen aktiviert
werden. KanuControl zeigt über die Datenstatus an, welche Angaben für
den aktuellen Arbeitsschritt noch fehlen oder empfohlen werden.

Die Vollständigkeit wird damit schrittweise aufgebaut und nicht pauschal
bei der Neuanlage erzwungen.

### Veranstaltungsdaten und Programmentwurf

Die Maske hat zwei unabhängig aufklappbare Bereiche: **Veranstaltungsdaten** ist anfangs geöffnet, **Programmentwurf** geschlossen. Beide dürfen gleichzeitig offen sein. Zuklappen erhält die Eingaben; gespeichert wird gemeinsam über **Speichern** bzw. **Anlegen**.

Im Programmentwurf stehen eine einzeilige Überschrift mit höchstens 200 Zeichen und ein formatierbarer Text mit höchstens 50.000 sichtbaren Zeichen zur Verfügung. Unterstützt werden Fett, Kursiv, deren Kombination, Aufzählungen und nummerierte Listen ohne Verschachtelung, Absätze und manuelle Zeilenumbrüche. **Formatierung entfernen** erhält den Text und löst Listen auf. Fremde Schriftarten, Farben und nicht unterstützte Inhalte werden beim Einfügen entfernt. Bei der ersten Texteingabe wird die Überschrift „Programmentwurf“ vorgeschlagen; sie bleibt frei änderbar.

Der Bereichskopf zeigt, ob Programmtext vorhanden ist. Ein Titel allein gilt nicht als Programmtext. Der gesamte Entwurf ist optional. Nach dem Leeren des Texts und Speichern entfallen die Programmseiten der nächsten Anmeldung. Vor dem Verlassen ungespeicherte Änderungen speichern oder bewusst verwerfen.

### Veranstaltung kopieren

1. Die gewünschte Vorlage öffnen und **Kopieren** wählen.
2. Im Dialog **Veranstaltung mit Planung kopieren** Name, Termine und Veranstaltungsdaten prüfen bzw. ändern.
3. Mit **Anlegen** die neue Veranstaltung samt eigener Planung speichern.

Übernommen werden auch Beschreibung, Programmentwurf, Unterkunft, Verpflegung, Land/PLZ/Ort, Beitragsstruktur und vorhandene Gebühreneinstellungen. Die Planung enthält die gespeicherten Teilnehmer- und Mitarbeiterzahlen, Beiträge, Preise, Kostenansätze und Planungspositionen. Automatische Beträge werden für die neuen Veranstaltungsdaten neu berechnet.

Die neue Planung steht immer **In Bearbeitung**, auch wenn die Vorlage bereits eingereicht ist. Änderungen der Kopie verändern die ursprüngliche Planung nicht. Hat die Vorlage noch keine gespeicherte Planung, legt KC eine Planung mit Standardwerten an. Teilnehmerlisten, Abrechnung und Belege werden nicht kopiert; die Leitung wird wie bei einer Neuanlage zugeordnet. Die neue Veranstaltung wird nach dem Anlegen aktiv. Bei einem Speicherfehler bleibt der Dialog mit den Eingaben geöffnet.

### Aktive Veranstaltung

Die als **aktiv** gewählte Veranstaltung bestimmt den Arbeitskontext. Teilnehmer, Dokumente und die Veranstaltungsfinanzen beziehen sich auf diese Veranstaltung. Prüfe vor der Bearbeitung den im Kopfbereich angezeigten Kontext.

## 7. Teilnehmer

Menü: **Teilnehmer** bzw. Teilnehmerverwaltung innerhalb einer
Veranstaltung

Personen werden einer Veranstaltung als Teilnehmer oder Mitarbeiter
zugeordnet.

Bei der Teilnehmerverwaltung können insbesondere Rollen und
veranstaltungsbezogene Daten gepflegt werden.

Die Teilnehmerdaten bilden die Grundlage für:

-   Teilnehmerlisten
-   Förderunterlagen
-   Teilnehmerbeiträge
-   Finanzgruppen/Konten
-   spätere Abrechnung und Auswertung

Fehlende oder unvollständige Personendaten sollten möglichst früh
ergänzt werden.

## 8. Finanzen – Bereiche

Die Finanzverwaltung im KJFP-Modul ist in drei Bereiche
gegliedert.

### Vorbereitung

-   **Simulation**
-   **Planung**

### Durchführung

-   **Beiträge**
-   **Abrechnung**
-   **Fahrkosten**
-   **Konten**

### Auswertung

-   **Dashboard**
-   **Finanzausgleich**

Die Planung wird im Menü nur angezeigt, wenn für die Veranstaltung
bereits eine Planung vorhanden ist bzw. die entsprechende Funktion
verfügbar ist.

------------------------------------------------------------------------

## 9. Finanzen – Vorbereitung

### 9.1 Simulation

Die Simulation dient dazu, die Finanzierung einer Veranstaltung vor der
verbindlichen Planung durchzuspielen.

Verändert werden können Teilnehmer- und Mitarbeiterzahlen, Beiträge sowie die Preise und Ansätze für Unterkunft, Verpflegung, Fahrkosten, Honorare, Verbrauchsmaterial, Kultur, Miete und sonstige Kosten. Die Berechnung berücksichtigt die Veranstaltungsdauer, die hinterlegten Fördersätze und gegebenenfalls KiK.

Nach einer Eingabe wird automatisch neu gerechnet. **Beitragsvorschlag übernehmen** setzt die vorgeschlagenen Teilnehmer- und Mitarbeiterbeiträge ein. Änderst du einen dieser Beiträge anschließend manuell, wird die Schaltfläche wieder aktiv. Erst **Simulation speichern** übernimmt die Änderungen dauerhaft in die Planung. Bei eingereichter Planung sind Speichern und Beitragsübernahme gesperrt; die Planung muss zuerst wieder geöffnet werden.

#### Voraussetzungen der Simulation

Der aktuelle Simulationsablauf benötigt eine **Beitragsstruktur**, eine **Unterkunftsart** und ein **Verpflegungsmodell** an der Veranstaltung. Fehlende Voraussetzungen werden angezeigt. Für Maßnahmen ohne Übernachtung können die berechneten Übernachtungskosten null sein; die Auswahl der Unterkunftsart wird im aktuellen Formular trotzdem vorausgesetzt.

#### KiK

Die Simulation berücksichtigt das KiK-Zertifikat des Vereins. Das
Vorhandensein eines gültigen KiK-Zertifikats kann in der Simulation
berücksichtigt bzw. simuliert werden.

Der KiK-Zuschlag wird anhand der in KanuControl hinterlegten gültigen
Daten berechnet.

### 9.2 Planung

Die Planung übernimmt die Ergebnisse der Simulation in die dauerhaft
gespeicherte Veranstaltungsplanung.

Die Planung enthält unter anderem:

-   Teilnehmer- und Mitarbeiterzahlen
-   Teilnehmerbeiträge
-   Unterkunftskosten
-   Verpflegungskosten
-   Fahrkosten
-   Honorare
-   Verbrauchsmaterial
-   Kulturprogramm
-   Miete
-   sonstige Kosten
-   KJFP-Förderung
-   KiK-Zuschlag, sofern vorhanden
-   Eigenanteil
-   Gesamtkosten und Gesamteinnahmen

#### Simulation und Planung

- **Simulation:** Szenarien ausprobieren und berechnen. Änderungen überschreiben eine bereits gespeicherte Planung nicht automatisch.
- **Planung:** Das gewählte Szenario dauerhaft für die Veranstaltung speichern und für die weitere Verarbeitung verwenden.

Die gespeicherte Planung wird im weiteren Ablauf unter anderem für
Förderunterlagen und die finanzielle Weiterverarbeitung verwendet.

------------------------------------------------------------------------

## 10. Finanzen – Durchführung

### 10.1 Beiträge

Menü: **Finanzen → Durchführung → Beiträge**

Hier werden die Teilnehmerbeiträge der Veranstaltung verwaltet.

KanuControl kann Beiträge auf Basis eines festen Beitrags oder einer
Beitragsstruktur berechnen.

#### Beitragsstrukturen

Eine Beitragsstruktur besteht aus Regeln. Eine Regel kann insbesondere
festlegen:

-   Altersbereich
-   Rolle
-   Beitrag

Damit können beispielsweise unterschiedliche Beiträge für Teilnehmer und
Mitarbeiter oder für verschiedene Altersgruppen hinterlegt werden.

#### Zahlungsnachweise

Zahlungseingänge werden über **Zahlungsnachweise** dokumentiert.

Bei einem Zahlungsnachweis werden unter anderem Zahlungsweg, Betrag und
die zugehörigen Teilnehmer bzw. Zahlungspositionen erfasst. Zu einem
Zahlungsnachweis können Dokumente hinterlegt werden.

#### Überweisung und Quittung

KanuControl unterscheidet zwischen zwei Zahlungswegen:

**Überweisung:** Für die Zuordnung im Finanzausgleich ist die Finanzgruppe des jeweiligen Teilnehmers maßgeblich.

**Quittung:** Für die Zuordnung ist die Finanzgruppe des Zahlungsnachweises maßgeblich. Wähle dort das Konto der Person oder Gruppe, die das Geld tatsächlich entgegengenommen hat.

In der VK-Auswertung werden die Zahlungsnachweise nach Zahlungsweg und Betrag berücksichtigt. Rückzahlungen vermindern die zugehörigen Eingänge. Bei Sammelzahlungen sollten sowohl die Zahlungspositionen als auch die Finanzgruppe geprüft werden.

#### Überzahlungen

KanuControl erkennt offene Überzahlungen. Diese können geprüft und --
sofern erforderlich – über die Funktion **Rückzahlung** verarbeitet
werden.

#### Rückzahlungen

Eine Rückzahlung eines Teilnehmerbeitrags wird mit dem ursprünglichen
Zahlungsnachweis verknüpft. Dadurch bleibt nachvollziehbar, auf welchen
Zahlungseingang sich die Rückzahlung bezieht.

Rückzahlungen werden bei der Finanzberechnung berücksichtigt und
vermindern den ursprünglich eingegangenen Betrag entsprechend dem
Zahlungsweg.

### 10.2 Abrechnung

Menü: **Finanzen → Durchführung → Abrechnung**

Die Abrechnung dient zur Erfassung der tatsächlichen Kosten und
Einnahmen.

#### Belege

Für einen Beleg können unter anderem erfasst werden:

-   Datum
-   Belegnummer
-   Aussteller
-   Beschreibung
-   Summe
-   Finanzgruppe/Konto
-   Positionen
-   Dokumente

Dokumente können Fotos und PDF-Dateien sein. Ein Foto einer Rechnung
sollte vollständig und gut lesbar sein. Prüfe die Ausrichtung und die PDF-Vorschau vor der Weiterverwendung.

#### Buchungen

Eine Buchung wird einer Finanzkategorie und einem Konto bzw. einer
Finanzgruppe zugeordnet. Die Herkunft der Buchung wird gespeichert,
sodass automatisch erzeugte und manuell erfasste Buchungen unterschieden
werden können.

Die Finanzkategorien trennen **Kosten** und **Einnahmen**.

Beispiele für Kosten:

-   Unterkunft
-   Verpflegung
-   Fahrkosten
-   Honorare
-   Verbrauchsmaterial
-   Kultur
-   Miete
-   sonstige Kosten

Beispiele für Einnahmen:

-   Teilnehmerbeiträge
-   Zuschüsse
-   Pfand
-   sonstige Einnahmen

### 10.3 Fahrkosten

Menü: **Finanzen → Durchführung → Fahrkosten**

Fahrkosten werden separat erfasst und können anschließend in die
finanzielle Auswertung einfließen.

Eine Reisekostenabrechnung kann insbesondere enthalten:

-   Fahrer
-   Mitfahrer
-   Fahrtabschnitte
-   Strecke
-   Kilometer
-   wurde ein Vereins-Hänger gezogen
-   Erstattungsbetrag

Für Reisekostenabrechnungen können PDFs erzeugt werden.

### 10.4 Konten / Finanzgruppen

Menü: **Finanzen → Durchführung → Konten**

Konten werden in KanuControl als **Finanzgruppen** geführt.

Eine Finanzgruppe fasst Teilnehmer zusammen, deren Zahlungen und
Ausgaben gemeinsam betrachtet werden sollen, beispielsweise:

-   Familien
-   Geschwister
-   Sammelzahler
-   sonstige Teilnehmergruppen

Teilnehmer können einer Finanzgruppe zugeordnet werden. Buchungen,
Zahlungsnachweise und weitere Finanzdaten können einer Finanzgruppe
zugeordnet sein.

Für den späteren Finanzausgleich ist eine korrekte Zuordnung besonders
wichtig.

Das Systemkonto **VK** steht für das Vereinskonto. Es wird für
vereinsbezogene bzw. systemisch zugeordnete Finanzbewegungen verwendet.

------------------------------------------------------------------------

## 11. Finanzen – Auswertung

### 11.1 Dashboard

Menü: **Finanzen → Auswertung → Dashboard**

Das Finanz-Dashboard stellt Planung und Ist-Zahlen gegenüber.

Es zeigt unter anderem:

-   geplante Kosten
-   tatsächliche Kosten
-   Differenz Kosten
-   geplante Einnahmen
-   tatsächliche Einnahmen
-   Differenz Einnahmen
-   geplanter Saldo
-   tatsächlicher Saldo
-   Differenz Saldo

Zusätzlich werden Kosten und Einnahmen nach Finanzkategorien
aufgeschlüsselt.

Damit lässt sich schnell erkennen, wo die tatsächlichen Werte von der
Planung abweichen.

### 11.2 Finanzausgleich

Menü: **Finanzen → Auswertung → Finanzausgleich**

Der Finanzausgleich stellt die finanzielle Situation der einzelnen
Finanzgruppen dem Vereinskonto gegenüber.

Für eine Finanzgruppe werden unter anderem angezeigt:

-   Soll-Beiträge
-   Überweisungen
-   Quittungen
-   Ausgaben
-   Fahrkosten
-   Ausgleich/Erstattung

Das VK-Konto wird gesondert betrachtet.

#### Prüfung vor dem Ausgleich

KanuControl prüft insbesondere:

-   ob Finanzgruppen konsistent sind
-   ob Teilnehmer korrekt Finanzgruppen zugeordnet sind
-   ob Beiträge fehlen
-   ob Überzahlungen vorhanden sind

Mögliche Gesamtstatus sind:

-   **OK**
-   **FINANZGRUPPEN_ABWEICHUNG**
-   **BEITRAEGE_FEHLEN**
-   **UEBERZAHLUNG**

Bei einer Abweichung sollte zunächst die Ursache korrigiert werden,
bevor der Finanzausgleich als abgeschlossen betrachtet wird.

#### Fachliche Zuordnung der Zahlungen

Bei **Quittungen** ist die Finanzgruppe des Zahlungsnachweises
maßgeblich.

Bei **Überweisungen** wird die Finanzgruppe des zugehörigen Teilnehmers
berücksichtigt.

Rückzahlungen werden beim jeweiligen Zahlungsweg berücksichtigt.

#### Nachweis

Für den Finanzausgleich kann ein Nachweis erzeugt und zugehörige
Dokumentation verwaltet werden.

------------------------------------------------------------------------

## 12. Dokumente und PDF-Erzeugung

Menü: **Dokumente**

KanuControl stellt abhängig von der Veranstaltung und den vorhandenen
Daten verschiedene Dokumente bereit.

Dazu gehören unter anderem:

-   Anmeldung FM/JEM
-   Abrechnung FM/JEM
-   Teilnehmerliste
-   Erhebungsbogen
-   Reisekostenabrechnungen
-   Zahlungsnachweise
-   Nachweis zum Finanzausgleich

Vor der PDF-Erzeugung werden die für das jeweilige Dokument
erforderlichen Daten geprüft.

Dabei unterscheidet KanuControl zwischen:

-   🔴 **erforderlichen Angaben:** Fehlen diese, kann die PDF-Erstellung
    blockiert werden.
-   🟡 **empfohlenen Angaben:** Die PDF kann trotzdem erzeugt werden.
    Fehlende Angaben können bei geeigneten Dokumenten gegebenenfalls
    manuell ergänzt werden.

Die Dokumentprüfung berücksichtigt damit den konkreten Zweck des
Dokuments und nicht pauschal die Vollständigkeit aller Stammdaten.

### Anmeldung mit Programmentwurf

**Anmeldung (Vorschau)** und **Anmeldung (Download)** verwenden den zuletzt gespeicherten Stand. Die bestehende Anmeldung hat zwei Seiten; der Programmtext beginnt danach auf **Seite 3** und wird bei Bedarf auf weitere Seiten umgebrochen. Name und Zeitraum der Veranstaltung stehen im Kopf der Programmseiten. Eine interne Veranstaltungsnummer wird dort nicht ausgegeben.

Unterstützte Hervorhebungen, Listen und Zeilenumbrüche werden übernommen. Ohne Programmtext entstehen keine Zusatzseiten, auch bei einem Titel oder ausschließlich leeren Absätzen. Nach Änderungen zuerst die Veranstaltung speichern und die PDF erneut erzeugen. Ein Upload eines endgültigen Programm-PDFs ist in dieser Ausbaustufe nicht vorgesehen.

In der Kostenaufstellung der Anmeldung wird **Kultur / Programm** zusammen mit sonstigen Kosten unter **Sonstige Kosten** ausgewiesen. Fahrkosten stehen in der eigenen Fahrkostenzeile. Beide Beträge fließen in die Gesamtsummen ein.

### Digitale Belege

Belegdokumente können als Foto oder PDF gespeichert werden. Dokumente
können angezeigt, heruntergeladen und gelöscht werden.

### Zahlungsnachweis-Dokumente

Auch Zahlungsnachweise können mit Dokumenten versehen werden. Dadurch
lassen sich beispielsweise Überweisungsbelege oder Quittungen direkt
beim Zahlungsvorgang dokumentieren.

------------------------------------------------------------------------

## 13. Verwaltung

Menü: **Verwaltung**

Hier werden vereinsbezogene bzw. allgemeine Stammdaten gepflegt.

Dazu gehören insbesondere:

-   Beitragsstrukturen
-   Unterkunftsarten
-   Verpflegungsmodelle

Auch Fördersätze, KiK-Zuschläge und die Fahrkosten-Konfiguration werden hier gepflegt. Für alle fachlichen Funktionen genügt die Rolle KJFP; eine zusätzliche ADMIN-Rolle ist nicht erforderlich.

Postleitzahlen und Audit-Funktionen bleiben in der Administration.

### 13.1 Administration

Administratoren haben zusätzliche Bereiche zur Verfügung, unter anderem:

-   Postleitzahlen
-   Audit / Sitzungen
-   Audit-Historie

Diese Funktionen sind nicht für die normale Veranstaltungsbearbeitung
erforderlich.

------------------------------------------------------------------------

## 14. Sitzung und automatische Abmeldung

KanuControl beendet eine Sitzung nach **30 Minuten ohne
Benutzeraktivität** automatisch.

Damit wird verhindert, dass eine angemeldete Sitzung über längere Zeit
unbeaufsichtigt bestehen bleibt.

Nach einer automatischen Abmeldung ist eine erneute Anmeldung
erforderlich.

------------------------------------------------------------------------

## 15. Empfohlene Reihenfolge

Für eine neue Veranstaltung empfiehlt sich folgende Reihenfolge:

1.  Verein anlegen bzw. prüfen.
2.  Personen/Mitglieder erfassen oder per CSV importieren.
3.  Angezeigte 🔴 erforderliche bzw. 🟡 empfohlene Daten ergänzen.
4.  Stammdaten wie Beitragsstrukturen, Unterkunft und Verpflegung
    prüfen.
5.  Veranstaltung anlegen.
6.  Veranstalter und Veranstaltungsleiter zuordnen.
7.  Die angezeigten Datenstatus prüfen und erforderliche Angaben
    ergänzen.
8.  Teilnehmer und Mitarbeiter zuordnen.
9.  Finanzielle **Simulation** durchführen.
10. Simulation in die **Planung** übernehmen und speichern.
11. Teilnehmerbeiträge prüfen.
12. Für die Durchführung die **Konten/Finanzgruppen** anlegen und
    Teilnehmer zuordnen.
13. Zahlungsnachweise erfassen.
14. Belege und tatsächliche Buchungen erfassen.
15. Fahrkosten erfassen.
16. Finanz-Dashboard kontrollieren.
17. Vor dem Finanzausgleich die Finanzgruppen und Beiträge prüfen.
18. **Finanzausgleich** durchführen bzw. kontrollieren.
19. Benötigte PDFs und Nachweise erzeugen und die Dokumentprüfung
    beachten.

------------------------------------------------------------------------

## 16. Wichtige Grundregeln

### Planung und Ist-Abrechnung trennen

Die Planung beschreibt die erwartete Finanzierung. Die Abrechnung und
das Dashboard zeigen die tatsächlichen Werte.

### Finanzgruppen frühzeitig zuordnen

Teilnehmer sollten vor der finanziellen Auswertung einer passenden
Finanzgruppe zugeordnet sein.

### Zahlungsweg korrekt erfassen

Die Unterscheidung zwischen Überweisung und Quittung beeinflusst die
Zuordnung zum VK bzw. TN-Konto und damit den späteren Finanzausgleich.

### Belege digital hinterlegen

Wenn möglich sollten Belege direkt beim Erfassen als PDF oder Bild
hinterlegt werden. Dadurch bleiben Buchung und Nachweis zusammen.

### Überzahlungen prüfen

Offene Überzahlungen sollten vor dem Abschluss der Veranstaltung
kontrolliert und gegebenenfalls über Rückzahlungen verarbeitet werden.

------------------------------------------------------------------------

## 17. Kurzfassung des Workflows

``` text
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

Das KJFP-Modul verbindet damit die Planung, Durchführung und
finanzielle Auswertung einer Veranstaltung in einem durchgängigen Ablauf
und ergänzt diesen um eine dynamische, verwendungsabhängige
Datenvollständigkeit.

## 18. Häufige Fragen

### Ich sehe das KJFP-Modul nicht

Die sichtbaren Module hängen von den freigeschalteten Rollen ab. Für KJFP wird die Rolle KJFP oder ADMIN benötigt. Lass die Zuordnung durch die Administration prüfen.

### Teilnehmer oder Finanzdaten gehören zur falschen Veranstaltung

Prüfe zuerst die aktive Veranstaltung im Kopfbereich und den Veranstaltungsbezug der geöffneten Finanzseite. Wechsle anschließend zur richtigen Veranstaltung.

### Ein Datensatz lässt sich speichern, eine PDF aber nicht erstellen

Speichern und Dokumenterstellung haben unterschiedliche Anforderungen. Öffne die angezeigten Hinweise und ergänze die für das betreffende Dokument erforderlichen Daten.

### Der Finanzausgleich passt nicht

Kontrolliere die Teilnehmerzuordnung zu Finanzgruppen, die Finanzgruppe der Quittungen, Überweisungspositionen, Rückzahlungen und offene Überzahlungen. Vergleiche anschließend Dashboard und Finanzausgleich erneut.

### Wo finde ich die Bootshausbuchungen?

Wechsle zur Modulauswahl und öffne **Vermietung**. Der Ablauf ist in der [Bedienungsanleitung Vermietung](/info/bedienungsanleitung-vermietung.md) beschrieben.
