package com.kcserver.vermietung.exception;

public class BuchungLoeschbestaetigungException extends RuntimeException {
    public static final String CODE = "BUCHUNG_ABRECHNUNGEN_BESTAETIGEN";

    public BuchungLoeschbestaetigungException(long anzahl) {
        super("Zu dieser Buchung sind " + anzahl + " Abrechnungsdokument(e) archiviert. "
                + "Beim Löschen werden die Buchung, alle archivierten Abrechnungen, "
                + "das Versandarchiv und die zugehörigen Buchungsdaten endgültig gelöscht. "
                + "Möchten Sie wirklich alles löschen?");
    }
}
