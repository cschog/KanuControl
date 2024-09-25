KanuControl
===========

KanuControl ist eine Anwendung zur Beantragung und Abrechnung von Zuschüssen aus dem Kinder-, Jugend und Freizeitplan (KJFP) vom Landessportbund NRW (LSB NRW). 
Die Anträge und Abrechnungen werden von der Geschäftsstelle des KanuVerbandes NRW (KVNRW) verarbeitet.

Der generelle Ablauf besteht in den folgenden Schritten:
  - Anlegen eines Vereins, falls noch nicht vorhanden
  - Eintragen von Mitglieder in diesen Verein

  Antragstellung:
  - Anlegen einer Jugend-Veranstaltung, für die Zuschussmittel beantragt werden sollen. Dies ist dann die aktive Veranstaltung.
  - Ausgabe der Antragsformulare als bearbeitbare PDF-Formulare. Dort eventuell fehlende Angaben manuell nachtragen

  Durchführung der Veranstaltung
  - Eintragen der Teilnehmer dieser Veranstaltung
    - Falls die Teilnehmer noch nicht in der Mitgliedertabelle enthalten sind, werden sie  angelegt und automatische der aktiven Veranstaltung als Teilnehmer zugeordnet.
   
  Abrechnung:
  - Eintragen der Einnahmen und Kosten der Veranstaltung
  - Eventuell Erfassen von Reisekosten (i.d.R. Fahrtkosten von PKW´s)
  - Ausgabe der Abrechnungsdokumente als PDF
    - Deckblatt
    - Erhebungsbogen (das ist sind mehrere Blätter mit statistischen Auswertungen)
    - Teilnehmerliste

   KanuControl - Technik
   =====================

   Das Programm ist eine Client-Server Anwendung, die mit jedem Internet-Browser auf PC, Tablett oder auch Smartphone funktioniert. Einzige Vorraussetzung ist eine stabile Internetverbindung.

   Die Daten werden auf einem Server im Internet in einer mySQL Datenbank gespeichert. 
   
   Das Programm unterstützt bei der Erfassung der Veranstaltung, der Teilnehmer, der Einnahmen und Ausgaben. Aus diesen Daten werden die erforderlichen PDF Formulare erzeugt bzw. ausgefüllt. Im wesentlichen sind das:
   
   - Anmeldeforumular
   - Abrechnungs-Deckblatt
   - Teilnehmerliste
   - Erhebungsbogen 
   - eventuell Reisekosten
   
   Frontend
   ========
   Das Frontend ist eine REACT Anwendung, die mit einem Java-Backendserver kommuniziert. Es wird TypeScript verwendet. Als Entwicklungsumgebung wird VS Code eingesetzt.
   
   Backend
   =======
   Der Backendserver ist eine Spring Boot Anwendung. 
   Spring Boot Version 3.2.2, Java Version 17. Dependency Mgmt mit Maven.
   Das Datenbank Schema wird über Hibernate im Backend definiert. In mySQL wird nur die Datenbank selber einmal angelegt. 
   
   Schema von KanuControl
   ======================
   Es gibt verschiedene Tabellen (Entities), die fast alle über n:m Beziehungen (relationships) verbunden sind. Jede Tabelle hat einen Primarykey, der keine inhaltliche Bedeutung hat.
   
   In der Version 1.0 gibt es die folgenden Tabellen:
   
   1. Verein
   2. Person
   3. Veranstaltung
   4. Veranstaltungstyp (z.B. FM, JEM, Bildung, Großveranstaltung)
   5. Funktion (Jugendwart, Sportwart, ...)
   6. Finanzen => Veranstaltung (1:1)
   
   Weiterhin gibt es die Beziehungen:
   
   1. Mitglied = Verein : Person (n:m) mit Attributen (z.B. Funktion)
   2. Teilnehmer = Veranstaltung : Person (n:m)
