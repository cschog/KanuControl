# KanuControl – Schäden und Fehlbestände in der Vermietungsabrechnung

Stand: 06.11.2026\
Geltungsbereich: Direktbuchungen und Airbnb-Buchungen\
Auftrag: Lokal implementieren und prüfen; kein Deployment.

## 1. Ziel

Beschädigtes oder fehlendes Inventar soll einer Vermietungsbuchung zugeordnet und dem Mieter mit der Abrechnung in Rechnung gestellt werden können. Grundlage ist ein pflegbarer Entschädigungskatalog je Mietobjekt.

In der Abrechnung erscheint der Abschnitt **„Entschädigung“ mit Einzelaufstellung ausschließlich dann, wenn Entschädigungspositionen vorliegen**. Ohne Positionen entfallen Überschrift, Aufstellung und Summenzeile vollständig. Insbesondere darf keine Nullzeile „Entschädigung 0,00 €“ erscheinen, auch wenn die bisherige Beispielabrechnung eine solche Zeile enthält.

Die Erfassung erfolgt ausdrücklich durch einen berechtigten Benutzer. Fehlende Gegenstände werden als „Verlust“ bezeichnet. Eine Einstufung als Diebstahl ist weder notwendig noch automatisch vorzunehmen.

## 2. Umfang und Abgrenzung

- Erfassung und Abrechnung gelten gleichermaßen für die Buchungsquellen DIREKT und AIRBNB.
- Bei beiden Quellen fließen die Positionen in die Abrechnungssumme und in das PDF ein.
- Eine automatische Übermittlung, Zahlungsanforderung oder Schadenmeldung an Airbnb ist nicht Bestandteil der Erweiterung.
- Die bestehende Ausnahme von der Verbrauchswertprüfung beim Abschluss einer Airbnb-Buchung bleibt bestehen.
- Leihgebühren und zusätzliche Reinigungs- oder Entsorgungskosten aus der Preisliste gehören nicht zu dieser Erweiterung.
- Es wird keine automatische Lagerbestandsverwaltung eingeführt.

## 3. Vorgehen im vorhandenen Projekt

Backend: `kanucontrolServer`, Spring Boot / Java / PostgreSQL / Liquibase.\
Frontend: `kanucontrolClient`, React / TypeScript / MUI.

Vor der Umsetzung die bestehenden Buchungs-, Preis-, Finanz- und Abrechnungsmodelle sowie die PDF-Erzeugung prüfen. Die Erweiterung in die vorhandenen Abläufe integrieren. Vorhandene Änderungen anderer Aufgaben erhalten und nicht überschreiben. Bestehende Projektvorgaben beachten.

Die folgenden Modellbezeichnungen sind fachliche Vorschläge. Die konkrete technische Benennung und Integration soll den Projektkonventionen folgen.

## 4. Entschädigungskatalog je Mietobjekt

Jeder Katalogeintrag enthält:

| Feld | Anforderung |
|---|---|
| ID | Eindeutige technische Identität |
| Mietobjekt | Pflichtzuordnung zu genau einem Mietobjekt |
| Bezeichnung | Pflichtfeld |
| Einzelpreis | Positiver Eurobetrag mit höchstens zwei Nachkommastellen |
| Sortierung | Reihenfolge in Verwaltung und Auswahl |
| Aktiv | Steuert die Auswahl für neue Buchungspositionen |

Die bestehende Preisverwaltung verwenden oder erweitern, soweit fachlich passend. Entschädigungen werden pro Stück berechnet und nicht nach Mietdauer.

### Ausgangswerte aus „2026-10 Preise gültig.pdf“

| Gegenstand | Einzelpreis |
|---|---:|
| Mülleimer | 7,50 € |
| Großer / tiefer Teller | 7,50 € |
| Kleiner Teller / Müslischälchen | 5,00 € |
| Untertasse | 3,00 € |
| Tasse | 5,00 € |
| Becher | 7,50 € |
| Glas | 3,00 € |
| Glasschälchen | 4,00 € |
| Schüssel | 10,00 € |
| Kaffeekanne | 35,00 € |
| Thermoskanne | 30,00 € |
| Spiegel 60 × 60 | 50,00 € |
| Deckenlampe | 50,00 € |
| Wasserkocher | 35,00 € |
| Mikrowelle | 120,00 € |
| Toaster | 100,00 € |

Diese Preise sind pflegbare, objektspezifische Stammdaten. Nicht als feste Berechnungswerte im Anwendungscode hinterlegen. Sie gelten nicht automatisch für andere Mietobjekte oder Mandanten. Eine Initialbefüllung nur mit eindeutiger Zuordnung zum betreffenden Bootshaus durchführen; keine Objekt- oder Mandanten-IDs erraten.

Inaktive Einträge sind für neue Positionen nicht auswählbar. Bereits zugeordnete Buchungspositionen bleiben erhalten. Verwendete Einträge bevorzugt deaktivieren statt löschen.

## 5. Erfassung in der Buchung

Im bestehenden Buchungsformular einen Bereich **„Schäden und Fehlbestände“** mit der Aktion **„Position hinzufügen“** ergänzen.

| Feld | Verhalten |
|---|---|
| ID | Eindeutige technische Identität der Buchungsposition |
| Buchung | Pflichtzuordnung zu genau einer Buchung |
| Katalogeintrag | Optionaler Verweis; nur zum Mietobjekt der Buchung passende Einträge |
| Bezeichnung | Aus dem Katalog übernehmen und eigenständig in der Position speichern; bei freier Position manuell erfassen |
| Anlass | Pflichtauswahl: Beschädigung oder Verlust |
| Anzahl | Positive ganze Zahl; Standardwert 1 |
| Einzelpreis | Aus dem Katalog übernehmen; für die Buchungsposition änderbar |
| Gesamtbetrag | Anzahl × Einzelpreis; serverseitig berechnen |
| Bemerkung | Optional; ausschließlich intern |
| Preisabweichungsbegründung | Bei Abweichung vom übernommenen Katalogpreis verpflichtend; ausschließlich intern |
| Sortierung | Reihenfolge der Positionen in Formular und Abrechnung |

Neben Katalogpositionen freie Positionen ermöglichen, beispielsweise „Beschädigte Tür – Reparatur“. Dafür sind Bezeichnung, Anlass, Anzahl und Einzelpreis manuell zu erfassen. Eine freie Position benötigt keine Preisabweichungsbegründung, da kein Katalogpreis zugrunde liegt.

Positionen lassen sich hinzufügen, bearbeiten und entfernen. Im Bereich wird die Gesamtsumme der Entschädigungen angezeigt. Keine Pflicht zur Erfassung von Schäden bei einer unbeschädigten Rückgabe einführen.

## 6. Preisübernahme und historische Stabilität

Beim Hinzufügen einer Katalogposition Bezeichnung und Einzelpreis als eigenständige Werte in der Buchungsposition speichern. Den ursprünglich übernommenen Katalogpreis zusätzlich erhalten, damit Preisabweichungen nachvollziehbar und auch nach späteren Katalogänderungen prüfbar bleiben.

Spätere Änderungen am Katalog verändern bestehende Buchungspositionen nicht. Das gilt für Preise, Bezeichnungen und Aktivstatus. Beim erneuten Laden oder Speichern einer Buchung keine automatische Aktualisierung aus dem Katalog durchführen.

Bei Änderungen des Mietobjekts dürfen bestehende Entschädigungspositionen nicht stillschweigend einem anderen Objekt zugeordnet werden. Den Konflikt im vorhandenen Bearbeitungsablauf validieren und eine ausdrückliche Bereinigung verlangen.

## 7. Validierung und Berechnung

- Alle fachlichen Regeln auch serverseitig durchsetzen.
- Anzahl muss eine positive ganze Zahl sein.
- Einzelpreis muss größer als null sein und höchstens zwei Nachkommastellen besitzen.
- Bezeichnung darf nicht leer sein; projektübliche Längenbegrenzungen festlegen und in Datenbank, DTOs und Formular konsistent anwenden.
- Anlass muss ein gültiger Enumwert sein.
- Katalogeintrag, Buchung und Mietobjekt müssen zum selben Mandanten gehören.
- Bei einer neuen Auswahl darf der Katalogeintrag nicht inaktiv sein. Bereits gespeicherte Positionen bleiben trotz späterer Deaktivierung bearbeitbar.
- Gesamtbeträge aus Anzahl und Einzelpreis berechnen; vom Client gelieferte Summen nicht als maßgeblich übernehmen.
- Geldberechnung mit Dezimalarithmetik, im Java-Backend insbesondere `BigDecimal`; keine binäre Gleitkommaarithmetik für maßgebliche Beträge.
- Summe aller Entschädigungspositionen genau einmal zum bestehenden Abrechnungsbetrag addieren.
- Bestehende Ermäßigungen für Mietbereiche gelten nicht für Entschädigungen.
- Bereits gezahlte Beträge und Kautionen weiterhin nach den vorhandenen Regeln behandeln. Keine zweite Verrechnung oder zusätzliche Zahlungslogik einführen.
- Entschädigungen lösen nicht automatisch eine Zahlung oder Finanzbewegung aus; die bestehende Abrechnungs- und Zahlungslogik verwenden.

## 8. Abrechnung und PDF

### Ohne Positionen

Den gesamten Abschnitt auslassen: keine Überschrift, keine Einzelaufstellung, keine Summenzeile und keine Nullzeile.

### Mit Positionen

Nach den regulären Kosten und vor Zahlungsabzügen den Abschnitt **„Entschädigung“** mit allen Einzelpositionen und einer Zwischensumme ausgeben.

| Anzahl | Gegenstand | Anlass | Einzelpreis | Betrag |
|---:|---|---|---:|---:|
| 2 | Glas | Beschädigung | 3,00 € | 6,00 € |
| 1 | Thermoskanne | Verlust | 30,00 € | 30,00 € |
| | **Entschädigung gesamt** | | | **36,00 €** |

Die bestehende Abrechnungssumme erhöht sich in diesem Beispiel um 36,00 €. Die Aufstellung erläutert den bereits einbezogenen Betrag und darf ihn nicht nochmals addieren.

Weitere Anforderungen:

- Deutsche Zahlen- und Währungsformatierung verwenden.
- Interne Bemerkungen und Preisabweichungsbegründungen nicht im PDF ausgeben.
- Freie Positionen genauso wie Katalogpositionen darstellen.
- Lange Bezeichnungen und mehrseitige Aufstellungen vollständig und lesbar ausgeben; Seitenumbrüche berücksichtigen.
- Anzeige im Frontend und PDF müssen auf derselben serverseitigen Berechnungsgrundlage beruhen.

## 9. Status, Berechtigungen und Dokumentkorrekturen

- Vorhandene Berechtigungen für die Bearbeitung von Vermietungsbuchungen verwenden. Die Katalogverwaltung in die bestehende Berechtigungsstruktur der Vermietungsverwaltung integrieren.
- Entschädigungspositionen dürfen nicht buchungs- oder mandantenübergreifend gelesen oder geändert werden.
- Das Erfassen oder Bearbeiten ändert den Buchungsstatus nicht automatisch.
- Insbesondere bleiben abgeschlossene, stornierte oder abgelehnte Buchungen beim Öffnen und Korrigieren zunächst in ihrem bestehenden Status.
- Die vorhandenen Regeln zum ausdrücklichen erneuten Abschluss beachten.
- Bereits ausgestellte Abrechnungsdokumente nicht durch spätere Änderungen stillschweigend verändern. Den vorhandenen Korrekturprozess verwenden.
- Falls das Projekt noch keinen ausreichenden Korrekturprozess für ausgestellte Dokumente besitzt, diese Lücke ausdrücklich benennen und die betroffene Bearbeitung absichern; keinen unbemerkten Austausch ausgestellter Dokumente einführen.

## 10. Persistenz, API und Migration

Benötigt werden fachlich ein Katalog je Mietobjekt und davon getrennte Buchungspositionen mit historischen Werten. Die konkrete Einbindung anhand des bestehenden Modells festlegen.

- Neue Tabellen oder Erweiterungen mit einer neuen Liquibase-Tenant-Migration anlegen. Bereits bestehende Changesets nicht nachträglich ändern.
- Passende Fremdschlüssel, Pflichtfelder und Datenbankbedingungen vorsehen.
- DTOs, Mapping, Services und APIs ergänzen.
- Bestehende Speicherabläufe und Transaktionsgrenzen berücksichtigen; keine teilweise gespeicherten Positionslisten hinterlassen.
- Keine Positionen anderer Buchungen durch übergebene IDs übernehmen oder ändern.
- Vorhandene Buchungen starten ohne Entschädigungspositionen. Ihre bisherigen Beträge bleiben unverändert.
- Bestehende Buchungslösch- und Aufbewahrungsregeln auch auf die neuen Daten anwenden.

## 11. Abnahmekriterien und Prüfungen

1. Bei einer Buchung ohne Entschädigungspositionen bleibt die bisherige Berechnung unverändert. Im PDF erscheint kein Entschädigungsabschnitt.
2. Zwei beschädigte Gläser zu je 3,00 € und eine verlorene Thermoskanne zu 30,00 € ergeben exakt 36,00 € zusätzliche Kosten.
3. Alle Einzelpositionen, ihre Beträge und die Zwischensumme erscheinen vollständig in der Abrechnung.
4. Die Entschädigungssumme fließt genau einmal in den Rechnungsbetrag ein.
5. Das Entfernen der letzten Position entfernt den gesamten Abschnitt aus der nächsten zulässigen Abrechnungserstellung.
6. Spätere Änderungen von Katalogpreis, Bezeichnung oder Aktivstatus verändern gespeicherte Buchungspositionen nicht.
7. Eine Preisabweichung vom ursprünglich übernommenen Katalogpreis verlangt eine interne Begründung. Diese erscheint nicht im PDF.
8. Eine freie Position lässt sich ohne Katalogeintrag erfassen und korrekt abrechnen.
9. Nullwerte, negative Beträge, ungültige Anlässe und nicht ganzzahlige Mengen werden serverseitig abgewiesen.
10. Bestehende Mietrabatte reduzieren die Entschädigung nicht. Bereits gezahlte Beträge werden weiterhin korrekt berücksichtigt.
11. Die Regeln gelten bei DIREKT und AIRBNB identisch. Airbnb-Abschlüsse verlangen weiterhin keine Verbrauchswertprüfung.
12. Das Bearbeiten der Positionen verändert den Buchungsstatus nicht automatisch.
13. Fremde Buchungspositions-IDs und Katalogeinträge anderer Mietobjekte oder Mandanten werden abgewiesen.
14. Mehrseitige Aufstellungen und lange Bezeichnungen sind im PDF vollständig lesbar.
15. Bereits ausgestellte Dokumente bleiben erhalten; Korrekturen folgen dem bestehenden Verfahren.

Gezielte Tests für Geldberechnung, historische Preiswerte, Validierung, Berechtigungen und beide Buchungsquellen ergänzen. Die PDF-Darstellung mit und ohne Positionen sowie mit einer langen Aufstellung prüfen. Die projektüblichen relevanten Backend- und Frontend-Prüfungen ausführen und tatsächliche Ergebnisse berichten.

## 12. Erwarteter Abschlussbericht

Nach der lokalen Umsetzung kurz angeben:

- Welche Bereiche geändert wurden und wo die neuen Funktionen bedient werden.
- Welche Migration hinzugekommen ist und wie der Katalog initial gepflegt wird.
- Welche Prüfungen bestanden haben und welche wegen konkreter Umgebungsgrenzen nicht ausgeführt werden konnten.
- Welche fachlichen oder technischen Lücken gegebenenfalls verbleiben.

Kein Deployment, keine produktive Datenbankmigration und kein automatischer Versand von Abrechnungen oder Airbnb-Anfragen im Rahmen dieses Auftrags.
