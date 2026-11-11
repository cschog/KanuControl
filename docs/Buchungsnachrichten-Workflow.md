# Buchungsnachrichten: Status, Vorschau, PDF-Bearbeitung und Versand

Stand: 11.11.2026. Implementiert im lokalen Arbeitsstand; kein produktiver Datenbankeingriff, kein Deployment und kein realer SMTP-Versand.

## Einrichtung

Die Tenant-Migration `20261111-02-buchungsnachrichten.xml` regulär bereitstellen. Sie ergänzt gespeicherte, benutzergebundene Nachrichtenentwürfe und die eingefrorenen Stornowerte an der Buchung. Vorherige Vorlagen-/Zahlungsvariantenmigrationen bleiben erforderlich. Bestehende Buchungen und ausgestellte PDF-Dokumente werden nicht überschrieben.

Für jedes Mietobjekt die Vorgänge RESERVIERUNG, BESTAETIGUNG, ABLEHNUNG, STORNIERUNG und ABGERECHNET prüfen und konfigurieren. Ein aktualisiertes Beispielpaket für Bootshaus (Ziel-ID 1) liegt als Testressource unter `kanucontrolServer/src/test/resources/vorlagenpakete/bootshaus-status-beispiel.zip`; dieselbe Datei wird separat zum Import geliefert. Das Paket enthält keine Briefpapierdatei: gespeichertes Briefpapier, Grußformel und Unterschrift bleiben erhalten. Die Objektzuordnung muss beim Import ausdrücklich erfolgen.

Die Beispieltexte sind Entwürfe, keine automatische Migration vorhandener individueller Formulierungen. Die Stornotexte setzen die vom Betreiber vorgegebene Grenze um: unter 56 Kalendertagen keine Erstattung; ab 56 Tagen einschließlich alle geleisteten Zahlungen zurück, abzüglich tatsächlicher bereits ausgeführter Rückzahlungen.

## Bedienung

Der eigene Button „Vorschau“ öffnet E-Mail und PDF für den ausgewählten Dokumentvorgang unabhängig vom tatsächlichen Statuswechsel. Eine Mieter-E-Mail-Adresse, Versandkonfiguration und vollständige Kautionsabwicklung sind dafür nicht erforderlich. Die Vorschau liest den gespeicherten Buchungsstand und schreibt weder Nachrichtenentwürfe noch Rechnungs-/Versandarchive, Status- oder Finanzdaten. Entfernte Ablesungen werden nur für diese Ansicht verwendet und als Hinweis markiert; beim wirklichen Abschluss bleibt die ausdrückliche Bestätigung erforderlich. Für eine Rechnungsvorschau müssen die benötigten Verbrauchswerte vorhanden sein.

Im Vorschau-Dialog lassen sich Betreff und E-Mail-Text direkt in den Feldern bearbeiten. Diese Textänderungen werden dort nicht gespeichert; Schließen oder erneutes Laden verwirft sie. Für den Versand die Texte im regulären Statusdialog mit „Individuelle Texte übernehmen und neu prüfen“ speichern. PDF-Dateien können separat exportiert, extern bearbeitet und wieder zur Ansicht importiert werden. Die PDF-Bearbeitung bleibt bis zum Schließen im Dialog; zum Aufbewahren das PDF exportieren. Zum späteren Versand das bearbeitete PDF im regulären Statusdialog importieren und dort die aktuelle Nachricht bestätigen. Bei Statuswechseln ohne Nachricht, insbesondere reserviert → bestätigt, wird keine eigene Mail-/PDF-Vorschau angeboten.

Die Buchungsansicht bietet nun „Reservierung prüfen“, „Bestätigung prüfen“, „Ablehnung prüfen“, „Stornierung prüfen“ beziehungsweise „Abschluss und Nachricht prüfen“ an. Der Dialog erstellt einen gespeicherten Entwurf. Das verändert weder Status noch Finanzbewegungen.

Empfänger und Testumleitung, Betreff, Mailtext und vollständiges PDF prüfen. Individuelle Textänderungen mit „Individuelle Texte übernehmen und neu prüfen“ speichern; sie gelten nur für diesen Entwurf. Nach jeder Änderung oder einem PDF-Upload muss die aktuelle Nachricht erneut bestätigt werden. Erst die abschließende Aktion ändert den Status und versendet gegebenenfalls die Nachricht.

- Anfrage + Anzahlungsplan → reservieren: Anzahlung anfordern, Restmiete/Kaution mit Fristen nennen.
- Anfrage + Gesamtmiete voraus → reservieren: Gesamtmiete und optionale Kaution, keine zusätzliche Anzahlung.
- Anfrage + Zahlung bei Abrechnung → direkt bestätigen: keine Zahlungsaufforderung.
- Reserviert → bestätigt: geforderte Anzahlung genügt; keine Mail. Bei Gesamtvorauszahlung werden Gesamtmiete und geforderte Kaution geprüft.
- Reserviert/bestätigt → storniert: Stornonachricht und konkrete Abwicklung.
- Bestätigt nach Mietende → abrechenbar: automatisch und ohne Nachricht.
- Abrechenbar → abgeschlossen: Unter „Mailversand beim Abschluss“ mit oder ohne Mailversand wählen. Die Rechnung in beiden Fällen prüfen und ausdrücklich abschließen; mit Mail wird sie anschließend versandt, ohne Mail ausschließlich archiviert.

Die bestehende Kautionsabschlussprüfung und die Bestätigung abweichender Zählerablesungen bleiben maßgeblich. Bei einer Rechnungskorrektur die Buchung wieder öffnen, erforderliche Datenänderungen speichern und mit Korrekturgrund erneut abschließen. Eine zusätzliche unkontrollierte PDF-Ausstellung über die alte Oberfläche gibt es nicht mehr. Alte API-Schreibpfade für Statusaktionen und Reservierungsversand verweisen mit Konfliktmeldung auf den neuen Entwurfspfad; Wiederöffnen ohne Versand bleibt möglich.

## Externe Bearbeitung

„PDF zur Bearbeitung herunterladen“, in PDF Expert oder Acrobat ändern, speichern und mit „Bearbeitetes PDF übernehmen“ hochladen. Das ist ein Dateiaustausch mit der externen App; keine eingebettete PDF-Expert-Oberfläche.

Die Vorschau zeigt das hochgeladene Dokument, dessen Dateinamen und die Kennzeichnung „Extern bearbeitet“. Genau diese Bytes werden versandt und archiviert. Kein automatisches Neurendern nach Upload. „Automatisch erzeugtes PDF wiederherstellen“ ist eine ausdrückliche Aktion. Änderungen im PDF ändern keine Finanzdaten.

Anhänge müssen unverschlüsselt, lesbar, 1 bis 100 Seiten und höchstens 10 MB groß sein. Die Schutzprüfung gegen aktive oder eingebettete Inhalte wird wiederverwendet; der Einseitenvertrag für Briefpapier gilt nicht für fertige Anhänge.

## Storno und Finanzen

Das Stornodatum wird im Berliner Kalender bestimmt und bei der verbindlichen Aktion eingefroren. Ein Tageswechsel vor Ausführung erfordert einen neuen geprüften Entwurf. Zahlungseingänge, tatsächliche Rückzahlungen und interne Verrechnungen werden getrennt behandelt; Verrechnungen zählen nicht noch einmal als gezahltes Geld.

Die Stornonachricht kündigt eine Erstattung an und bucht keine Überweisung. Tatsächliche Rückzahlungen anschließend in der Finanzansicht erfassen. Stornierte Buchungen sind dort weiterhin erreichbar. Ein kurzfristiger Einbehalt wird ausdrücklich an der Buchung gespeichert und im Stornosaldo berücksichtigt; es werden keine fiktiven Verbrauchskosten oder Rückzahlungen angelegt. Rückzahlungen gegen einen gespeicherten kurzfristigen Einbehalt sind gesperrt.

Neue Platzhalter: `stornodatum`, `storno_gezahlter_betrag`, `storno_bereits_erstattet`, `storno_erstattung`, `storno_einbehalt`; dazu die bestehenden Mieterbankfelder. Bedingungen: `STORNO_ERSTATTUNG_POSITIV`, `STORNO_EINBEHALT_POSITIV`, `STORNO_OHNE_ZAHLUNG` (kein offener Erstattungs- oder Einbehaltsbetrag). Eine positive Erstattung benötigt die Mieter-IBAN; ein Block mit Bankname/BIC prüft diese verwendeten Werte weiterhin auf Vollständigkeit.

## Entwürfe, Versand und Archiv

Nicht ausgeführte Entwürfe gelten 30 Minuten und sind an Benutzer, Mandant, Buchung und Aktion gebunden. In der Buchungsansicht lassen sie sich erneut öffnen. Ein Abbruch verändert keine Buchungs- oder Finanzdaten. Ausgeführte Entwürfe bleiben für die Prüfung und ausdrückliche Versandwiederholung erreichbar.

Ein Fingerprint bindet Version, relevante Buchungs-/Finanzdaten, Verbrauch und Ablesungsbestätigungen, Adress- und Bankdaten, Vorlage/Briefpapier, Empfänger, individuelle Texte und finale Anhangbytes. Änderungen am Ausgangsstand sperren einen veralteten Entwurf. Betreff und Mailtext sind gegen unzulässige Header-/Steuerzeichen geprüft.

Die fachliche Aktion und der vorbereitete Archivversuch werden vor SMTP in einer Datenbanktransaktion gespeichert. SMTP erfolgt erst nach deren Commit. Bei Fehler oder unklarem Ausgang zeigt die Oberfläche den tatsächlichen Zustand; ein erneuter Versand erfordert das Prüfen des Postausgangs und eine ausdrückliche Bestätigung. Bei einem gerade vorbereiteten, noch unbestätigten Versuch wird eine Wiederholung zunächst fünf Minuten gesperrt. Sie führt keinen zweiten fachlichen Statuswechsel, keine zweite Rechnung und keine Rückzahlungsbuchung aus. Automatische Wiederholungen gibt es nicht.

Das bestehende Versandarchiv enthält die tatsächlichen PDF-Bytes und strukturierte Mailmetadaten. Archivierte Nachricht und PDF lassen sich unabhängig vom aktuellen Entwurf ansehen beziehungsweise herunterladen. Ältere Archivversuche ohne vollständigen Mailtext werden entsprechend gekennzeichnet. Bereits ausgestellte Rechnungsrevisionen bleiben unverändert.

## Vorlagenprüfung

Die Vorlagenverwaltung erlaubt die Auswahl der drei Zahlungsvarianten, der drei Rechnungssalden und der drei Stornofälle. Der ZIP-Import prüft fachliche Fälle sowie nur tatsächlich verwendete OG-/Fristbedingungen; identische sichtbare PDF- und Mailergebnisse je Vorlage werden zusammengefasst. Beispiel-PDFs sind synthetisch; vor echtem Versand ist weiterhin die konkrete Buchungsvorschau maßgeblich.

## Technische Prüfung und Grenzen

Gezielte Tests decken 55/56/57 Tage, Teilrückzahlungen, Verrechnungen, Bestätigung nach Anzahlung, stille Statusaktionen, Stornosaldo, benutzer-/mandantengebundene Entwürfe, veraltete Daten, Tageswechsel, PDF-Upload/Wiederherstellung, exakte MIME-/Archivbytes, Rechnungsausstellung und Versandwiederholung ab. Die Migration wird in isolierten H2-Schemas einschließlich Rollback geprüft. Dies ersetzt keinen vollständigen PostgreSQL-/Keycloak-/SMTP-End-to-End-Test.

Die Bedienoberfläche wird per Produktionsbuild und gezieltem ESLint sowie im Browser bei Desktop- und Mobilgröße mit simulierten APIs geprüft. Erzeugte PDF-Beispiele werden gerendert und visuell kontrolliert. Keine Änderungen an produktiven Daten oder SMTP-Empfängern im Rahmen dieser Entwicklung.

Beim Abschluss ohne Mailversand werden weder Mieter-E-Mail-Adresse noch SMTP-Konfiguration benötigt. Kautionsabwicklung, Verbrauchsfreigabe, Korrekturgrund und Prüfung des aktuellen Datenstands bleiben erforderlich. Die Rechnung kann auch ohne Mail heruntergeladen, extern bearbeitet und wieder importiert werden; beim Abschluss wird die geprüfte PDF-Fassung archiviert. Die Auswahl der Versandart wird im Nachrichtenentwurf gespeichert.
