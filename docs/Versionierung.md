# Versionierung

Client und Server bekommen gemeinsam eine Version nach MAJOR.MINOR.PATCH.

| Änderung | Beispiel | Befehl im Projektverzeichnis |
| --- | --- | --- |
| Fehlerkorrektur oder kleine kompatible Verbesserung | 2.3.0 → 2.3.1 | `python3 scripts/bump-version.py patch` |
| Neue kompatible Funktion | 2.3.1 → 2.4.0 | `python3 scripts/bump-version.py minor` |
| Produktmeilenstein: Fahrtenmodul funktioniert zuverlässig | 2.4.0 → 3.0.0 | `python3 scripts/bump-version.py major` |

Jedes auslieferbare Änderungspaket bekommt eine neue Version, auch kleine Updates. Während der Arbeit an einem Paket bleibt die Version gleich. Vorab lässt sich der Befehl mit `--dry-run` prüfen.

Das Skript aktualisiert `kanucontrolClient/package.json` und die Projektversion in `kanucontrolServer/pom.xml` gemeinsam; unterschiedliche Ausgangsversionen führen zum Abbruch. Vite und die Backend-Konfiguration übernehmen diese Werte bereits für die Versionsanzeige. Die Yarn-Lockdatei enthält keine eigene Projektversion und bleibt unverändert.

Danach Versionshinweise in `kanucontrolClient/public/info/releases/releases.md` ergänzen, einen Abschnitt als `V<Version>.md` archivieren und den Versionsstand im README aktualisieren. Historische Dokumentations- und Prüfstände bleiben erhalten. Tests und Builds prüfen. Bei einem späteren Release kann ein Git-Tag wie `v2.3.1` den genauen Commit markieren.

Das Skript erstellt keine Commits oder Tags und führt kein Deployment durch. Eine bereits laufende Installation zeigt die neue Version erst nach einem gesondert beauftragten Deployment und Neuladen des Clients.

V3.0.0 ist für das zuverlässig funktionierende Fahrtenmodul reserviert. Bis dahin bleiben Erweiterungen und Korrekturen in der Reihe 2.x.
