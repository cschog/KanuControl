package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.vermietung.enumtype.BankverbindungAuswahl;

public final class BuchungBankverbindung {
    private BuchungBankverbindung() {}

    public static void waehlen(Buchung buchung, Mietobjekt objekt, BankverbindungAuswahl auswahl) {
        var gewaehlt = auswahl != null ? auswahl : buchung.getBankverbindungAuswahl();
        if (gewaehlt == null) gewaehlt = BankverbindungAuswahl.PRIMAER;
        if (gewaehlt == BankverbindungAuswahl.ALTERNATIV) {
            var bank = objekt.getBankverbindungAlternativ();
            if (bank == null || bank.getIban() == null || bank.getIban().isBlank()) {
                throw new IllegalArgumentException("Für dieses Mietobjekt ist keine alternative Bankverbindung hinterlegt.");
            }
        }
        buchung.setBankverbindungAuswahl(gewaehlt);
    }
}
