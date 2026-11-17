# Ausrichtung von Belegdokumenten

Die Belegvorschau bietet pro Seite 90° links/rechts. Änderungen bleiben lokal,
bis „Drehungen speichern“ gewählt wird. Abbrechen fragt vor dem Verwerfen nach;
ein fehlgeschlagener Speicherversuch lässt den Entwurf bestehen. In der
schreibgeschützten Ansicht können keine Drehungen gespeichert werden.

## Speicherung und Ausgabe

- Das Original bleibt bytegleich in `dokument.inhalt`. Beleguploads verzichten
  auf die bisherige vorgelagerte Bildoptimierung; Grenze: 10 MB / 40 Megapixel.
- `seiten_drehungen` enthält zusätzliche Winkel je Seite (0, 90, 180, 270).
  Fehlende Metadaten bedeuten 0. PDF-/EXIF-Orientierung bildet die Ausgangslage.
- `ausrichtung_version` verhindert das Überschreiben zwischenzeitlicher Änderungen.
  Bei HTTP 409 muss die Vorschau geschlossen und erneut geöffnet werden.
- Die Migration `20261116-01-dokument-ausrichtung.xml` gehört zum Tenant-Changelog.
  Sie ändert keine Originalbytes. Vor dem Einsatz müssen Server und Client
  zusammen aktualisiert und die Tenant-Migration ausgeführt werden.
- Die serverseitige PNG-Vorschau enthält die Grundorientierung; der Client
  wendet den lokalen Entwurf darauf an. Download und Report lesen ausschließlich
  gespeicherte Winkel. Es gibt keinen neuen Reportcache; Downloads und
  Vorschauseiten werden mit `no-store` ausgeliefert.
- PDF-Downloads addieren den gespeicherten Winkel zu `/Rotate`. Bilddownloads
  werden als PNG ohne erneute EXIF-Drehung erzeugt. Die Originaldatei bleibt erhalten.
- Der Report übernimmt alle PDF-Seiten einzeln, mit CropBox, Ressourcen und
  gespeicherter Drehung. Die vorhandene A4-Layoutkonvention für querformatige
  Belege (seitliche Leserichtung) bleibt bestehen; das Seitenverhältnis bleibt erhalten.
- Die neuen Endpunkte verwenden dieselben Rollen, Tenant-Auflösung und
  Beleg-Dokument-Zuordnungsprüfungen wie bestehende Dokumentaktionen.

## Nachgewiesener Reportfehler

Der bisherige Form-Import kombinierte eine zusätzliche 180°-Korrektur mit einer
PDFBox-Importmatrix, die bei gedrehten PDFs unterschiedliche Achsenskalierungen
anwendete. Rotierte Seiten mit versetzter CropBox wurden dadurch falsch
positioniert bzw. angeschnitten. Die Importmatrix enthält jetzt ausschließlich
CropBox-Translation und intrinsische Rotation; das Einpassen skaliert proportional.

Der neue Pixelvergleich scheitert mit dem bisherigen Composer und besteht mit
der Korrektur für alle 16 Kombinationen aus intrinsischer und zusätzlicher
Vierteldrehung. Eine automatische Erkennung der Textrichtung wurde nicht ergänzt.

## Prüfungen

- `DokumentAusrichtungTest`: Winkelvalidierung, unveränderte Originale,
  16 PDF-Rotationskombinationen mit versetzter CropBox, EXIF 1–8, vier Vierteldrehungen,
  vollständiger mehrseitiger Beleg-Report mit gerenderten Beispielseiten.
- `DokumentAusrichtungSpeicherungTest`: Zuordnung, Speichern, Versionskonflikt,
  unveränderte Originalbytes und Tenant-Migration in zwei isolierten Schemas.
- Bestehende Tests für Belegausrichtung und Belegnummern bleiben erhalten.
- Browserprüfung unter `kanucontrolClient/tests/belegdrehung`: Seitenauswahl,
  zweimal/viermal 90°, unabhängige Seiten, Speichern/Fehler/Wiederöffnen,
  Abbrechen, Download, Nur-Lesen und Layout bei 375/1100 Pixeln.

Die Beispiele verwenden künstliche Belege. Kein Test verändert Produktionsdaten.
Eine Prüfung mit Anmeldung an einem produktiven Mandanten gehört nicht zu diesen
Tests. Die Bildformate richten sich nach den vorhandenen ImageIO-Lesern;
verschlüsselte oder beschädigte PDFs benötigen eine lesbare Ausgangsdatei.

Prüfergebnis vom 16.11.2026: Server-Gesamtlauf 835 Tests, 0 Fehler,
15 bestehende optionale PostgreSQL-Tests wegen fehlender separater
Testdatenbank-Konfiguration übersprungen (Vorlagenpakete 4, Buchungsdokumente 2,
Zählerimport 4, Belegnummern 5). Die sechs neuen Ausrichtungs-/Speichertests
wurden ausgeführt. Client-Typprüfung, ESLint der geänderten Komponenten,
Produktionsbuild und Browserprüfung erfolgreich. Reportseiten und Vorschau
wurden visuell geprüft.
