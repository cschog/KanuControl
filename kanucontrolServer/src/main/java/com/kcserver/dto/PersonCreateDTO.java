package com.kcserver.dto;

import lombok.*;

@Getter
@Setter
public class PersonCreateDTO {
    private String name;
    private String vorname;
    private String strasse;
    private String plz;
    private String Ort;
    private String telefon;
    private String bankName;
    private String iban;
    private String bic;
    private Long vereinId; // New field for Verein association

    // Getters and setters via lombok
}
