# KanuControl V1.7
## Dynamische Datenvollständigkeit und kontextabhängige Validierung

### Ziel der Version

Mit Version **1.7** erhält KanuControl eine dynamische Datenvollständigkeitsprüfung.

Daten müssen künftig nicht bereits bei der Anlage eines Datensatzes vollständig vorhanden sein. Stattdessen richtet sich der erforderliche Datenumfang danach, **wie ein Datensatz innerhalb von KanuControl verwendet wird**.

Dadurch kann ein Anwender mit wenigen notwendigen Stammdaten beginnen und die Daten schrittweise vervollständigen.

---

## 1. Grundprinzip

KanuControl unterscheidet künftig zwischen drei Ebenen:

### 1.1 Speichervalidierung

Prüft ausschließlich:

> Darf dieser Datensatz gespeichert werden?

Die bestehenden technischen und fachlichen Anforderungen für das Anlegen und Speichern bleiben erhalten.

Kontextabhängige Anforderungen werden **nicht** pauschal zu `@NotNull`-Feldern der Entity.

### 1.2 Dynamischer Datenstatus

Sobald ein Datensatz in einem bestimmten Zusammenhang verwendet wird, können weitere Daten erforderlich oder sinnvoll werden.

| Kennzeichnung | Bedeutung |
|---|---|
| 🔴 Rot | Daten sind aufgrund der aktuellen Verwendung erforderlich, fehlen aber |
| 🟡 Gelb | Daten sind für spätere Vorgänge oder Dokumente sinnvoll, fehlen aber; der Vorgang wird dadurch nicht blockiert |
| keine Kennzeichnung | Aktuell besteht kein Handlungsbedarf |

Der Status wird dynamisch aus der aktuellen Verwendung des Datensatzes ermittelt.

### 1.3 Vorgangsvalidierung

Bei einem konkreten Vorgang, insbesondere bei der Erstellung eines PDFs, wird nochmals geprüft, ob die für diesen Vorgang erforderlichen Daten vorhanden sind.

- 🔴 fehlende erforderliche Daten → Vorgang/PDF wird blockiert
- 🟡 fehlende optionale Daten → Vorgang/PDF kann durchgeführt werden
- vollständige Daten → Vorgang kann durchgeführt werden

Gelbe Daten können bei Bedarf im erzeugten Dokument manuell ergänzt werden.

---

## 2. Darstellung in Listen

Die Datenstatus werden in den relevanten Listen sichtbar gemacht:

- Personenliste
- Teilnehmerliste
- Vereinsliste
- Veranstaltungsliste

Dabei werden **nur problematische bzw. relevante Status angezeigt**.

Eine grüne Kennzeichnung wird bewusst **nicht** angezeigt.

Damit bleiben die Listen übersichtlich und zeigen dem Anwender nur dort einen Hinweis, wo Handlungsbedarf besteht.

Beispiel:

| Person | Status |
|---|---|
| Hans Müller | 🔴 |
| Peter Schmidt | |
| Klaus Meier | 🟡 |

---

## 3. Darstellung in Detailformularen

Im Detailformular werden Felder abhängig von ihrer aktuellen Verwendung gekennzeichnet.

Dies gilt auch für Felder, die aktuell noch leer sind.

Beispiel:

```text
Geburtsdatum   [                 ]   🔴
E-Mail         [                 ]   🔴
Telefon        [                 ]   🔴
Telefon Fest.  [                 ]   🟡
IBAN           [                 ]   🟡
```

Damit erkennt der Anwender unmittelbar:

- welches Feld fehlt,
- warum es relevant ist,
- ob es zwingend erforderlich oder lediglich sinnvoll ist.

---

## 4. Hover-/Tooltip-Hilfe

Für gekennzeichnete Felder wird eine kontextabhängige Hilfe angeboten.

Beim Überfahren des Feldes mit der Maus kann beispielsweise angezeigt werden:

### 🔴 Erforderlich

> Erforderlich für die Verwendung als Veranstaltungsleiter.

### 🟡 Sinnvoll

> Die Bankverbindung wird in späteren Dokumenten verwendet. Wenn sie fehlt, kann das Dokument trotzdem erstellt und anschließend manuell ergänzt werden.

Die Tooltip-Texte sollen zentral und möglichst einheitlich verwaltet werden.

---

## 5. Erster Use Case: Veranstaltungsleiter

Der erste vollständig umgesetzte Lebenszyklus ist die Verwendung einer Person als **Veranstaltungsleiter**.

Eine Person kann zunächst mit den notwendigen Basisdaten angelegt werden.

Weitere Daten müssen zu diesem Zeitpunkt noch nicht vorhanden sein.

Sobald die Person als Leiter einer Veranstaltung ausgewählt wird, werden die für einen Leiter erforderlichen Daten dynamisch aktiviert.

Dazu gehören insbesondere:

- Geburtsdatum
- Straße
- PLZ
- Ort
- E-Mail
- Telefon

Fehlende Daten werden:

- in der Personenliste 🔴 angezeigt,
- im Personendetail 🔴 gekennzeichnet,
- per Tooltip erklärt.

Die Person kann weiterhin gespeichert und bearbeitet werden.

---

## 6. Veranstaltender Verein

Auch beim Verein werden Anforderungen erst durch seine Verwendung aktiviert.

Ein neu angelegter Verein ist zunächst eine **Basis-/Zuordnungseinheit**.

Er dient insbesondere dazu, Personen/Mitglieder einem Verein zuordnen und diese in Listen und anderen Bereichen unterscheiden zu können.

Solange der Verein nicht als veranstaltender Verein verwendet wird, sind insbesondere folgende Daten nicht erforderlich:

- Kontoinhaber
- Bankname
- IBAN
- BIC

### 6.1 Verein wird veranstaltender Verein

Sobald der Verein als **veranstaltender Verein einer Veranstaltung** verwendet wird, ändern sich die Datenanforderungen.

Der Kontoinhaber kann dadurch zu einem 🔴 erforderlichen Feld werden, sofern er für den entsprechenden weiteren Prozess benötigt wird.

Die Bankdaten bleiben grundsätzlich 🟡:

- Bankname
- IBAN
- BIC

Sie sind für spätere Dokumente sinnvoll, blockieren den weiteren Prozess aber nicht.

---

## 7. Veranstaltung

Eine Veranstaltung kann zunächst angelegt und gespeichert werden.

Auch hier werden zusätzliche Anforderungen abhängig vom weiteren Lebenszyklus aktiviert.

Der grundlegende Ablauf lautet:

```text
Verein anlegen
      ↓
Person anlegen
      ↓
Veranstaltung anlegen
      ↓
veranstaltenden Verein auswählen
      ↓
Leiter auswählen
      ↓
dynamische Datenanforderungen
      ↓
Simulation
      ↓
Antrag / weitere Planung
```

---

## 8. Simulation als nächster Prozessschritt

Nach der Auswahl von

- veranstaltendem Verein und
- Veranstaltungsleiter

beginnt der nächste fachliche Schritt: die **Simulation**.

Für die Simulation werden die dafür benötigten Veranstaltungsdaten dynamisch geprüft.

### Unterkunftsart

Die Unterkunftsart ist **nicht generell erforderlich**.

Bei einer Tagesveranstaltung wird keine Unterkunftsart benötigt.

Bei einer Veranstaltung, bei der eine Unterkunft bzw. Übernachtung relevant ist, wird die Unterkunftsart für die Simulation erforderlich.

Damit ist auch diese Prüfung kontextabhängig.

### Verpflegungsmodell

Das Verpflegungsmodell wird entsprechend der für die Simulation geltenden fachlichen Anforderungen geprüft.

---

## 9. Bankdaten

Bankdaten werden in V1.7 grundsätzlich als **sinnvolle, aber nicht blockierende Daten** behandelt.

Dazu gehören:

- Bankname
- IBAN
- BIC

Fehlen diese Daten:

- kann KanuControl weiterarbeiten,
- wird kein Vorgang allein deswegen blockiert,
- werden sie in entsprechenden PDFs nicht automatisch eingetragen,
- können sie dort gegebenenfalls manuell ergänzt werden.

In der Oberfläche werden fehlende Bankdaten mit 🟡 gekennzeichnet, sobald sie für einen späteren Vorgang bzw. ein Dokument relevant sind.

---

## 10. Fachliches Ziel von V1.7

KanuControl soll den Anwender nicht dazu zwingen, bereits bei der Anlage eines Vereins, einer Person oder einer Veranstaltung sämtliche denkbaren Daten einzugeben.

Stattdessen gilt:

> **Ein Datensatz muss nicht vollständig sein. Er muss für seine aktuelle Verwendung ausreichend vollständig sein.**

KanuControl zeigt dem Anwender frühzeitig, welche Daten aufgrund seiner aktuellen Arbeit benötigt oder sinnvoll sind.

Der Anwender kann seine Arbeit trotzdem fortsetzen und die Daten schrittweise vervollständigen.

Erst wenn ein konkreter Vorgang tatsächlich bestimmte Daten zwingend benötigt, wird dieser Vorgang bei fehlenden Daten blockiert.

---

## 11. Ziel für Version 1.7

Der erste vollständig umgesetzte Lebenszyklus soll folgende Kette abbilden:

```text
Verein
  ↓
Person
  ↓
Veranstaltung
  ↓
veranstaltender Verein
  ↓
Veranstaltungsleiter
  ↓
dynamische Datenstatus
  ↓
Simulation
  ↓
Veranstaltung beantragen
```

Dabei müssen die Statusinformationen durchgängig funktionieren:

**Verwendung → Klassifizierung → Liste → Detailformular → Tooltip → Vorgangsvalidierung**

Die Architektur soll anschließend auf weitere Verwendungen und Dokumente erweitert werden können, insbesondere auf:

- Teilnehmer
- Teilnehmerliste
- Erhebungsbogen
- Abrechnung
- Anmeldung
- Fahrkostenabrechnung
- weitere PDF-Dokumente

---

## Definition of Done für V1.7

Version 1.7 gilt fachlich als abgeschlossen, wenn:

- kontextabhängige Datenanforderungen funktionieren,
- Speichervalidierung und Vorgangsvalidierung getrennt bleiben,
- 🔴 und 🟡 korrekt unterschieden werden,
- Listen nur bei Handlungsbedarf einen Status anzeigen,
- Detailfelder dynamisch gekennzeichnet werden,
- Tooltips den Grund der Kennzeichnung erklären,
- Veranstaltungsleiter als erster vollständiger Use Case funktioniert,
- der veranstaltende Verein als erster Vereins-Use-Case funktioniert,
- die Simulation diese dynamischen Anforderungen berücksichtigt,
- Tagesveranstaltungen keine unnötige Unterkunftsart verlangen,
- gelbe Daten keinen Vorgang blockieren,
- rote Daten bei einem tatsächlich betroffenen Vorgang die Ausführung verhindern,
- die Architektur anschließend auf weitere Use Cases erweitert werden kann.

**V1.7 steht damit für eine schrittweise, nutzungsabhängige Vervollständigung der Daten statt einer pauschalen Vollständigkeitsprüfung.**
