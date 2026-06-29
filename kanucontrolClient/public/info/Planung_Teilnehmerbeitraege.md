# Berechnung der Teilnehmerbeiträge in der Planung

## Überblick

Für die Planung der Teilnehmerbeiträge stehen zwei Berechnungsverfahren zur Verfügung:

1. **Fester Teilnehmerbeitrag**
2. **Berechnung anhand einer Beitragsstruktur**

Die Berechnungsart wird in der Planung festgelegt.

---

# 1. Fester Teilnehmerbeitrag

Bei dieser Berechnungsart wird für jede geplante Person derselbe Beitrag angesetzt.

## Berechnung

```text
Teilnehmerbeitrag =
geplante Gesamtpersonen × Teilnehmergebühr
```

Dabei gilt:

```text
geplante Gesamtpersonen =
geplante Teilnehmer
+ geplante Mitarbeiter
+ Leiter
```

## Beispiel

| Position | Wert |
|----------|-----:|
| Geplante Teilnehmer | 24 |
| Geplante Mitarbeiter inkl. Leiter | 6 |
| Teilnehmergebühr | 80 € |

Berechnung:

```text
(24 + 6) × 80 € = 2.400 €
```

---

# 2. Berechnung über eine Beitragsstruktur

Bei dieser Berechnungsart werden die Beiträge aus der hinterlegten Beitragsstruktur des Vereins ermittelt.

## Berechnungsregel

### Förderfähige Teilnehmer

Für alle förderfähigen geplanten Teilnehmer wird der Beitrag der Altersstufe verwendet, welche das Förderhöchstalter einschließt.

Beispiel:

Förderhöchstalter: **20 Jahre**

Beitragsstruktur:

| Alter | Beitrag |
|-------|--------:|
| 0–2 | 0 € |
| 3–10 | 50 € |
| 11–20 | 100 € |
| 21–27 | 200 € |
| 28–99 | 300 € |

Für alle förderfähigen Teilnehmer wird der Beitrag von **100 €** verwendet.

Enthält die Beitragsstruktur stattdessen folgende Staffelung:

| Alter | Beitrag |
|-------|--------:|
| 0–2 | 0 € |
| 3–10 | 50 € |
| 19–22 | 150 € |
| 23–99 | 250 € |

Dann wird für alle förderfähigen Teilnehmer der Beitrag von **150 €** verwendet, da diese Altersstufe das Förderhöchstalter von 20 Jahren einschließt.

---

### Mitarbeiter und Leiter

Für alle geplanten Mitarbeiter einschließlich der Leiter wird immer der höchste Beitragssatz der gesamten Beitragsstruktur verwendet.

---

## Berechnung

```text
Teilnehmerbeitrag =
(förderfähige geplante Teilnehmer × Beitrag der Altersstufe mit Förderhöchstalter)
+
(geplante Mitarbeiter inkl. Leiter × höchster Beitragssatz)
```

---

# Beispiel

## Beitragsstruktur

| Alter | Beitrag |
|-------|--------:|
| 0–2 | 0 € |
| 3–10 | 50 € |
| 11–20 | 100 € |
| 21–27 | 200 € |
| 28–99 | 300 € |

## Planung

| Position | Anzahl |
|----------|-------:|
| Förderfähige Teilnehmer | 18 |
| Mitarbeiter inkl. Leiter | 5 |

Berechnung:

```text
18 × 100 € = 1.800 €
 5 × 300 € = 1.500 €
--------------------
Gesamt = 3.300 €
```

---

# Weiteres Beispiel

## Beitragsstruktur

| Alter | Beitrag |
|-------|--------:|
| 0–2 | 0 € |
| 3–10 | 50 € |
| 19–22 | 150 € |
| 23–99 | 250 € |

## Planung

| Position | Anzahl |
|----------|-------:|
| Förderfähige Teilnehmer | 18 |
| Mitarbeiter inkl. Leiter | 5 |

Berechnung:

```text
18 × 150 € = 2.700 €
 5 × 250 € = 1.250 €
--------------------
Gesamt = 3.950 €
```

---

# Hinweise

- Die tatsächlichen Alters der geplanten Teilnehmer spielen bei der Planung keine Rolle.
- Es werden ausschließlich die geplanten Anzahlen verwendet.
- Für alle förderfähigen Teilnehmer wird derselbe Beitrag angesetzt.
- Für alle Mitarbeiter einschließlich der Leiter wird stets der höchste Beitragssatz der Beitragsstruktur verwendet.
- Das Förderhöchstalter ist konfigurierbar und wird nicht fest im Programmcode hinterlegt.