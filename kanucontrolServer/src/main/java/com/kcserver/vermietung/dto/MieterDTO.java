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
    private String bankName;
    private String iban;
    private String bic;
    private boolean aktiv;
    private int buchungen;
    private List<Buchungsquelle> buchungsquellen;
}
