# Was ist neu? – KanuControl 2.4.1

**Quellstand: 19. November 2026 · Client und Server: 2.4.1**

- Unbefristete Buchungen erhalten einen eigenen Jahresabschluss. Die Jahresabrechnung kann jederzeit im laufenden Kalenderjahr erstellt werden, wenn die Buchung im Abrechnungsjahr beginnt oder bereits läuft. Buchungsstatus, fehlendes Enddatum und Kalenderbelegung bleiben erhalten.
- Eine Vorschau zeigt die vereinbarte Miete und die Mietzahlungen/Anzahlungen mit Zahlungsdatum im Abrechnungsjahr. Es erfolgt keine zeitanteilige Kürzung. Kaution und Verbrauchskosten sind nicht Teil der Jahresabrechnung; der Jahresabschluss löst keine Rückzahlung und keine E-Mail aus.
- Für jedes Jahr gibt es ein aktuelles Dokument. Eine erneute Erstellung ersetzt das PDF desselben Jahres, etwa nach einer Zahlungskorrektur. Abrechnungen anderer Jahre bleiben getrennte Jahresabschlüsse. Airbnb und „Ohne Abrechnung“ erhalten einen Jahresabschluss ohne PDF.
- Reservierungen können ohne Bankdaten und ohne Bestätigungsmail bestätigt werden. Die Kaution wird auch bei kurzfristiger Stornierung vollständig zur Rückzahlung berücksichtigt; bereits erfolgte Erstattungen werden abgezogen. Der alte Status „Anfrage“ erscheint nicht im Buchungskalender.
- Ergänzt die Vermietungsabläufe: Homepage-Reservierung mit separater Eingangsbestätigung, Wiederherstellung stornierter Buchungen als bestätigt, Zahlungskorrekturen auch nach Abschluss sowie Stammdaten-Zusammenführung.

Beim Serverstart wird die zusätzliche Mandantenmigration `20261119-02-jahresabrechnung.xml` für die getrennten Jahresabrechnungen ausgeführt. Bestehende Abschlussrechnungen bleiben erhalten. Kein Deployment ausgeführt.

---

# Was ist neu? – KanuControl 2.3.2

**Quellstand: 17. November 2026 · Client und Server: 2.3.2**

- Die Verbrauchsabrechnung wählt je Mietobjekt und Verbrauchsart genau den Preis, dessen Zeitraum den Anreisetag enthält. Dieser Preis gilt für den gesamten Aufenthalt.
- Historische Preise dürfen als getrennte Positionen erfasst bleiben. Positionen mit früheren oder späteren Zeiträumen werden automatisch ausgelassen; ein Zusammenführen ist nicht nötig. Mehrere Preiszeiträume innerhalb einer Position werden weiterhin unterstützt.
- Aktiv-Schalter und Aktiv-Status entfallen bei Verbrauchskosten. Auch bisher inaktive Preise werden anhand ihrer Zeiträume berücksichtigt.
- Fehlende oder sich überschneidende Preise werden je Verbrauchsart verständlich gemeldet und verhindern eine unvollständige oder doppelte Abrechnung. Bezeichnung und Mengeneinheit machen aus überlappenden Preisen keine getrennten Verbrauchsarten.
- Abgeschlossene Buchungen behalten ihre gespeicherten Verbrauchskosten. Die Prüfung der zur Buchung gehörenden Zählerstände bleibt bestehen.

Es ist keine zusätzliche Datenmigration nötig. Bestehende Preiszeiträume können unverändert verwendet werden. Sind bisher inaktive doppelte Einträge vorhanden, müssen deren Zeiträume bereinigt werden. Kein Deployment ausgeführt.

---

# Was ist neu? – KanuControl 2.3.1

**Quellstand: 17. November 2026 · Client und Server: 2.3.1**

In der Verbrauchskostenverwaltung werden alle hinterlegten Preise mit ihrem Gültigkeitszeitraum angezeigt. Damit bleiben historische und zukünftige Preise auch ohne aktuell gültigen Preis direkt in der Übersicht und auf Mobilgeräten sichtbar.

Kein Deployment ausgeführt.

---

# Was ist neu? – KanuControl 2.3.0

**Quellstand: 17. November 2026 · Client und Server: 2.3.0**

- Verbrauchspreise lassen sich mit Gültigkeitsbeginn und optionalem Enddatum verwalten, auch rückwirkend. Neue Zeiträume begrenzen unveränderte Nachbarzeiträume automatisch.
- Für die gesamte Buchung gilt der Preis am Anreisetag. Fehlende Preise werden in der Vorschau angezeigt und verhindern den Abschluss.
- Abgeschlossene Buchungen verwenden ihre gespeicherten Verbrauchskosten. Kostenlose Buchungen behalten ihre Verbrauchswerte für Auswertungen ohne Kostenforderung.
- Vorhandene Preise werden bei der nächsten Mandantenmigration ab dem 1. Januar 0001 übernommen; bestehende Abrechnungssnapshots bleiben erhalten.
- Fördersätze, KiK-Zuschläge und Fahrkosten-Konfiguration stehen unter Administration. Die entsprechenden KJFP-Reiter entfallen.

Diese Versionsnummer bezeichnet den vorbereiteten Quellstand. Es wurde kein Deployment ausgeführt. Die tatsächlich laufenden Versionen stehen im Kopfbereich der Anwendung.

---

# Was ist neu? – KanuControl 2.2.2

**Dokumentationsstand: 13. November 2026 · Client und Server: 2.2.2**

## KJFP: Programmentwurf und Anmeldung

- Veranstaltungsdaten und Programmentwurf stehen in zwei unabhängig aufklappbaren Bereichen.
- Der optionale Programmentwurf unterstützt Überschrift, Fett/Kursiv, Listen, Absätze und Zeilenumbrüche und wird gemeinsam mit der Veranstaltung gespeichert.
- Die Anmeldung übernimmt den gespeicherten Entwurf nach den beiden vorhandenen Formularseiten; längere Programme erhalten weitere Seiten.
- Fahrkosten werden in der vorgesehenen Formularzeile ausgegeben. Kultur / Programm wird unter „Sonstige Kosten“ berücksichtigt und fließt in die Gesamtsumme ein.

## KJFP: Kopieren mit eigener Planung

- Beim Kopieren einer KJFP-Veranstaltung entsteht eine eigene Planung im Status „In Bearbeitung“.
- Teilnehmerzahlen, Beiträge, Preise, Kostenansätze und Planungspositionen werden übernommen. Automatische Beträge werden für die neuen Veranstaltungsdaten neu berechnet.
- Auch Unterkunft, Verpflegung, Ort, Beitragsstruktur, Beschreibung, Gebühreneinstellungen und Programmentwurf werden übernommen.
- Die Vorlage bleibt als eigene Planung erhalten. Teilnehmerlisten, Abrechnung und Belege werden nicht kopiert.
- Bei manueller Änderung des Teilnehmer- oder Mitarbeiterbeitrags wird „Beitragsvorschlag übernehmen“ in der Simulation wieder aktiv.

## Vermietung: Bedienung und Dokumente seit 2.1.1

Die aktuellen Versionen ergänzen die ältere Hilfe insbesondere um:

- Getrennte Mieter- und Veranstalterverwaltung.
- Sieben aufklappbare Buchungsbereiche und eine erreichbare Aktionsleiste; Löschen steht unter „Weitere Aktionen“.
- Jahresfilter nach Mietbeginn in der Abrechnung und überarbeitete Tabellen- und Formularbedienung.
- Vorschau, Speichern/Ersetzen, PDF-Import und manuellen Versand von Buchungsdokumenten unabhängig von Statusaktionen.
- Simulationsabrechnungen mit eindeutiger Entwurfskennzeichnung, frühere Buchungsnummern und nachvollziehbares Versandarchiv.
- Direkten Statusmail- und Dokumentversand über KC-SMTP. WordPress übernimmt weiterhin die erste Eingangsbestätigung.

## Qualität und Versionsanzeige

Der Produktionsbuild des Clients und der vollständige Server-Testlauf für 2.2.2 sind erfolgreich. Von 829 Testfällen wurden 814 ausgeführt und 15 optionale PostgreSQL-Integrationstests mangels gesonderter Testdatenbank-Konfiguration übersprungen; es gab keine Fehler oder fehlgeschlagenen Prüfungen.

Die tatsächlich laufenden Frontend- und Backend-Versionen stehen im Kopfbereich. Der dokumentierte Quellstand sagt nicht aus, ob eine Installation bereits aktualisiert wurde.

[Bedienungsanleitung KJFP](/info/bedienungsanleitung-kjfp.md) · [Bedienungsanleitung Vermietung](/info/bedienungsanleitung-vermietung.md) · [Bedienungsanleitung Fahrten](/info/bedienungsanleitung-fahrten.md) · [Homepage-Anbindung](/info/homepage-anbindung.md)

---

# Frühere Versionshinweise

Die folgenden Angaben dokumentieren den damaligen Stand und sind keine Beschreibung des aktuellen Prüfstands.

# Was ist neu? – KanuControl 2.1.1

**Veröffentlicht: 6. November 2026**

## Fahrten und gemeinsame Veranstaltungsverwaltung

- Neues Fahrtenmodul mit Fahrten, Gewässern, Kanus, Lizenzen und Besatzungen.
- Veranstaltungen und Teilnehmer werden gemeinsam verwaltet; KJFP und Fahrten verwenden dieselben Stammdaten.
- KJFP-Zuordnungen lassen sich getrennt von der allgemeinen Teilnahme verwalten.
- In Abrechnungsbuchungen kann der KJFP-Anteil getrennt vom Gesamtbetrag erfasst werden.

## Vermietung und Homepage

- Statusmails werden direkt vom KC-Backend über SMTP versendet; ein Testmodus kann Nachrichten an eine festgelegte Testadresse umleiten.
- WordPress übernimmt weiterhin die Eingangsbestätigungen, fragt aber keine Statusmail-Aufträge mehr ab.
- Verbrauchsabrechnungen berücksichtigen auch Buchungen am selben Tag und suchen zeitlich getrennte Anfangs- und Endablesungen.
- Hinweise zu Ablesungen außerhalb des Buchungszeitraums sowie Buchungs- und Finanzansichten wurden überarbeitet.

## Prüfstand

Der Frontend-Produktionsbuild ist erfolgreich. Der Backend-Testlauf umfasst 455 Tests: 452 erfolgreich, zwei fehlgeschlagene Prüfungen im Finanzdashboard und einen Fehler durch eine fehlende Testabhängigkeit im AbrechnungSynchronisationsServiceTest. Der Stand ist damit nicht vollständig fehlerfrei getestet.

## Betrieb und Aktualisierung

Frontend und Backend tragen die Version 2.1.1. Für den direkten Mailversand müssen die SMTP-Umgebungsvariablen des Backends eingerichtet werden; die Konfiguration ist in der Infrastruktur-Dokumentation beschrieben.

Die enthaltenen Liquibase-Migrationen erweitern Veranstaltungen und Teilnehmer, legen die Fahrtenstrukturen an und entfernen das Feld für geschätzte Geburtsdaten. Vor der Bereitstellung ist eine Datenbanksicherung erforderlich; die entfernten Kennzeichnungen werden bei einem Rollback nicht wiederhergestellt.

---

# Was ist neu? – KanuControl 2.1.0

**Dokumentationsstand: 3. November 2026**

## Zwei Bedienungsanleitungen

Die Hilfe unterscheidet jetzt klar zwischen **KJFP** und **Vermietung**. Beide Anleitungen stehen direkt im Menü **Hilfe & Info**. Die bisherige allgemeine Hilfe dient als Einstieg; Kapitel und weiterführende Anleitungen lassen sich im Hilfefenster öffnen.

- [Bedienungsanleitung KJFP](/info/bedienungsanleitung-kjfp.md)
- [Bedienungsanleitung Vermietung](/info/bedienungsanleitung-vermietung.md)
- [Einrichtung der Homepage-Anbindung](/info/homepage-anbindung.md)

## Vermietung

- Mieterverwaltung, Buchungsnummern nach Anreisejahr und Teilnehmerangaben.
- Direkt- und Airbnb-Buchungen mit getrennten Freigaben und Mietpreisen.
- Checkbox ohne Mengenfeld bei Bestand 1; Mengenfeld bei größerem Bestand.
- Mietbereichsvoraussetzungen und gemeinsame Belegungsressourcen.
- Anzahlung, Kaution, Miete, Verbrauchskosten und zugehörige Finanzbewegungen.
- Sperrzeiten je Mietobjekt, ganztägig oder mit Uhrzeit, einschließlich Konfliktprüfung.
- Homepage-Anfrageformular und kompakter Bootshauskalender mit tatsächlichen Belegungszeiten, Buchungsnummern und Sperrbalken.
- KC → Airbnb als geschützter iCal-Link für das ganze Bootshaus mit EG und OG. Reservierte und bestätigte Buchungen sowie Sperrzeiten werden berücksichtigt. Auch der Abreisetag wird bei Vermietungen für Airbnb vollständig gesperrt.

Der Homepagekalender zeigt weiterhin die genauen Uhrzeiten. Die zusätzliche Sperrung des Abreisetags für Airbnb verändert nicht die Miettage oder die gespeicherten Buchungszeiten in KC.

## KJFP

Die Anleitung zu Vereinen, Personen, Veranstaltungen und Teilnehmern sowie zu Simulation, Planung, Beiträgen, Abrechnung, Fahrkosten, Konten, Finanzausgleich und Dokumenten ist jetzt als eigener Leitfaden aufbereitet. Kapitelnummern und Zahlungsweg-Erklärungen wurden überprüft und überarbeitet.

## Orientierung

Die Reiter **Sperrzeiten** und **Airbnb-Export** stehen rechts in der Vermietungsverwaltung. Die Reiterleiste lässt sich horizontal verschieben.

Die Hinweise beschreiben den aktuellen Quellstand. Ein neu bereitgestelltes Frontend muss geladen sein, damit neue Menüeinträge und Hilfetexte sichtbar werden. Die tatsächlich laufenden Versionen stehen im Kopfbereich.

[Zur Hilfeübersicht](/info/bedienungsanleitung.md)
