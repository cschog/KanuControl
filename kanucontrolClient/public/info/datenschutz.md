# Datenschutz

## Datenschutz in KanuControl

Der Schutz personenbezogener Daten ist uns wichtig. KanuControl unterstützt Vereine bei der Verwaltung von Mitgliedern, Personen, Veranstaltungen, Teilnehmern und Abrechnungen.

## Verarbeitete Daten

Je nach Nutzung können unter anderem folgende Daten gespeichert werden:

- Vor- und Nachname
- Geburtsdatum
- Kontaktdaten
- Vereinszugehörigkeiten
- Funktionen und Rollen im Verein
- Veranstaltungs- und Teilnehmerdaten
- Reisekosten- und Abrechnungsdaten
- Bankverbindung (IBAN, BIC, Bankname)
- Anmelde- und Protokolldaten

## Zweck der Verarbeitung

Die Daten werden ausschließlich zur Durchführung der Vereinsverwaltung und der damit verbundenen organisatorischen Aufgaben verarbeitet.

Hierzu gehören insbesondere:

- Mitgliederverwaltung
- Veranstaltungsverwaltung
- Teilnehmerverwaltung
- Abrechnung von Beiträgen und Reisekosten
- Berechtigungs- und Benutzerverwaltung
- Nachvollziehbarkeit von Änderungen im System

## Hosting und Datensicherheit

KanuControl wird auf einer privaten Linux-Serverinfrastruktur betrieben.

Die Anwendung wird auf mehreren voneinander getrennten virtuellen Systemen (VMs) ausgeführt. Zur Sicherstellung der Verfügbarkeit werden tägliche Datensicherungen auf einem separaten System erstellt.

Jeder Verein wird in einem eigenen Datenbankschema geführt, wodurch eine logische Trennung der Vereinsdaten gewährleistet ist.

Der Zugriff auf personenbezogene Daten ist auf berechtigte Personen beschränkt.

Der Betreiber trifft angemessene technische und organisatorische Maßnahmen, um die Vertraulichkeit, Integrität und Verfügbarkeit der gespeicherten Daten sicherzustellen.


## Weitergabe von Daten

Personenbezogene Daten werden grundsätzlich nicht an Dritte weitergegeben, sofern keine gesetzliche Verpflichtung besteht oder die Weitergabe für die Durchführung einer Vereinsveranstaltung erforderlich ist.

## Auskunft und Berichtigung

Betroffene Personen haben im Rahmen der geltenden Datenschutzgesetze insbesondere das Recht auf:

- Auskunft über gespeicherte Daten
- Berichtigung unrichtiger Daten
- Löschung gesetzlich zulässiger Daten
- Einschränkung der Verarbeitung
- Widerspruch gegen die Verarbeitung

Ansprechpartner hierfür ist der jeweilige Verein bzw. Betreiber der Installation.

## Technische Maßnahmen

KanuControl unterstützt den Schutz personenbezogener Daten durch:

- Zentrale Authentifizierung über Keycloak
- Unterstützung von OTP (One-Time Password)
- Protokollierung sicherheitsrelevanter Vorgänge
- Mandantenfähige Datenhaltung
- Verschlüsselte Kommunikation über HTTPS

## Stand

Version: V0.9