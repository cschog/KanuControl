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
