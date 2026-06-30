# IST-Buchungen (Abrechnung)

## Ziel

Die IST-Buchungen bilden die tatsächlichen Einnahmen und Ausgaben einer Veranstaltung ab.

Im Gegensatz zur **Planung** werden hier keine Schätzwerte verwendet, sondern ausschließlich tatsächlich entstandene Kosten und Einnahmen.

Die IST-Buchungen sind die Grundlage für

- die Endabrechnung einer Veranstaltung
- den KJFP-Verwendungsnachweis
- die Vereinsbuchhaltung
- den Vergleich Planung ↔ IST

---

# Grundprinzip

Die Buchungsseite führt alle finanziellen Informationen der Veranstaltung an einer Stelle zusammen.

Dabei gibt es drei Arten von Buchungen:

| Typ | Beschreibung |
|------|--------------|
| 🟦 automatisch | wird vollständig vom System berechnet |
| 🟨 übernommen | stammt aus einem anderen Modul und wird automatisch übernommen |
| ✏️ manuell | wird direkt auf der Buchungsseite erfasst |

Dadurch müssen identische Informationen niemals mehrfach eingegeben werden.

---

# Einnahmen

## Teilnehmerbeiträge 🟨

Die bezahlten Teilnehmerbeiträge werden automatisch aus der Beitragsverwaltung übernommen.

Beispiel

| Teilnehmer | Beitrag | bezahlt |
|------------|---------|----------|
| Anna | 80 € | ✓ |
| Peter | 80 € | ✓ |
| Julia | 80 € | ✗ |

Automatisch übernommen werden

```
160 €
```

Zusätzlich können weitere Einnahmen erfasst werden, beispielsweise

- Barzahlungen
- Nachzahlungen
- Einnahmen ohne Teilnehmerzuordnung

Dadurch ergibt sich

```
Teilnehmerbeiträge

automatisch      160 €
manuell          120 €

Gesamt           280 €
```

---

## KJFP-Zuschuss 🟦

Der KJFP-Zuschuss wird vollständig automatisch berechnet.

Grundlage sind

- tatsächlich teilnehmende Personen
- tatsächliche Fördertage
- aktuelle Fördersätze
- KIK-Zuschläge
- Förderobergrenzen

Eine manuelle Änderung ist nicht möglich.

---

## Sonstige Einnahmen ✏️

Manuell erfasst werden beispielsweise

- Zuschüsse
- Spenden
- Sponsoring
- Pfandrückzahlungen
- sonstige Einnahmen

---

# Ausgaben

## Fahrkosten 🟨

Alle genehmigten Reisekostenabrechnungen werden automatisch übernommen.

Beispiel

| Fahrer | Betrag |
|---------|--------|
| Müller | 126 € |
| Schmidt | 84 € |

Automatisch

```
210 €
```

Zusätzlich können weitere Fahrtkosten erfasst werden

- Bahntickets
- Mietwagen
- Taxi
- Parkgebühren

Dadurch ergibt sich

```
Fahrtkosten

automatisch      210 €
manuell          95 €

Gesamt           305 €
```

---

## Unterkunft ✏️

Manuelle Erfassung der tatsächlich entstandenen Unterkunftskosten.

Beispielsweise

- Jugendherberge
- Campingplatz
- Hotel
- Gruppenhaus

---

## Verpflegung ✏️

Erfassung der tatsächlichen Verpflegungskosten.

Beispiele

- Lebensmittel
- Catering
- Restaurant
- Grillabend

---

## Honorare ✏️

Beispielsweise

- Referenten
- Trainer
- Dozenten

---

## Material ✏️

Beispielsweise

- Bastelmaterial
- Sportmaterial
- Verbrauchsmaterial

---

## Kultur ✏️

Beispielsweise

- Eintrittsgelder
- Museumsbesuche
- Schwimmbad
- Kino

---

## Miete ✏️

Beispielsweise

- Bootsanhänger
- Seminarräume
- Boote
- Technik

---

## Sonstige Ausgaben ✏️

Alle Kosten, die keiner anderen Kategorie zugeordnet werden können.

---

# Darstellung

Die Buchungen werden getrennt dargestellt in

## Einnahmen

- Teilnehmerbeiträge
- KJFP-Zuschuss
- sonstige Einnahmen

## Ausgaben

- Unterkunft
- Verpflegung
- Fahrkosten
- Honorare
- Material
- Kultur
- Miete
- sonstige Ausgaben

Innerhalb jeder Kategorie werden die einzelnen Buchungen chronologisch angezeigt.

---

# Kennzeichnung

Zur besseren Übersicht werden alle Positionen gekennzeichnet.

| Symbol | Bedeutung |
|---------|-----------|
| 🟦 automatisch | wird vom System berechnet |
| 🟨 übernommen | stammt aus einem anderen Modul |
| ✏️ manuell | wurde direkt erfasst |

Dadurch ist jederzeit nachvollziehbar, woher eine Buchung stammt.

---

# Vorteile

Die IST-Buchungen vermeiden doppelte Datenerfassung.

| Information | Wird gepflegt in |
|-------------|-----------------|
| Teilnehmer | Teilnehmerverwaltung |
| Teilnehmerbeiträge | Beitragsverwaltung |
| Fahrkosten | Reisekostenabrechnungen |
| KJFP-Zuschuss | automatische Berechnung |
| übrige Kosten | Buchungen |

Dadurch existiert jede Information im System genau einmal.

---

# Planung ↔ IST

Während der Veranstaltung entsteht folgender Ablauf:

```
Planung
      │
      ▼
Durchführung
      │
      ▼
Teilnehmer
Beiträge
Reisekosten
Buchungen
      │
      ▼
IST-Abrechnung
      │
      ▼
KJFP-Verwendungsnachweis
```

Dadurch lassen sich jederzeit vergleichen

- geplante Einnahmen ↔ tatsächliche Einnahmen
- geplante Ausgaben ↔ tatsächliche Ausgaben
- geplanter Zuschuss ↔ tatsächlicher Zuschuss

---

# Philosophie

KanuControl versteht die Buchungsseite nicht als einfache Liste von Einnahmen und Ausgaben.

Sie ist das zentrale Finanzcockpit der Veranstaltung.

Alle Informationen werden aus den jeweiligen Fachmodulen übernommen und an einer Stelle zusammengeführt.

Dadurch bleiben alle Daten konsistent und müssen nicht mehrfach gepflegt werden.