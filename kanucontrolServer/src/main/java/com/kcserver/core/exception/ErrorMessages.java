package com.kcserver.core.exception;

public final class ErrorMessages {

    // =========================================================
    // Allgemein
    // =========================================================

    public static final String UNEXPECTED_ERROR =
            "Ein unerwarteter Serverfehler ist aufgetreten.";

    public static final String INVALID_REQUEST =
            "Die Anfrage ist ungültig.";

    public static final String VALIDATION_FAILED =
            "Bitte überprüfen Sie Ihre Eingaben.";

    // =========================================================
// Sicherheit / Berechtigungen
// =========================================================

    public static final String ACCESS_DENIED =
            "Sie haben keine Berechtigung für diese Funktion.";

    // =========================================================
    // Veranstaltung
    // =========================================================

    public static final String VERANSTALTUNG_NAME_REQUIRED =
            "Name der Veranstaltung ist erforderlich.";

    public static final String VERANSTALTUNG_PLZ_REQUIRED =
            "PLZ des Veranstaltungsortes ist erforderlich.";

    public static final String VERANSTALTUNG_ORT_REQUIRED =
            "Ort des Veranstaltungsortes ist erforderlich.";

    public static final String VERANSTALTUNG_LAND_REQUIRED =
            "Land des Veranstaltungsortes ist erforderlich.";

    public static final String VERANSTALTUNG_BEGINN_ZEIT_REQUIRED =
            "Beginnzeit ist erforderlich.";

    public static final String VERANSTALTUNG_ENDE_ZEIT_REQUIRED =
            "Endzeit ist erforderlich.";

    public static final String VERANSTALTUNG_NOT_FOUND =
            "Veranstaltung nicht gefunden";

    public static final String VERANSTALTUNG_ALREADY_HAS_BEITRAGSSTRUKTUR =
            "Veranstaltung hat bereits eine Beitragsstruktur";

    public static final String VERANSTALTUNG_CANNOT_BE_DELETED_WITH_TEILNEHMER =
            "Die Veranstaltung kann nicht gelöscht werden, solange weitere Teilnehmer vorhanden sind.";

    public static final String NO_ACTIVE_VERANSTALTUNG =
            "Keine aktive Veranstaltung gefunden";

    public static final String INDIVIDUELLE_GEBUEHREN_REQUIRE_BEITRAGSSTRUKTUR =
            "Für individuelle Gebühren muss eine Beitragsstruktur gewählt werden.";
            
    public static final String VERANSTALTUNGSLEITER_REQUIRED =
            "Leiter fehlt.";

    public static final String VERANSTALTUNGSLEITER_GEBURTSDATUM_REQUIRED =
            "Für den Veranstaltungsleiter muss ein Geburtsdatum hinterlegt sein.";

    public static final String VERANSTALTUNGSLEITER_MIND_ALTER =
            "Der Veranstaltungsleiter muss mindestens 18 Jahre alt sein.";
            
    public static final String VERANSTALTUNG_REQUIRED =
            "Keine Veranstaltung vorhanden.";

    public static final String VERANSTALTUNGSTYP_REQUIRED =
            "Veranstaltungstyp fehlt.";

    public static final String VERANSTALTUNG_BEGINN_REQUIRED =
            "Beginn-Datum fehlt.";

    public static final String VERANSTALTUNG_ENDE_REQUIRED =
            "Ende-Datum fehlt.";

    public static final String VERANSTALTUNG_VEREIN_REQUIRED =
            "Verein fehlt.";

    public static final String VERANSTALTUNGSLEITER_INAKTIV =
            "Eine inaktive Person kann nicht als Veranstaltungsleiter eingesetzt werden.";
    // =========================================================
    // Person
    // =========================================================

    public static final String PERSON_REQUIRED =
            "Person ist nicht vorhanden.";

    public static final String PERSON_NAME_REQUIRED =
            "Nachname ist erforderlich.";

    public static final String PERSON_VORNAME_REQUIRED =
            "Vorname ist erforderlich.";

    public static final String PERSON_SEX_REQUIRED =
            "Geschlecht ist erforderlich.";

    public static final String PERSON_NOT_FOUND =
            "Person nicht gefunden";

    public static final String PERSON_ALREADY_EXISTS =
            "Eine Person mit gleichem Namen und Geburtsdatum existiert bereits";

    public static final String PERSON_CANNOT_BE_DELETED =
            "Person kann nicht gelöscht werden.";

    public static final String PERSON_USED_AS_FAHRER =
            "Person kann nicht gelöscht werden. Sie wird als Fahrer in einer Fahrkostenabrechnung verwendet.";

    public static final String PERSON_USED_AS_MITFAHRER =
            "Person kann nicht gelöscht werden. Sie wird als Mitfahrer in einer Fahrkostenabrechnung verwendet.";

    public static final String PERSON_ONLY_ONE_HAUPTVEREIN =
            "Eine Person darf nur einen Hauptverein haben";

    // =========================================================
    // Verein
    // =========================================================

    public static final String VEREIN_REQUIRED =
            "Verein ist nicht vorhanden.";

    public static final String VEREIN_DATA_INCOMPLETE =
            "Die Daten des Veranstaltervereins sind noch nicht vollständig.";

    public static final String VEREIN_NAME_REQUIRED =
            "Vereinsname ist erforderlich.";

    public static final String VEREIN_ABK_REQUIRED =
            "Vereinsabkürzung ist erforderlich.";

    public static final String VEREIN_STRASSE_REQUIRED =
            "Straße ist erforderlich.";

    public static final String VEREIN_PLZ_REQUIRED =
            "PLZ ist erforderlich.";

    public static final String VEREIN_ORT_REQUIRED =
            "Ort ist erforderlich.";

    public static final String VEREIN_LAND_REQUIRED =
            "Land ist erforderlich.";

    public static final String VEREIN_NOT_FOUND =
            "Verein nicht gefunden";

    public static final String VEREIN_ALREADY_EXISTS =
            "Verein mit Abkürzung und Name existiert bereits";

    public static final String VEREIN_KANN_NICHT_GELOESCHT_WERDEN_MITGLIEDER =
            "Der Verein kann nicht gelöscht werden, da noch Mitglieder zugeordnet sind.";

    public static final String VEREIN_KANN_NICHT_GELOESCHT_WERDEN_VERANSTALTER =
            "Der Verein kann nicht gelöscht werden, da er noch als Veranstalter verwendet wird.";

    public static final String KONTOINHABER_NOT_FOUND =
            "Kontoinhaber nicht gefunden";

    public static final String VERANSTALTER_SCHUTZKONZEPT_RECOMMENDED =
            "Für den Verein ist kein Schutzkonzept hinterlegt.";

    // =========================================================
// Kontoinhaber / Veranstalter
// =========================================================

    public static final String KONTOINHABER_REQUIRED =
            "Für einen veranstaltenden Verein muss ein Kontoinhaber hinterlegt sein.";

    public static final String KONTOINHABER_GEBURTSDATUM_REQUIRED =
            "Für den Kontoinhaber muss ein Geburtsdatum hinterlegt sein.";

    public static final String KONTOINHABER_MIND_ALTER =
            "Der Kontoinhaber muss mindestens 18 Jahre alt sein.";

    public static final String KONTOINHABER_STRASSE_REQUIRED =
            "Für den Kontoinhaber muss eine Straße hinterlegt sein.";

    public static final String KONTOINHABER_PLZ_REQUIRED =
            "Für den Kontoinhaber muss eine PLZ hinterlegt sein.";

    public static final String KONTOINHABER_ORT_REQUIRED =
            "Für den Kontoinhaber muss ein Ort hinterlegt sein.";

    public static final String VERANSTALTER_BANKNAME_RECOMMENDED =
            "Bankname wird für den Veranstalter empfohlen.";

    public static final String VERANSTALTER_IBAN_RECOMMENDED =
            "IBAN wird für den Veranstalter empfohlen.";

    public static final String VERANSTALTER_BIC_RECOMMENDED =
            "BIC wird für den Veranstalter empfohlen.";

    public static final String KONTOINHABER_BANKNAME_RECOMMENDED =
            "Bankname des Kontoinhabers wird empfohlen.";

    public static final String KONTOINHABER_IBAN_RECOMMENDED =
            "IBAN des Kontoinhabers wird empfohlen.";

    public static final String KONTOINHABER_BIC_RECOMMENDED =
            "BIC des Kontoinhabers wird empfohlen.";

    // =========================================================
    // Mitglied
    // =========================================================

    public static final String MITGLIED_NOT_FOUND =
            "Mitglied nicht gefunden";

    public static final String PERSON_ALREADY_MEMBER =
            "Person ist schon Mitglied in diesem Verein";

    public static final String NO_HAUPTVEREIN_FOUND =
            "Kein Hauptverein für die Person gefunden";

    public static final String MITGLIED_PERSON_CHANGE_NOT_ALLOWED =
            "Die Person eines Mitglieds darf nicht geändert werden.";

    public static final String MITGLIED_VEREIN_CHANGE_NOT_ALLOWED =
            "Der Verein eines Mitglieds darf nicht geändert werden.";

    // =========================================================
    // Teilnehmer
    // =========================================================

    public static final String TEILNEHMER_NOT_FOUND =
            "Teilnehmer nicht gefunden";

    public static final String LEITER_KANN_NICHT_GELOESCHT_WERDEN =
            "Der Veranstaltungsleiter kann nicht gelöscht werden.";

    public static final String TEILNEHMER_IN_VERANSTALTUNG_NOT_FOUND =
            "Teilnehmer nicht in der Veranstaltung gefunden";

    public static final String TEILNEHMER_ALREADY_EXISTS =
            "Person ist bereits Teilnehmer";

    public static final String TEILNEHMER_NOT_IN_VERANSTALTUNG =
            "Teilnehmer gehört nicht zur Veranstaltung";

    public static final String TEILNEHMER_USED_IN_REISEKOSTEN =
            "Der Teilnehmer wird in einer Fahrkostenabrechnung als Fahrer oder Mitfahrer verwendet.";

    public static final String TEILNEHMER_HAS_ZAHLUNGSNACHWEISE =
            "Der Teilnehmer kann nicht gelöscht werden, solange Zahlungsnachweise vorhanden sind.";

    public static final String TEILNEHMER_OHNE_PERSON =
            "Teilnehmer ohne Person gefunden.";

    public static final String LEITER_MUSS_UEBER_VERANSTALTUNG_GESETZT_WERDEN =
            "Der Leiter muss über die Veranstaltung gesetzt werden.";

    public static final String LEITER_DARF_NICHT_ENTFERNT_WERDEN =
            "Der Leiter kann nicht entfernt werden. Bitte weisen Sie zuerst einen anderen Leiter zu.";

    public static final String GEBURTSDATUM_REQUIRED_FOR_LEITER =
            "Für den Leiter muss ein Geburtsdatum hinterlegt sein.";
            
    public static final String TEILNEHMER_GEBURTSDATUM_REQUIRED =
           "Geburtsdatum fehlt bei: ";

    public static final String LEITER_MIND_ALTER =
            "Der Leiter muss mindestens 18 Jahre alt sein.";
            
    public static final String TEILNEHMER_EMPTY =
        "Leerer Teilnehmerdatensatz.";

    // =========================================================
    // Beitragsstruktur
    // =========================================================

    public static final String BEITRAGSSTRUKTUR_NOT_FOUND =
            "Beitragsstruktur nicht gefunden";

    // =========================================================
    // Beitragsregeln
    // =========================================================

    public static final String MINDESTENS_EINE_BEITRAGSREGEL_REQUIRED =
            "Mindestens eine Beitragsregel erforderlich";

    public static final String SORTIERUNG_REQUIRED =
            "Sortierung darf nicht null sein";

    public static final String ALTER_BIS_MUST_NOT_BE_NEGATIVE =
            "alterBis darf nicht negativ sein";

    public static final String ONLY_ONE_OPEN_AGE_RULE =
            "Nur eine offene Altersregel erlaubt";

    public static final String OPEN_AGE_RULE_MUST_BE_LAST =
            "Offene Altersregel muss die letzte Regel sein";

    public static final String ALTER_BIS_MUST_BE_ASCENDING =
            "alterBis muss aufsteigend sein";

    public static final String LAST_BEITRAGSREGEL_MUST_BE_OPEN =
            "Die letzte Beitragsregel muss offen sein";

    // =========================================================
    // Unterkunftsart
    // =========================================================

    public static final String UNTERKUNFTSART_NOT_FOUND =
            "Unterkunftsart nicht gefunden";

    public static final String UNTERKUNFTSART_ALREADY_EXISTS =
            "Eine Unterkunftsart mit dieser Bezeichnung existiert bereits.";

    // =========================================================
    // Verpflegungsmodell
    // =========================================================

    public static final String VERPFLEGUNGSMODELL_NOT_FOUND =
            "Verpflegungsmodell nicht gefunden";

    public static final String VERPFLEGUNGSMODELL_ALREADY_EXISTS =
            "Ein Verpflegungsmodell mit dieser Bezeichnung existiert bereits.";

    // =========================================================
    // Fördersatz
    // =========================================================

    public static final String FOERDERSATZ_NOT_FOUND =
            "Fördersatz nicht gefunden";

    public static final String FOERDERSATZ_GUELTIG_VON_REQUIRED =
            "gueltigVon darf nicht null sein";

    public static final String FOERDERSATZ_OVERLAP =
            "Zeitraum überschneidet sich mit bestehendem Fördersatz";

    public static final String NO_VALID_FOERDERSATZ =
            "Kein gültiger Fördersatz für Typ gefunden";

    // =========================================================
    // KiK-Zuschlag
    // =========================================================

    public static final String NO_VALID_KIK_ZUSCHLAG =
            "Kein gültiger KiK-Zuschlag gefunden";

    public static final String KIK_ZUSCHLAG_NOT_FOUND =
            "KiK-Zuschlag nicht gefunden";

    // =========================================================
    // Planung
    // =========================================================

    public static final String PLANUNG_NOT_FOUND =
            "Planung nicht gefunden";

    public static final String PLANUNG_ALREADY_SUBMITTED =
            "Planung bereits eingereicht";

    public static final String NO_PLANUNG_FOR_VERANSTALTUNG =
            "Für diese Veranstaltung wurde noch keine Planung gespeichert.";

    public static final String PLANUNG_LOCKED =
            "Die Planung wurde bereits eingereicht und ist gesperrt. Bitte öffnen Sie die Planung zunächst wieder.";

    // =========================================================
    // Abrechnung
    // =========================================================

    public static final String ABRECHNUNG_NOT_FOUND =
            "Abrechnung nicht gefunden";

    public static final String ABRECHNUNG_ALREADY_COMPLETED =
            "Abrechnung ist bereits abgeschlossen";

    public static final String ABRECHNUNG_NOT_BALANCED =
            "Abrechnung ist nicht ausgeglichen";

    // =========================================================
    // VK / Finanzierung
    // =========================================================

    public static final String VK_KONTO_NOT_CONFIGURED =
            "Für die Veranstaltung ist kein VK-Konto eingerichtet.";

    public static final String VK_KONTO_NOT_ALLOWED =
            "Für VK-Konto gibt es keine Finanzausgleichzahlung.";

    public static final String VK_NOT_CONFIGURED =
            "Für die Veranstaltung ist kein VK-Konto eingerichtet.";

    public static final String FINANZIERUNG_NOT_BALANCED =
            "Finanzierung nicht ausgeglichen";

    public static final String EIGENANTEIL_TOO_LOW =
            "Der Eigenanteil muss mindestens 250 € betragen.";

    public static final String FINANZAUSGLEICH_ZAHLUNG_BETRAG_MUST_BE_POSITIVE =
            "Der Betrag der Finanzausgleichszahlung muss größer als 0 sein.";

    public static final String FINANZAUSGLEICH_BEREITS_VOLLSTAENDIG_GEZAHLT =
            "Der Finanzausgleich wurde bereits vollständig ausgezahlt.";

    public static final String FINANZAUSGLEICH_ZAHLUNG_EXCEEDS_OPEN_AMOUNT =
            "Der Zahlungsbetrag überschreitet den noch offenen Finanzausgleich.";

    public static final String FINANZAUSGLEICH_ZAHLUNG_NOT_FOUND =
            "Die Finanzausgleichszahlung wurde nicht gefunden.";

    // =========================================================
    // Rückzahlung Teilnehmerbeitrag
    // =========================================================

    public static final String RUECKZAHLUNGSBETRAG_MUST_BE_POSITIVE =
            "Der Rückzahlungsbetrag muss größer als 0 sein.";

    public static final String RUECKZAHLUNG_ZAHLUNGSWEG_REQUIRED =
            "Der Zahlungsweg der Rückzahlung ist erforderlich.";

    public static final String RUECKZAHLUNG_NOT_POSSIBLE =
            "Für diesen Zahlungsnachweis besteht keine rückzahlbare Überzahlung.";

    public static final String RUECKZAHLUNG_EXCEEDS_OPEN_OVERPAYMENT =
            "Der Rückzahlungsbetrag überschreitet die noch verfügbare Überzahlung.";

    // =========================================================
    // Konto
    // =========================================================

    public static final String FINANZGRUPPE_NOT_FOUND =
            "Konto nicht gefunden";

    public static final String FINANZGRUPPE_SYSTEM_NOT_EDITABLE =
            "System-Konto darf nicht manuell geändert werden.";

    public static final String KUERZEL_REQUIRED =
            "Konto ist Pflicht";

    public static final String KUERZEL_ALREADY_EXISTS =
            "Konto existiert bereits";

    public static final String KUERZEL_CHANGE_NOT_ALLOWED_WITH_BELEGE =
            "Konto kann nicht geändert werden, da Belege existieren";

    public static final String KUERZEL_CHANGE_NOT_ALLOWED_WITH_BELEGE_2 =
            "Konto kann nicht geändert werden, da Belege existieren";

    public static final String GRUPPE_NOT_IN_VERANSTALTUNG =
            "Konto gehört nicht zur Veranstaltung";

    public static final String KUERZEL_CANNOT_BE_DELETED_WITH_TEILNEHMER =
            "Konto kann nicht gelöscht werden – Teilnehmer zugeordnet";

    public static final String KUERZEL_CANNOT_BE_DELETED_WITH_BELEGE =
            "Konto kann nicht gelöscht werden – Belege vorhanden";

    public static final String GRUPPE_BELONGS_TO_OTHER_VERANSTALTUNG =
            "Konto gehört zu anderer Veranstaltung";

    // =========================================================
    // Dokumente / Belege
    // =========================================================

    public static final String DOKUMENT_NOT_FOUND =
            "Dokument nicht gefunden.";

    public static final String BELEG_NOT_FOUND =
            "Beleg nicht gefunden";

    public static final String BELEG_NOT_FOUND_AFTER_CREATION =
            "Beleg nicht gefunden nach Erstellung";

    public static final String ZAHLUNGSNACHWEIS_NOT_FOUND =
            "Zahlungsnachweis nicht gefunden.";

    public static final String ZAHLUNGSBETRAG_MUST_BE_POSITIVE =
            "Der Zahlungsbetrag muss größer als 0 sein.";

    public static final String AT_LEAST_ONE_TEILNEHMER_REQUIRED =
            "Mindestens ein Teilnehmer muss ausgewählt werden.";

    public static final String UEBERZAHLUNG_TEILNEHMERKONTO_REQUIRED =
            "Bei einer Überzahlung müssen alle ausgewählten Teilnehmer einem Konto zugeordnet sein.";
           
    public static final String TEILNEHMER_REQUIRED =
           "Keine Teilnehmer vorhanden.";

    public static final String FINANZGRUPPE_NOT_IN_VERANSTALTUNG =
            "Konto gehört nicht zur Veranstaltung";

    public static final String POSITION_NOT_FOUND =
            "Position nicht gefunden";

    public static final String KUERZEL_NOT_FOUND =
            "Konto nicht gefunden";

    public static final String BELEG_WRONG_VERANSTALTUNG =
            "Beleg gehört zu anderer Veranstaltung";

    public static final String ABRECHNUNG_CLOSED =
            "Abrechnung ist abgeschlossen und nicht mehr änderbar";

    public static final String SYSTEMBELEG_NOT_EDITABLE =
            "Systembelege dürfen nicht geändert werden.";

    public static final String DOKUMENT_NO_OWNER =
            "Dokument hat keinen Besitzer.";

    public static final String UEBERZAHLUNG_REQUIRES_FINANZGRUPPE =
            "Bei einer Überzahlung müssen alle ausgewählten Teilnehmer einem Konto zugeordnet sein.";

    public static final String UEBERZAHLUNG_REQUIRES_COMMON_FINANZGRUPPE =
            "Bei einer Überzahlung müssen alle ausgewählten Teilnehmer demselben Konto zugeordnet sein.";

    // =========================================================
// CSV
// =========================================================

    public static final String CSV_EMPTY =
            "Die CSV-Datei ist leer.";

    public static final String CSV_NO_DATA =
            "Die CSV-Datei enthält keine Daten.";

    public static final String CSV_DELIMITER_NOT_DETECTED =
            "Das Trennzeichen der CSV-Datei konnte nicht erkannt werden. "
                    + "Unterstützt werden Semikolon (;), Komma (,) und Tabulator.";

    public static final String CSV_INVALID =
            "Die CSV-Datei ist ungültig. "
                    + "Bitte prüfen Sie Anführungszeichen und Spaltentrennung.";

    public static final String CSV_NOT_READABLE =
            "Die CSV-Datei konnte nicht gelesen werden.";

    public static final String CSV_NO_PERSON_SELECTED =
            "Es wurden keine Personen zum Export ausgewählt.";

    public static final String CSV_PERSON_NOT_FOUND =
            "Eine oder mehrere ausgewählte Personen wurden nicht gefunden.";

    public static final String CSV_EXPORT_FAILED =
            "Der CSV-Export konnte nicht erstellt werden.";

    public static final String PERSON_NO_SELECTION =
            "Es wurden keine Personen ausgewählt.";

    // =========================================================
    // Datei / PDF
    // =========================================================

    public static final String REFERENZOBJEKT_REQUIRED =
            "Referenzobjekt darf nicht leer sein.";

    public static final String PDF_NO_PAGE =
            "Die PDF-Datei enthält keine Seite.";

    public static final String PDF_NOT_READABLE =
            "PDF-Datei konnte nicht gelesen werden.";

    public static final String UNSUPPORTED_DOCUMENT_TYPE =
            "Nicht unterstützter Dokumenttyp.";

    public static final String NO_FILE_SELECTED =
            "Keine Datei ausgewählt.";

    public static final String FILE_TOO_LARGE =
            "Datei ist größer als 10 MB.";

    public static final String ONLY_IMAGE_OR_PDF_ALLOWED =
            "Es dürfen nur Bilder oder PDF-Dateien hochgeladen werden.";

    public static final String NO_BELEGE_FOR_ABRECHNUNG =
            "Für die Abrechnung sind keine Belege vorhanden.";

    public static final String ONLY_FM_OR_JEM_ALLOWED =
            "Nur FM oder JEM erlaubt";

    // =========================================================
    // Postleitzahl
    // =========================================================

    public static final String PLZ_NOT_FOUND =
            "PLZ nicht gefunden";

    // =========================================================
    // Vermietung / Buchungen
    // =========================================================

    public static final String BUCHUNG_NOT_FOUND =
            "Buchung nicht gefunden: %s";

    public static final String MIETER_NOT_FOUND =
            "Mieter nicht gefunden.";

    public static final String MIETOBJEKT_NOT_FOUND =
            "Mietobjekt nicht gefunden.";

    public static final String VERANSTALTER_NOT_FOUND =
            "Veranstalter nicht gefunden.";

    public static final String MIETBEREICH_NOT_FOUND =
            "Mindestens ein Mietbereich wurde nicht gefunden.";

    public static final String MIETBEREICH_WRONG_MIETOBJEKT =
            "Mindestens ein Mietbereich gehört nicht zum angegebenen Mietobjekt.";

    public static final String MIETBEREICH_NOT_RENTABLE =
            "Mindestens ein ausgewählter Mietbereich ist nicht mietbar.";

    public static final String BUCHUNG_POSITION_MIETBEREICH_REQUIRED =
            "Eine Buchungsposition enthält keinen Mietbereich.";

    public static final String BUCHUNG_POSITION_ANZAHL_INVALID =
            "Die Anzahl muss mindestens 1 betragen.";

    public static final String BUCHUNG_QUELLE_REQUIRED =
            "Die Buchungsquelle muss angegeben werden.";

    public static final String BUCHUNG_QUELLE_NOT_ENABLED =
            "Die Buchungsquelle ist für dieses Mietobjekt nicht aktiviert.";

    public static final String BUCHUNG_QUELLE_MIETBEREICH_NOT_ENABLED =
            "Die Buchungsquelle %s ist für den Mietbereich '%s' nicht aktiviert.";

    public static final String BUCHUNG_BESTAND_UNZUREICHEND =
            "Nicht genügend Bestand für den Mietbereich '%s'. "
                    + "Bestand: %d, bereits gebucht: %d, angefordert: %d.";

    public static final String BUCHUNG_QUELLE_MIETOBJEKT_NOT_ENABLED =
            "Die Buchungsquelle %s ist für dieses Mietobjekt nicht aktiviert.";

    private ErrorMessages() {
    }
}