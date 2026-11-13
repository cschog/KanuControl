# KanuControl

Webbasierte Vereins-, Veranstaltungs- und Vermietungsverwaltung für Kanuvereine und Kanuverbände.

**Client und Server:** 2.2.2 · **Dokumentationsstand:** 13. November 2026

KanuControl unterstützt KJFP-Maßnahmen von der Simulation bis zur Abrechnung, gemeinsame Vereinsaktionen und Fahrten sowie die Vermietung von Objekten wie einem Bootshaus. Welche Module und Funktionen verfügbar sind, hängt von Freischaltung, Benutzerrollen und Konfiguration ab. Die tatsächlich laufenden Client- und Serverversionen stehen im Kopfbereich der Anwendung.

## Inhalt

- [Module und Funktionen](#module-und-funktionen)
- [Architektur und Technologie](#architektur-und-technologie)
- [Projektstruktur](#projektstruktur)
- [Lokale Entwicklung](#lokale-entwicklung)
- [Build und Tests](#build-und-tests)
- [Betrieb und Dokumentation](#betrieb-und-dokumentation)
- [Lizenz und Marken](#lizenz-und-marken)

## Module und Funktionen

### KJFP

- Vereine, Personen, Mitglieder und Teilnehmer mit kontextabhängiger Datenprüfung.
- Veranstaltungen mit Unterkunft, Verpflegung, Beitragsstruktur und gemeinsam nutzbaren Stammdaten für KJFP und Fahrten.
- Simulation und gespeicherte Planung mit Beiträgen, Kostenansätzen, Förderung und KiK-Berücksichtigung.
- Kopieren einer KJFP-Veranstaltung einschließlich einer unabhängigen Planung im Status „In Bearbeitung“.
- Optionaler formatierter Programmentwurf; Ausgabe nach den beiden bestehenden Seiten der PDF-Anmeldung, bei Bedarf über mehrere Seiten.
- Teilnehmerbeiträge, Zahlungsnachweise, Rückzahlungen, Belege, Konten/Finanzgruppen, Reisekosten, Finanz-Dashboard und Finanzausgleich.
- Dokumentprüfung, PDF-Ausgaben sowie Verwaltung von Beleg- und Zahlungsnachweisdokumenten.

Die Planung gehört jeweils zu einer Veranstaltung. Sie ist keine eigenständige Jahresplanungsverwaltung.

[Bedienungsanleitung KJFP](kanucontrolClient/public/info/bedienungsanleitung-kjfp.md)

### Vermietung

- Getrennte Mieter- und Veranstalterverwaltung, Mietobjekte, Mietbereiche, Bestände, Preise und Rabatte.
- Direkt- und Airbnb-Buchungen, Statusabläufe, Sperrzeiten und Kalenderansichten.
- Anzahlung, Zahlungsvarianten, Kaution, Verbrauchskosten, Zählerstände und Abrechnung.
- Finanz-Dashboard sowie Verbrauchs- und Belegungsauswertung.
- Vorschau, Speichern/Ersetzen, PDF-Import, Simulationsabrechnung und manueller Dokumentversand mit Versandarchiv.
- Homepage-Anfrageformular und Bootshauskalender über die WordPress-Anbindung.
- Direkter KC-SMTP-Versand für Statusnachrichten und Dokumente; WordPress übernimmt weiterhin die erste Eingangsbestätigung.
- Geschützter einseitiger iCal-Export von KC zu Airbnb; kein Airbnb-Import in dieser Ausbaustufe.

[Bedienungsanleitung Vermietung](kanucontrolClient/public/info/bedienungsanleitung-vermietung.md)

### Fahrten

- Gemeinsame Personen, Aktionen und Teilnehmerzuordnungen.
- Flüsse/Gewässer und Fahrten mit Datum, Strecke, Uhrzeiten und Kilometern.
- Kanus, private Veranstaltungsboote, Lizenzen und Besatzungen.
- Kilometer-Auswertung nach Teilnehmer und Gewässer.

[Bedienungsanleitung Fahrten](kanucontrolClient/public/info/bedienungsanleitung-fahrten.md)

### Gemeinsame Funktionen

Keycloak-Anmeldung, rollenbasierte Zugriffe, mandantenbezogene Datenhaltung, Administration sowie Audit- und Sitzungsfunktionen. KJFP und Fahrten verwenden dieselben Personen- und Veranstaltungsstammdaten; fachliche Zuordnungen bleiben unterscheidbar.

## Architektur und Technologie

```text
React / TypeScript / Vite
          |
          v
Spring Boot REST API ---- Keycloak / OpenID Connect
          |
          v
PostgreSQL mit getrennten Vereinsschemas
```

Das Frontend verwendet unter anderem React 18, Material UI, TanStack Table, React Router und Axios. Das Backend verwendet Java 21, Spring Boot 3, Spring Security, Hibernate/JPA und Liquibase. PDFs werden mit Apache PDFBox verarbeitet; jsoup bereinigt den Programmentwurf. Die Datenbankstruktur wird über getrennte globale und Tenant-Migrationen gepflegt.

Die konkrete Server-, Reverse-Proxy-, TLS-, Backup- und Monitoring-Konfiguration hängt von der Installation ab. Docker kann dafür genutzt werden, ist aber keine Voraussetzung für jeden lokalen Entwicklungsschritt.

## Projektstruktur

```text
kanucontrolClient/       React-/TypeScript-Frontend und öffentliche Hilfe
kanucontrolServer/       Spring-Boot-Backend und Datenbankmigrationen
integrationen/          Integrationsquellen, unter anderem WordPress
WordPress_Plugin/       Ausgelieferte WordPress-ZIP-Pakete
keycloak/               Projektdateien zur Authentifizierungsanbindung
docs/                   Fachliche und technische Dokumentation
legal/                  Rechtliche Betriebsunterlagen und Drittanbietertexte
```

Das zuletzt im Repository abgelegte WordPress-Paket ist **0.1.14**. Seine Versionsnummer ist unabhängig von der KanuControl-Version.

## Lokale Entwicklung

### Voraussetzungen

- Java 21 für das Backend.
- Node.js 22 oder neuer entsprechend `kanucontrolClient/package.json`; die `.nvmrc` wählt die Hauptversion 22.
- Yarn Classic für das vorhandene `yarn.lock`.
- Maven oder der mitgelieferte Maven Wrapper.
- PostgreSQL, Keycloak und die für die eigene Entwicklungsumgebung eingerichtete Konfiguration.

Die versionierten Konfigurationsdateien sind vor einem Start mit der eigenen Datenbank-, Keycloak- und Mandantenkonfiguration abzugleichen. Zugangsdaten und Secrets gehören in die dafür vorgesehenen lokalen bzw. betrieblichen Einstellungen.

### Backend starten

Aus dem Repository-Hauptverzeichnis:

```sh
cd kanucontrolServer
./mvnw spring-boot:run
```

Alternativ kann eine passend installierte Maven-Version über `mvn` verwendet werden. Die Anwendung verwendet laut Basiskonfiguration Port 8090; abweichende Profile können andere Einstellungen setzen.

### Frontend starten

In einem weiteren Terminal, ausgehend vom Repository-Hauptverzeichnis:

```sh
nvm install
nvm use
cd kanucontrolClient
yarn install --frozen-lockfile
yarn dev
```

Die beiden `nvm`-Befehle sind nur bei Verwendung von nvm nötig. Das Frontend benötigt die Werte `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM` und `VITE_KEYCLOAK_CLIENT_ID` für die eigene Installation. Der Vite-Entwicklungsserver verwendet Port 5173 und leitet `/api` an `http://localhost:8090` weiter.

## Build und Tests

### Frontend

Im Verzeichnis `kanucontrolClient`:

```sh
yarn build
yarn lint
```

Der Build führt TypeScript-Prüfung und Vite/PWA-Build aus; die Ausgabe liegt in `dist/`. Zusätzliche Browserprüfungen liegen unter `kanucontrolClient/tests/` und verwenden je nach Test API-Mocks. Einrichtung und Aufruf stehen in den jeweiligen README-Dateien.

### Backend

Im Verzeichnis `kanucontrolServer`:

```sh
./mvnw clean test
```

Die Spring-Integrationstests verwenden die lokale PostgreSQL-Testkonfiguration aus `src/test/resources/application-junit.properties`. Diese Konfiguration muss auf eine geeignete Testdatenbank zeigen. Der Testlauf legt Testschemas an und bereinigt Testdaten; hierfür keine Produktivdatenbank verwenden.

Ein Teil der PostgreSQL-Integrationstests benötigt ausdrücklich bereitgestellte separate Testinstanzen und die Variablen `KC_BELEG_TEST_URL`, `KC_DOCUMENT_TEST_JDBC`, `KC_PAKET_TEST_PG` bzw. `KC_IMPORT_TEST_JDBC`. Ohne diese Konfiguration werden die betreffenden Tests übersprungen. Einzelne Klassen prüfen zusätzlich feste lokale Ports bzw. Benutzer. Siehe die Testklassen und die [Belegnummern-Dokumentation](docs/BELEGNUMMERN.md), [Buchungsdokumentation](docs/Buchungsdokumente-Vorschau-Versand.md) und [Vorlagenpakete](docs/Vorlagenpakete.md).

**Prüfstand für V2.2.2:** Client-Produktionsbuild erfolgreich; vollständiger Serverlauf mit 829 Testfällen, davon 814 ausgeführt und 15 optionale Integrationstests übersprungen, ohne Fehler oder fehlgeschlagene Prüfungen. Das ist ein dokumentierter Release-Prüfstand und keine Zusage über jede spätere Umgebung oder Änderung.

## Betrieb und Dokumentation

- [Hilfeübersicht](kanucontrolClient/public/info/bedienungsanleitung.md)
- [Aktuelle Release-Hinweise](kanucontrolClient/public/info/releases/releases.md)
- [Homepage-Anbindung](kanucontrolClient/public/info/homepage-anbindung.md)
- [Programmentwurf](docs/KJFP-Programmentwurf.md)
- [Veranstaltung mit Planung kopieren](docs/KJFP-Veranstaltungskopie.md)
- [Infrastruktur](docs/infrastructure.md)
- [Deployment-Unterlagen](docs/deployment.md)

Die technischen Betriebsunterlagen beschreiben die projektspezifische Installation. Vor einer Bereitstellung sind die tatsächliche Konfiguration und die vorgesehenen Liquibase-Migrationen zu prüfen.

## Lizenz und Marken

KanuControl wird unter der [Apache License 2.0](LICENSE) bereitgestellt. Copyright-, Herkunfts- und Drittanbieterhinweise stehen in [NOTICE.md](NOTICE.md).

Die Lizenzversion **2.0** ist unabhängig von der Softwareversion **2.2.2**. Für Produktname, Logo und Icon gelten die [Markenhinweise](TRADEMARK.md), einschließlich der dort erläuterten Herkunftsnennung. Drittanbieterkomponenten behalten ihre jeweiligen Lizenzen.

Copyright © 2026 Chris Schog.
