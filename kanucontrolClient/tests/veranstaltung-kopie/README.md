# Browserprüfung der Veranstaltungskopie

Testseite mit der echten Veranstaltungsansicht, synthetischer Vorlage und abgefangenen API-Aufrufen. Keine Verbindung zu Produktionsdaten erforderlich.

```sh
npx vite --config tests/programmentwurf/vite.config.mts
node tests/veranstaltung-kopie/check.mjs
```

Playwright muss verfügbar sein. Optional: `PLAYWRIGHT_MODULE` als Modulpfad, `CHROME_PATH` für ein Browserprogramm, `TEST_URL` für eine abweichende Test-URL.

Geprüft werden der Kopierdialog aus der Veranstaltungsliste, alle kopierten Veranstaltungsfelder einschließlich der serverseitigen Planungsquelle und das Offenhalten des Dialogs bei einem fehlgeschlagenen Speicherversuch.
