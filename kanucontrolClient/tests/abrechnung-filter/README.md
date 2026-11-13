# Abrechnungsfilter

Lokaler Browser-Test mit API-Beispieldaten; keine produktiven Daten werden verändert.

1. Im Client `node tests/abrechnung-filter/server.cjs` starten (Port 18771).
2. `KC_PLAYWRIGHT_MODULE=/pfad/zu/playwright node tests/abrechnung-filter/check.cjs` ausführen.

Prüft Vorauswahl des aktuellen Berliner Kalenderjahrs, Zuordnung nach Mietbeginn, alle Jahre, offene positive und negative Salden, Status ABRECHENBAR, kombinierte Filter, leere Ergebnisse, URL-Erhalt, Direktlinks auf ausgeblendete Vorjahresbuchungen und Airbnb-Hinterlegung wie in der Buchungsliste mit echtem App-Theme (geschlossen, Hover und geöffnet). Zusätzlich mobile Breite 390 px und aufgeklappte Airbnb-Buchung. Screenshot: `/private/tmp/kc-abrechnung-filter-mobile.png`.
