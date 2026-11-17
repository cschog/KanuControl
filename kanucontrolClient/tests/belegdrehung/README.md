# Browserprüfung Belegdrehung

Der Test rendert die echte Belegkomponente mit kontrollierten API-Antworten.
Er benötigt Playwright und einen verfügbaren Chromium-Browser. Keine echten
Dokumente oder Benutzerkonten werden verwendet.

Im Client-Verzeichnis starten:

```sh
npx vite --config tests/programmentwurf/vite.config.mts
node tests/belegdrehung/check.mjs
```

Optional: `PLAYWRIGHT_MODULE` als Pfad zum Playwright-Modul, `CHROME_PATH` zum
Chrome-Browser und `TEST_URL` für eine abweichende lokale Adresse setzen.
Screenshots werden im temporären Verzeichnis `/tmp` erzeugt.
