# Bedienungsanleitung Vermietung

**Anwendung:** KanuControl 2.1.0 · **Dokumentationsstand:** 3. November 2026

Diese Anleitung beschreibt die Vermietung von Objekten wie dem Bootshaus. Für Veranstaltungen, Förderung und Veranstaltungsfinanzen gibt es die [Bedienungsanleitung KJFP](/info/bedienungsanleitung-kjfp.md).

## Inhalt

- [1. Einstieg und Arbeitskontext](#1-einstieg-und-arbeitskontext)
- [2. Mieter und Kontakte](#2-mieter-und-kontakte)
- [3. Mietobjekte und Mietbereiche](#3-mietobjekte-und-mietbereiche)
- [4. Eine Buchung anlegen](#4-eine-buchung-anlegen)
- [5. Buchungsstatus und Bearbeitung](#5-buchungsstatus-und-bearbeitung)
- [6. Buchungsliste und KC-Kalender](#6-buchungsliste-und-kc-kalender)
- [7. Mietpreise und Rabatte](#7-mietpreise-und-rabatte)
- [8. Abrechnung, Anzahlung und Kaution](#8-abrechnung-anzahlung-und-kaution)
- [Finanz-Dashboard: Jahresumsatz und Planungsgrenze](#finanz-dashboard-jahresumsatz-und-planungsgrenze)
- [Verbrauch & Belegung](#verbrauch-belegung)
- [9. Verbrauchskosten und Zähler](#9-verbrauchskosten-und-zahler)
- [10. Sperrzeiten](#10-sperrzeiten)
- [11. Homepage-Anfragen und Bootshauskalender](#11-homepage-anfragen-und-bootshauskalender)
- [12. Airbnb-Kalenderexport](#12-airbnb-kalenderexport)
- [13. E-Mails und Homepage-Warteschlange](#13-e-mails-und-homepage-warteschlange)
- [14. Häufige Fragen](#14-haufige-fragen)


## 1. Einstieg und Arbeitskontext

Wähle nach der Anmeldung das Modul **Vermietung**. Dafür benötigst du die Rolle VERMIETUNG oder ADMIN. Ist nur dieses Modul freigeschaltet, wird es direkt geöffnet.

Das Startmenü enthält **Mieter**, **Mietobjekte**, **Buchung**, **Abrechnung**, **Finanz-Dashboard**, **Verbrauch & Belegung** und **Verwaltung**. Im Kopfbereich stehen Verein und aktives Mietobjekt. Prüfe diesen Kontext vor dem Anlegen einer Buchung.

Die Verwaltung enthält mehrere Reiter. **Sperrzeiten** und **Airbnb-Export** stehen rechts hinter **Homepage-Anbindung**. Wenn nicht alle Reiter sichtbar sind, scrolle die Reiterleiste horizontal oder nutze den Pfeil am rechten Rand. Auf einem kleinen Bildschirm kannst du die Leiste verschieben.

## 2. Mieter und Kontakte

Öffne **Vermietung → Mieter**. Suche zuerst nach der Person, bevor du einen neuen Kontakt anlegst. Pflege Name, E-Mail-Adresse, Telefon und Anschrift der verantwortlichen Person.

Bei einer neuen Buchung wählst du den Mieter im Suchfeld aus. Eine frei eingetippte Suche allein ist noch keine Zuordnung; wähle einen Treffer aus. Bei Homepage-Anfragen werden neue Kontakte angelegt, ohne bestehende Personendaten anhand einer unbestätigten E-Mail-Adresse zu überschreiben. Prüfe daher mögliche doppelte Kontakte in KC.

Eine korrekte E-Mail-Adresse ist für Statusnachrichten an Mieter von Homepage-Buchungen erforderlich.

### Mieter archivieren oder löschen

**Archivieren** kennzeichnet den Mietereintrag als inaktiv. Der Kontakt und seine Buchungen bleiben erhalten; in der Liste steht „Archiviert“. Eine auch in KJFP verwendete Person kann weiterhin als Kontakt auswählbar sein.

Zum endgültigen Entfernen einen Mieter auswählen und **Löschen** anklicken. KC fragt mit dem Namen des Mieters nach; erst **Mieter löschen** bestätigt den Vorgang. **Abbrechen** oder Escape verwirft die Nachfrage.

Ein Mieter kann erst gelöscht werden, wenn **keine zugehörige Buchung mehr vorhanden ist**. Das gilt für alle Buchungsstatus, auch Anfragen, abgelehnte, stornierte und abgeschlossene Buchungen. Solange die Liste Buchungen ausweist, ist die Schaltfläche gesperrt und ein Hinweis erklärt den Grund. Die Serverprüfung schützt zusätzlich vor zwischenzeitlich hinzugekommenen Buchungen.

Nach dem Löschen der zugehörigen Buchungen die Mieterverwaltung neu öffnen bzw. neu laden. Anschließend ist das Löschen des Mieters möglich. Der Mietereintrag wird entfernt; eine zugehörige KJFP-Person bleibt erhalten.


## 3. Mietobjekte und Mietbereiche

Öffne **Vermietung → Mietobjekte**. Ein Mietobjekt ist das übergeordnete Objekt, beispielsweise „Bootshaus“. Mietbereiche sind die einzeln auswählbaren Räume und Extras, beispielsweise EG mit Wiese, OG, Grill oder Bettwäsche.

### Mietobjekt vorbereiten

1. Bezeichnung, Beschreibung und Anschrift pflegen.
2. **Standard-Anreisezeit** und **Standard-Abreisezeit** hinterlegen, beispielsweise 16:00 und 14:00 Uhr.
3. Festlegen, ob das Objekt mietbar ist und ob Direktbuchung bzw. Airbnb erlaubt sind.
4. Das gewünschte Objekt als aktiv wählen.
5. Die zugehörigen Mietbereiche, Bestände und Berechnungsarten prüfen.

### Bestand und Buchungsquelle

Der Bestand beschreibt die verfügbare Anzahl eines Mietbereichs oder Extras. Ein Bereich mit Bestand 1 kann nur einmal gleichzeitig belegt werden. Ein Extra mit größerem Bestand kann mit einer Menge gebucht werden.

Für Direkt- und Airbnb-Buchungen sind die jeweils freigegebenen Mietbereiche auswählbar. Fehlt ein Bereich im Buchungsformular, prüfe seine Freigabe für die verwendete Buchungsquelle.

### Abhängigkeiten und gemeinsame Belegung

Unter **Verwaltung → Abhängigkeiten** werden benötigte weitere Mietbereiche und gemeinsam belegte Ressourcen verwaltet. Wenn OG nur zusammen mit EG gebucht werden darf, muss diese Voraussetzung eingerichtet sein. Direkt- und Airbnb-Voraussetzungen können unterschiedlich sein.

Das Formular ergänzt verfügbare Voraussetzungen beim Auswählen. Wird eine Voraussetzung abgewählt, können davon abhängige Positionen ebenfalls entfernt werden. Die Serverprüfung kontrolliert den Mietumfang nochmals beim Speichern und bei verbindlichen Statuswechseln.

## 4. Eine Buchung anlegen

Öffne **Vermietung → Buchung** und starte die Anlage einer Direkt- oder Airbnb-Buchung für das aktive Mietobjekt.

1. Mieter auswählen; bei Bedarf den Kontakt zuvor anlegen.
2. Veranstalter auswählen, sofern relevant.
3. Anreise- und Abreisedatum sowie Uhrzeiten prüfen.
4. Mietbereiche auswählen.
5. Preise und Rabatte kontrollieren.
6. Teilnehmerzahlen, Bemerkung sowie bei Direktbuchung Anzahlung und Kaution erfassen.
7. Speichern und anschließend den passenden Status wählen.

### Checkbox oder Menge

Bei **Bestand 1** erscheint eine Checkbox ohne Mengenfeld. Das Auswählen übernimmt automatisch Menge 1; das Abwählen entfernt die Position. Bei größerem Bestand bleibt die Mengenangabe erhalten.

### Zeitraum und Mindestaufenthalt

Anreise und Abreise werden mit Uhrzeiten gespeichert. Der Mindestaufenthalt beträgt eine Übernachtung: Das Abreisedatum liegt frühestens am Folgetag. Es müssen keine 24 Stunden zwischen Anreise und Abreise liegen. Freitag 16:00 Uhr bis Samstag 14:00 Uhr ist somit eine Übernachtung.

Bei „Buchung ohne Enddatum“ ist die Laufzeit noch offen. Tagespreise werden vorläufig ohne eine unbegrenzte Laufzeit dargestellt. Für eine endgültige Abrechnung wird ein konkretes Ende benötigt.

### Buchungsnummer

Neue Buchungsnummern richten sich nach dem Jahr der Anreise und werden innerhalb dieses Jahres fortlaufend vergeben, beispielsweise 2027-001. Bereits vergebene Nummern werden nicht allein durch das Öffnen einer Buchung verändert.

## 5. Buchungsstatus und Bearbeitung

- **Anfrage:** Bearbeitung ist offen. Die Anfrage blockiert noch keinen Bestand.
- **Reserviert:** Vorläufige Belegung; blockiert den ausgewählten Mietumfang.
- **Bestätigt:** Verbindliche Belegung; blockiert den ausgewählten Mietumfang.
- **Abgelehnt:** Eine Anfrage wird nicht angenommen.
- **Storniert:** Eine zuvor reservierte, bestätigte oder abrechenbare Buchung wurde aufgehoben.
- **Abrechenbar:** Der Aufenthalt kann abgerechnet werden. Beim Öffnen einer bestätigten Buchung nach der Abreise kann KC diesen Status setzen.
- **Abgeschlossen:** Die Abrechnung ist abgeschlossen.

Aus einer Anfrage kannst du reservieren, bestätigen oder ablehnen. Aus „Reserviert“ kannst du bestätigen oder stornieren. Eine bestätigte Buchung kann storniert oder nach der Abreise abgerechnet werden.

Beim Reservieren und Bestätigen prüft KC Bestand und Sperrzeiten erneut. Eine frühere Verfügbarkeitsanzeige oder eine Eingangsbestätigung aus dem Formular ist noch keine verbindliche Zusage.

Zum Korrigieren einer abgeschlossenen Abrechnung kann sie wieder auf **Abrechenbar** geöffnet werden. Nach der Korrektur muss erneut abgeschlossen werden; Verbrauchskosten werden dabei nochmals geprüft und berechnet.

## 6. Buchungsliste und KC-Kalender

In der Buchungsübersicht kannst du nach Mietobjekt filtern und zwischen Liste und Kalender wechseln. In der Liste grenzt der Filter **Jahr** die Buchungen nach ihrem Anreisejahr ein; **Alle Jahre** zeigt den gesamten Bestand. Der Jahresfilter lässt sich mit Mietobjekt und Buchungsstatus kombinieren. Für das Anlegen aus einer Kalenderauswahl muss das aktive Mietobjekt im Filter ausgewählt sein.

Im Kalender markieren zwei Klicks den Beginn und das Ende eines Zeitraums. Über **Buchung für Zeitraum anlegen** werden diese Daten in ein neues Formular übernommen. Prüfe dort die Uhrzeiten und den Mietumfang.

Buchungen zeigen ihren Status. Sperrzeiten erscheinen als **Gesperrt**; der interne Grund ist in KC im Tooltip sichtbar. Ein markierter Zeitraum ist noch keine Buchung und keine Reservierung.

## 7. Mietpreise und Rabatte

Unter **Verwaltung → Mietpreise** werden die Preise je Mietbereich, Buchungsquelle und Gültigkeit gepflegt. Für Direktbuchung und Airbnb können unterschiedliche Preise gelten.

Bei der Berechnungsart **TAG** gilt:

```text
Einzelpreis × Menge × Miettage
```

Die Miettage entsprechen der Differenz der Kalendertage von Anreise und Abreise. Freitag 16:00 Uhr bis Sonntag 14:00 Uhr ergibt zwei Miettage. Bei 300 Euro pro Tag und Menge 1 sind das 600 Euro. Ein Rabatt von 50 Prozent reduziert diese Summe auf 300 Euro.

Andere Berechnungsarten, etwa eine Pauschale pro Buchung, werden nicht mit Miettagen multipliziert. Im Formular werden der verwendete Preis, die Berechnungsart und gegebenenfalls die Miettage angezeigt.

Unter **Verwaltung → Rabatte** werden die vorgesehenen Mietbereichsrabatte gepflegt. Individuelle Buchungsrabatte sind im Buchungsformular zu prüfen. Gespeicherte Preis- und Rabattschnappschüsse sind für bestehende Buchungen maßgeblich; eine Preisänderung in der Verwaltung bedeutet keine automatische Neuberechnung aller alten Buchungen.

## 8. Abrechnung, Anzahlung und Kaution

Öffne **Vermietung → Abrechnung** oder die Schaltfläche **Abrechnung** einer Buchung. Dort werden Soll, erfasste Zahlungen, offene Beträge und Bewegungen zu Anzahlung, Miete, Verbrauchskosten und Kaution betrachtet.

### Sollbeträge und Zahlungseingänge

Ein im Buchungsformular eingetragener Anzahlungs- oder Kautionsbetrag ist eine Forderung und noch kein Zahlungseingang. Tatsächliche Eingänge müssen als Finanzbewegung erfasst werden.

Die Anzahlung ist Teil der gesamten Miete und wird nicht zusätzlich zur Miete berechnet. Bei Airbnb-Buchungen werden in KC keine Anzahlungs- oder Kautionsforderungen erhoben.

### Rückzahlungen und Verrechnung

Eingänge, Rückzahlungen und Verrechnungen werden je Kategorie erfasst. Prüfe Betrag, Datum und Kategorie, bevor du speicherst. Die Übersicht berücksichtigt bereits erfasste Bewegungen bei offenen Beträgen und Guthaben.

Vor dem Abschließen muss die Kaution geklärt sein. Noch nicht zurückgezahlte oder verrechnete Kaution kann den Abschluss einer Direktbuchung verhindern. Verwende die Finanzübersicht zur Prüfung der zugehörigen Bewegungen.

## 9. Verbrauchskosten und Zähler

Unter **Verwaltung → Verbrauchskosten** und **Zählerverwaltung** werden Verbrauchsarten, Preise und Zähler eingerichtet. Zähler können den relevanten Mietbereichen zugeordnet werden.

Erfasse für eine Buchung die benötigten Zählerstände und prüfe die Verbrauchskostenvorschau. Beim Abschließen werden die Verbrauchskosten geprüft und gespeichert. Werden Daten nachträglich korrigiert, öffne die Abrechnung wieder und schließe sie nach der Prüfung erneut ab.

### Entfernte Ablesungen für die Abrechnung freigeben

Bei einer abrechenbaren Direktbuchung sucht KC je Zähler die zeitlich nächstliegende Ablesung zu Anreise und Abreise. Bis einschließlich 24 Stunden Abstand ist keine zusätzliche Freigabe erforderlich. Bei größerem Abstand zeigt die Verbrauchsvorschau die nächstliegenden Stände, Ablesezeitpunkte und vorläufigen Kosten an. Diese Vorschläge zählen noch nicht als vollständig berechnete Verbrauchskosten.

Beim **Abschließen** lädt KC die Vorschau neu. Der Dialog **Entfernte Zählerstände verwenden?** zeigt Anfangs- und Endstand, die jeweiligen Ablesezeitpunkte, den Abstand zu den Buchungsterminen und die Kosten. Prüfe, ob der gemessene Zeitraum für diese Buchung verwendbar ist; er kann Verbrauch außerhalb des Aufenthalts enthalten. Mit **Werte verwenden und abschließen** erlaubst du die Verwendung dieser konkreten Stände. **Abbrechen** lässt die Buchung und bisherige Verbrauchskosten unverändert.

Die Zustimmung gilt nur für die angezeigten Stände und den geprüften Buchungszeitraum. Ändern sich diese vor dem Speichern, muss die aktuelle Vorschau erneut geprüft werden. Fehlende Ablesungen, derselbe Stand für Anfang und Ende, falsche zeitliche Reihenfolge und fallende Zählerstände bleiben gesperrt. Die tatsächlich verwendeten Werte und Ablesezeitpunkte werden beim Abschluss im Verbrauchskosten-Snapshot gespeichert. Beim Wiederöffnen und erneuten Abschließen wird erneut geprüft und gegebenenfalls nachgefragt. Für Airbnb bleibt die Verbrauchsabrechnung entfallen.

## 10. Sperrzeiten

Öffne **Verwaltung → Sperrzeiten** und wähle das Mietobjekt.

1. **Ganztägig** aktivieren oder abwählen.
2. Bei Ganztag den ersten und den letzten gesperrten Tag eingeben. Beide Tage sind eingeschlossen. Für einen einzelnen Tag dasselbe Datum zweimal wählen.
3. Bei einer Sperre mit Uhrzeit Beginn und Ende angeben.
4. Internen Grund eintragen und speichern.

Eine Sperre gilt für alle Mietbereiche des ausgewählten Objekts. Sie verhindert neue Buchungen und Homepage-Anfragen im betroffenen Zeitraum sowie das Reservieren oder Bestätigen einer überschneidenden älteren Anfrage.

Überlappende Sperren werden abgewiesen. Vorhandene reservierte, bestätigte, abrechenbare oder abgeschlossene Buchungen müssen vor dem Anlegen einer überschneidenden Sperre geklärt werden; KC nennt die betroffenen Buchungsnummern. Eine Sperre ändert diese Buchungen nicht automatisch.

Mit **Bearbeiten** änderst du eine Sperrzeit. **Sperre aufheben** erfordert eine Bestätigung. Ist der Datensatz inzwischen geändert worden, lade die Liste neu und prüfe den aktuellen Stand.

Der interne Grund bleibt in KC. Auf der Homepage und im Airbnb-Feed wird nur **Gesperrt** ausgegeben.

## 11. Homepage-Anfragen und Bootshauskalender

Die WordPress-Anbindung wird unter **Verwaltung → Homepage-Anbindung** konfiguriert. Mit der Rolle VERMIETUNG oder ADMIN können diese Einstellungen gelesen und geändert werden. Auch Dokumentvorlagen, PDF-Briefpapier und Abschluss sind mit dem normalen Vermietungszugriff bearbeitbar.

Eine neue Homepage-Anfrage wird als **Anfrage** mit Kennzeichnung **Homepage-Anfrage** und Buchungsquelle **Direkt** übernommen. Prüfe Kontakt, Zeitraum, Mietbereiche, Preis, Teilnehmerzahlen sowie Anzahlung und Kaution, bevor du reservierst oder bestätigst.

Der Bootshauskalender auf der Homepage zeigt fest **Erdgeschoss mit Wiese**. Die Tagesfelder enthalten Datum, Zeitbalken und Buchungsnummern; Beim Darüberfahren sind Uhrzeiten und vollständiger Mietername abrufbar. Der Name ist damit öffentlich für alle Homepagebesucher zugänglich; Kontaktdaten und interne Sperrgründe werden nicht ausgegeben. Sperren erscheinen als schraffierte Balken. Ein freier zukünftiger Tag kann das vorbelegte Anfrageformular öffnen.

Ein Abreisetag kann dort teilweise belegt und ab der nächsten Anreisezeit wieder verfügbar sein. Das Formular prüft immer den ganzen gewünschten Aufenthalt. Für den Airbnb-Export gilt die gesonderte Regel im nächsten Kapitel.

Die vollständige Einrichtung mit WordPress-Shortcodes, technischen Zugangsdaten, E-Mail-Versand und Warteschlange steht in der [Anleitung zur Homepage-Anbindung](/info/homepage-anbindung.md).

## 12. Airbnb-Kalenderexport

Öffne **Verwaltung → Airbnb-Export**. Für das ganze Bootshaus sind **EG und OG** auszuwählen. Eine Belegung eines dieser Bereiche blockiert das gesamte Airbnb-Inserat.

1. Bootshaus auswählen und die vorausgewählten Bereiche EG und OG prüfen.
2. **Export zu Airbnb aktivieren** einschalten.
3. **Export-Link erstellen** anklicken. Damit werden auch die Einstellungen gespeichert.
4. Den angezeigten **Airbnb-iCal-Link** kopieren.
5. Bei Airbnb den Inseratskalender öffnen: **Verfügbarkeit → Kalender verknüpfen → Mit anderer Website verknüpfen**.
6. Den Link als externen Kalender hinzufügen, beispielsweise unter „KC Bootshaus“, und den Import prüfen.

Falls Airbnb zuerst die Übersicht mit einer Zeile je Inserat zeigt, öffne zunächst den einzelnen Inseratskalender über den Inseratsnamen.

### Was blockiert Airbnb?

Reservierte und bestätigte Buchungen von EG oder OG sowie Sperrzeiten des Mietobjekts. Reine Anfragen werden nicht exportiert. Der Feed enthält nur Datumsbereiche mit **Belegt** oder **Gesperrt**, ohne Namen, Buchungsnummern oder interne Sperrgründe.

**Auch der Abreisetag bleibt bei Airbnb vollständig gesperrt.** Freitag bis Sonntag um 14:00 Uhr blockiert deshalb auch die Sonntagnacht; eine neue Anreise ist bei Airbnb frühestens Montag möglich. Das verändert weder die gespeicherte Abreisezeit in KC noch die Mietpreisberechnung. Ein Ende exakt um 00:00 Uhr gilt weiterhin als exklusive Tagesgrenze.

Ganztägige Sperren können außerdem die Nacht vor dem gesperrten Tag betreffen, wenn deren Abreise in die Sperre fällt. Eine Sperre ausschließlich in der Wechselpause von 14:00 bis 16:00 Uhr überschneidet sich mit keiner Standardübernachtung.

### Aktualisierung und Link

Airbnb ruft importierte Kalender normalerweise alle drei Stunden ab; du kannst den Abruf dort auch manuell aktualisieren. Die Schritte beschreibt die [Airbnb-Hilfe zur Kalendersynchronisierung](https://www.airbnb.de/help/article/99). Der Export enthält die kommenden zwei Jahre sowie die letzten 30 Tage.

Der geheime Link wird nur unmittelbar nach dem Erzeugen angezeigt. Er bleibt nach dem Schließen der Hilfe oder der Verwaltung gültig. **Neuen Link erzeugen** macht den bisherigen Link ungültig und erfordert eine Bestätigung. Ersetze ihn anschließend bei Airbnb.

Wird der Export deaktiviert und gespeichert, liefert der vorhandene Link einen gültigen, leeren Kalender. Airbnb-Buchungen werden in dieser Ausbaustufe noch nicht nach KC importiert.

## 13. E-Mails und Homepage-Warteschlange

WordPress verschickt die Eingangsbestätigung nach erfolgreicher KC-Übernahme gemäß den KC-Einstellungen. Statusänderungen von Homepage-Buchungen erzeugen außerdem Mailaufträge in KC, die WordPress abholt und über seine Mailkonfiguration versendet.

Eine erfolgreiche Mailübergabe ist noch kein Zustellnachweis. Prüfe bei fehlenden Nachrichten den WordPress-Versand und die Warteschlange unter **Einstellungen → KC Buchungsanfrage**. Ein erneuter Mailversand kann Nachrichten verdoppeln; prüfe zuerst den vorhandenen Vorgang.

## 14. Häufige Fragen

### Ich finde Sperrzeiten oder Airbnb-Export nicht

Die Reiter stehen weit rechts in der Verwaltung. Scrolle die Reiterleiste horizontal. Falls die Bereiche auch dann fehlen, prüfe den veröffentlichten Frontend-Stand und lade KC vollständig neu.

### Ein Mietbereich fehlt im Buchungsformular

Prüfe das aktive Mietobjekt und die Freigabe des Bereichs für Direktbuchung bzw. Airbnb. Kontrolliere außerdem seine Voraussetzungen.

### Eine Anfrage lässt sich nicht bestätigen

Bestand und Sperrzeiten werden beim Statuswechsel erneut geprüft. Kontrolliere zwischenzeitliche Belegungen und den gesamten Zeitraum. Bei Homepage-Buchungen muss die erforderliche Mieter-E-Mail-Adresse gültig sein.

### Der Kalender ist frei, der Aufenthalt lässt sich trotzdem nicht buchen

Ein einzelnes Kalenderfeld bestätigt nicht die Verfügbarkeit aller Nächte, aller gewählten Mengen und aller erforderlichen Bereiche. Maßgeblich ist die vollständige Prüfung beim Absenden oder Statuswechsel.

### Bei Airbnb sehe ich noch den alten Stand

Aktualisiere den verknüpften Kalender bei Airbnb manuell und prüfe, ob dort der aktuelle KC-Link hinterlegt ist. Nach einer Linkerneuerung muss die URL bei Airbnb ersetzt werden.

### Die Abrechnung lässt sich nicht abschließen

Prüfe Zählerstände, Verbrauchskosten und noch offene Kautionsvorgänge. Folge der konkreten Fehlermeldung; das Ändern eines Sollbetrags allein erfasst keine Zahlung.

[Zur Hilfeübersicht](/info/bedienungsanleitung.md)


## Finanz-Dashboard: Jahresumsatz und Planungsgrenze

Unter **Vermietung → Finanz-Dashboard** siehst du die Einnahmen aller in KC erfassten Vermietungen des Vereins. Die Jahresauswahl folgt dem Datum der erfassten Zahlungsbewegung, nicht dem Aufenthaltsjahr. Anzahlungen, Miete und Verbrauchskosten zählen als Einnahmen; Rückzahlungen mindern sie im Rückzahlungsjahr. Reine Kautionen zählen nicht. Eine mit Verbrauchskosten verrechnete Kaution zählt einmal bei den Verbrauchskosten. Tatsächliche Zahlungen bleiben auch bei einer Stornierung enthalten, bis die Rückzahlung erfasst wurde.

Die Planungsgrenze beträgt zunächst 25.000 Euro. Über **Grenze für … ändern** kannst du sie für jedes Jahr separat speichern. Fortschrittsbalken, Restspielraum und Warnungen beziehen sich auf diese persönliche Planungsgrenze. Das Dashboard berücksichtigt ausschließlich Vermietungen in KC, keine anderen Umsätze des Vereins.

Die **Buchungsplanung** addiert zum erfassten Jahresumsatz bekannte offene Beträge aus reservierten, bestätigten, abrechenbaren und abgeschlossenen Buchungen mit Anreise im gewählten Jahr. Bereits erfasste Zahlungen aus früheren Jahren werden dabei abgezogen. Noch nicht abgerechnete Verbrauchskosten und unbefristete künftige Mieten werden nicht geschätzt. Da KC keinen erwarteten Zahlungstermin kennt, ist das eine Planung nach Anreisejahr und keine garantierte Einnahmeprognose.

Der **lineare Trend** rechnet den bisherigen Jahresumsatz anhand der vergangenen Kalendertage auf das Jahresende hoch. Saisonale Schwankungen werden dabei nicht berücksichtigt. Die Jahresprognose verwendet vorsichtig den höheren Wert aus Buchungsplanung und linearem Trend. Für vergangene Jahre wird der tatsächliche erfasste Umsatz angezeigt; für künftige Jahre gibt es nur die Buchungsplanung.

Monatsübersicht, Vorjahresverlauf, Direkt-/Airbnb-Summen und die Liste der Buchungen machen die Beträge nachvollziehbar. Über eine Buchungsnummer öffnest du ihre **Abrechnung**. Erfasse Zahlungen vollständig; bei Plattformbuchungen dürfen Gebühren nicht einfach als Umsatzminderung vom Mieterlös abgezogen werden.

Die Planungsgrenze ersetzt keine steuerliche Beurteilung: [§ 19 UStG](https://www.gesetze-im-internet.de/ustg_1980/__19.html) unterscheidet aktuell 25.000 Euro Gesamtumsatz im Vorjahr und 100.000 Euro im laufenden Jahr. Welche Umsätze im Einzelfall einzubeziehen sind, muss für den Verein gesondert beurteilt werden.


## Verbrauch & Belegung

Unter **Vermietung → Verbrauch & Belegung** wählst du Mietobjekt und Kalenderjahr. Die Übersicht zeigt Gas, Wasser und Strom pro zugeordnetem Zähler in dessen hinterlegter Einheit (z. B. m³ oder kWh). Unterschiedliche Einheiten werden nicht addiert oder automatisch umgerechnet. Auch inaktive Zähler mit historischen Daten bleiben sichtbar. Über **Zählerstände** öffnest du die Zählerverwaltung direkt.

Grundlage sind jeweils zwei aufeinanderfolgende Ablesungen. Nur deren Differenz ist gemessen. Liegt das ganze Messintervall innerhalb einer Buchungszeit oder ganz ohne Buchung, ist die Zuordnung eindeutig. Überspannt ein Intervall Buchungen und Leerzeiten, Monats- oder Jahresgrenzen, wird der Verbrauch zeitanteilig verteilt und als **geschätzt** gekennzeichnet. Häufigere Ablesungen, besonders bei An- und Abreise, verbessern die Aussagekraft. Es wird nicht über die letzte vorhandene Ablesung hinaus hochgerechnet. Lücken und fallende Zählerstände (z. B. bei einem Zählerwechsel) werden ausgelassen und erläutert.

Die Kennzahlen vergleichen Verbrauch in Zeiten **mit Buchung** und **ohne Buchung**, zusätzlich jeweils pro 24 abgedeckte Stunden. „Ohne Buchung“ ist kein Nachweis tatsächlichen Leerstands: Vereinsnutzung, Reinigung, Heizung und Sperrzeiten können ebenfalls Verbrauch verursachen. Reservierte, bestätigte, abrechenbare und abgeschlossene Buchungen zählen; Anfragen, abgelehnte und stornierte Buchungen nicht. Die genauen Uhrzeiten werden berücksichtigt, überlappende Buchungen zählen zeitlich nur einmal.

Das Monatsdiagramm stellt Verbrauch und Buchungsanteil gegenüber. Schraffierte Säulen kennzeichnen zeitanteilige Schätzungen. Die grauen Markierungen und Striche in der Tabelle bedeuten **keine Messdaten**, nicht Nullverbrauch. Der Buchungsanteil bezieht sich auf die betrachtete Monatszeit bis zum Datenstand; die Verbrauchssummen dagegen nur auf die durch gültige Ableseintervalle abgedeckten Stunden. Beachte deshalb die Messabdeckung.

Das Streudiagramm verwendet nur vollständige, gemessene Intervalle im gewählten Zeitraum. Jeder Punkt vergleicht den Buchungsanteil mit dem gemessenen Verbrauch pro 24 Stunden desselben Intervalls. Pearson r wird erst bei mindestens drei Intervallen mit Unterschieden in Buchungsanteil und Verbrauchsrate angezeigt. Geschätzte Monatsstücke werden nicht als unabhängige Messpunkte gezählt. Ein Zusammenhang beweist keine Ursache; insbesondere bei Gas beeinflussen Wetter und Heizverhalten den Verbrauch.

Der Zeitstrahl und die aufklappbare Intervalltabelle zeigen die zugehörigen Buchungsnummern. Maßgeblich ist die aktuelle Zuordnung der Zähler zu Mietbereichen. Wird ein Zähler über mehrere Mietobjekte geteilt, umfasst seine Auswertung auch deren zugeordnete Buchungen und trägt einen entsprechenden Hinweis. Der gemessene Gesamtverbrauch eines gemeinsamen Zählers lässt sich ohne zusätzliche Messungen nicht auf die einzelnen Objekte aufteilen. Diese Übersicht verändert keine Verbrauchsabrechnung und keine Zählerstände.
