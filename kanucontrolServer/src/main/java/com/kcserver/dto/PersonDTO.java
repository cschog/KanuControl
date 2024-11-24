package com.kcserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {
    private Long id; // Matches 'id' in Person
    private String name; // Matches 'name' in Person
    private String vorname; // Matches 'vorname' in Person
    private String strasse; // Matches 'strasse' in Person
    private String plz; // Matches 'plz' in Person
    private String ort; // Matches 'ort' in Person
    private String telefon; // Matches 'telefon' in Person
    private String bankName; // Matches 'bankName' in Person
    private String iban; // Matches 'iban' in Person
    private String bic; // Matches 'bic' in Person
}