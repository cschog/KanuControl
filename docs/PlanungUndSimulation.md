# Planung und Simulation

## Ziel

Die Planung dient der finanziellen Vorbereitung einer Veranstaltung.

Im Gegensatz zur IST-Abrechnung werden hier ausschließlich geplante Werte verwendet.

Alle Berechnungen erfolgen automatisch auf Basis der Stammdaten der Veranstaltung und können jederzeit angepasst werden.

Die Planung beantwortet unter anderem folgende Fragen:

- Ist die Veranstaltung finanzierbar?
- Reichen die Teilnehmerbeiträge aus?
- Wie hoch wird der KJFP-Zuschuss ungefähr sein?
- Welche Auswirkungen haben mehr oder weniger Teilnehmer?
- Welche Auswirkungen haben Änderungen der Teilnahmegebühren?
- Wie verändert sich der Zuschuss bei einer anderen Altersstruktur?

---

# Grundprinzip

Die Planung besteht aus zwei Arten von Positionen.

| Typ | Beschreibung |
|------|--------------|
| 🟦 automatisch berechnet | wird aus den Veranstaltungsdaten berechnet |
| ✏️ manuell | wird vom Benutzer ergänzt |

Dadurch muss nur ein kleiner Teil der Planung tatsächlich eingegeben werden.

---

# Automatisch berechnete Einnahmen

## Teilnehmerbeiträge

Die Teilnehmerbeiträge werden automatisch berechnet.

Grundlage sind

- geplante Teilnehmerzahl
- geplante Mitarbeiterzahl
- Standardgebühr oder Beitragsstruktur

Ändert sich einer dieser Werte, wird der Gesamtbetrag sofort neu berechnet.

---

## KJFP-Zuschuss

Der geplante Zuschuss wird automatisch berechnet.

Grundlage sind

- geplante Teilnehmer
- geplante Altersstruktur
- geplante Veranstaltungstage
- Fördersätze
- KIK-Zuschläge
- Förderobergrenzen

Da noch keine echten Teilnehmer vorhanden sind, handelt es sich um eine Prognose.

---

# Automatisch berechnete Ausgaben

## Unterkunft

Die Unterkunftskosten werden automatisch berechnet.

Grundlage sind

- Unterkunftsart
- Preis pro Person und Nacht
- geplante Teilnehmer
- geplante Mitarbeiter
- Anzahl der Übernachtungen

Formel

```
Personen
×
Nächte
×
Preis pro Person/Nacht
```

---

## Verpflegung

Die Verpflegungskosten werden automatisch berechnet.

Grundlage sind

- Verpflegungsmodell
- Preis pro Person und Tag
- geplante Personen
- Anzahl der Veranstaltungstage

Formel

```
Personen
×
Tage
×
Preis pro Person/Tag
```

---

# Manuelle Positionen

Alle weiteren Einnahmen und Ausgaben werden manuell ergänzt.

Beispiele

## Einnahmen

- Spenden
- Zuschüsse
- Sponsoring
- Pfand
- sonstige Einnahmen

## Ausgaben

- Honorare
- Material
- Kulturprogramm
- Miete
- Sonstige Kosten

---

# Simulation

Die Planung ist gleichzeitig eine Simulation.

Jede Änderung an den Veranstaltungsdaten wirkt sich sofort auf die Planung aus.

Beispiele

## Teilnehmerzahl erhöhen

```
24 Teilnehmer
↓

30 Teilnehmer

↓

Teilnehmerbeiträge steigen

↓

Unterkunft steigt

↓

Verpflegung steigt

↓

KJFP-Zuschuss steigt
```

---

## Teilnahmegebühr ändern

```
80 €

↓

90 €

↓

Teilnehmerbeiträge steigen

↓

Saldo verbessert sich
```

---

## Unterkunft wechseln

```
Jugendherberge

↓

Campingplatz

↓

Unterkunftskosten sinken

↓

Gesamtausgaben sinken
```

---

## Altersstruktur ändern

Mehr förderfähige Jugendliche

↓

höherer KJFP-Zuschuss

↓

besseres Gesamtergebnis

---

# Saldo

Während der Planung werden ständig berechnet

```
Gesamteinnahmen

-

Gesamtausgaben

=

Saldo
```

Dadurch erkennt der Veranstalter jederzeit,

- ob die Veranstaltung kostendeckend ist,
- ob ein Defizit entsteht,
- wie hoch der geplante Überschuss ist.

---

# Änderungen

Automatische Positionen werden niemals direkt bearbeitet.

Stattdessen werden die zugrunde liegenden Veranstaltungsdaten geändert.

Beispiele

| gewünschte Änderung | erfolgt in |
|---------------------|-----------|
| Teilnehmerzahl | Veranstaltung |
| Mitarbeiterzahl | Veranstaltung |
| Teilnahmegebühr | Veranstaltung |
| Beitragsstruktur | Beitragsverwaltung |
| Unterkunft | Veranstaltung |
| Verpflegung | Veranstaltung |

Nach jeder Änderung werden alle automatischen Positionen neu berechnet.

---

# Einreichen

Ist die Planung abgeschlossen, kann sie eingereicht werden.

Nach dem Einreichen

- können keine Positionen mehr geändert werden,
- dient die Planung als Grundlage für die Durchführung.

Falls notwendig, kann die Planung später wieder geöffnet werden.

---

# Übergang zur IST-Abrechnung

Nach Beginn der Veranstaltung wird die Planung nicht mehr verändert.

Stattdessen entstehen nach und nach die IST-Daten.

```
Planung

↓

Durchführung

↓

Teilnehmer

↓

Beiträge

↓

Reisekosten

↓

Buchungen

↓

IST-Abrechnung
```

Dadurch können später jederzeit

- Planung und IST
- Prognose und Realität

miteinander verglichen werden.

---

# Philosophie

Die Planung ist bewusst als Simulation konzipiert.

Anstatt jeden Betrag einzeln eingeben zu müssen, werden die meisten Werte aus wenigen Stammdaten automatisch berechnet.

Dadurch kann der Veranstalter verschiedene Szenarien innerhalb weniger Sekunden ausprobieren.

Beispiele

- Was passiert bei fünf zusätzlichen Teilnehmern?
- Reicht eine Teilnahmegebühr von 75 €?
- Ist ein anderer Veranstaltungsort günstiger?
- Wie wirkt sich eine andere Beitragsstruktur aus?

Die Planung wird dadurch zu einem Werkzeug für Entscheidungen und nicht nur zu einer Eingabemaske für Zahlen.