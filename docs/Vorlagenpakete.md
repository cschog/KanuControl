# ZIP-Transport der Vorlagen (Format 1)

## Bedienung

In **Vermietung → Verwaltung → E-Mail- und PDF-Vorlagen** lädt **Alle Vorlagen als ZIP exportieren** den gespeicherten Stand des aktuellen Mandanten herunter. Offene Editoränderungen werden angezeigt und nicht mitgespeichert. Enthalten sind alle gespeicherten Objekt-/Vorgangsvorlagen sowie vorhandenes Briefpapier, Grußformel und Abschlussbild. Buchungen, Empfänger, Archive und Versandkonfiguration sind nicht enthalten.

1. ZIP lokal entpacken. Die Vorlagen unter `vorlagen/objekt-…/VORGANG.md` als UTF-8 bearbeiten. Das Manifest und die enthaltene `ANLEITUNG.md` erklären die Zuordnung.
2. `manifest.json` und die Dateien direkt wieder als ZIP verpacken, ohne äußeren Sammelordner, Passwort oder weitere Archive. Prüfsummen müssen nicht bearbeitet werden.
3. Im Editor offene Änderungen zunächst speichern oder verwerfen. **Vorlagen aus ZIP importieren** öffnen und die Datei wählen.
4. Jedes Quellobjekt ausdrücklich einem Zielobjekt zuordnen oder bewusst überspringen. Gleiche Namen sind kein Identitätsnachweis. Je Vorlage **Neu**, **Vorhandene ausdrücklich ersetzen** oder **Überspringen** wählen.
5. Das gemeinsame Briefpapier samt Grußformel und Bild nur als Einheit auswählen. Es wirkt auf **alle** Mietobjekte des Zielmandanten. `bild:null` entfernt bei ausgewählter Einheit das bisherige Bild; fehlendes Briefpapier bewirkt keine Löschung.
6. **Zuordnung und Auswahl prüfen** zeigt Zahlen und die vollständigen Inhalte vorher/nachher. Anschließend **Wirksamen Entwurf als PDF und E-Mail prüfen** aufrufen. Bei Briefpapierwechsel werden auch die beibehaltenen Vorlagen geprüft.
7. Vier synthetische Szenarien je wirksamer Vorlage (OG ja/nein, kurz-/langfristig) sowie gegebenenfalls die gemeinsame Briefpapiervorschau prüfen und im Dialog bestätigen. Das ist eine Layoutkontrolle; Grenzen für echte Buchungsdaten werden weiterhin zur Laufzeit geprüft.
8. Nicht renderbare Vorlagen werden ausdrücklich als **nicht erfolgreich visuell geprüft** angezeigt. Fehler korrigieren oder die fehlende Prüfbarkeit pro Vorlage bewusst klären und die Verwaltung ohne Layoutprüfung freigeben; anschließend Auswahl erneut prüfen und Vorschau erneut ausführen. Das ist keine Versandfreigabe.
9. **Geprüfte Auswahl übernehmen** speichert alle ausgewählten Änderungen gemeinsam. Unberücksichtigte Vorlagen bleiben erhalten. Danach müssen normale Vorlagen- und Buchungsvorschauen erneut erstellt werden. **Abbrechen** verwirft nur den Entwurf.

Entwürfe sind 30 Minuten gültig, an Benutzer und Mandant gebunden und in der Tenant-Datenbank gespeichert (auch bei mehreren Serverinstanzen). Je Benutzer sind drei offene Entwürfe vorgesehen. Ein neuer Upload bereinigt abgelaufene Entwürfe. Verbrauchte Entwürfe enthalten keine Paketbytes und keinen Plan mehr; ihr Übernahmenachweis bleibt bis zum Ablauf für Wiederholungen nutzbar. Bei Zieländerungen meldet die API 409; Auswahl und Vorschau erneut prüfen. Auch zwischenzeitliche Neuanlagen werden erkannt. Pakethashes binden die Bytes, belegen aber weder Herkunft noch Signatur.

## Format

ZIP-Wurzel: `manifest.json`, `ANLEITUNG.md`; gegebenenfalls `mandant/briefpapier.pdf`, `mandant/grussformel.md`, `mandant/unterschrift.png` oder `.jpg`; je gespeicherter Vorlage `vorlagen/<paketschluessel>/<VORGANG>.md`. Objektverzeichnisse sind paketinterne Schlüssel, keine Datenbank-IDs. Vollständig leere Konfigurationen haben leere Objekt-/Vorlagenlisten und `briefpapier:null`. Es gibt keinen Spiegelungs- oder Löschmodus.

Das Manifest enthält `format:"kanucontrol-vorlagen"`, ganzzahliges `formatVersion:1`, `exportiertAm` als UTC-Zeitstempel, `quelle:{mandantBezeichnung}`, `briefpapier:null` oder `{pdf,grussformel,bild}`, `mietobjekte:[{schluessel,bezeichnung}]`, `vorlagen:[{mietobjekt,vorgang,datei}]`. `bild` ist ausdrücklich ein Pfad oder `null`. Unbekannte Eigenschaften/Versionen und doppelte JSON-Schlüssel sind Fehler. Quellrevisionen und Primärschlüssel werden nicht importiert.

Jede Vorlagendatei beginnt mit genau einem Codeblock `kanucontrol-template`, dessen JSON `formatVersion`, `anrede`, `betreff`, `bloecke` enthält. Blöcke besitzen `id`, `titel`, `bedingung`, `varianten`; Varianten `id`, `bedingung`. Die Arrayreihenfolge bestimmt die Textreihenfolge. IDs sind innerhalb des jeweiligen Blocks beziehungsweise der Datei eindeutig. `varianten:null` und eine leere Variantenliste bleiben unterscheidbar.

Nach dem Metadatenblock folgen genau die Bereiche:

```text
<!-- kc:mailtext -->
E-Mail-Text
<!-- kc:end -->

<!-- kc:block b01 -->
Blocktext
<!-- kc:end -->

<!-- kc:variante b01 v01 -->
Variantentext
<!-- kc:end -->
```

Marker müssen allein auf ihrer Zeile stehen. Nach jedem Startmarker und vor jedem Endmarker ist genau eine protokollbedingte Zeilentrennung; alle zusätzlichen Leerzeilen, Einrückungen und Textzeichen bleiben erhalten. Ein leerer Text hat daher eine leere Zeile zwischen den Markern. Außerhalb von Metadaten und Bereichen sind nur vollständig leere Zeilen erlaubt. Keine weiteren Kommentare, Beschriftungen oder Codeblöcke ergänzen. Reserviert sind Zeilen, die mit `<!-- kc:` beginnen, und die Zeile `` ```kanucontrol-template ``. Sie können nicht als gewöhnlicher Vorlagentext verwendet werden. Grußformeldateien enthalten direkt den Grußtext; ein leerer Gruß ist bei reinem Bildabschluss gültig.

UTF-8-BOM am Dateianfang wird akzeptiert. Ungültiges UTF-8 wird abgewiesen; CRLF und einzelne CR werden zu LF normalisiert. Markdown bleibt ein Textcontainer und führt keine neue HTML-/Markdown-Darstellung im Renderer ein. Betreff, Titel und PDF-Anrede bleiben einzeilig; Blocktitel bleiben ausschließlich Editorhilfen. Bedingungen, Tokens und fachliche Grenzen werden aus `VorlagenModell`, PDF-Felder aus `BriefpapierPdf.PFLICHT` validiert. Erweiterungen der Modellrecords blockieren den Codec, bis das Format angepasst wurde; unbekannte gespeicherte JSON-Eigenschaften werden nicht still entfernt.

## Sicherheit und Transaktionen

Alle Paketendpunkte unter `/api/vermietung/vorlagen/pakete` verlangen ADMIN oder VERMIETUNG. Export/Entwurfsantworten und Paketfehler verwenden `Cache-Control: no-store`. Es gibt keinen Zieltenant im Requestvertrag. Der Export läuft in einer read-only REPEATABLE_READ-Transaktion. PDF- und Bildbytes werden unverändert transportiert.

Grenzen werden während des Lesens geprüft: 25.000.000 komprimierte Bytes, 50.000.000 tatsächliche entpackte Bytes, 500 Einträge, 1.000.000 Bytes je JSON/Markdown, zusätzlich 5 MB PDF und 1 MB Bild. Die bestehenden Multipartgrenzen (25 MB je Datei, 100 MB Request in dev/test/prod) wurden nicht vergrößert. ZIP wird ohne Dateisystemextraktion verarbeitet. Pfadtraversal, Backslashes, Laufwerke, Symlinks, Verschlüsselung, normalisiert/kasierend kollidierende Pfade und nicht deklarierte Dateien werden abgewiesen. Nur `.DS_Store` und `__MACOSX/` werden nach Pfad- und Größenprüfung ignoriert.

Der Entwurf speichert Paketbytes und Hash, Eigentümer, Tenant, Ablauf, Plan und Fingerprint des gesamten relevanten Zielstands inklusive fehlender/neuer Zeilen. Jede Planänderung entwertet Vorschauen. Bei der Übernahme werden der Entwurf und die drei betroffenen PostgreSQL-Tabellen kurz gesperrt, der gesamte Zielstand erneut verglichen und alle Writes in einer Transaktion ausgeführt. Eine konkurrierende Editoränderung kann nicht zwischen Vergleich und Writes geraten. Die Tabellensperre hat ein Wartebudget von fünf Sekunden. Erfolgreiche Wiederholungen liefern den verbrauchten Übernahmenachweis. Versionszähler entstehen regulär. Auch bei beibehaltenem Briefpapier wird dessen lokale Revision erhöht, damit nach einem Teilimport alle bisherigen Versandfingerprints des Mandanten verfallen; die Briefpapierbytes und der Abschluss bleiben dabei unverändert. Archive werden nicht verändert.

Migration: `20261110-01-vorlagenpakete.xml` legt ausschließlich die neue Tenant-Stagingtabelle und ihren Ablaufindex an. Nicht im Rahmen dieser Entwicklung auf produktiven Schemas ausgeführt.

## Reproduzierbare Prüfung

Aus `kanucontrolServer`:

```sh
mvn -Dtest=VorlagenPaketTest,VorlagenPaketSecurityTest,VorlagenVorschauTest,VorlagensystemTest,VorlagenVersandMappingTest,BriefpapierPersistenzTest,VorlagensystemMigrationTest test
```

Der opt-in PostgreSQL-Test verlangt eine **frisch angelegte, ausschließlich wegwerfbare lokale Testinstanz** auf Port 55439 mit Testbenutzer `kc_paket_test`, ohne Anwendungsprofile oder Produktionsverbindung:

```sh
KC_PAKET_TEST_PG='jdbc:postgresql://127.0.0.1:55439/postgres?user=kc_paket_test' \
  mvn -Dtest=VorlagenPaketPostgresTest test
```

Er erstellt zufällige Tenant-Schemas, testet echte JPA-Transaktionen, Fehler nach geflushten Writes, Konflikte einschließlich paralleler Neuanlage, Benutzer-/Tenantbindung, Ablauf, Idempotenz und Liquibase up/rollback. Er nutzt Hibernate-DDL für die übrigen Testtabellen; die vollständige historische Tenant-Migrationskette wird dadurch nicht nachgewiesen. Zwei Contexts stellen zwei getrennte Tenant-Verbindungen dar; ein produktiver HTTP-/Keycloak-End-to-end-Test ist das nicht.

Frontend: `npm run build` und gezielter ESLint für `VorlagenPakete.tsx`/`VorlagenVerwaltung.tsx`. Der Dialog wurde mit einer temporären synthetischen API-Prüfansicht bei schmaler (390 px) und breiter Ansicht geprüft. Die Prüfansicht wird nicht ausgeliefert. Ein synthetisches, serverseitig eingelesenes Beispielpaket liegt unter `src/test/resources/vorlagenpakete/beispiel-ohne-briefpapier.zip`.
