# KJFP-Simulation: Browserregression

Die echte SimulationPage einschließlich Hook wird mit kontrollierten API-Daten
geprüft. Abgesichert sind die Wiederaktivierung des Beitragsvorschlags nach
TN-/MA-Beitragsänderung, Übernahme, fehlgeschlagenes und erfolgreiches Speichern,
Wiederöffnen, die Sperre nach Einreichung und fehlende Voraussetzungen.

Alle KJFP-Browserprüfungen sequenziell ausführen:

```sh
npm run test:kjfp
```

Voraussetzung ist ein zugängliches Playwright-Modul und ein Chromium-Browser.
Optional `PLAYWRIGHT_MODULE` (Modulpfad), `CHROME_PATH` (Browserpfad) und
`KJFP_TEST_PORT` (Standard 5177) setzen. Es wird ein eigener lokaler Vite-Server
mit festem Port gestartet und anschließend beendet. Die Tests greifen auf
keinen echten Server und keine Produktionsdaten zu. Für einen bereits laufenden
Testserver kann `check.mjs` mit `TEST_URL` separat aufgerufen werden.
