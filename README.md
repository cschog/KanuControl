# KanuControl

> ⚠️ Hinweis:
> KanuControl befindet sich weiterhin in aktiver Entwicklung.
>
> Die Anwendung wird bereits produktiv zur Verwaltung von Vereinsdaten eingesetzt und deckt die wesentlichen Anforderungen moderner Kanuvereine ab.
>
> Der Schwerpunkt der Entwicklung liegt inzwischen auf Stabilisierung, Benutzerfreundlichkeit, Datenschutz, Sicherheit und der Vorbereitung einer breiteren Nutzung.

---

# 🚀 Release-Status

## Aktueller Stand: v0.9.x

**KanuControl ist eine mandantenfähige Vereinsverwaltungsplattform für Kanuvereine und Verbände.**

Bereits verfügbar:

- Vereinsverwaltung
- Mitgliederverwaltung
- Benutzer- und Rollenverwaltung
- Veranstaltungsverwaltung
- Teilnehmerverwaltung
- Reisekostenabrechnung
- Beitragsverwaltung
- Finanzverwaltung
- PDF-Generierung
- Dashboard & Kennzahlen
- Multi-Tenant-Betrieb
- Keycloak-Authentifizierung
- Datenschutz- und Sicherheitskonzept

Der Schwerpunkt der weiteren Entwicklung liegt auf:

- Produktionsreife
- Benutzerfreundlichkeit
- Performance
- Erweiterte Rollenmodelle
- Qualitätssicherung
- Dokumentation

---

# 🎯 Ziel von KanuControl

KanuControl ist eine moderne webbasierte Vereinsverwaltungssoftware für Kanuvereine und Verbände.

Die Anwendung unterstützt die digitale Verwaltung von:

- Mitgliedern
- Vereinen
- Veranstaltungen
- Finanzen
- Beiträgen
- Reisekosten
- Zuschüssen und Förderungen

Ziel ist die Ablösung papierbasierter Prozesse durch eine sichere, transparente und nachvollziehbare digitale Lösung.

---

# 🧭 Fachbereiche

## Stammdaten

- Vereine
- Personen
- Mitgliedschaften
- Hauptvereinslogik
- Kontoinhaber
- Qualifikationen
- Vereinsfunktionen

---

## Benutzerverwaltung

- Benutzerkonten
- Rollenverwaltung
- Vereinsbezogene Berechtigungen
- Keycloak-Integration
- OTP-Unterstützung

---

## Veranstaltungen

- Veranstaltungsplanung
- Teilnehmerverwaltung
- Veranstaltungsleitung
- Unterkunft und Verpflegung
- Gebührenverwaltung
- Länder und Orte

---

## Fördermanagement (KJFP)

- Förderfähigkeitsprüfung
- Altersgrenzen
- Zuschlagsregelungen
- Historisierte Fördersätze
- Förderberechnung
- Förderauswertungen

---

## Finanzverwaltung

- Kostenplanung
- Einnahmenplanung
- Finanzgruppen
- Beitragsverwaltung
- Reisekostenabrechnung
- Abrechnungen
- Dashboard-Auswertungen

---

## Dokumentenerstellung

- Teilnehmerlisten
- Anmeldungen
- Förderabrechnungen
- PDF-Formulare
- Automatische Berechnungen

---

# 🌐 Mandantenfähigkeit

KanuControl ist vollständig mandantenfähig aufgebaut.

## Eigenschaften

- Schema-per-Tenant Architektur
- Eigene Datenhaltung je Verein
- Logische Trennung aller Vereinsdaten
- Keycloak-basierte Mandantenerkennung
- JWT-basierte Authentifizierung
- Liquibase-basierte Schemaverwaltung

Jeder Verein arbeitet ausschließlich mit seinen eigenen Daten.

Eine Vermischung von Vereinsdaten unterschiedlicher Mandanten ist technisch ausgeschlossen.

---

# 🔐 Sicherheit & Datenschutz

KanuControl wurde unter Berücksichtigung der Anforderungen der DSGVO entwickelt.

## Sicherheitsmaßnahmen

- HTTPS-Verschlüsselung
- Keycloak-Authentifizierung
- OTP-Unterstützung
- Rollenbasierte Berechtigungen
- Firewall-geschützte Infrastruktur
- Getrennte Serverdienste
- Monitoring der Infrastruktur
- Regelmäßige Sicherheitsupdates

## Datenschutzmaßnahmen

- Mandantentrennung auf Datenbankebene
- Dokumentierte technische und organisatorische Maßnahmen (TOM)
- Auftragsverarbeitungsverträge (AVV)
- Dokumentierte Verarbeitungsbeschreibung
- Lösch- und Backupkonzept

---

# 🏗️ Infrastruktur

KanuControl wird auf einer Linux-basierten Serverinfrastruktur betrieben.

Die Plattform nutzt mehrere getrennte virtuelle Maschinen für:

- Anwendung
- Datenbank
- Authentifizierung
- Reverse Proxy
- Monitoring

Zur Sicherstellung der Verfügbarkeit werden tägliche Datensicherungen auf einem separaten System erstellt.

---

# 🧪 Qualität

## Testabdeckung

- Controller-Tests
- Service-Tests
- Repository-Tests
- Multi-Tenant-Tests
- PDF-Tests
- Förderlogik-Tests

Der Umfang der automatisierten Tests wird kontinuierlich erweitert.

---

# 🎯 Roadmap

## v0.9.x

Fokus:

- UX-Optimierung
- Plausibilitätsprüfungen
- Erweiterte Rollenmodelle
- Fehlerbehandlung
- Dokumentation

---

## v1.0

Ziel:

- Vollständige Produktionsreife
- Erweiterte Audit-Funktionen
- Backup- und Restore-Werkzeuge
- Erweiterte Administrationsfunktionen
- Performance-Optimierungen
- Security Hardening
- Langfristige API-Stabilität

---

# 🌍 Open Source

KanuControl wird als Open-Source-Projekt entwickelt.

Ziele:

- Transparenz
- Nachvollziehbarkeit
- Nachhaltigkeit
- Vereinsfreundlichkeit
- Einfache Erweiterbarkeit

Beiträge in Form von Bugfixes, Tests, Dokumentation und Verbesserungsvorschlägen sind willkommen.

---

# ❤️ Motivation

KanuControl entsteht aus der Praxis für die Praxis.

Ziel ist es, Vereine bei der täglichen Verwaltungsarbeit zu entlasten und gleichzeitig Datenschutz, Transparenz und Nachvollziehbarkeit zu verbessern.