# KJFP-Veranstaltung mit Planung kopieren

## Verhalten

„Kopieren“ öffnet die neue Veranstaltung mit den vorbelegten Veranstaltungsdaten. Beim Anlegen werden Veranstaltung und Planung gemeinsam in einer Transaktion gespeichert. Die Quellveranstaltung wird im aktuellen Mandanten über den bestehenden berechtigten Veranstaltungszugriff geprüft. Es wird keine neue API-Route benötigt; die Create-Anfrage enthält optional `kopieVonVeranstaltungId`.

Die neue Planung ist immer **IN_BEARBEITUNG** und initialisiert. Auch eine bereits eingereichte Vorlage kann so als Grundlage für eine neue, bearbeitbare Planung dienen. Die Vorlage bleibt unverändert.

Übernommen werden sämtliche gespeicherten Planungsparameter:

- Teilnehmer- und Mitarbeiterzahlen, einschließlich der sechs Aufteilungen für die Anmeldung.
- KiK-Simulationsparameter, Teilnehmer-/Mitarbeiterbeiträge.
- Unterkunfts- und Verpflegungspreise.
- Honorare, Fahrkosten, Verbrauchsmaterial, Kultur, Miete und sonstige Kostenansätze.
- Planungspositionen mit Menge, Einheit, Einzelpreis, Kommentar und Metadaten.

Planung und Positionen bekommen neue IDs und Auditdaten. Automatische Beträge werden anhand der neuen Veranstaltung neu berechnet. Dabei bleiben zusätzliche/ältere Planungspositionen und deren Angaben erhalten. Fehlt der Vorlage eine gespeicherte Planung, wird für die Kopie eine initialisierte Planung mit den vorhandenen Standardwerten erzeugt.

Teilnehmerlisten, Abrechnung und Belege werden nicht kopiert. Die Veranstaltungsleitung wird wie beim regulären Anlegen neu zugeordnet. Die neue Veranstaltung wird nach der bestehenden Konvention aktiv.

## Geschlossene Lücken

Der Client überträgt nun auch Beschreibung, KJFP-/Fahrten-Aktivierung, individuelle Gebühren, Standardgebühr und Scope. Unterkunft, Verpflegungsmodell, Land, PLZ, Ort und Beitragsstruktur werden beim Anlegen serverseitig gespeichert. Diese Korrektur gilt ebenfalls für reguläre Neuanlagen.

Der Kopier-/Anlagedialog bleibt bei einem Speicherfehler geöffnet. Während der Anfrage sind erneutes Anlegen und Abbrechen gesperrt. Vorlagen ohne KJFP-Aktivierung verwenden beim Kopieren weiterhin die reguläre Neuanlage ohne Planungskopie.

## Prüfungen

- Produktionsbuild und TypeScript-Prüfung; ESLint für die geänderten Formular-/Hook-/Screen-Dateien.
- `PlanungKopierServiceTest`: alle skalaren Planungsfelder, tiefe Positionskopie ohne gemeinsame Objekte/alte IDs, bearbeitbarer Status, Original unverändert, automatische Neuberechnung, Erhalt zusätzlicher Positionen, Standardplanung bei fehlender Quellplanung.
- `VeranstaltungKopieTest`: vollständige Veranstaltungsdetails, Zuordnung der neuen Planung, fehlende Quelle ohne Anlegen/Deaktivieren, weiterhin funktionierende reguläre Neuanlage.
- Bestehende Tests `ProgrammentwurfSpeicherungTest` und `KjfpVeranstaltungRegelnTest`.
- Browserprüfung der echten Veranstaltungsansicht: Auswahl einer Vorlage, Kopierdialog, vollständige Create-Payload, absichtlich fehlgeschlagene Anfrage mit geöffnetem Dialog und erfolgreicher Wiederholversuch.

Die Serverprüfungen verwenden Repository-Mocks. Ein vollständiger Durchlauf gegen PostgreSQL/Keycloak bzw. produktive Mandanten wurde nicht durchgeführt. Keine Datenbankmigration erforderlich; kein Deployment und keine Änderungen an Produktionsdaten.
