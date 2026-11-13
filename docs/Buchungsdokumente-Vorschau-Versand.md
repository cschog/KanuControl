# Buchungsdokumente: Vorschau, Ablage und manueller Versand

Umsetzung vom 13.11.2026, lokal. Kein Deployment, keine Migration gegen Anwendungsdatenbanken und kein Versand an echte Mieter während der Entwicklung.

## Bedienung

Unter **Vorschau** sind Reservierungsbestätigung, Buchungsbestätigung, Ablehnung, Stornierung und Abrechnung/Rechnung als Simulation unabhängig vom Buchungsstatus auswählbar. Die fachliche Regel „Ohne Abrechnung“ verhindert dort weiterhin die Erstellung einer Rechnung mit verständlichem Hinweis.

Beim Öffnen erscheint die gespeicherte aktuelle Fassung. **Vorschau mit aktuellen Daten** erzeugt eine getrennte, noch ungespeicherte Fassung aus den gespeicherten Buchungsdaten. Beide Fassungen können ausdrücklich ausgewählt werden. Ist das Buchungsformular zur Bearbeitung geöffnet, warnt der Dialog davor, dass ungespeicherte Eingaben nicht übernommen werden.

**PDF speichern** legt die Vorschau als aktuelle Fassung ab. Eine vorhandene Fassung wird nur nach ausdrücklicher Bestätigung ersetzt. Gleichzeitige Ersetzungen führen zu einem Konflikt. **Herunterladen** exportiert exakt die angezeigte Fassung, auch eine noch ungespeicherte Vorschau.

**Bearbeitete PDF hochladen** übernimmt eine normale PDF nach serverseitiger PDFBox-Prüfung. Wiederverwendet wird das bestehende Anhanglimit von **10 MB (10.000.000 Bytes)**. Verschlüsselte, beschädigte oder aktive/eingebettete Inhalte werden abgewiesen; alle Seiten werden geprüft. Es werden keine Buchungsdaten ausgelesen. Ungültige Uploads und fehlgeschlagene Ersetzungen lassen die vorherige Fassung bestehen. Dateiname, Herkunft, Zeitpunkt, Benutzer und SHA-256 werden festgehalten. Für importierte PDFs wird keine inhaltliche Übereinstimmung mit den Buchungsdaten behauptet.

**E-Mail vorbereiten** zeigt den Mieter, tatsächliche Testempfänger und CC sowie den exakten Anhang mit Prüfsumme und Vorschau. Betreff und Mailtext sind editierbar; Vorgaben stammen aus der aktuellen Vorlage der Dokumentart und des Mietobjekts. Das Laden dieser Texte erzeugt keine neue PDF. Ungültige Mieteradressen blockieren den Versand, aber nicht den Export. Erst **Jetzt senden** löst SMTP aus. Globale SMTP-Sperren, Testumleitung und bestehende Unterdrückung von CC im Testmodus gelten weiterhin. Die vorhandene Routingkonfiguration besitzt an dieser Stelle keine zusätzliche BCC-Regel. Ein deaktivierter automatischer Gastversand sperrt den manuellen Versand nicht.

Ändern sich Daten, Vorlage oder Empfänger nach der Vorbereitung, muss erneut geprüft werden. Bei zwischenzeitlicher Dateiersetzung wird die ursprünglich ausgewählte unveränderliche Fassung versandt oder ein Zugriffskonflikt gemeldet. Pro Versandversuch verhindert eine eindeutige Kennung doppelte SMTP-Aufrufe. Bei Fehlern bleiben Texte erhalten; unklare Ergebnisse werden nicht automatisch wiederholt. Vor bewusstem erneutem Senden den Postausgang prüfen.

Im bestehenden **Versandarchiv** bleiben Anhang, finale Texte, Benutzer, ursprünglicher Mieter, tatsächliche Empfänger, Prüfsumme und Simulationseigenschaft nachvollziehbar. Die Anzeige aktualisiert sich nach manuellem Versand und lässt sich auch ausdrücklich neu laden. Spätere PDF-Ersetzungen verändern den archivierten Anhang nicht.

## Frühere Buchungsnummer

Das optionale Feld **Frühere Buchungsnummer** erlaubt maximal 100 Zeichen, ohne Eindeutigkeitsregel und ohne Eingriff in aktuelle Nummernkreise. Es ist auch bei stornierten und abgelehnten Buchungen bearbeitbar.

Im vorhandenen Vorlagenkatalog stehen `{{fruehere_buchungsnummer}}` und die Bedingung `FRUEHERE_BUCHUNGSNUMMER_VORHANDEN` zur Verfügung. Für optionale Absätze diese Bedingung verwenden. Ohne frühere Nummer werden Sätze mit diesem Platzhalter ausgelassen, damit keine leeren Beschriftungen erscheinen.

Der auswählbare Vorschlag **Umstieg auf KanuControl** nennt alte und neue Nummer. Ohne alte Nummer formuliert er entsprechend anders. Konfigurierte Anrede und Grußformel werden übernommen; ungeprüfte Aussagen über unveränderte Konditionen werden vermieden. Textänderungen im Dialog gelten nur für diesen Versand.

## Technik und Migration

Neue API: `/api/vermietung/buchungen/{buchung}/dokumente` mit Übersicht, Vorschau, Fassung lesen, Speichern/Ersetzen, Import, Mailvorbereitung und Versand.

Wiederverwendet werden `BuchungsnachrichtEntwurf`, Vorlagendienst, PDF-Layouts, Finanz-/Verbrauchsberechnung, SMTP-Routing und `VorlagenVersand`. Eine zweite Dateiablage wurde nicht eingeführt. Pro Buchung und Dokumentart gibt es eine aktuelle unabhängige Fassung; ein partieller PostgreSQL-Unique-Index sichert dies ab. Neue Vorschauen können 24 Stunden lang gespeichert/versandt werden, gespeicherte Fassungen laufen nicht ab. Es wurde kein zusätzlicher Aufräum-Scheduler eingeführt.

Der Datenfingerprint umfasst Finanzstand, Verbrauchsvorschau, Anschriften einschließlich Veranstalter, Bankauswahl, frühere Nummer und Vorlagen-/Briefpapierstand. Bei Imports beschreibt er den Datenstand beim Import, keine PDF-Inhaltsprüfung. Jede Fassung erhält eine unveränderliche Kennung und SHA-256. Ersetzungen legen neue Fassungen an. Vor SMTP werden die exakten Bytes und Texte im bisherigen Archiv gespeichert. Ein eindeutiger Versandschlüssel verhindert doppelte Ausführung.

ADMIN/VERMIETUNG werden am Controller und im Dokumentdienst geprüft. Neue Vorschauen sind benutzer- und mandantengebunden. Aktuelle gespeicherte Fassungen sind für andere berechtigte Mitarbeiter desselben Mandanten verfügbar; die bestehende Schema-Mandantentrennung bleibt erhalten. Unabhängige Entwürfe sind von Statusaktions-Endpunkten und deren Listen ausgeschlossen. Automatische Statusmails verwenden diese manuell gespeicherten/importierten Fassungen nicht.

Bestehende endgültige Rechnungen und historische Versandanhänge bleiben in ihren bisherigen Ansichten. Sie werden nicht automatisch in eine unabhängige aktuelle Vorschaufassung umgewandelt.

Die Tenant-Migration `kanucontrolServer/src/main/resources/db/changelog/changes/20261113-03-buchungsdokumente.xml` ergänzt die frühere Nummer, Dokumentkennzeichen, Zeit-/Hinweismetadaten und den eindeutigen Versandschlüssel. Sie ist im Tenant-Changelog eingebunden und verändert keine vorhandenen PDFs oder Buchungsnummern.

## Simulation und Grenzen

Die Simulation nutzt dieselbe Snapshot-/Finanzberechnung, erlaubt aber die reine Berechnung vor Mietende. Die reguläre Abschlussberechnung behält ihre Mietendeprüfung. Es werden weder Verbrauchssnapshots gespeichert noch Zahlungen gebucht oder Rechnungsnummern vergeben. Jede erzeugte Seite trägt **ENTWURF / SIMULATION – keine endgültige Rechnung**. Importierte Inhalte bleiben bytegleich; die Simulationseigenschaft bleibt als Metadatum erhalten und wird angezeigt.

Fehlen erforderliche Verbrauchsablesungen, erfolgt ein konkreter Fehler statt einer unvollständigen Teilrechnung. Unbekannte Verbräuche werden nicht als null angenommen. Verwendbare entfernte Ablesungen erhalten entsprechende Hinweise. Eine importierte Simulation wird nicht zur geprüften oder endgültigen Rechnung.

## Prüfungen

- Frontend: `npm run build`, einschließlich TypeScript und Vite/PWA, erfolgreich.
- Backend: **201 Testfälle bestanden**, vier bestehende optionale PostgreSQL-Vorlagenpaketfälle übersprungen. Enthalten sind Vermietungs-, Statusmail-, Vorlagen-, Abrechnungs-, Bankauswahl-, Buchungs- und Verbrauchsregressionen.
- Neue Dokumenttests prüfen freie Dokumentartwahl, Statusneutralität, bewusste Ersetzung, Datenänderungshinweise, bytegleichen Import/Export/Anhang, exakte Texte, Idempotenz, SMTP-Fehler, geänderte Empfänger, Benutzer-/Tenant-/Rollentrennung, Ablauf, verschlüsselte/ungültige Uploads, frühere Nummern und Simulation.
- Ein MIME-Test mit gemocktem SMTP-Sender prüft Objekt-/Globaltestmodus, CC/BCC, globale Sperre, deaktivierte Automatik und bytegleichen Anhang.
- Zwei Tests in einer eigens gestarteten temporären PostgreSQL-18-Instanz prüfen die echte Liquibase-Migration mit Altbestand, Unique-Index und atomarem Rollback sowie echte JPA-Abfragen zur Tenant-/Benutzer-/Buchungstrennung. Testschemas wurden entfernt, der Server gestoppt.
- Java-21-Tests liefen mit `-Djava.awt.headless=true` und explizitem Byte-Buddy-Agent, da die Sandbox automatische Agentanbindung verhindert.
- Keine interaktive Browserabnahme mit angemeldetem Benutzer durchgeführt. Kein echter Mailserver wurde benutzt.

Regressionen wurden mit `-Dtest=com.kcserver.vermietung.service.*Test,BuchungBankauswahlTest,BuchungStatusBenachrichtigungTest,BuchungVeranstaltungTest,VerbrauchAblesungBestaetigungTest` ausgeführt. Nach den letzten Anpassungen wurden die betroffenen Dokument-, MIME-, Bankauswahl- und Vorlagentests erneut geprüft.

Der PostgreSQL-Test ist nur mit `KC_DOCUMENT_TEST_JDBC=jdbc:postgresql://127.0.0.1:55473/postgres` aktiv und benötigt eine eigens bereitgestellte wegwerfbare Testinstanz. Er liest keine Anwendungskonfiguration.
