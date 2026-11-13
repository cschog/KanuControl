# Buchungsmaske – Umsetzung vom 13.11.2026

Die Buchungsmaske verwendet sieben Accordions in einer durchgehenden Spalte (maximal 1320 px). Mieter, Veranstalter und Rabatt sind zusammengefasst. Datum/Uhrzeit, Jugendliche/Alter und Mietbereiche/Preise nutzen die tatsächlich verfügbare Formularbreite als Container. Schäden bleiben im bestehenden Editor; die Aktionsleiste haftet unten und berücksichtigt die Safe Area. Löschen steht unter Weitere Aktionen.

## Komponenten

- `BuchungBaseForm`: bestehende Felder umgeordnet, aktuelle Zusammenfassungen, Verbrauchsanzeige und relevante aktive Verbrauchspositionen vor der Abrechnung; keine neue Eingabedatenhaltung für Zählerstände.
- `BuchungFormView`: Startzustände und Abrechnungsvorbereitung, Speicherfehler öffnen betroffene Bereiche und fokussieren ein betroffenes Feld.
- `BuchungActionBar`: haftende Aktionen, Löschen-Menü, eigener rein lesender Vorbereitungsschritt vor bestehendem Abschlussdialog.
- `BuchungAbrechnungUebersicht`: vorhandene Serverabrechnung, getrennte Zahlungs-/Kautionsbewegungen und Kennzeichnung unvollständiger Kosten. Übersicht bezieht sich ausdrücklich auf gespeicherte Daten; geänderte Zeiträume kennzeichnen die alte Verbrauchsvorschau.
- `buchungBereiche`: Bereichsschlüssel und reine Speicherprüfung. Abschlussregeln bleiben im vorhandenen Ablauf.
- `EntschaedigungEditor`: Anpassung an schmale Formularbreiten.

## Prüfung

- Client: TypeScript und Produktionsbuild erfolgreich; ESLint für geänderte Komponenten ohne Befund.
- Vorhandene Chrome-Regressionen: Mietpreis einschließlich Miettage, Sommerzeit und Rabatt-Snapshot; Mieterwechsel/Abbrechen einschließlich Suchtext und Löschen der Auswahl erfolgreich.
- Neuer isolierter Chrome-Test: sieben Bereiche, neue/bestehende Buchung, Abrechnung, Zustandserhalt, Teilnehmer 0 versus leer, Fehler in geschlossenem Bereich, Tastatur und Abbrechen erfolgreich.
- Layout bei 375/390/768/1280/1920 px und mit simulierter offener Navigation geprüft; kein horizontaler Seitenüberlauf. Screenshots visuell kontrolliert.

## Grenzen

Die Browserprüfungen verwenden API-Mocks und ersetzen keine Abnahme mit produktiven Daten, Safari oder realem Smartphone. Der bestehende Server und dessen Preis-, Zahlungs-, Verbrauchs-, DIREKT-/AIRBNB-Abschluss- und Versandregeln wurden nicht geändert. Relevante Verbrauchspositionen stammen vor der Abrechnung aus der aktiven Mietobjekt-Konfiguration; Abrechnung und gespeicherte Verbrauchswerte verwenden weiter die Buchungsdaten. Es gibt keine neue Zählerstandserfassung: Ablesungen werden weiterhin über die vorhandene Zählerverwaltung erfasst und im Abschlussdialog geprüft/bestätigt. Kein Deployment oder Versand durchgeführt.
