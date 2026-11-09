# Modulares Vorlagensystem – Umsetzung

Stand: 09.11.2026. Implementiert und isoliert geprüft; kein Deployment und kein realer SMTP-Versand.

## Umfang

Die erste Versandintegration gilt für Bootshaus-Direktbuchungen und deren Reservierungsbestätigung. Vorlagensätze für Bestätigung, Stornierung, Ablehnung, zur Abrechnung vorgesehen und Abrechnung abgeschlossen lassen sich bereits verwalten; deren bisheriger Versand-/Abrechnungspfad ist noch nicht auf das neue System umgestellt. Weitere Mietobjekte bleiben ein Folgeumfang. Die vorhandene Zuordnung des OG über ID 2 einschließlich Zugehörigkeits- und Zyklusprüfung bleibt erhalten.

## Einrichtung nach Bereitstellung

1. Tenant-Migration `20261109-01-vorlagensystem.xml` regulär ausführen lassen. Sie wurde hier ausschließlich in isolierten H2-Testschemas geprüft, nicht gegen eine bestehende Mandanten-Datenbank angewendet.
2. Unter Vermietung → Verwaltung → Dokumentvorlagen das gemeinsame Gerüst hochladen. Die vorbereitete Datei `KanuControl_Briefpapier_Bootshaus.pdf` basiert auf dem gelieferten `PDF_Geruest.pdf`; der alte gedruckte Abschluss, das Unterschriftsbild und die rote Zwischenlinie wurden aus dieser Kopie entfernt und der Inhaltsmarker ergänzt. Die Quelldatei blieb unverändert. Eine Referenzkopie liegt unter `kanucontrolServer/src/test/resources/pdf/Briefpapier_Bootshaus.pdf`.
3. Grußformel/Name eintragen; optional das separat extrahierte Unterschriftsbild hochladen. Ein reiner Bildabschluss ist ebenfalls zulässig.
4. Gerüst mit Beispieldaten prüfen und erst anschließend speichern. Änderungen an Datei, Grußformel oder Bild machen die vorherige Prüfung ungültig.
5. Mietobjekt und Reservierung auswählen. Die angebotenen Texte sind ein Entwurf, keine automatisch aktivierte Migration alter Texte. Alle fachlichen Hinweise, Kontakte und Bedingungen vor dem Speichern prüfen und vervollständigen.
6. Mit einer gespeicherten Buchungs-ID dieses Mietobjekts den Entwurf prüfen. Danach die Vorlage speichern und in der Buchungsansicht eine aktuelle Versandvorschau öffnen.

Es gibt keine stille Rückfallvorlage. Ohne eingerichtetes Briefpapier oder Objektvorlage ist der PDF-Versand gesperrt. Reservieren ohne Mail bleibt möglich.

## Daten und Berechtigungen

Tenantlokale Tabellen: `vermietung_briefpapier` (PDF, Abschluss, Bild, Revision), `vermietung_vorlage` (Objekt/Vorgang, Blockliste, Mailtexte, Revision), `vermietung_vorlagenversand` (unveränderte Anhangbytes, Daten-/Vorlagenmetadaten und Versuchsergebnis). Byte-Dateien werden als PostgreSQL BYTEA gespeichert. Eindeutigkeit und optimistische Sperren schützen die Konfiguration. Neue Dienste verweigern Arbeit ohne expliziten Tenant-Kontext.

ADMIN und VERMIETUNG dürfen die neue Verwaltung und das Archiv benutzen, entsprechend den bisherigen Verwaltungsberechtigungen. Keine Beschränkung der Bearbeitung allein auf Administratoren. Gerüste/Bilder und archivierte PDFs werden nicht über öffentliche Dateipfade ausgeliefert.

Der REST-Bereich ist `/api/vermietung/vorlagen`. Katalog, Briefpapierprüfung/-speicherung, Objekt-/Vorgangsvorlagen und Archiv sind darüber zugänglich. Buchungsbezogene Vorschau bleibt unter `/api/vermietung/buchungen/{id}/reservierungsbestaetigung`.

## Gerüstvertrag und Seitenregel

Unverschlüsseltes A4-PDF, eine Seite, keine Rotation/abweichender Beschnitt, maximal 5 MB. Genau zehn eindeutig zugeordnete Textfelder:

`empfaenger_name`, `empfaenger_strasse`, `empfaenger_ort`, `briefdatum`, `buchungsnummer`, `anrede`, `bank_name`, `bank_iban`, `bank_bic`, `textbereich`.

`textbereich` ist ein rechteckiger Marker für Blöcke und anschließenden Abschluss. Er muss innerhalb der Seite liegen, mindestens 350 × 150 PDF-Punkte groß sein und darf die anderen Felder nicht überdecken. Er bleibt in der Ausgabe leer; Inhalte werden über ihn gesetzt. Feste Hintergrundgrafiken und gedruckte Texte in dieser Fläche sind zusätzlich visuell zu prüfen. Aktive Skripte, eingebettete Dateien und unerlaubte Medien/Aktionen werden zurückgewiesen.

Ein bis fünf betitelte Textblöcke, ohne Lücken bei ausgeblendeten Blöcken. Haupttext 10 pt; Datum 9,5 pt, Bankfelder 8 pt. Keine automatische Verkleinerung. Hauptschreiben einschließlich Abschluss immer eine Seite; Überlauf sperrt Versand. Optionales PNG-/JPEG-Abschlussbild maximal 1 MB und 4000 × 2000 Pixel.

Entschädigungspositionen beginnen ausschließlich auf Seite zwei und werden bei Bedarf vollständig auf weitere Seiten fortgesetzt. Jede Zusatzseite nennt Buchung und Seitennummer und wiederholt die Listenüberschrift. Keine Fortsetzung des Haupttextes auf diesen Seiten. Die Vorschau bietet das vollständige PDF zusätzlich zum ersten Vorschaubild.

## Platzhalter und Bedingungen

Nur katalogisierte Platzhalter mit `{{name}}`; keine Ausdrücke, Skripte oder Datenbankabfragen. Restmiete = bestehend berechnete Miete minus vereinbarte Anzahlung. Kaution bleibt separat. Vereinbarter Zahlungsplan und tatsächliche Zahlungseingänge werden nicht verwechselt.

Feste Bedingungen für Anzahlungs-/Kautions-/Restmietbetrag, OG-Belegung und die vorhandene 56-Tage-Stornogrenze. Bedingungen steuern ganze Blöcke oder Zusatztexte. Unterschiedliche Texte werden im Editor als Varianten mit ihrer jeweiligen Bedingung gepflegt; bei gleichzeitig erfüllten Varianten erscheinen alle zutreffenden Zusatztexte. Die beiden OG- und Stornogegenvarianten schließen sich durch die Fachlogik aus.

Zahlungs-, Rabatt-, Bank-, Frist-, OG- und Datenvalidierung stammt weiterhin aus dem bestehenden Reservierungsdatenpfad. Vorlagen ändern keine fachlichen Beträge.

## Vorschau und Versandarchiv

Der Fingerprint berücksichtigt Tenant, Buchung, Renderdaten einschließlich Berlin-Erstellungsdatum, Konfigurationsrevisionen, Texte, Bedingungen, Entschädigungen und tatsächliches Empfänger-Routing. Die Versandvorbereitung vergleicht ihn mit der geprüften Vorschau. Ohne aktuellen Fingerprint wird kein modularer Versand vorbereitet. Dafür existieren die POST-Aktionen `/reservieren` und `/senden` unter dem bestehenden Vorschaupfad. Die bisherige Status-API kann ohne geprüften Fingerprint keinen Direktbuchungs-PDF-Versand umgehen.

Die fertig vorbereiteten PDF-Bytes werden vor SMTP in einer getrennten Transaktion archiviert. Danach wird SMTP-Bestätigung oder Fehler/Unklarheit gespeichert. Prozessabbruch kann einen vorbereiteten, unbestätigten Versuch hinterlassen. Archivbytes sind identisch zum Mailanhang; ein späterer Download rendert nichts neu. Die Buchungsansicht und der Vorschaudialog zeigen das Archiv.

Bestehende Erfolgsmarkierung und Buchungssperre bleiben maßgeblich. Vorlagenänderungen entsperren keinen erneuten Versand. SMTP ist keine Zustellbestätigung; bei unklarem externem Ausgang bleibt die vorhandene Warnung vor Wiederholung. Ein Fehler beim Archivstatus nach bestätigtem SMTP hebt die erfolgreiche Buchungsmarkierung nicht auf.

Globale Testadresse unterdrückt CC; Objekt-Testadresse erhält die bisherige CC-Regel. Testversand ist realer SMTP-Versand an umgeleitete Adressen und setzt dieselbe Erfolgsmarkierung. Das Archiv dokumentiert den Testmodus und die tatsächlichen Empfänger.

## Prüfung und praktische Grenzen

57 ausgewählte Backend-Tests erfolgreich: neues Vorlagensystem und Migration sowie vorhandene Reservierungs-PDF-, MIME-, Status-, Routing- und Versandmarkierungstests. Die neue Migration wurde in zwei getrennten H2-Schemas im PostgreSQL-Modus einschließlich Eindeutigkeit und Rollback geprüft. Das ist keine PostgreSQL-Laufzeitprüfung.

TypeScript-Prüfung, gezielter ESLint-Lauf ohne Warnungen und Frontend-Produktionsbuild erfolgreich. Isolierter Chrome-Test mit echten React-Komponenten und simulierten APIs bei 320 und 1280 px: Gerüstprüfung vor Speichern, Entwertung der Prüfung nach Änderung, fünf Blöcke, Reihenfolge, Entwurfsvorschau und Speichern. Kein horizontaler Überlauf oder JavaScript-Laufzeitfehler. Hauptbrief und Entschädigungsseiten visuell geprüft.

Die Testumgebung benötigt `-Djava.awt.headless=true` und hier einen expliziten Byte-Buddy-Agenten für Mockito. Es wurde keine echte SMTP-Verbindung benutzt und keine vollständige Anwendung mit echten Mandanten gestartet. Archivaufbewahrung/-löschung ist noch fachlich festzulegen; bis dahin bietet die neue Oberfläche keine Löschaktion.
