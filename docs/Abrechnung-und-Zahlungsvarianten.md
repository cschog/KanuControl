# Abrechnungsvorlagen und Zahlungsvarianten

Die nach Abschluss ausgestellte Vermietungsabrechnung verwendet jetzt die gespeicherte Objektvorlage `ABGERECHNET` und das gemeinsame Mandantenbriefpapier. Die bisherigen Revisionen und gespeicherten PDF-Bytes bleiben unverändert. Downloads rendern keine Dokumente neu. Neue Ausstellungen erfordern wie bisher den aktuellen Abschluss; Korrekturen benötigen letzte Dokument-ID und Begründung. Ohne passende Vorlage oder Briefpapier wird die neue Ausstellung mit einer verständlichen Meldung gesperrt; es gibt keinen stillen Rückfall auf die frühere feste PDF.

## Einrichtung

1. Die reguläre Tenant-Migration `20261111-01-abrechnung-vorlagen.xml` ergänzt Zahlungsvariante, Kurzfristgrenze sowie Organisation und Kontoinhaber. Bestehende Buchungen erhalten `ANZAHLUNG`; ihre Beträge werden nicht verändert. Die Migration wurde ausschließlich in isolierten H2-Testschemas geprüft und nicht produktiv ausgeführt.
2. Unter Vermietung → Verwaltung → E-Mail- und PDF-Vorlagen je Mietobjekt `ABGERECHNET` einrichten. Bei fehlender Vorlage bietet die Oberfläche einen Entwurf mit Einleitung, Restzahlungs-, Erstattungs- und Ausgeglichen-Block. Entwurf prüfen und ausdrücklich speichern. Die Tabelle folgt automatisch dem ersten sichtbaren Textblock; weitere Blöcke und der gemeinsame Abschluss folgen anschließend. Der erste Block sollte deshalb eine unbedingte Einleitung sein.
3. Bestehende Reservierungsvorlagen überprüfen: Alte frei formulierte Zahlungstexte werden nicht automatisch umgeschrieben. In einem leeren Zahlungsblock bedingte Varianten für `ANZAHLUNG_POSITIV`, `RESTMIETE_POSITIV`, `ZAHLUNG_GESAMTMIETE_VORAUS`, `ZAHLUNG_BEI_ABRECHNUNG` und `KAUTION_POSITIV` verwenden. Neue Reservierungsentwürfe enthalten diese Varianten bereits. Die beiden Stornobedingungen beziehen sich weiterhin unabhängig davon auf acht Wochen.
4. Mieterorganisation und gegebenenfalls abweichenden Kontoinhaber in der Mieterverwaltung hinterlegen. Bankname, IBAN und BIC bleiben getrennte Felder; die bisher irreführende Beschriftung „Kontoinhaber“ für `bankName` lautet jetzt „Bankname“. Vorhandene Einträge fachlich prüfen; es findet keine automatische Umdeutung statt.
5. Die Kurzfristgrenze am Mietobjekt beträgt standardmäßig 42 Tage und ist von 0 bis 365 einstellbar. 0 deaktiviert den Vorschlag. Weniger als die Grenze bedeutet kurzfristig, am Grenztag gilt der normale Plan.

## Zahlungsplan

Jede Buchung speichert ausdrücklich `ANZAHLUNG`, `BEI_ABRECHNUNG` oder `GESAMTMIETE_VORAUS`. Bei neuen Buchungen wird anhand von Mietbeginn und Berlin-Datum ein Vorschlag gemacht; bei gespeicherten kurzfristigen Anfragen bietet die Oberfläche eine bewusst übernehmbare Empfehlung. Eine manuelle Auswahl bleibt erhalten. Datumsänderungen ändern die Variante einer gespeicherten Buchung nicht automatisch. Ältere Clients ohne Variantenfeld erhalten bei Updates die gespeicherte Variante.

- Anzahlung: `anzahlung` enthält den vereinbarten Betrag, `restmiete` die verbleibende vereinbarte Miete.
- Zahlung bei Abrechnung: keine Mietvorauszahlung; die Kaution kann unabhängig davon angefordert werden.
- Gesamtmiete im Voraus: Aufforderung über `mietbetrag`, keine zusätzliche Anzahlung oder Restmietaufforderung.

Außerhalb der Anzahlungsvariante speichert der Buchungsdienst den angeforderten Anzahlungsbetrag als null Euro. Tatsächliche Finanzbewegungen bleiben unberührt. Die Reservierungsfrist ist sieben Tage vor Anreise, bei sehr kurzfristigen Buchungen frühestens das Ausstellungsdatum. Die Schlussrechnungsfrist ist 14 Tage nach Ausstellung.

## Schlussrechnung

Der feste Tabellenbereich enthält Mietpositionen mit Anzahl und Abrechnungseinheiten, separate Rabattzeilen mit Bezug auf die nummerierten Mietpositionen, einzelne endgültige Verbrauchssnapshots mit Menge/Einheit und Tarif, Entschädigungen sowie alle datierten Finanzbewegungen. Rückzahlungen und Kautionsverrechnungen werden ausdrücklich benannt. Die bestehende Finanzlogik bleibt für Kosten und Saldo maßgeblich. Wenn der bestehende Saldo zusätzlich eine Kautionsanforderung oder Kategorieverrechnung enthält, wird die Differenz als eigene Ausgleichszeile ausgewiesen. Kaution ist keine zusätzliche Mietkostenposition.

Neue Felder: `abrechnungssumme`, `saldo`, `restzahlung`, `erstattung`, `organisation`, `mieter_bank_name`, `mieter_bank_iban`, `mieter_bank_bic`, `mieter_kontoinhaber`. `bank_name`, `bank_iban`, `bank_bic` bleiben das ausgewählte Vereinskonto. Fehlt ein abweichender Kontoinhaber, wird der Personenname verwendet. Bei Erstattung ist die Mieter-IBAN erforderlich; weitere tatsächlich verwendete Platzhalter unterliegen weiterhin der Pflichtwertprüfung.

`RESTZAHLUNG_POSITIV`, `ERSTATTUNG_POSITIV` und `AUSGEGLICHEN` schließen sich aus. Die Zahlungs-/Erstattungsblöcke müssen mit diesen Bedingungen konfiguriert sein; Freitext kann nicht automatisch semantisch erkannt werden. Bei ausgeglichenem Saldo wird keine Zahlung angefordert. Die neue Rechnung ist über die Entwurfsvorschau mit einer abgeschlossenen Buchungs-ID prüfbar. Die synthetische Abrechnungsvorschau zeigt einen Erstattungsfall; echte Buchungen liefern ihre tatsächlichen Salden.

Die erste Seite nutzt die vorhandene Textfläche. Organisation und Ansprechpartner werden innerhalb der gemeinsamen Fläche der drei bestehenden Empfängerfelder gesetzt; reicht sie nicht aus, wird die Ausgabe gesperrt. Folgeseiten nennen Buchungsnummer und Seite und wiederholen den Tabellenkopf; Haupttext und Abschluss können nach der Tabelle fortgesetzt werden. Reservierungsschreiben behalten ihre bisherige Einseitenregel. Die ZIP-Struktur und die Modellrecords bleiben unverändert; die neuen katalogisierten Felder/Bedingungen können im bestehenden Format verwendet werden.

## Prüfung

Gezielte Backendtests prüfen die drei Zahlungsvarianten einschließlich ausgeblendeter Kautionsaufforderung bei null Euro, Kurzfristgrenzen und Fristen, die Beispielrechnung mit zwei Zahlungen und 99 Euro Erstattung, positive/ausgeglichene Salden, fehlendes Erstattungskonto, Revisionierung und unveränderte Downloadbytes, mehrseitige Tabellen und Migration up/rollback in zwei isolierten Schemas. Dazu kommen bestehende Reservierungs-, Vorlagenpaket-, Buchungs-, Routing-, Sicherheits- und Mietertests. Frontend: Produktionsbuild und gezieltes ESLint. Rechnungsseiten wurden gerendert und visuell geprüft. Kein produktiver Datenbankzugriff, Deployment oder SMTP-Versand.

Kostenpositionen werden fortlaufend nummeriert. Ungebuchte und Nullpositionen entfallen; eine vollständig rabattierte Mietposition bleibt mit ihrem ursprünglichen Betrag und einer separaten Rabattzeile sichtbar. Gleiche Prozentsätze werden etwa als „Rabatt von 10 % auf Positionen 1 und 2“ zusammengefasst, verschiedene Sätze getrennt ausgewiesen. Pauschale oder ältere Rabatte ohne gespeicherten Prozentsatz erscheinen als Euro-Nachlass mit Positionsbezug. Verbrauch wird je Snapshot mit Zählerbezeichnung ausgewiesen. Die Abrechnungssumme enthält den Rabatt genau einmal.
