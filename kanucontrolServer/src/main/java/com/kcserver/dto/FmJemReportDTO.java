package com.kcserver.dto;

import java.time.LocalDate;

public class FmJemReportDTO {

    // ===== Träger =====
    private String vereinName;
    private String vereinAdresse;
    private String vereinTelefon;
    private String iban;
    private String bic;
    private String kontoinhaber;

    // ===== Leitung =====
    private String leiterName;
    private String leiterTelefon;
    private String leiterEmail;

    // ===== Maßnahme =====
    private String titel;
    private String ort;
    private String land;
    private String unterkunft;
    private String verpflegung;

    private LocalDate beginn;
    private LocalDate ende;
    private int tage;

    private Integer tnMaennlich;
    private Integer tnWeiblich;
    private Integer tnGesamt;

}
