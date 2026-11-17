# KJFP-Use-Case-Tests

Ergänzungen aus der Bestandsprüfung vom 16.11.2026.

## Ausführen

Server im Verzeichnis `kanucontrolServer`:

```sh
./test-kjfp.sh
```

Der Befehl umfasst die neuen Use-Case-Tests und vorhandene KJFP-Regressionen
sowie gemeinsam verwendete Veranstaltungs-/Teilnehmerfunktionen. Weitere
Maven-Optionen können angehängt werden. Der Gesamtlauf bleibt `mvn clean test`.
Es gilt die bestehende lokale JUnit-Datenbankkonfiguration.

Die neuen gespeicherten Fachszenarien verwenden das eigene Schema
`kjfp_usecase_test`. Normale Testtransaktionen werden zurückgerollt; Tests mit
getrennten, tatsächlich committeten Transaktionen bereinigen ihre Hilfsdaten.
Der Mandantengrenzentest erstellt zusätzlich ein zufälliges Schema und entfernt
es anschließend. Nicht gleichzeitig gegen dieselbe Testdatenbank ausführen.
Keine Produktionsdatenbank verwenden.

Die fünf Belegnummern-PostgreSQL-Tests werden mit `KC_BELEG_TEST_URL` aktiviert.
Sie legen zufällige Testschemas an und löschen diese wieder. Die Variable muss
auf eine dedizierte lokale Testdatenbank mit Schema-Erstellungsrechten zeigen.
Ohne diese Variable bleiben die fünf Tests bewusst optional.

Client im Verzeichnis `kanucontrolClient`:

```sh
npm run test:kjfp
```

Dafür werden Playwright und Chromium benötigt. Optional `PLAYWRIGHT_MODULE`
und `CHROME_PATH` setzen, wenn sie außerhalb der Projektabhängigkeiten liegen.
`KJFP_TEST_PORT` wählt den lokalen Port (Standard 5177). Der gemeinsame Runner
startet Vite, führt die vier Browserprüfungen sequenziell aus und beendet den
Server. Jede Testmaske wird vorab als Einstieg optimiert; der temporäre Cache
ist nach Projektpfad und Port getrennt.

## Neue Absicherung

| Testklasse / Browserprüfung | Fachliche Aussagen |
|---|---|
| FoerderregelnUseCaseTest | Mindest-/Höchstalter einschließlich Geburtstagen am Beginn/Ende, Leitung/Mitarbeiter ausgeschlossen, 20/21/22 Fördertage, KiK-Stichtage, 15-Euro-Deckel, offizieller Satz gegenüber Planungsfallback |
| SimulationBerechnungUseCaseTest | Echte Kosten-/Förder-/Beitragsberechnung mit unabhängig vorgegebenen Sollbeträgen, Fünf-Euro-Beitragsvorschlag, keine Personen, Tagesveranstaltung, 21-Tage-Begrenzung |
| SimulationSpeicherungUseCaseTest | Entwurf speichert nicht von selbst, Werte nach Flush/Clear wieder lesen, eingereichte Planung gesperrt, nach Wiederöffnung speicherbar, fehlende Voraussetzungen |
| ZahlungenUseCaseTest | Sammelzahlung mit Cent-Rest, Änderungen ohne Doppelzählung, Teilnehmerwechsel, Löschen, Überzahlung, verknüpfte Teilrückzahlungen und Rückzahlungsgrenze, fremde Teilnehmer |
| RueckzahlungParallelUseCaseTest | Zwei echte Transaktionen können dieselbe Überzahlung nicht doppelt erstatten; die zweite sieht nach dem ersten Commit den verbleibenden Betrag |
| KjfpHttpSecurityTest | Produktive Filterkette und Methodensicherheit für ADMIN/KJFP gegenüber USER/VERMIETUNG/FAHRTEN/technischer Rolle sowie unangemeldet; Lesen, Schreiben und PDF-Download einschließlich Header/Inhalt |
| MandantenGrenzenUseCaseTest | Echte Repositorys in zwei Schemas; fremde Zahlungen und Dokumente über HTTP weder lesen noch löschen bzw. drehen; Originalzustand bleibt erhalten |
| FinanzenGesamtUseCaseTest | Zwei Teilnehmerkonten und VK, Quittung/Überweisung, Überzahlung/Rückzahlung, Kultur-/Unterkunftskosten/Pfand/Fahrkosten, Plan-/Ist-Dashboard, wiederholte Synchronisation, gelöschte Zahlungen, alle vier Prüfstatus |
| FahrkostenUseCaseTest | Mehrere Abschnitte, eigene Mitfahrer je Abschnitt, Anhänger, gespeicherte Kilometer und Beträge, Änderungen/Löschen, minderjähriger Fahrer, doppelte Fahrzeugzuordnung, fremder Mitfahrer, fehlende Konfiguration |
| BelegeSchutzUseCaseTest | Sämtliche manuellen Änderungs-/Löschwege nach Abschluss gesperrt, leere Systembelege geschützt, KJFP-Anteil außerhalb Gesamtbetrag abgelehnt, fremder Veranstaltungskontext |
| DokumentHttpUseCaseTest | Echter Bild-Upload, Speicherung, JPA-Versionsfortschritt, Wiederlesen, gedrehter PNG-Download und Cacheheader, veraltete Version, falscher Beleg, Löschen, Upload über 10 MB |
| WeiterePdfUseCaseTest | Teilnehmerliste mit 1/15/16/31 Namen und exakter Seitenzahl, Umlaute und alle Formularwerte, Datenkontrolle trotz fehlender Pflichtdaten, gesperrte reguläre Teilnehmerliste, Erhebungsbogen mit echten Dauer-/Alterswerten |
| VeranstaltungKopieSpeicherungUseCaseTest | Neue Veranstaltung und Planung mit eigenen Datenbankidentitäten, unabhängige Änderung, Status und Positionen nach Wiederlesen; realer später Datenbankfehler rollt Kopie und Aktivwechsel zurück |
| BeitragsregelValidierungUseCaseTest | Rollenabhängige Altersstaffeln; leere, negative, doppelte, absteigende oder falsch offene Staffeln und fehlende Sortierung ergeben Fachfehler |
| kjfp-simulation | Echter React-Ablauf: Beitragsvorschlag nach TN-/MA-Änderung erneut aktiv, Übernahme, Fehler erhält Entwurf, Speichern/Wiederöffnen, Einreichungssperre und fehlende Voraussetzungen |

Die bestehenden Browserprüfungen für Programmentwurf, Veranstaltungskopie und
Belegdrehung sind in den gemeinsamen Testbefehl aufgenommen.

## Durch die Tests gefundene und korrigierte Fehler

1. Wiederholte Synchronisation konnte automatische Buchungen im selben
   Persistenzkontext ohne Belegreferenz zurücklassen. Vorhandene automatische
   Positionen werden jetzt aktualisiert, statt fortlaufend ersetzt zu werden.
   Beim Wegfall einer Zahlung wird die zugehörige Position weiterhin entfernt.
2. Leere Systembelege waren manuell bearbeitbar, weil der Schutz ausschließlich
   die vorhandenen Positionen betrachtete. Zusätzlich werden jetzt die
   reservierten Systembelegnummern berücksichtigt.
3. Zwei parallele Rückzahlungen konnten dieselbe offene Überzahlung doppelt
   verwenden. Der ursprüngliche Zahlungsnachweis wird jetzt vor Prüfung und
   Rückzahlung über die vorhandene pessimistische Repository-Sperre geladen.
4. Eine Beitragsregel ohne Sortierung löste vor der vorgesehenen Validierung
   einen NullPointerException aus. Die Pflichtangabe wird vor dem Sortieren
   geprüft und als HTTP-400-Fachfehler behandelt.

Die neuen Tests sind mit den jeweiligen vorherigen Implementierungen an diesen
Fällen gescheitert. Sie wurden nicht durch Abschwächen der fachlichen Erwartungen
an das fehlerhafte Verhalten angepasst.

## Grenzen

Dies ist keine Zusage einer vollständigen Abdeckung sämtlicher denkbarer
Kombinationen. Die Browserprüfungen verwenden kontrollierte API-Antworten;
Server-/Datenbankpfade werden separat geprüft. Es gibt keinen automatisierten
Login gegen einen echten Keycloak-Server und keinen produktiven Gesamtdurchlauf.
Die Rollenprüfung verwendet die produktive Sicherheitskonfiguration mit
Test-JWTs, der Mandantentest prüft separat die tatsächlichen Datenbankgrenzen.
Eine externe CI-Konfiguration oder ein Deployment wurde nicht eingerichtet.

## Prüfergebnis vom 16.11.2026

- Vollständiger Serverlauf: **912 Tests, 0 Fehler, 10 übersprungen**.
- **77 neue Tests**; alle ausgeführt und erfolgreich.
- Die fünf optionalen KJFP-Belegnummern-PostgreSQL-Tests waren aktiviert und
  erfolgreich. Übersprungen wurden nur die übrigen optionalen Tests für
  Vermietungsvorlagen, Buchungsdokumentmigration und Zählerimport.
- Alle vier KJFP-Browserprüfungen über `npm run test:kjfp` erfolgreich.
- Client-Typprüfung und Produktionsbuild erfolgreich. ESLint der neuen
  ausführbaren Browserprüfungen und der Testmaske ohne Fehler; die neue
  Vite-Konfiguration liegt außerhalb der bestehenden ESLint-Dateiauswahl
  und wurde durch den erfolgreichen Browserlauf geprüft.
- Die neuen Testdateien und vier eng begrenzte Fehlerkorrekturen wurden
  übernommen. Kein Deployment, Commit oder Push; keine Produktionsdaten geändert.
