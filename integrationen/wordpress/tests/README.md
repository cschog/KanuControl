# Lokale WordPress-Integrationstests

Diese Dateien gehören nicht in das installierbare Plugin. Sie verwenden eine Wegwerf-WordPress-/MySQL-Installation unter localhost:18765, simulieren sämtliche KC-Antworten und fangen jede E-Mail ab. Es werden keine Produktionsdaten benötigt.

Voraussetzungen: Docker, Python 3, Node.js, das Node-Modul Playwright und Google Chrome. Bei außerhalb des Projektpfads installiertem Playwright die Variable KC_PLAYWRIGHT_MODULE auf dessen Modulpfad setzen. KC_TEST_OUTPUT bestimmt optional den Ordner für Screenshots.

Start:

```sh
sh integrationen/wordpress/tests/run-local.sh
```

Der Testablauf erzeugt ausschließlich eigene Container mit den Namen kc-hp-test-db und kc-hp-test-wp und ein eigenes Netzwerk. Bereits belegte Namen werden nicht überschrieben. Die Testressourcen werden am Ende entfernt. Die Images mysql:8.0 und wordpress:php8.2-apache werden bei Bedarf von Docker geladen. Die mitgelieferten Zugangsdaten local-test-only gelten ausschließlich für diese lokalen, nicht produktiven Testressourcen.

39 Prüfungen decken Sitzungsschutz, Verfügbarkeit, Übernahme, identische und abweichende Wiederholungen, Netzwerkausfall, getrennte Mailwiederholungen, Zielwechsel, Aufruflimit und schreibfreie Vorschau ab. Der Browsertest prüft Desktop und Mobilbreite einschließlich Absenden und Wiederaufnahme nach Neuladen. Die Fixture blockiert andere WordPress-HTTP-Zugriffe und ersetzt den Mailversand. Sie darf nicht auf einer echten Homepage installiert werden.

Für Twenty Eleven wurde zusätzlich das offizielle Theme 5.2 in derselben lokalen Testumgebung installiert und der Browsertest wiederholt. Standardmäßig testet das Skript das im WordPress-Image enthaltene Theme.

Zusätzlich werden alle Statusnachrichten, Reihenfolge je Buchung, wiederholte Abholung, Mail- und Quittierungsfehler, Zielwechsel, Versandpause und das Upgrade unter Erhalt vorhandener Anfragen geprüft.
