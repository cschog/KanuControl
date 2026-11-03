# Homepage-Anbindung: Speichern

Regressionstests der echten React-Komponente mit simulierten Authentifizierungs- und API-Modulen. Keine Produktionsaufrufe.

Aus dem Clientverzeichnis zunächst `node tests/homepage-anbindung/server.cjs` starten. In einer zweiten Konsole `node tests/homepage-anbindung/check.cjs` ausführen. Voraussetzung: Playwright und Google Chrome; bei extern installiertem Playwright kann KC_PLAYWRIGHT_MODULE auf den Modulpfad gesetzt werden.

Die acht Fälle prüfen sichtbare URL-/Pflichtfeld-/Mailfehler, Speichern, Wiederladen und den Erhalt ungespeicherter Angaben bei einem Serverfehler. Die API im Test verwendet ausschließlich Browser-localStorage. Das ist keine Prüfung der produktiven Datenbankpersistenz.
