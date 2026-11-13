# Buchungsmaske: Struktur und Zustand

Isolierter Vite-/Chrome-Test mit Beispieldaten und API-Mocks; keine produktiven Daten werden geschrieben.

1. Im Client `node tests/buchungsmaske-struktur/server.cjs` starten (Port 18769).
2. `KC_PLAYWRIGHT_MODULE=/pfad/zu/playwright node tests/buchungsmaske-struktur/check.cjs` ausführen.

Prüft sieben Bereiche, Startzustände, mehrere offene Bereiche, ungespeicherte Zusammenfassungen, Zustandserhalt, leere Teilnehmer versus 0, Speicherfehler in geschlossenen Bereichen, Tastatur, Abbrechen, unveränderte Preisübernahme und rein lesenden Einstieg in die Abrechnung. Screenshots unter `/private/tmp/kc-structure-*.png` für 375, 390, 768, 1280 und 1920 Pixel sowie eingeschränkte Breite mit Navigation.

Bestehende Prüfungen unter `tests/buchung-abbrechen` und `tests/mietpreis` ergänzen dies. Die Serverregeln für Direkt-/Airbnb-Abschluss, Kautionsverrechnung und Versand werden durch diese UI-Mocks nicht ersetzt; der vorhandene Abschlussdialog bleibt dafür zuständig.
