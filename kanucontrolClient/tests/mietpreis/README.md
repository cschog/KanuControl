# Mietpreis und Rabatt im Browser

Die echte Buchungsmaske wird mit simulierten Stammdaten und Preisen dargestellt. Keine Produktionsaufrufe.

Im Clientverzeichnis `node tests/mietpreis/server.cjs` starten, in einer zweiten Konsole `node tests/mietpreis/check.cjs` ausführen. Benötigt Playwright und Chrome. Optional KC_PLAYWRIGHT_MODULE auf einen externen Playwright-Modulpfad setzen.

Prüft zwei bzw. drei Miettage mit 50 Prozent Rabatt, einen gespeicherten Rabatt unabhängig von späteren Regeln, Tageszählung beim Sommerzeitwechsel und einmalige Pauschalen.
