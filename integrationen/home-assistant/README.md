# Home Assistant → KanuControl: Stromzählerimport

Stand: 12.11.2026. Lokal implementiert; kein Deployment, kein Schlüssel provisioniert,
keine HA-Installation und kein Versand aktiviert.

## Geprüfter Bestand und Zählerzuordnung

Die vorhandene Benutzer-API `POST /api/vermietung/zaehler/{zaehlerId}/staende`
benötigt ADMIN/VERMIETUNG und bietet keine Idempotenz. Sie bleibt unverändert.
Für HA gibt es deshalb einen getrennten, ausschließlich schreibenden Importzugang.
Der Client benötigt keine Anpassung: importierte Werte stehen in `zaehlerstand`.

Per rein lesender SQL-Abfrage am 12.11.2026 bestätigt, sowohl lokal (`kanu`)
als auch auf `kc-test` (`kanucontrol`, Ziel test.kanucontrol.de):

| Feld | Wert |
|---|---|
| Mandant/Schema | `ekc_eschweilerkanuclub` |
| **Interne Zähler-ID** | **`1`** |
| Bezeichnung | Strom-Zähler Bootshaus |
| Physische Nummer | `9728276` |
| Verbrauchsart / Einheit / aktiv | STROM / kWh / ja |

Für `kc.kanucontrol.de` wurde keine Produktionsdatenbank abgefragt.
Die ID dort vor Umstellung mit `resolve-meter.sql` prüfen. Nicht `9728276` als ID einsetzen.
Bei fehlender oder mehrdeutiger Zuordnung nicht aktivieren.

## Importvertrag

`POST /api/integrationen/zaehler-import`, HTTPS, JSON,
Header `X-KC-Import-Key: <separater zufälliger Schlüssel>`.

```json
{
  "tenant": "ekc_eschweilerkanuclub",
  "meterId": 1,
  "measuredAt": "2026-11-12T12:00:00Z",
  "value": 1234.567,
  "unit": "kWh",
  "remark": "Automatischer Import von Home Assistant"
}
```

- `measuredAt`: echter Zeitpunkt mit UTC/Offset, ganze Sekunden, maximal fünf Minuten in der Zukunft.
- `value`: kumulierter Bezug, nicht Stundenverbrauch; 0 bis 999999999999.999, höchstens drei Nachkommastellen.
- `unit`: exakt `kWh`; `remark`: Pflichtfeld, maximal 1000 Zeichen.
- HTTP 201: angelegt; 200: identische Wiederholung, gleiche Datensatz-ID; 409: anderer Wert/andere Bemerkung zum bestehenden Zeitpunkt oder gebundener Zähler nicht mehr passend.
- HTTP 400: ungültiger Messpunkt; 401: fehlender/falscher Schlüssel oder Import deaktiviert/unvollständig konfiguriert; 403: abweichender Mandant/Zähler bzw. nicht erlaubte Methode.
- Antwort: `{"id":123,"created":true}` bzw. `false` bei Wiederholung.

Der Schlüssel ist serverseitig an genau ein Schema, eine interne Zähler-ID und
zusätzlich die physische Nummer gebunden. Aktiv/STROM/kWh werden bei jedem Import
geprüft. Die mitgesendeten Felder können diese Bindung nicht erweitern. Der Schlüssel
eröffnet weder Lesezugriff noch Zugriff auf andere API-Routen. Der Server hält nur den
SHA-256-Hash; Schlüssel nicht in URLs, Git oder öffentliche Logs schreiben.

Die Tenant-Migration `20261112-04-meter-import.xml` ergänzt `import_messzeit`
(timestamptz) und eine eindeutige Datenbank-Constraint `(zaehler_id, import_messzeit)`.
Sie verhindert Duplikate auch bei parallelen Requests und Serverneustarts.
UTC-gleiche Zeitpunkte mit verschiedenen Offsets sind derselbe Messpunkt.
Die beiden 02:00-Uhr-Zeitpunkte der Herbstumstellung bleiben unterschiedliche Messpunkte.
Für vorhandene Ansichten steht in `ablesedatum` zusätzlich die Zeit in Europe/Berlin;
die bisherigen Ansichten zeigen keinen Offset und können dann zweimal 02:00 anzeigen.
Historische/manuelle Werte erhalten keinen Importzeitpunkt und werden nicht verändert.

## Spätere Einrichtung auf dem Testsystem

1. Backend einschließlich Tenant-Migration regulär deployen. Die Implementierung nutzt
   explizit qualifizierte JDBC-Abfragen und eine eigene atomare Transaktion; kein
   vom Aufrufer gesetzter Tenant-Header und kein allgemeines Benutzer-JWT nötig.
2. Separaten zufälligen Schlüssel mit mindestens 32 Zufallsbytes erzeugen, z. B. lokal:

   ```python
   import secrets, hashlib
   key = secrets.token_urlsafe(32)
   print("HA secret:", key)
   print("Server hash:", hashlib.sha256(key.encode()).hexdigest())
   ```

   Diesen Schritt noch nicht ausgeführt. Für Test und Produktion verschiedene Schlüssel verwenden.
3. `server.env.example` in die vorhandene Server-/Container-Konfiguration übernehmen.
   Hash einsetzen, ID/Nummer kontrollieren und **erst zur bewussten Aktivierung**
   `KC_METER_IMPORT_ENABLED=true` setzen. Server neu starten. Widerruf: deaktivieren
   oder Hash ersetzen und Server neu starten. Nur ein gebundener Importzugang pro Instanz.
4. `kanucontrol_meter.yaml` als `/config/packages/kanucontrol_meter.yaml` ablegen;
   die Einträge aus `secrets.example.yaml` in die vorhandene `/config/secrets.yaml`
   übernehmen und den echten Testschlüssel einsetzen.
5. In `configuration.yaml` in den vorhandenen `homeassistant:`-Block integrieren:

   ```yaml
   homeassistant:
     packages: !include_dir_named packages
   ```

   Bestehende Packages-Konfiguration beibehalten und entsprechend ergänzen;
   keine doppelten YAML-Schlüssel anlegen. Package nicht zusätzlich in automations.yaml einbinden.
6. HA-Konfiguration prüfen (bei HA OS: `ha core check`), danach regulär neu starten.
   In dieser Arbeit erfolgte keine Prüfung in der echten HA-Laufzeit.
7. Die Automation bleibt durch `initial_state: false` nach **jedem** HA-Neustart
   deaktiviert. Erst zur bewussten Testfreigabe in HA einschalten. Für späteren
   dauerhaften Betrieb diese Zeile entfernen, damit HA den letzten Aktivzustand wiederherstellt.

Die Automation liest zur vollen Stunde einen Snapshot des aktuellen Sensors und
kennzeichnet ihn mit dem UTC-Abfragezeitpunkt (Sekundengenauigkeit). Sie liest nicht
nachträglich einen historischen Sensorwert aus dem Recorder. `unknown`, `unavailable`,
fehlende Werte, NaN/Infinity, negative Werte, falsche Einheit und seit über fünf Minuten
nicht gemeldete Werte führen zu **keinem** POST und zu einer HA-Benachrichtigung.
`last_reported` wird für Aktualität verwendet, damit ein unveränderter echter Zählerstand
nicht allein wegen unverändertem `last_changed` ausgelassen wird.
`max_sensor_age_seconds` ist im Package konfigurierbar. Gültige 0 ist erlaubt;
es gibt bewusst keine Umwandlung ungültiger Werte durch `float(0)`.

Die HA-Vorlage rundet gültige Werte auf die drei Nachkommastellen des vorhandenen
Datenmodells. Bei Netzwerk-/Serverfehlern und HTTP 429 maximal drei Versuche mit je einer
Minute Abstand; Payload und Zeitpunkt bleiben unverändert. Andere 4xx werden nicht
wiederholt. Endgültiger Fehler wird als persistente Benachrichtigung angezeigt.
Es gibt keine dauerhafte Warteschlange: HA-Neustarts und längere Ausfälle können
Messpunkte verlieren; die nächste volle Stunde liefert wieder einen aktuellen Stand.

## Übernahme auf Home Assistant / proxmox-ekc

Dasselbe Package verwenden. In `secrets.yaml` Zieladresse auf
`https://kc.kanucontrol.de/api/integrationen/zaehler-import`, Sensor auf den echten
Bootshaus-BitShake und Schlüssel auf den separaten Produktionsschlüssel umstellen.
Mandant bleibt gleich. Interne ID zuvor **in Produktion** ermitteln und sowohl auf
HA- als auch auf Serverseite setzen; physische Nummer ebenfalls prüfen.
Eine bloße Änderung von HA-Secrets gibt keine Berechtigung für einen anderen Zähler.
Die fünfminütige Aktualitätsgrenze an das tatsächliche Meldeintervall anpassen.

## Lokale Verifikation

- Java-21-Kompilierung erfolgreich.
- Vier HTTP-Security-/Validierungstests mit echten Spring-Security-Filterketten erfolgreich.
- Vier PostgreSQL-Tests erfolgreich; Migration in zufälligen temporären Testschemas
  angewendet und Schemas anschließend entfernt. Getestet: Speicherung, äquivalente
  Offsets, Zeitumstellung, 16 parallele Wiederholungen, Konflikt bei geändertem Wert,
  Mandanten-/Zählerbindung und Abgleich der physischen Nummer, 0 und unzulässige Zeiten.
- Bestehende Airbnb-HTTP-Security-Tests als Regression geprüft.
- HA-YAML und Templates lokal geprüft; echte HA-Konfigurationsprüfung und Ende-zu-Ende-
  Übertragung stehen nach Installation/Deployment noch aus.

Tests (aus `kanucontrolServer`; bei Mockito gegebenenfalls den vorhandenen ByteBuddy-Javaagent verwenden):

```sh
mvn -Dtest=MeterImportHttpSecurityTest,AirbnbHttpSecurityTest test
# Nur gegen eine lokale Test-/Entwicklungsdatenbank, deren Zugang Schemas anlegen darf:
KC_IMPORT_TEST_JDBC=jdbc:postgresql://localhost:5432/kanu \
KC_IMPORT_TEST_USER=kcuser KC_IMPORT_TEST_PASSWORD='<lokales Passwort>' \
mvn -Dtest=MeterImportPostgresTest test
```

Quellen für die HA-Konfiguration:
[RESTful Command](https://www.home-assistant.io/integrations/rest_command/),
[Automation YAML / initial_state](https://www.home-assistant.io/docs/automation/yaml/),
[Time pattern](https://www.home-assistant.io/triggers/time_pattern/),
[Templating](https://www.home-assistant.io/docs/templating/).
