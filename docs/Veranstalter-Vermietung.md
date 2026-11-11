# Veranstalter in der Vermietung

Umgesetzt am 12.11.2026 gemäß KanuControl-Spec-Veranstalter.md.

## Bedienung

Unter Vermietung → Veranstalter können Organisationen nach Name oder Abkürzung gesucht, angelegt und bearbeitet werden. Die Suche enthält auch vorhandene KJFP-Vereine; es entsteht kein zweiter Stammdatensatz. Die Auswahl öffnet die Detailansicht, „Zurück zur Suche“ führt zur Liste zurück. Adresse, Land, Bankdaten und Kontoinhaber verwenden das vorhandene Vereinsformular. Telefon, Schutzkonzept und KiK-Angaben werden in dieser Ansicht nicht angeboten.

In der Buchung bleiben Mieter und Veranstalter getrennt. Die Veranstalterauswahl kann entfernt werden. Bei gesetztem Veranstalter zeigt die Maske Rechnungsempfänger und Ansprechpartner an; beim Laden und Abbrechen der Bearbeitung wird die gespeicherte Auswahl wiederhergestellt.

## Server und Daten

- Eigene API `/api/vermietung/veranstalter` (Suche, Lesen, Anlegen, Bearbeiten), geschützt mit ADMIN oder VERMIETUNG. KJFP-Rechte werden nicht erweitert.
- Gemeinsame Entität `Verein`, bestehender Fremdschlüssel `buchung.veranstalter_verein_id` bleibt erhalten.
- Liquibase-Tenantmigration `20261111-03-veranstalter.xml`: `verein.kjfp_verein`, NOT NULL, Standardwert true. Alle Bestandsvereine bleiben KJFP-Vereine. Neuanlagen über die Veranstalter-API setzen false. KJFP-Suchen und -Verknüpfungen berücksichtigen die Kennzeichnung.
- Veranstalter-Updates schreiben ausschließlich die gemeinsamen Stammdaten. Telefon, Schutzkonzept, beide KiK-Daten, Hausverein und KJFP-Kennzeichnung werden serverseitig nicht aus dem Request übernommen; sie bleiben auch bei ausgelassenen oder explizit leeren Feldern erhalten.
- Mandantengebundene Repositories werden weiterverwendet; die neue API verlangt einen Mandantenkontext.

## Dokumente und Versand

`Buchungsadressat` liefert die vorhandenen Empfängerfelder `empfaenger_name`, `empfaenger_strasse` und `empfaenger_ort` gemeinsam für den älteren Reservierungspfad, den Buchungsnachrichtenworkflow und die Abrechnung. Der Name enthält bei Veranstalterbuchungen Veranstalter und Mieter in zwei aufeinanderfolgenden Zeilen. Straße, PLZ, Ort und ggf. Länderzeile stammen vollständig vom Veranstalter. Fehlende erforderliche Angaben werden erst bei Dokumenterzeugung beanstandet; es gibt keinen Rückfall auf die Mieteranschrift.

Der bestehende Textplatzhalter `organisation` verwendet bei Veranstalterbuchungen ebenfalls den Veranstalternamen. Die vorhandenen PDF-Formularfelder und Briefpapiervorlagen bleiben kompatibel; ein Austausch der PDF-Dateien ist nicht erforderlich. Vorschau, Download und automatische Anhänge verwenden dieselbe Zuordnung. Veranstalterwechsel und Änderungen seiner Adressdaten entwerten einen noch nicht ausgeführten Nachrichtenentwurf. Bereits archivierte Dokumente werden nicht verändert.

Die E-Mail-Empfängerermittlung verwendet weiter ausschließlich den Mieter. Vorhandene interne CC-Konfiguration und Testumleitungen bleiben erhalten; Veranstalterkontakte werden nicht hinzugefügt. Eine fehlende Mieter-E-Mail wird nicht ersetzt.

## Prüfung

85 erfolgreiche gezielte Backend-Tests in 11 Testklassen: VeranstalterServiceTest, VeranstalterSecurityTest, BuchungsadressatTest, VeranstalterMigrationTest, VereinSecurityTest, ReservierungsbestaetigungTest, StatusMailVorschauTest, ReservierungMailTest, BuchungsnachrichtWorkflowTest, AbrechnungAbschlussLayoutTest und AbrechnungVorlagenTest.

Dabei geprüft: echte HTTP-/Methodenberechtigungen; API-Anlage ohne KJFP-/Bankdaten; Erhalt ausgeblendeter Daten bei API-Updates; Adressierung einschließlich Länderzeile, Wechsel, Entfernen und fehlender Anschrift; PDF-Text für Reservierung, Bestätigung und Abrechnung; Mieter-Mailadresse trotz Veranstalterkontakt; Entwurfsinvalidierung; Migration und Rollback in zwei getrennten H2-Mandantenschemata unter Erhalt bestehender Buchungszuordnungen.

Backend-Kompilierung, Frontend-Produktionsbuild (`npm run build`) und ESLint der neuen API/Seite sowie angepasster gemeinsamer Vereinskomponenten erfolgreich. Backendtests liefen mit Java 21, Headless-AWT und dem bereits im Projekt verwendeten Byte-Buddy-Agenten:

```sh
mvn -o '-DargLine=-Djava.awt.headless=true -javaagent:/Users/chris/.m2/repository/net/bytebuddy/byte-buddy-agent/1.17.8/byte-buddy-agent-1.17.8.jar' -Dtest=VeranstalterServiceTest,BuchungsadressatTest,VeranstalterSecurityTest,VereinSecurityTest,ReservierungsbestaetigungTest,StatusMailVorschauTest,ReservierungMailTest,BuchungsnachrichtWorkflowTest,AbrechnungAbschlussLayoutTest,AbrechnungVorlagenTest,VeranstalterMigrationTest test
```

Kein Produktionsdeployment, keine Migration auf einer bestehenden Produktivdatenbank und kein interaktiver Browser-Abnahmetest. Die mobile Ansicht verwendet das vorhandene responsive Formularraster.

## Anpassung der Liste und Buchungsnavigation

Das Veranstalterfeld steht direkt unter dem Mieter. Die Veranstalterverwaltung verwendet das Tabellen- und Suchdesign der Mieterverwaltung mit den Spalten Name, Abk, Länderkennzeichen, PLZ, Ort und Buchungen. Alle Spalten sind sortierbar; auf mobilen Geräten werden die Angaben einschließlich Buchungslink als Karten angezeigt.

Die Buchungszahl wird mandantenbezogen über alle Status und Mietobjekte ermittelt, für die Liste in einer gruppierten Datenbankabfrage. Der Link öffnet `/vermietung/buchung?veranstalterId=…` mit passendem Veranstalterfilter, zunächst ohne Einschränkung auf ein Mietobjekt und einschließlich stornierter/abgelehnter Buchungen. Der Filter lässt sich in der Übersicht aufheben. Keine weitere Schemaänderung erforderlich.

Für diese Anpassung geprüft: Frontend-Produktionsbuild, ESLint aller fünf geänderten/neuen Frontenddateien und 14 erfolgreiche API-/Security-Tests (VeranstalterServiceTest und VeranstalterSecurityTest), einschließlich korrekter Buchungszahlen und Organisationen ohne Buchungen.
