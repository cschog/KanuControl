package com.kcserver.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigInteger;
import java.util.Map;

public class IbanValidator implements ConstraintValidator<ValidIban, String> {

    // IBAN Länge je Land (nur wichtige, erweiterbar)
    private static final Map<String, Integer> IBAN_LENGTHS = Map.of(
            "DE", 22,
            "AT", 20,
            "CH", 21,
            "NL", 18,
            "BE", 16,
            "FR", 27,
            "ES", 24,
            "IT", 27
    );

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {

        // ✔ leer erlaubt (wichtig für CSV / optional)
        if (iban == null || iban.isBlank()) {
            return true;
        }

        // Leerzeichen entfernen + Uppercase
        String normalized = iban.replaceAll("\\s+", "").toUpperCase();

        // Mindestlänge
        if (normalized.length() < 4) {
            return false;
        }

        // Länderkennung + Länge prüfen
        String country = normalized.substring(0, 2);
        Integer expectedLength = IBAN_LENGTHS.get(country);

        if (expectedLength == null || normalized.length() != expectedLength) {
            return false;
        }

        // Nur A-Z + 0-9 erlaubt
        if (!normalized.matches("[A-Z0-9]+")) {
            return false;
        }

        // ISO 13616 Schritt 1: Erste 4 Zeichen nach hinten
        String rearranged = normalized.substring(4) + normalized.substring(0, 4);

        // Schritt 2: Buchstaben → Zahlen (A=10, B=11, …)
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else {
                numeric.append((int) c - 55);
            }
        }

        // Schritt 3: MOD-97 == 1 ?
        try {
            BigInteger ibanNumber = new BigInteger(numeric.toString());
            return ibanNumber.mod(BigInteger.valueOf(97)).intValue() == 1;
        } catch (Exception e) {
            return false;
        }
    }
}