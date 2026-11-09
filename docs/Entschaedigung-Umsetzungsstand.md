# Entschädigung – Wiederaufnahme

Verbindlicher Auftrag: Spezifikation in `KanuControl-Spezifikation-Entschaedigung.md` lokal in Frontend und Backend umsetzen, kein Deployment und keine produktive Migration.

Zusatz vom 07.11.2026: Bei Antwortmails Testempfänger und optionale CC-Adresse ermöglichen. Der globale SMTP-Testmodus muss Vorrang behalten und darf keine echten CC-Empfänger erreichen.

Stand der Wiederaufnahme: Frühere temporäre Arbeitskopie nicht mehr vorhanden. Die damals gemeldeten 61 Backendtests und PostgreSQL-Prüfungen sind kein Nachweis für den aktuellen Projektstand. Die Funktion muss erneut implementiert und geprüft werden.

Umsetzungsumfang:
- Objektspezifischer pflegbarer Katalog, getrennte Buchungspositionen mit historischem Preis und Anlass Beschädigung/Verlust.
- Transaktionale Synchronisation, Prüfung fremder IDs, Objektwechsel und serverseitige Dezimalberechnung.
- Buchungsformular und Katalogverwaltung, Einbindung in Finanzübersicht für Direkt und Airbnb ohne doppelte Verrechnung.
- PDF mit optionalem Entschädigungsabschnitt und unveränderlicher Dokumenthistorie sowie ausdrücklichem Korrekturprozess.
- Bearbeiten ohne automatischen Statuswechsel, erneuter Abschluss vor Korrekturdokument.
- Konfigurierbare Test- und CC-Adressen für Antwortmails in Frontend und Backend.
- Gezielte Backendtests, Frontend-Build, Browserprüfungen und PDF-Sichtprüfung.

Kein Katalog-Seed ohne eindeutige Zuordnung zum betreffenden Bootshaus.

## Dauerhaft umgesetzt am 07.11.2026

Antwortmail-Konfiguration unter Vermietung → Verwaltung → Homepage-Anbindung:
- Testadresse je Mietobjekt; leer bedeutet regulärer Mieterempfänger.
- Optionale CC-Adresse.
- Globaler SMTP-Testmodus hat Vorrang und unterdrückt CC.
- Neue additive Tenant-Migration `20261107-01-antwortmail-adressen.xml`.
- Backend kompiliert; TypeScript-Prüfung erfolgreich; alle 19 Tests in `HomepageStatusMailTest` erfolgreich, einschließlich objektspezifischer Testadresse/CC und globaler Testpriorität.
- Kein echter Mailversand und keine Datenbankmigration ausgeführt.

Weiter offen: Entschädigungsmodell, Katalog, Positionserfassung, Abrechnungsintegration und PDF-Dokumentkorrekturen gemäß Spezifikation. Dafür liegt weiterhin kein wiederhergestellter Funktionscode vor.

Zusätzlich dauerhaft angelegt: EntschaedigungKatalog mit DTO, Repository, validierendem Service und rollenbeschränkter REST-API. Neue Tenant-Migration `20261107-02-entschaedigung-katalog.xml`. Keine automatische Initialbefüllung. Die Oberfläche, Buchungspositionen und PDF-Integration sind noch offen.

## Frontend-Katalogverwaltung

Unter Vermietung → Verwaltung → Entschädigung ist der objektspezifische Katalog jetzt bedienbar. Einträge können angelegt, bearbeitet, sortiert und aktiviert/deaktiviert werden. Deutsche Dezimaleingabe, Preis-/Längenvalidierung, Fehleranzeige, erneutes Laden und Speicherschutz sind integriert. Während eines offenen Editors ist der Objektwechsel gesperrt. API-Änderungen übernehmen den Versionswert zur Konflikterkennung.

TypeScript-Prüfung und ESLint für alle neuen/geänderten Katalogdateien bestanden. Noch kein Browser-Livetest gegen einen migrierten Backendserver ausgeführt. Die Buchungspositionen und PDF-Integration bleiben offen.

## Buchungspositionen, Abrechnung und PDF umgesetzt

Der Bereich „Schäden und Fehlbestände“ erlaubt Katalog- und freie Positionen, Anlass Beschädigung/Verlust, Anzahl, Einzelpreis, Sortierung sowie interne Bemerkungen und Preisbegründungen. Neue Katalogpositionen speichern den ursprünglichen Preis. Bestehende Positionen bleiben bei Katalogänderungen stabil. Änderungen an Referenzen, fremde IDs, ungültige Geldwerte/Mengen und Objektwechsel mit vorhandenen Positionen werden serverseitig abgewiesen.

Die Entschädigung wird in der Finanzübersicht für Direkt und Airbnb zusätzlich zur rabattierten Miete und zum Verbrauch berücksichtigt. Die vorhandene Zahlungs-/Kautionsverrechnung wird über das gemeinsame Nebenkostensoll weiterverwendet; Entschädigungen erzeugen keine automatische Finanzbewegung. Der Endsaldo kommt jetzt aus dem Backend und wird identisch im Frontend und PDF verwendet.

Unter der Buchung sind Abrechnungsdokumente ausstellbar. PDFs werden unveränderlich als eigene Datensätze gespeichert. Korrekturen verlangen das letzte Dokument, einen internen Korrekturgrund und einen abgeschlossenen aktuellen Buchungsstand. Bei nachträglichen Änderungen bleibt der Buchungsstatus erhalten; „Erneut abschließen“ gibt den aktuellen Stand für ein neues Dokument frei. „Wieder öffnen“ bleibt als ausdrückliche Aktion verfügbar. Buchungen mit ausgestellten Dokumenten sind durch einen Fremdschlüssel gegen Löschung geschützt.

Zusätzliche Tenant-Migrationen: `20261107-03-entschaedigung-positionen.xml` und `20261107-04-vermietung-abrechnung.xml`. Keine Migration gegen die bestehende Datenbank ausgeführt. Keine automatische Kataloginitialisierung ohne eindeutige Objektzuordnung.

Aktuelle Validierung: Backend kompiliert; 54 gezielte Backendtests bestanden (Positionen, PDF, Buchungsnummern, Mietberechnung, Statusmails und Finanzdashboard). TypeScript und Produktionsbuild bestanden. PDF-Tests mit 0, 1 und 100 Positionen bestanden, erzeugte Seiten mit/ohne Positionen und die letzte Seite der langen Liste visuell geprüft. Interne Texte bleiben im PDF verborgen. Kein End-to-End-Test gegen einen migrierten laufenden Backendserver durchgeführt; keine produktive Migration, kein Deployment und kein echter Mailversand.
