# Reservierungsbestätigung und Vorschau

Stand: 08.11.2026. Kein Deployment und kein realer Mailversand im Rahmen der Umsetzung.

## Bedienung

Bei einer gespeicherten Direktbuchung im Status ANFRAGE öffnet „Reservieren“ zuerst die Vorschau. Sie enthält den Mailtext, tatsächliche Empfänger/Kopie einschließlich Testumleitung, ein responsives Bild des ausgefüllten PDFs und einen Link zum Öffnen des PDFs. Öffnen, Neuladen und Abbrechen sind reine Leseaktionen. Unsaved Änderungen werden nicht verwendet; die Statusaktionen sind im Bearbeitungsmodus ausgeblendet.

Erst „Reservieren und E-Mail senden“ setzt den Status und versendet. „Reservieren ohne E-Mail“ bleibt erhalten, auch wenn unvollständige Kontaktdaten die PDF-Vorschau verhindern. Bei PDF-/Konfigurationsfehlern ist der Mailversand gesperrt. Der Anwender kann abbrechen, die Daten bearbeiten/speichern und erneut prüfen.

Im Status RESERVIERT zeigt die Ansicht den SMTP-Übergabezeitpunkt oder den letzten Fehler. „Bestätigung prüfen und senden“ bzw. „Bestätigung prüfen und Versand wiederholen“ öffnet erneut die Vorschau und erlaubt den Versand ohne weiteren Statuswechsel. Nach erfolgreicher SMTP-Übergabe wird ein weiterer Versand serverseitig gesperrt. Die weiterhin verfügbare Vorschau zeigt den **aktuellen Datenstand**, kein historisch archiviertes Dokument. Erneutes Speichern verschickt nichts.

## Vorlage und echtes Feldmapping

Verwendet wird `kanucontrolServer/src/main/resources/pdf/Reservierungsbestaetigung.pdf`, eine interaktive Vorlage ohne Beispieldaten. Sie wurde aus der vom Betreiber bereitgestellten `KC_Support/Formulare/VERMIETUNG/Reservierungsbestaetigung.pdf` abgeleitet. Die Quelldatei bleibt unverändert; auch der dort ergänzte rote Hinweis zur neuen IBAN wird übernommen. Briefkopf, Logo, Unterschrift, Hintergrund und sonstige Hinweise bleiben erhalten. Die Vorlage besitzt 14 Textfelder und zwei Checkboxen.

| Technischer Feldname | Inhalt |
| --- | --- |
| `empfaenger_name` | Vorname und Nachname der zugeordneten Person |
| `empfaenger_strasse` | Straße der Person |
| `empfaenger_ort` | PLZ, Ort; bei Auslandsanschrift zusätzlich gespeicherter Ländercode |
| `briefdatum` | Erstellungstag in Europe/Berlin, TT.MM.JJJJ |
| `buchungsnummer` | `Buchung.getBuchungsnummer()` |
| `anrede` | bestehendes „Hallo {Vorname},“ |
| `aufenthalt` | vollständiger Einleitungstext mit Anreise und Abreise |
| `mietpreissatz` | vollständiger Mietpreissatz für VEREIN / VERBAND / sonstige Rabattart |
| `anzahlung_text` | neuer Reservierungsabsatz mit vereinbarter Anzahlung |
| `restzahlung_text` | Miete minus Anzahlung, Kaution separat, Anreise minus 7 Kalendertage |
| `stornierung_text` | Stornobedingung abhängig vom Abstand zwischen Erstellungstag und Mietbeginn |
| `bank_name` | Bankname derselben Verbindung |
| `bank_iban` | IBAN derselben Verbindung |
| `bank_bic` | BIC derselben Verbindung |
| `og_nicht_genutzt` | `/Yes` bei mitgebuchtem OG, sonst `/Off` |
| `og_zusaetzlich` | `/Off` bei mitgebuchtem OG, sonst `/Yes` |

Alle Werte einschließlich beider Checkboxen werden explizit gesetzt. Die dynamischen Textfelder erhalten eingebettete Schrift und eigene Appearance-Streams mit kontrollierten Zeilenumbrüchen. Zu lange Inhalte werden mit einem Fehler abgewiesen, statt abgeschnitten. Die Ausgabe wird geflattet; weder Widgets noch ein AcroForm-Feldbaum verbleiben. Die wiederverwendbare Ressource bleibt interaktiv.

Der Hausregeln-Satz ist fester Text. Auf dem Wort „Hausregeln“ wird **nach** dem Flatten eine Link-Annotation mit der geprüften vorhandenen `HomepageAnbindung.hausregelnUrl` ergänzt. Der Bildvorschau liegt exakt das generierte PDF zugrunde; anklickbare Links sind über „PDF öffnen“ erreichbar.

## Fachliche Regeln

- Mietbetrag: gemeinsame `BuchungPreiskalkulation` (Positionspreis, Menge, Tage und vorhandene Rabattregeln). Keine Zahlungseingänge und keine Kaution im Mietbetrag/Restzahlungsplan.
- Vereins- und Verbandsrabatt sind im vorhandenen Modell alternative Enum-Werte. Es gibt keine gleichzeitigen Kennzeichen und daher keine neue Prioritätsregel.
- Nullbeträge sind gültig, fehlende Beträge nicht. Eine Anzahlung über dem Mietbetrag wird als ungültiger Zahlungsplan gemeldet.
- Die Zahlungsfrist bleibt auch bei kurzfristigen Buchungen Anreise minus sieben Kalendertage.
- Fachliche Zuordnung durch Chris: **EG-ID 1, OG-ID 2**. Die OG-Erkennung nutzt ID 2 und durchläuft `belegteMietbereiche` rekursiv mit Zyklenschutz. Namen werden nicht ausgewertet. ID 2 muss im aktuellen Mandanten zum Mietobjekt gehören; andere Mietobjekte erhalten einen konkreten Konfigurationsfehler statt einer falschen Bestätigung.
- Primäre oder alternative Bankdaten kommen ausschließlich vom Mietobjekt der Buchung. Die Fußzeile enthält Bankname, IBAN und BIC in einer Zeile. Der Kontoinhaber bleibt am Mietobjekt gespeichert und erscheint nicht im PDF. Für die drei vorgesehenen Bankfelder werden vollständige Angaben verlangt, ohne Rückfall auf das andere Konto.
- Für unbefristete Aufenthalte gibt es keine abgestimmte Textvorlage; PDF-Versand wird deshalb mit einem konkreten Fehler abgewiesen. Reservieren ohne Mail bleibt möglich.
- Airbnb behält den bisherigen Status-/Textmailablauf; der neue Direktbuchungs-Zahlungsplan wird dort nicht angewendet. Ein erneuter Eintritt in RESERVIERT aus anderen Status ist bereits durch die vorhandenen Statusregeln ausgeschlossen.

Bei Mietbeginn vor dem Erstellungstag plus acht Wochen (weniger als 56 Kalendertage) wird ausschließlich folgender Stornotext verwendet: „Da die Buchung in weniger als 8 Wochen beginnt, können wir geleistete Anzahlungen bei Stornierung nicht mehr zurückzahlen.“ Ab genau 56 Tagen bleibt der bisherige Absatz erhalten. Maßgeblich ist der Erstellungstag in Europe/Berlin, identisch für Vorschau und Versand.

## Technischer Ablauf

- `GET /api/vermietung/buchungen/{id}/reservierungsbestaetigung`: read-only, `Cache-Control: no-store`, PDF und PNG als Base64 sowie Mailmetadaten. Nutzt bewusst nicht den bestehenden Buchungs-Getter mit automatischen Statusaktualisierungen. Zugriff wie übrige Buchungsverwaltung für ADMIN/VERMIETUNG im aktuellen Mandanten.
- Beim reservierenden Statuswechsel mit Mail werden Daten validiert und die MIME-Mail mit PDF aus der gesperrten Buchung **vor** dem Status-Commit aufgebaut. Vorbereitungsfehler verhindern den Statuswechsel.
- Nach erfolgreichem Status-Commit erfolgt SMTP-Versand in einem getrennten Ablauf. Ergebnis/Fehler werden in einer eigenen Transaktion auf der Buchung festgehalten. SMTP-Fehler setzen den bereits gespeicherten Status nicht zurück.
- `POST /api/vermietung/buchungen/{id}/reservierungsbestaetigung/senden`: expliziter Versand/Wiederholung für RESERVIERT, mit Datensatzsperre und Prüfung auf bereits bestätigten SMTP-Versand. Die wiederholte Vorbereitung nutzt die aktuellen gespeicherten Daten.
- Neue Tenant-Migration `20261108-02-reservierung-mail.xml`: `reservierung_mail_gesendet_am` und `reservierung_mail_fehler`. Keine PDF-Historie, kein Archiv und keine neue Versandkonfiguration.
- Vorhandenes Routing wird gemeinsam verwendet: globale Testadresse hat Vorrang und unterdrückt CC; die bestehende objektbezogene Testadresse ersetzt den Mieterempfänger und behält die bestehende CC-Regel. In der Vorschau sind die tatsächlich verwendeten Adressen sichtbar.
- SMTP-Übergabe ist keine Zustellbestätigung. Bei Verbindungsabbruch oder Prozessausfall nach Übergabe kann SMTP keine garantierte Genau-einmal-Zustellung liefern. Die Fehlermeldung weist bei unbestätigtem Versand auf Prüfung des Postausgangs vor Wiederholung hin.

## Prüfungen

- 51 Tests erfolgreich: `ReservierungsbestaetigungTest`, `ReservierungMailTest`, `ReservierungMailMigrationTest`, `BuchungStatusBenachrichtigungTest`, `HomepageStatusMailTest`, `BankverbindungTest`, `BuchungBankauswahlTest`.
- Abgedeckt: Preis-/Rabattberechnung, Zahlungsfrist/-plan, Stornotext bei 0/1/55/56/57 Tagen, einzeilige Fußzeile ohne Kontoinhaber, OG direkt/indirekt und Zyklen, Bankauswahl ohne Fallback, fehlende Angaben, ungültige URL, Airbnb-Abgrenzung, geflattete Ausgabe mit Link, exakter Mailtext und Anhang, beide Testmodi und CC, reine Vorschau, Reihenfolge Vorbereitung/Commit/SMTP, Fehler und Wiederholung, Doppelversandsperre, Tenant-Migration und Rollback.
- Sechs PDF-Varianten erzeugt (drei Rabattarten × mit/ohne OG), Ausgabe visuell geprüft; erkannter Zeilenumbruchfehler behoben.
- Isolierter Chrome-Test mit echten React-Komponenten und simulierten APIs bei 320 und 1280 px: Vorschau, PDF-Bild/Link, Abbrechen, Reservieren ohne/mit Mail, Doppelklick, Vorschaufehler, Versandfehler/Wiederholung und unveränderter Airbnb-Dialog. Kein horizontaler Überlauf, keine JavaScript-Laufzeitfehler.
- Frontend-Lint mit `--max-warnings 0` und Produktionsbuild geprüft. Kein realer SMTP-Test und kein Deployment.
