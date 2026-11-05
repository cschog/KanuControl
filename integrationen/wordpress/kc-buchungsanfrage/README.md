# Update 0.1.11 – Mietername im Kalender-Tooltip

Die Belegungsbalken zeigen beim Darüberfahren Uhrzeit, Status, Buchungsnummer und den vollständigen Mieternamen. Die kompakten Tagesfelder zeigen weiterhin nur Datum, Balken und Buchungsnummer. Sperrzeiten enthalten keinen Namen und keinen internen Grund.

Der vollständige Mietername wurde für diesen öffentlichen Kalender ausdrücklich zur Anzeige angefordert. Er ist für alle Besucher abrufbar, auch wenn er nur im Tooltip erscheint. Kontaktdaten, Bankdaten und interne Personen-IDs werden nicht übertragen. Der Airbnb-Export bleibt bei anonymen Belegungstiteln.

## Update installieren

1. KC-Backend mit der erweiterten Kalenderschnittstelle bauen und veröffentlichen.
2. In WordPress die ZIP `kc-buchungsanfrage-0.1.11.zip` hochladen und die vorhandene Version ersetzen.
3. Kalenderseite neu laden und gegebenenfalls den Seiten-/Asset-Cache leeren.
4. Mit einer bekannten reservierten oder bestätigten Buchung den Tooltip prüfen.

Die KC-Projektquellen sind entsprechend angepasst. Eine Datenbankmigration ist dafür nicht nötig. Der vorhandene Shortcode, die feste EG-Auswahl und die Verbindungseinstellungen bleiben erhalten. Mit einem älteren KC-Server zeigt das Plugin den bisherigen Tooltip ohne Namen; das Backend muss daher ebenfalls aktualisiert werden.

---

# Homepage-Anbindung: Anfrageformular und Bootshauskalender

**Dokumentationsstand: 3. November 2026 · WordPress-Plugin 0.1.11**

Diese Anleitung beschreibt die Einrichtung durch die KC- und WordPress-Administration. Die tägliche Bearbeitung von Anfragen steht in der [Bedienungsanleitung Vermietung](/info/bedienungsanleitung-vermietung.md).

## 1. Überblick

Die Homepage liest freigegebene Mietbereiche und ihre Verfügbarkeit aus KC. Ausgefüllte Formulare werden als Buchung mit Status **Anfrage**, Kennzeichnung **Homepage-Anfrage** und Buchungsquelle **Direkt** übernommen. KC bleibt die Quelle für Belegung, Sperrzeiten und Buchungsstatus.

Eine Eingangsbestätigung ist noch keine Reservierung. Reine Anfragen blockieren keinen Bestand. Vor dem Reservieren oder Bestätigen prüft KC den Zeitraum, die benötigten Ressourcen und die Sperrzeiten erneut.

Die aktuellen Server-, Client- und Pluginstände müssen zusammen veröffentlicht sein. Für Zeitbalken und Sperrzeiten wird auch die passende KC-Servererweiterung benötigt. Die installierte Plugin-Version ist in WordPress in der Pluginliste sichtbar.

## 2. Vorbereitung in KC

1. Den richtigen Verein auswählen und **Vermietung → Verwaltung → Homepage-Anbindung** öffnen.
2. Das gewünschte Mietobjekt wählen. Die Auswahl hier ändert nicht automatisch das aktive Objekt in den anderen KC-Bereichen.
3. Bezeichnung, Buchungsseite und vollständige HTTPS-Links zu Hausregeln und Datenschutzhinweis hinterlegen.
4. Die online angebotenen Mietbereiche auswählen. Erforderliche weitere Bereiche müssen ebenfalls freigegeben sein.
5. Die technische Client-ID eintragen und die Anbindung freischalten.
6. Festlegen, ob Teilnehmerzahlen verpflichtend sind und ob Gast bzw. Verwaltung eine erste Eingangsbestätigung erhalten sollen. Bei Benachrichtigung der Verwaltung die Empfängeradresse pflegen.

ADMIN kann die Einstellungen ändern. VERMIETUNG kann sie lesen. Freigaben, Preise und Anreise-/Abreisezeiten des Mietobjekts müssen zur tatsächlichen Vermietung passen.

Für den Bootshauskalender muss **Erdgeschoss mit Wiese** eindeutig freigegeben sein. Dieser Kalender ist fest auf das EG eingestellt; er bietet keine Mietbereichsauswahl. Das Anfrageformular kann weiterhin weitere freigegebene Räume und Extras anbieten.

## 3. Technischer Zugang und WordPress

### Service-Account

Für die Anbindung wird ein eigener Keycloak-Service-Account je Verein benötigt:

- Client-Authentifizierung und Service-Account aktivieren.
- Realm-Rolle **HOMEPAGE_INTEGRATION** zuweisen.
- Im Token den richtigen Vereinsmandanten über den Claim `tenant` hinterlegen.
- Der Claim `azp` muss der in KC eingetragenen technischen Client-ID entsprechen.
- Der Token-Aussteller muss zur bestehenden KC-Installation passen.

Die Rolle darf nur die freigegebenen Integrationsendpunkte nutzen. Client-Secret und Access-Tokens gehören auf den Homepage-Server, nicht in öffentliche Seiten oder Browser-JavaScript.

### Plugin installieren und Verbindung prüfen

1. In WordPress die aktuelle ZIP **kc-buchungsanfrage-0.1.11.zip** über **Plugins → Plugin hochladen** installieren bzw. die vorhandene Version ersetzen.
2. **Einstellungen → KC Buchungsanfrage** öffnen.
3. KC-API-Basisadresse einschließlich `/api`, Keycloak-Token-Adresse, technische Client-ID, Client-Secret und Mietobjekt-ID eintragen.
4. Zunächst **Vorschau nur für Administratoren** wählen und speichern.
5. **Verbindung zu KC prüfen (ohne Anfrage)** ausführen. Der Test liest nur den Katalog und legt keine Buchung an.

Die Token-Adresse folgt dem Muster:

```text
https://<keycloak-host>/realms/<realm>/protocol/openid-connect/token
```

Host, Realm, Objekt-ID und Client-ID aus der eigenen Installation übernehmen. Das Secret wird nicht wieder angezeigt; ein leeres Secret-Feld behält den gespeicherten Wert. Alternativ kann der Betreiber es serverseitig in `wp-config.php` hinterlegen:

```php
define('KC_HOMEPAGE_CLIENT_SECRET', '<geheimes Service-Account-Secret>');
```

Die Konstante hat Vorrang vor dem Datenbankwert.

## 4. Formular und Kalender einbinden

### Buchungsanfrage

In WordPress eine Seite für die Buchungsanfrage anlegen und einen Shortcode-Block bzw. den Shortcode im Classic Editor verwenden:

```text
[kc_buchungsanfrage]
```

### Bootshauskalender auf einer eigenen Seite

```text
[kc_belegungskalender anfrage_url="https://verein.example/bootshaus-anfragen/"]
```

Die Beispieladresse durch die tatsächliche Formularseite ersetzen. Mit dem Shortcode-Block einfügen; nicht als normalen Link behandeln.

### Kalender und Formular auf derselben Seite

```text
[kc_belegungskalender]
[kc_buchungsanfrage]
```

Ohne `anfrage_url` verweist der Kalender auf seine eigene Seite. Dort muss dann auch das Formular vorhanden sein.

### WordPress-Menü

Unter **Bootshaus** können die beiden Seiten als gleich eingerückte Unterpunkte **Buchungsanfrage** und **Bootshauskalender** angeordnet werden. Im Menüeditor beide Seiten hinzufügen, unter Bootshaus einrücken und das Menü speichern.

## 5. Was Gäste sehen

### Anfrageformular

Gäste wählen Anreise und Abreise sowie die angebotenen Mietbereiche. Bei Bestand 1 gibt es eine Checkbox, bei größerem Bestand eine Menge. Erforderliche weitere Mietbereiche werden automatisch berücksichtigt; der Zusatztext „Benötigt zusätzlich“ wird nicht mehr angezeigt.

Das Formular erfasst Name, E-Mail mit Wiederholung, Telefon, Postanschrift, gegebenenfalls Veranstalter und Bemerkungen sowie Teilnehmerzahlen und Altersspanne der Jugendlichen. Die Hausregeln müssen bestätigt und der Datenschutzhinweis zur Kenntnis genommen werden.

Die Uhrzeiten kommen aus dem KC-Mietobjekt. Mindestens eine Übernachtung ist erforderlich; eine volle Dauer von 24 Stunden ist nicht nötig. Anzahlung und Kaution werden bei der späteren Bearbeitung in KC geprüft und ergänzt. Eine freie Verfügbarkeit allein ist noch keine Buchungszusage.

### Kompakter Bootshauskalender

Der Kalender zeigt fest **Erdgeschoss mit Wiese**. In den Tagesfeldern stehen nur Tageszahl, Zeitbalken und Buchungsnummern. Beim Darüberfahren erscheinen Uhrzeit, Status, Buchungsnummer und der vollständige Mietername. Diese Angaben sind auch über zugängliche Beschriftungen abrufbar.

- Rot: belegt.
- Gelb: reserviert.
- Grau schraffiert: gesperrt.

Die Zeitbalken zeigen die tatsächlichen KC-Zeiten. Bei einer Abreise um 14:00 Uhr endet die Belegung um 14:00 Uhr; eine neue Anreise kann ab 16:00 Uhr folgen. Abweichende Zeiten aus KC werden übernommen. Ein Balken bezieht sich auf die einzelne Belegung; weitere Belegungen und gegebenenfalls Bestände sind ebenfalls zu beachten.

Ein freier zukünftiger Tag kann das Anfrageformular vorbelegen. Das Formular prüft den gesamten gewählten Aufenthalt nochmals. Der Kalender wird beim Öffnen und beim Monatswechsel neu geladen, nicht fortlaufend im Hintergrund.

Buchungsnummern und vollständige Mieternamen sind im öffentlichen Kalender für alle Besucher abrufbar. Der Mietername erscheint im Tooltip; er ist dadurch nicht vertraulich. Kontaktdaten, Bankdaten und interne Sperrgründe werden nicht veröffentlicht. Sperrzeiten enthalten keinen Mieternamen.

Die zusätzliche Sperrung des vollständigen Abreisetags betrifft den **Airbnb-Export**. Sie ändert die genaue Zeitdarstellung im Homepagekalender nicht. Der Airbnb-Export wird direkt in KC eingerichtet; siehe [Vermietungsanleitung](/info/bedienungsanleitung-vermietung.md#12-airbnb-kalenderexport).

## 6. Vorschau und Freischaltung

- **Deaktiviert:** Öffentliches Formular und Kalender sind nicht freigeschaltet; die Verarbeitung pausiert.
- **Vorschau nur für Administratoren:** Nur angemeldete WordPress-Administratoren sehen Formular und Kalender. Katalog und Verfügbarkeit werden aus KC gelesen. Das Absenden erzeugt keine echten KC-Buchungen oder Mails.
- **Öffentlich freigeschaltet:** Gäste können Anfragen stellen; bestätigte Eingänge werden nach den KC-Einstellungen per E-Mail weitergegeben.

Die Freischaltung in KC und der WordPress-Betriebsmodus sind getrennte Einstellungen. Für eine Vorschau muss der technische KC-Zugang bereits funktionieren.

Vor der öffentlichen Freigabe die in KC erfassten Belegungen prüfen, den Formularlink im Kalender testen und den Mailversand mit dem zuständigen Betreiber abstimmen. Eine öffentliche Anfrage ist ein echter Vorgang und kein Verbindungstest.

## 7. E-Mails und Warteschlange

WordPress versendet Mails über seine vorhandene Mailkonfiguration. Das Anfrage-Plugin richtet selbst keinen SMTP-Server ein. Die Mail- bzw. SMTP-Einstellungen stammen vom jeweiligen Hostinganbieter.

Nach erfolgreicher KC-Übernahme werden die gewünschten Eingangsbestätigungen weiterhin von WordPress angestoßen. Statusmails nach einer Änderung in KC versendet der KC-Server direkt über seine SMTP-Konfiguration. WordPress holt keine Statusmail-Aufträge mehr ab. Der Schalter für die erste Eingangsbestätigung schaltet diese Statusnachrichten nicht aus.

Die SMTP-Konfiguration des KC-Backends erfolgt über `KC_SMTP_ENABLED`, `KC_SMTP_HOST`, `KC_SMTP_PORT`, `KC_SMTP_USERNAME`, `KC_SMTP_PASSWORD`, `KC_SMTP_FROM`, `KC_SMTP_STARTTLS` und `KC_SMTP_SSL`; Details stehen in [`docs/infrastructure.md`](../../../docs/infrastructure.md). Die Zugangsdaten werden nur in der Serverumgebung hinterlegt. Die Mailstatus-Spalten in der nachfolgenden WordPress-Tabelle beziehen sich ausschließlich auf die Eingangsbestätigungen und Verwaltungsbenachrichtigungen der Formularanfrage.

Bei einer fehlenden oder ungültigen Mieter-E-Mail kann ein Statuswechsel mit Mailauftrag abgewiesen werden. Korrigiere zuerst die Kontaktadresse in KC. Bereits erzeugte Aufträge beziehen sich auf den Zustand beim Statuswechsel.

### Vorgänge in WordPress prüfen

Unter **Einstellungen → KC Buchungsanfrage** zeigt die Tabelle die letzten lokalen Vorgänge:

- `pending`: Übertragung an KC noch offen.
- `confirmed`: KC hat den Eingang mit Buchungsnummer bestätigt. Das ist nicht der Buchungsstatus „Bestätigt“.
- `failed`: Übertragung angehalten; den angezeigten Fehler prüfen.
- Mailstatus `pending`: Übergabe an den Maildienst steht aus.
- Mailstatus `sent`: An WordPress übergeben; kein Beleg für die Zustellung im Postfach.
- Mailstatus `off`: Für diesen Empfänger war der Versand nicht vorgesehen.

Bei Netzwerk- und Serverfehlern erfolgen automatische Wiederholungen mit wachsenden Abständen. Fachliche Ablehnungen oder geänderte Formularhinweise müssen geprüft werden. **Erneut bearbeiten** verwendet denselben Vorgang; bestätigte KC-Anfragen werden nicht erneut angelegt.

**E-Mails erneut versenden** betrifft die vorgesehenen Nachrichten des vorhandenen Vorgangs. Es kann zu doppelten Nachrichten führen, wenn eine frühere Übergabe doch zugestellt wurde. Prüfe daher zuerst Versanddiagnose, Empfänger und vorhandene Nachrichten.

Eine geänderte Zielverbindung darf alte Warteschlangenvorgänge nicht zu einem anderen Objekt oder Verein übertragen. Bei einem solchen Fehler zuerst die ursprüngliche Verbindung wiederherstellen und den Vorgang prüfen.

Die Verarbeitung verwendet WP-Cron. Bei seltenen Seitenaufrufen kann sie sich verzögern; der Hostingbetreiber kann den regelmäßigen Cron-Aufruf einrichten. Zur Bedeutung der Mailübergabe und zu Cron siehe die [WordPress-Dokumentation zu wp_mail](https://developer.wordpress.org/reference/functions/wp_mail/) und [WP-Cron](https://developer.wordpress.org/plugins/cron/).

## 8. Häufige Fehler

### Keine Mietbereiche oder Verbindungstest fehlgeschlagen

Mietobjekt-ID, richtigen Vereinsmandanten, KC-Freischaltung, Client-ID, Rolle, Token-Aussteller und Freigaben der Bereiche prüfen. Die KC-API-Adresse muss `/api` enthalten.

### „Erdgeschoss mit Wiese“ ist nicht eindeutig freigegeben

Der feste Bootshauskalender sucht genau diesen freigegebenen Bereich. Die Bezeichnung und Freigabe in KC prüfen. Der Kalender wechselt nicht ersatzweise auf das OG oder ein Extra.

### Es erscheinen noch ganze Tagesfarben ohne Zeitbalken

Plugin und KC-Serverstand prüfen. Die neue Darstellung braucht die Zeitbelegungen der KC-Schnittstelle. Danach Seiten- und Browsercache leeren bzw. die Seite neu laden.

### Die Anfrage kommt an, aber eine Mail fehlt

Den Mailstatus des vorhandenen Vorgangs prüfen und den WordPress-Versand testen. Ein neuer Formularversuch würde einen neuen Anfragevorgang erzeugen und ist kein Ersatz für eine Mailprüfung.

### Eine Übertragung bleibt nach geändertem Hinweistext stehen

Neue Formulare müssen den aktuellen Katalogstand und die geltenden Hinweise verwenden. Nicht mit veränderten Eingaben und derselben Anfrage-ID blind wiederholen; den konkreten Fehler mit dem Gast klären.

## 9. Technischer Anhang: eigene Homepage-Systeme

Die nachfolgenden Pfade sind implementiert. Alle Integrationsaufrufe sind Server-zu-Server-Aufrufe über HTTPS mit:

```text
Authorization: Bearer <Access-Token des technischen Clients>
Content-Type: application/json
```

Die konkrete Serveradresse ist installationsabhängig. Der Mandant wird aus dem von KC geprüften Token bestimmt. Eine frei übermittelte Schema-ID wird nicht akzeptiert.

### Katalog

```text
GET /api/integrationen/homepage/objekte/{mietobjektId}/katalog
```

Die Antwort liegt im üblichen KC-Format `{ "data": ..., "warnings": [] }`. `data` enthält:

- `konfigurationVersion`, `bezeichnung`, `mietobjekt`
- `anreisezeit`, `abreisezeit`, `datenschutzUrl`, `hausregelnUrl`
- `teilnehmerPflicht`
- `bereiche`: `id`, `bezeichnung`, `beschreibung`, `bestand`, `mengeneinheit`, `berechnungsart`, `einzelpreis`, `erforderlicheIds`

Nur freigegebene, derzeit direkt buchbare Mietbereiche werden geliefert. Interne Empfängeradressen, Mieter und technische Zugangsdaten gehören nicht zum öffentlichen Katalog. Die angezeigten Einzelpreise sind keine verbindliche Gesamtrechnung.

### Belegungskalender

```text
POST /api/integrationen/homepage/objekte/{mietobjektId}/kalender
```

Beispiel einer Abfrage:

```json
{"monat":"2027-05","mietbereichId":10}
```

Die Bereichs-ID ist ein Beispiel und muss zu den Freigaben des Mietobjekts passen. Die Antwort enthält `monat`, `mietbereichId` und `tage`. Ein Tag enthält `datum`, `status`, `verfuegbar`, `buchungsnummern` und `belegungen`. Jede Zeitbelegung enthält `von`, `bis`, `status` sowie gegebenenfalls `buchungsnummer` und `mietername`. Zeitbelegungen sind auf den jeweiligen Kalendertag begrenzt. Die Zeiten sind lokale KC-Zeiten; nicht zusätzlich durch die Zeitzone des Browsers verschieben.

Neben FREI, RESERVIERT und BELEGT ist **GESPERRT** möglich. Interne Sperrgründe und interne Sperrzeit-IDs gehören nicht in die öffentliche Antwort. Reservierte, bestätigte, abrechenbare und abgeschlossene Buchungen können im Kalender dargestellt werden; für eine neue verbindliche Buchung zählen die aktuelle Verfügbarkeitsprüfung und Sperrzeiten.

### Verfügbarkeit

```text
POST /api/integrationen/homepage/objekte/{mietobjektId}/verfuegbarkeit
```

```json
{
  "anreise": "2027-06-10",
  "abreise": "2027-06-12",
  "positionen": [{ "mietbereichId": 10, "anzahl": 1 }]
}
```

Die Bereichs-ID ist nur ein Beispiel. Anreise und Abreise sind Kalendertage, die Uhrzeiten bestimmt KC. Es wird mindestens eine Übernachtung verlangt; Anreise darf nicht in der Vergangenheit liegen. Die aktuelle Tagesgrenze wird für dieses Formular in Europe/Berlin ausgewertet.

Eine fachliche Verfügbarkeitsantwort enthält `verfuegbar` und `hinweis`. Nicht freigegebene Bereiche oder unvollständige Eingaben werden als fehlerhafte Anfrage abgewiesen. `verfuegbar: true` speichert oder reserviert nichts.

### Anfrage anlegen oder identisch wiederholen

```text
POST /api/integrationen/homepage/objekte/{mietobjektId}/anfragen
```

```json
{
  "anfrageId": "83344780-71d5-4b83-a75f-a497c70801ee",
  "konfigurationVersion": 2,
  "auswahl": {
    "anreise": "2027-06-10",
    "abreise": "2027-06-12",
    "positionen": [{ "mietbereichId": 10, "anzahl": 1 }]
  },
  "vorname": "Erika",
  "name": "Mustermann",
  "email": "erika@example.org",
  "telefon": "0123456789",
  "strasse": "Musterstraße 1",
  "plz": "12345",
  "ort": "Musterort",
  "countryCode": "DE",
  "veranstalter": "Beispielverein",
  "bemerkung": "Optionale Hinweise",
  "anzahlJugendliche": 8,
  "jugendAlterVon": 12,
  "jugendAlterBis": 16,
  "anzahlErwachsene": 2,
  "hausregelnAkzeptiert": true,
  "datenschutzGelesen": true
}
```

Die Anfrage-ID muss pro neuem fachlichem Vorgang neu als UUID erzeugt und bei jeder Wiederholung beibehalten werden. WordPress verwendet UUID v4. Daten und Reihenfolge der Positionen müssen für eine Wiederholung gleich bleiben. Dieselbe ID mit veränderten Angaben ergibt einen Konflikt. Auch nach einer zwischenzeitlichen Änderung der Konfiguration muss eine unklare Übertragung mit ihren ursprünglichen Angaben wiederholt werden; eine bereits gespeicherte Anfrage wird dabei wiedererkannt.

`konfigurationVersion` muss beim erstmaligen Eingang dem zuvor gelesenen Katalog entsprechen. Bei geänderten Hinweisen muss das Formular neu geladen und die neue Fassung geprüft werden. Ohne bestätigte Hausregeln und Kenntnisnahme des Datenschutzhinweises wird keine Anfrage angelegt.

Eine erfolgreiche Antwort enthält `buchungId`, `buchungsnummer`, `status` mit dem Wert ANFRAGE sowie `gastBestaetigung`, `verwaltungsBenachrichtigung`, `empfaengerEmail` und `mietumfang`. Die internen Versandangaben sind ausschließlich für den authentifizierten Homepage-Server bestimmt. Eigene Adapter dürfen insbesondere `empfaengerEmail` nicht unkontrolliert an Gäste ausgeben.

Eingangsbestätigungen nach dem Absenden einer Anfrage bleiben Aufgabe des Homepage-Adapters; die KC-Antwort enthält dafür die Versandvorgaben. Statusänderungen, die später in KC vorgenommen werden, versendet dagegen KC direkt über seine SMTP-Konfiguration.

### Legacy-API für frühere Statusnachrichten

Die folgenden API-Endpunkte bleiben für ältere Integrationsclients verfügbar. Die ausgelieferte WordPress-Erweiterung fragt diese Statusnachrichten nicht mehr ab; neue Statusmails versendet KC direkt über SMTP.

```text
GET /api/integrationen/homepage/objekte/{mietobjektId}/nachrichten
POST /api/integrationen/homepage/objekte/{mietobjektId}/nachrichten/{nachrichtId}/quittieren
```

Beide Aufrufe benötigen den bestehenden technischen Zugang mit Rolle HOMEPAGE_INTEGRATION, korrektem Mandanten, freigeschaltetem Mietobjekt und passender Client-ID. Für alte, vor der SMTP-Umstellung eingereihte Nachrichten liefert der Abruf höchstens 20 offene Nachrichten in Erstellungsreihenfolge.

Jede Nachricht enthält `id` als UUID, `buchungsnummer`, `status`, `empfaenger`, `replyTo`, `betreff`, `text` und `createdAt`. Diese Daten sind ausschließlich für den Homepage-Server bestimmt und dürfen nicht öffentlich im Formular angezeigt werden.

Ein eigener Adapter muss zuerst die empfangene Nachricht mit ihrer ID dauerhaft speichern, dann die Mail versenden und den Versandstatus dauerhaft festhalten. Erst anschließend darf er per POST quittieren. Die Antwort enthält `{ "data": { "quittiert": true }, "warnings": [] }`. Wiederholte Quittierungen sind zulässig und ändern die ursprüngliche Quittierungszeit nicht. Bei einem Netzwerkfehler nach dem Versand wird nur die Quittierung wiederholt. Je Nachricht und aktivem Adapter ist eine lokale Eindeutigkeitsprüfung erforderlich.

Eine Statusmail ist ein eigener Vorgang und legt keine neue Buchung an. Das WordPress-Plugin speichert sie unabhängig von der nach 30 Tagen entfernten ursprünglichen Formularkopie; spätere Bestätigungen oder Abrechnungen funktionieren deshalb weiterhin.

### Verwaltungs-API

Die interne Konfiguration kann weiter unter diesen Pfaden gelesen bzw. geändert werden:

```text
GET /api/vermietung/objekte/{mietobjektId}/homepage-anbindung
PUT /api/vermietung/objekte/{mietobjektId}/homepage-anbindung
```

GET benötigt ADMIN oder VERMIETUNG, PUT benötigt ADMIN. Der technische Homepage-Zugang darf diese Endpunkte nicht benutzen. Neben den Links, Bereichs-IDs und Mailwünschen enthalten die Einstellungen `aktiv`, `technischerClientId` und `version`. Beim PUT wird das Objekt ohne Antwort-Umschlag gesendet. Die zuletzt gelesene Version muss unverändert übernommen werden; beim erstmaligen Anlegen ist sie `null`. Nach der Speicherung liefert KC eine Versionsnummer zurück.

### Fehlerbehandlung für eigene Adapter

- **400:** ungültige Eingaben, Belegungskonflikt beim Anlegen oder nicht erlaubte Mietbereiche. Nicht blind mit veränderten Daten und derselben ID wiederholen.
- **401:** Token fehlt, ist ungültig oder abgelaufen. Technische Anmeldung erneuern und denselben Vorgang wiederholen.
- **403:** Rolle, Mandantenzuordnung, Freischaltung oder Client-ID prüfen.
- **409:** Konfigurationsversion, geänderte Daten derselben Anfrage-ID oder Objektfreigabe prüfen.
- **429 / Netzwerkfehler / 5xx:** Vorgang mit denselben Angaben und derselben ID später wiederholen. Keine neue ID erzeugen, solange der ursprüngliche Eingang unklar ist.

Eine Antwort mit erfolgreichem Speichern darf nicht durch einen nachfolgenden Mailfehler als fehlende KC-Anfrage behandelt werden.


[Zur Vermietungsanleitung](/info/bedienungsanleitung-vermietung.md) · [Zur Hilfeübersicht](/info/bedienungsanleitung.md)
