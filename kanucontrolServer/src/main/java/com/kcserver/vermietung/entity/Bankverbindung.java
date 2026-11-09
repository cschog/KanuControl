package com.kcserver.vermietung.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kcserver.core.validation.ValidIban;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.Locale;

/** Bankdaten eines Mietobjekts; keine Bankdaten des Mieters. */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Bankverbindung {
    @Size(max = 255)
    private String kontoinhaber;
    @Size(max = 255)
    private String bankName;
    @ValidIban @Size(max = 64)
    private String iban;
    @Size(max = 11)
    private String bic;

    @JsonIgnore
    @AssertTrue(message = "Eine Bankverbindung benötigt eine IBAN.")
    public boolean isVollstaendig() {
        return !hasText(kontoinhaber) && !hasText(bankName) && !hasText(bic) || hasText(iban);
    }

    public static Bankverbindung normalisiert(Bankverbindung bank) {
        if (bank == null) return null;
        String iban = compact(bank.iban);
        String bic = compact(bank.bic);
        String inhaber = trim(bank.kontoinhaber);
        String name = trim(bank.bankName);
        if (iban == null && bic == null && inhaber == null && name == null) return null;
        return new Bankverbindung(inhaber, name, iban, bic);
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
    private static String trim(String value) { return hasText(value) ? value.trim() : null; }
    private static String compact(String value) {
        return hasText(value) ? value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT) : null;
    }
}
