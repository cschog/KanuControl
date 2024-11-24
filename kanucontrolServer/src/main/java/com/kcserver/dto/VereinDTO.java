package com.kcserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VereinDTO {
    private Long id;         // ID of the Verein
    private String name;     // Name of the Verein
    private String abk;      // Abbreviation of the Verein
    private String strasse;  // Street address
    private String plz;      // Postal code
    private String ort;      // City
    private String telefon;  // Phone number
    private String bankName; // Name of the bank (optional)
    private String kontoInhaber; // Account holder name (optional)
    private String kiAnschrift;  // Address of the account holder (optional)
    private String iban; // Matches 'iban' in Verein
    private String bic; // Matches 'bic' in Verein
}