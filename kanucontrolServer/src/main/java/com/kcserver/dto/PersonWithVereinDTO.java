package com.kcserver.dto;

import lombok.Setter;
import lombok.Getter;

@Setter
@Getter
public class PersonWithVereinDTO {

    private Long personId;
    private String personName;
    private String personTelefon;
    private String personStrasse;
    private String personPLZ;
    private String personOrt;
    private String personBank;
    private String personIBAN;
    private String personBIC;
    private String vereinName;
}
