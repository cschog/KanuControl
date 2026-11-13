# Gemeinsame Belegnummern

Normale Belege, Quittungen (einschließlich Rückzahlungsquittungen) und Fahrkostenabrechnungen verwenden `BelegnummerService`. Die Nummer wird beim Speichern vergeben, nicht beim Export. Eine Quittung mit mehreren Zahlungspositionen und eine Fahrkostenabrechnung mit mehreren Abschnitten erhalten jeweils eine Nummer.

## Geltungsbereich und Bestand

Jedes Konto hat einen eigenen Zähler, gemeinsam für alle drei Dokumenttypen. Ein neues Konto beginnt bei 001: RZ_028 beeinflusst Hil_001 nicht. Bereits vorhandene Nummern bleiben unverändert; ein Konto mit Bestandsnummern zählt oberhalb seines höchsten vorhandenen numerischen Suffixes weiter.

Die bereits veröffentlichte Tenant-Migration `20261112-01-gemeinsame-belegnummern` bleibt unverändert. Sie übernimmt vorhandene Belegnummern in ein Reservierungsregister und ergänzt vorhandene Quittungen mit Kontozuordnung. Die neue Folgemigration `20261112-02-belegnummern-je-konto` ersetzt anschließend den ursprünglichen Veranstaltungszähler durch Kontozähler. Jeder Kontozähler wird anhand der höchsten reservierten Nummer seines Präfixes initialisiert; auch Reservierungen gelöschter Dokumente bleiben berücksichtigt. Bereits vergebene Nummern werden nicht verändert.

Bei Bestandskollisionen bricht die erste Migration mit einer Meldung ab. Liquibase führt beide Migrationen jeweils einmalig in einer Transaktion aus. Der Upgradepfad aus einer bereits ausgeführten ersten Migration wird mit echtem Liquibase getestet, einschließlich der auf dem Server protokollierten Prüfsumme `9:a0a448d3763e69f4feaada03055d5b54` und eines wiederholten Updates.

## Behebung des Startfehlers vom 12.11.2026

Der Fehler entstand durch nachträgliches Ändern der bereits ausgeführten ersten Migration. Ihre SQL-Datei wurde auf den ursprünglichen Inhalt zurückgesetzt; die Änderung des Zählerumfangs erfolgt ausschließlich in der neuen Folgemigration. Das Backend mit diesen Dateien neu bauen und über den üblichen Prozess bereitstellen. Die Prüfsummenprüfung bleibt aktiv. Weder `clearCheckSums` noch manuelle Änderungen an `DATABASECHANGELOG` sind für diesen Upgradepfad erforderlich. Diese Korrektur wurde ausschließlich lokal geprüft; ein erneutes Deployment wurde nicht ausgeführt.

Fahrkostenabrechnungen erhalten keine erfundene Kontozuordnung. Bestehende Einträge ohne Konto werden als nicht druckbar mit Korrekturhinweis angezeigt. In der Fahrerbearbeitung der Fahrkostenabrechnung ein Konto auswählen und speichern. Unnummerierte Quittungen ohne Konto werden ebenfalls mit einem Hinweis angezeigt; Konto zuordnen und Quittung speichern.

Ein Kontowechsel nummerierter Vorgänge wird mit einer fachlichen Fehlermeldung abgewiesen. Nummern bleiben nach dem Löschen eines Dokuments im Register reserviert. Der atomare PostgreSQL-UPSERT sperrt die Zählerzeile bis zum Ende der fachlichen Transaktion. Aktualisierungen von Quittungen und Fahrkostenabrechnungen verwenden zusätzlich eine Schreibsperre auf dem Vorgang.

## Prüfung

Aus `kanucontrolServer`:

```sh
./mvnw -Dtest=BelegnummerSpeichernTest,BelegnummerPdfTest,KjfpBeitraegeZahlungsstatusTest,ZahlungsstatusServiceTest,KjfpAbrechnungRegressionTest test
KC_BELEG_TEST_URL='jdbc:postgresql://localhost:55439/postgres?user=chris' ./mvnw -Dtest=BelegnummerIntegrationTest test
```

Die Integrationstests benötigen eine eigens bereitgestellte lokale Testdatenbank mit Berechtigung zum Anlegen von Schemas. Pro Test wird ein zufälliges Schema erzeugt und anschließend entfernt. Ohne `KC_BELEG_TEST_URL` werden nur diese Integrationstests übersprungen. Geprüft werden Bestandsübernahme, fehlende Konten, unveränderte Nummern, wiederholte Ergänzung, Kollisionserkennung, Reservierung nach Löschung sowie 40 parallele Vergaben. PDF-Tests erzeugen echte Dokumente mit Anhängen und vergleichen den Text wiederholter Exporte einschließlich Reihenfolge und Beschriftung.

Aus `kanucontrolClient`: `npm run build`.

Keine produktive Datenbank wurde verändert; kein Deployment wurde ausgeführt.


## Anzeige in Übersichten und PDFs

Im Finanzausgleich-PDF führen die Tabellen für Belege, Zahlungsnachweise und Fahrkosten die gespeicherte Belegnummer als erste Spalte. Unnummerierte Zahlungsnachweise bleiben dort leer. Tabellenköpfe werden auch nach einem Seitenwechsel wiederholt.

Im Belege-PDF erscheinen bei jedem Kontowechsel und nach dem letzten Konto getrennte Zwischensummen für Einnahmen und Ausgaben sowie getrennte Gesamtsummen. Die Richtung folgt der vorhandenen Kategorie: Verpflegung ist eine Ausgabe, Pfand eine Einnahme. Auch Belege mit beiden Arten von Positionen werden getrennt dargestellt. Im Anlege- und Bearbeitungsdialog kann die Art ausdrücklich gewählt werden; die Kategorieauswahl wird entsprechend gefiltert. Die gespeicherte Zuordnung erfolgt weiterhin über die Kategorie, ohne zusätzliches widersprüchliches Typfeld.
