# Hausmeister-Wochenbericht

Unter Vermietung → Verwaltung → Homepage-Anbindung erscheint unter den bisherigen E-Mail-Einstellungen ein eigener Abschnitt „Hausmeister-Wochenbericht“. Die Empfängeradresse gilt für das ausgewählte Mietobjekt im aktuellen Mandanten. Leer bedeutet deaktiviert. Der Bericht ist unabhängig von der Freischaltung des WordPress-Zugangs.

1. Zunächst die eigene Testadresse eintragen und „Einstellungen speichern“ klicken.
2. „Hausmeisterbericht jetzt senden“ klicken. Mit VERMIETUNG oder ADMIN kann der Bericht in DEV, TEST und PROD manuell versendet werden. Dafür muss KC-SMTP aktiviert sein; die Freigabe des automatischen Berichts ist nicht erforderlich.
3. Die vorhandene KC-SMTP-Konfiguration wird verwendet. Bei aktiviertem KC_SMTP_TEST_MODE wird ausschließlich KC_SMTP_TEST_RECIPIENT verwendet, auch wenn eine andere Adresse gespeichert ist. Ein manueller Bericht umfasst die vier Wochen ab heute und verändert den automatischen Wochenversand nicht. Der Betreff enthält [MANUELL]; bei SMTP-Testumleitung zusätzlich [TEST].
4. Nach erfolgreicher Prüfung die gewünschte Empfängeradresse speichern. Eine eingetragene Adresse wird vom automatischen Bericht nur bei gesonderter Freigabe in PROD verwendet.

Der automatische Versand ist ausschließlich in PROD mit KC_ENVIRONMENT=PROD, app.environment=PROD, aktivem prod-Profil freigegeben. Die Freigabe je Mietobjekt erfolgt über den gespeicherten PROD-Schalter; ohne Objekteinstellung gilt KC_HAUSMEISTERBERICHT_ENABLED=true als Vorgabe. DEV und TEST versenden niemals automatisch.

Automatisch ab Sonntag 15:00 Uhr Europe/Berlin, einschließlich Sommer-/Winterzeit. Bei Fehlern oder einem Neustart wird bis Sonntag 23:55 alle fünf Minuten erneut geprüft. Nach erfolgreicher SMTP-Übergabe merkt sich KC die Woche pro Mietobjekt; Datenbanksperren verhindern parallelen Versand durch mehrere Instanzen. SMTP-Annahme ist kein Zustellnachweis. Ein Prozessabbruch zwischen SMTP-Annahme und Datenbank-Commit kann eine Wiederholung verursachen. Nach einem vollständigen Ausfall am Sonntag wird kein veralteter Bericht am Montag nachgeholt.

Zeitraum: Sonntag 00:00 bis zum vierten folgenden Sonntag 00:00 (exklusiv), also 28 Kalendertage. Auch schon laufende Aufenthalte mit Überschneidung sind enthalten. Status: RESERVIERT, BESTAETIGT, STORNIERT. Buchungen aller Quellen des jeweiligen Mietobjekts werden berücksichtigt. Stornierungen werden entsprechend dem Aufenthaltszeitraum aufgeführt, unabhängig vom Datum der Stornierung. Die Liste ist nach Anreise und Buchungs-ID sortiert.

Die Mail enthält Objekt, Buchungsnummer, Status, An-/Abreise, Mieternamen und Telefonnummer (Festnetz als Ersatz, wenn Mobiltelefon fehlt). Telefonnummern sind antippbar. Es gibt eine einspaltige HTML-Fassung sowie eine Textalternative. Eine leere Buchungsliste wird ausdrücklich als solche gemeldet.

Der Scheduler läuft ohne Benutzeranmeldung und ermittelt vorhandene Mandantenschemas mit der Namenskonvention des TenantLiquibaseMigrator ([a-z]{3,5}_…). Das Template kanu wird ausgeschlossen. Jeder Mandant erhält einen eigenen TenantContext vor dem Öffnen der Transaktion. Einstellungen und Buchungen werden ausschließlich aus diesem Mandanten gelesen. Fehler eines Mandanten verhindern den Bericht für andere nicht.

Die additive Liquibase-Migration 20261106-01-hausmeister-bericht.xml ergänzt die Empfängeradresse und die zuletzt versendete Woche in homepage_anbindung. Bestehende Einträge erhalten keine Adresse, deshalb beginnt nach dem Update kein Versand ohne Konfiguration. SMTP muss bereits eingerichtet sein. Ein echter SMTP-Versand und eine Migration gegen die Dev-Datenbank sind vor Ort zu prüfen; die automatisierten Prüfungen senden keine Nachrichten.


Seit 13.11.2026: Unter Homepage-Anbindung → Hausmeister-Wochenbericht kann der automatische Versand je Mietobjekt mit „Hausmeisterreport automatisch aus PROD senden“ ein- und ausgeschaltet werden. Änderungen speichern. Der Schalter ist nur in geprüfter PROD-Umgebung verfügbar und benötigt keinen Serverneustart. Ohne explizite Objekteinstellung gilt weiterhin KC_HAUSMEISTERBERICHT_ENABLED als Vorgabe. Eine CC-Adresse gilt für manuelle und automatische Berichte; der globale SMTP-Testmodus unterdrückt CC. Antwortmail-Testadressen unterdrücken ebenfalls CC.

Servervoraussetzung für den PROD-Schalter: KC_ENVIRONMENT=PROD setzen und den Dienst neu starten; zusätzlich müssen app.environment=PROD und ausschließlich das passende prod-Profil gelten. Der Status in der Maske meldet fehlende PROD-Voraussetzungen. KC_SMTP_ENABLED=true ist für den Versand erforderlich.
