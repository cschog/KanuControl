# KJFP-Programmentwurf – Stufe 1

## Umsetzung

Im KJFP-Einstieg haben Anlage und Bearbeitung zwei unabhängig aufklappbare Bereiche. Die Veranstaltungsdaten sind anfangs geöffnet, der Programmentwurf geschlossen. Der Entwurf verwendet dieselbe Speicheraktion, berücksichtigt Änderungen beim Verlassen über die Anwendung und warnt beim Schließen/Neuladen. Die PDF-Ausgabe verwendet den gespeicherten Stand. Validierungsfehler öffnen die Bereiche. Kopierte Veranstaltungen übernehmen den Entwurf.

Ein begrenzter nativer Rich-Text-Editor bietet Fett/Kursiv, kombinierte Hervorhebung, Aufzählungen, nummerierte Listen, Absätze, manuelle Umbrüche und Formatierung entfernen. Er nutzt die vorhandenen React/MUI-Abhängigkeiten, da im Projekt kein geeigneter Rich-Text-Editor existiert. Die Browserbefehle erhalten Auswahl und Undo-Verhalten. Keine zusätzliche Editor-Bibliothek. Die unterstützte Desktop-Browserbasis muss `contentEditable` und `execCommand` unterstützen; ein späterer Wechsel auf eine Editor-Bibliothek bleibt möglich.

Kanonisches HTML: `p`, `br`, `strong`, `em`, `ul`, `ol`, `li`, ohne Attribute. Fremdformatierungen werden sowohl im Client als auch serverseitig mit jsoup entfernt. Vor jeder PDF-Ausgabe wird erneut bereinigt. Listenverschachtelungen werden aufgelöst. Fremde Blockelemente behalten ihre Textstruktur soweit möglich; Word-spezifische automatische Listen, die nur aus CSS/Nummerierungsmetadaten bestehen, werden als Text übernommen.

Grenzen: 200 Unicode-Zeichen im einzeiligen Titel, 50.000 sichtbare Unicode-Zeichen inklusive Leerzeichen und Umbrüchen im Inhalt, zusätzlich 500.000 UTF-8-Bytes im Roh- und Speicherformat. Keine stille Kürzung. Leerinhalt, reine Formatierung und Titel allein erzeugen keine Zusatzseiten.

Die gemeinsame Entity `Veranstaltung` und ihre Create-/Update-/Detail-DTOs haben zwei neue nullable Felder. Die Tenant-Migration `20261113-04-programmentwurf.xml` ergänzt `programmentwurf_titel` und `programmentwurf_inhalt`. Update-Konvention: ausgelassen oder `null` erhält den gespeicherten Feldwert; `""` löscht ihn. Die bestehenden Veranstaltungscontroller, Rechte, Repository-Zugriffe und Tenant-Transaktionen bleiben die Zugriffspfade.

## PDF

Vorschau und Download führen über `PDFDokumentService.generateAnmeldung` zum bestehenden Generator. Ein anderer Anmeldungsversand wurde nicht gefunden. Der Generator lädt die gespeicherte Veranstaltung bei jedem Aufruf erneut; es gibt hier keinen Dokumentcache.

**Die bestehende Vorlage hat zwei Seiten**, einschließlich Kostenaufstellung und Unterschriften auf Seite 2. Beide bleiben erhalten. Der Programmentwurf beginnt daher auf **Seite 3**, direkt nach der Anmeldung. Das entspricht der Vorgabe, die bisherige Anmeldung zu erhalten; die Annahme einer einseitigen Ausgangsanmeldung trifft auf dieses Projekt nicht zu.

Die Zusatzseiten enthalten Veranstaltungsname, Zeitraum, Titel, feste Schriftgrößen, Kopftrennlinie und Fußzeile mit Seitennummer. Längere Texte und lange Wörter umbrechen automatisch. Vier eingebettete DejaVu-Schriften bilden die Hervorhebungen und deutsche Sonderzeichen ab; ihre Lizenz liegt bei den Fontressourcen. Zeichen außerhalb des Fontumfangs werden sichtbar durch ein Kästchen ersetzt.

## Prüfungen

- TypeScript-Projektprüfung und ESLint für die geänderten Formular-/Editor-/Hook-Dateien.
- Browserprüfung mit isoliertem Chrome-Profil: Formatierungen, Listen, Entfernen der Formatierung, schädliche Einfügeinhalte, Titelvorschlag, Längengrenzen, Zuklappen und responsive Darstellung bei 375/1280 Pixeln.
- `ProgrammentwurfTest`: Bereinigung, leere Inhalte, Text-/Titel-/Bytegrenzen, PDF ohne Entwurf, Titel allein, kurze und mehrseitige Ausgabe, erneute Ausgabe nach Leeren. Bildvergleich der bisherigen ersten Seite und Prüfung des erhaltenen AcroForm-Feldwerts.
- `ProgrammentwurfSpeicherungTest`: Service-Speicherung, Bereinigung, ausgelassene Felder, explizites Leeren.
- `ProgrammentwurfMigrationTest`: echte Liquibase-Migration mit nullable Bestandswerten und getrennten Schemas in H2/PostgreSQL-Modus.
- Bestehende `KjfpVeranstaltungRegelnTest` (8 Tests).
- Kurze Beispiel-PDF (3 Seiten) und lange Beispiel-PDF (11 Seiten) gerendert und visuell geprüft. Letzte Seite zusätzlich mit PDFBox gegengeprüft, da die lokale Poppler-Installation auf der letzten Seite teilweise Schriftzüge ausließ.

Die Service-/PDF-Tests verwenden Repository-Mocks; der Migrationstest verwendet H2. Ein vollständiger Durchlauf mit produktionsgleichem PostgreSQL, Keycloak und zwei angemeldeten Mandanten wurde nicht durchgeführt. Die fehlerhafte Zuordnung des Fahrkostenfelds wurde anschließend auf den tatsächlichen Vorlagennamen `fahrtkosten` korrigiert. Ein Regressionstest prüft Feldwert, sichtbare Ausgabe und Gesamtsumme mit 123,45 Euro sowie den Nullbetrag.

Kein Programm-PDF-Upload, keine Änderungen an der PDF-Abrechnung und kein Deployment. Die Datenbankmigration wird erst beim regulären Start/Migrationslauf der Anwendung ausgeführt.

## Folgekorrektur: Kultur in der Anmeldung

`KULTUR` aus den Planungspositionen wird in der unveränderten Anmeldevorlage unter „Sonstige Kosten“ zusammen mit `SONSTIGE_KOSTEN` ausgewiesen. Der Betrag fließt damit auch in Ausgabensumme und berechneten Eigenanteil ein. Regressionstest: Kultur allein sowie Kultur zusammen mit sonstigen Kosten, Miete und Fahrkosten; Feldwerte, ausgeglichene Gesamtsummen und sichtbare PDF-Ausgabe geprüft. Alle fünf Tests in `ProgrammentwurfTest` erfolgreich. Keine Produktionsdaten geändert und kein Deployment ausgeführt.
