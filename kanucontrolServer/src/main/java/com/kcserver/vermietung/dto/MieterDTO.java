package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Buchungsquelle;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MieterDTO {
    private Long id;
    private Long personId;
    private String name;
    private String vorname;
    private String email;
    private String telefon;
    private String telefonFestnetz;
    private String strasse;
    private String plz;
    private String ort;
    private String countryCode;
    @jakarta.validation.constraints.Size(max=255)
    private String organisation;
    @jakarta.validation.constraints.Size(max=255)
    private String kontoinhaber;
    private String bankName;
    private String iban;
    private String bic;
    private boolean aktiv;
    private List<String> mietobjekte;
    private int buchungen;
    private List<Buchungsquelle> buchungsquellen;
}
