# Über KanuControl

KanuControl unterstützt Vereine bei der Planung und Abrechnung von Veranstaltungen sowie bei der Vermietung. Diese Hilfe beschreibt die Arbeitsbereiche **KJFP**, **Vermietung** und **Fahrten**. Sie nutzen gemeinsame Vereins- und Personendaten im jeweiligen Vereinsmandanten.

## KJFP

Das KJFP-Modul begleitet Jugendmaßnahmen und Vereinsveranstaltungen von den Stammdaten über Teilnehmer, Simulation und Planung bis zu Beiträgen, Abrechnung, Fahrkosten, Finanzausgleich und Dokumenten. Ein Programmentwurf kann mit der Veranstaltung gespeichert und an die Anmeldung angefügt werden. Beim Kopieren einer KJFP-Veranstaltung entsteht eine eigene bearbeitbare Planung.

[Bedienungsanleitung KJFP](/info/bedienungsanleitung-kjfp.md)

## Vermietung

Das Vermietungsmodul verwaltet Mieter, Mietobjekte, Mietbereiche, Bestände, Preise und Buchungen. Hinzu kommen Anzahlung, Kaution, Verbrauchskosten, Sperrzeiten, die Homepage-Anbindung mit WordPress sowie der einseitige Kalenderexport von KC zu Airbnb.

[Bedienungsanleitung Vermietung](/info/bedienungsanleitung-vermietung.md)

## Fahrten

Das Fahrtenmodul verwaltet Fahrten, Flüsse, Kanus, Lizenzen und Besatzungen. Aktionen und Personen werden gemeinsam mit KJFP verwaltet; die Kilometer-Auswertung berücksichtigt die erfassten Fahrten und Besatzungen.

[Bedienungsanleitung Fahrten](/info/bedienungsanleitung-fahrten.md)

## Gemeinsame Bedienung

Welche Module sichtbar sind, hängt von der Freischaltung und den Benutzerrollen ab. Der Kopfbereich zeigt den Verein sowie je nach Modul die aktive Veranstaltung oder das aktive Mietobjekt. Die Anleitungen sind über das Fragezeichen erreichbar.

## Technologie und Betrieb

Die Anwendung verwendet React und TypeScript im Frontend sowie Spring Boot, PostgreSQL, Keycloak und Liquibase im Backend. Vereinsdaten werden mandantenbezogen verwaltet; Benutzer melden sich über den eingerichteten Authentifizierungsdienst an.

Die tatsächlich laufenden Frontend- und Backend-Versionen stehen im Kopfbereich der Anwendung. Die Dokumentation ist auf den Funktionsstand **2.2.2 vom 13. November 2026** abgestimmt. Angaben zu Datenschutz und Betreiber stehen in den jeweils eigenen Informationsseiten.

## Entwicklung und Kontakt

KanuControl wird kontinuierlich weiterentwickelt. Rückmeldungen sollten den verwendeten Bereich, den Arbeitsschritt und gegebenenfalls die angezeigte Fehlermeldung nennen.

Chris Schog · Deutschland

[Hilfeübersicht öffnen](/info/bedienungsanleitung.md)

© KanuControl
