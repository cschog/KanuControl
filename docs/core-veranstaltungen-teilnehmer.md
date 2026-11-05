# Gemeinsame Veranstaltungen und Teilnehmer

`com.kcserver.core` besitzt die gemeinsamen Entities `Veranstaltung` und
`Teilnehmer`, ihre Repositories, DTOs, Mapper, Suchkriterien sowie die
Verwaltungsservices und Controller. `fahrten` und `kjfp` verwenden dieselben
Identitäten und Repositories. Es gibt keine doppelten Veranstaltungs- oder
Teilnehmertabellen.

## Fachliche Zuordnung

- `core`: Stammdaten, allgemeine Teilnahme, Suche, Stammdatenänderungen und
  koordinierte Löschprüfung einschließlich bestehender Modulreferenzen.
- `kjfp`: Förderberechnung, FM/JEM-Regeln, fachliche Teilnehmeraufnahme,
  KJFP-Zuordnung, Beitragsübersicht, Beitragsstrukturzuweisung, Finanzgruppen,
  Abrechnung, Nachweise und PDF-Ausgaben.
- `fahrten`: Fahrten, Gewässer, Kanus, Lizenzen und Besatzungen.

Die KJFP-Teilnehmeraufnahme und die Zuordnung liegen in
`KjfpTeilnehmerController` / `KjfpTeilnehmerService`. Finanzendpunkte einer
Veranstaltung liegen im `KjfpVeranstaltungController`. FM/JEM-Regeln werden
über `KjfpVeranstaltungRegeln` aufgerufen. Die Zuweisung einer Beitragsstruktur
liegt im `VeranstaltungBeitragsstrukturService`.

## Kompatibilität

HTTP-Pfade, JSON-Felder, Rollenprüfungen, Entity-Namen, Tabellen, Spalten und
Fremdschlüssel bleiben erhalten. Daher ist für diesen Umbau keine
Datenbankmigration erforderlich. Die bisherigen KJFP-Verknüpfungen und
Felder in Entities und Antwort-DTOs bleiben bewusst bestehen; `core` ist
damit noch kein unabhängig von den Fachmodulen startbares Java-Modul.
Die gemeinsamen Services koordinieren weiterhin modulübergreifende
Validierung und Löschbedingungen. Eine spätere Aufteilung der persistenten
Fachdaten in eigene Erweiterungstabellen wäre eine separate Datenmigration.

`VeranstaltungTyp` bleibt in `kjfp`, weil seine Werte FM/JEM und seine
Altersgrenzen Förderregeln beschreiben. `VeranstaltungScope`,
`TeilnehmerRolle`, der Rollenconverter und das allgemeine `CodeEnum`
gehören zu `core`.

## Prüfung

Der JPA-Scan-Test prüft die Erkennung gemeinsamer Entities und der Fahrten-
Entities. Die Veranstaltungs- und Teilnehmer-Integrationstests prüfen die
bestehenden API-Abläufe. Zusätzlich wird allgemeine Teilnahme bei
deaktiviertem KJFP sowie das nachträgliche Zuordnen und Entfernen der
KJFP-Zuordnung über den ausgelagerten Controller geprüft.
