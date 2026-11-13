# Programmentwurf: Browserprüfung

Die Testseite verwendet die echten Formular- und Editor-Komponenten mit synthetischen Daten. Sie benötigt keinen laufenden Server und keine Anmeldung. API-Aufrufe werden im Browsertest abgefangen.

```sh
npx vite --config tests/programmentwurf/vite.config.mts
```

In einem zweiten Terminal mit installiertem Playwright:

```sh
node tests/programmentwurf/check.mjs
```

Optional: `PLAYWRIGHT_MODULE` (Modulpfad), `CHROME_PATH` (Browserprogramm), `TEST_URL` (Testseite). Der Browser läuft mit einem eigenen temporären Profil. Screenshots werden im temporären Verzeichnis abgelegt.

Geprüft werden Titelvorschlag, Fett/Kursiv kombiniert, beide Listenarten, Formatierung entfernen einschließlich Listen, bereinigtes HTML-Einfügen, Eingabeerhalt beim Zuklappen, Längengrenzen ohne Abschneiden und Layout bei 375/1280 Pixeln.
