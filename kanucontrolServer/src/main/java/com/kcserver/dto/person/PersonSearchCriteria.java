package com.kcserver.dto.person;

import com.kcserver.enumtype.Sex;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonSearchCriteria {

    private String name;        // contains
    private String vorname;     // contains
    private Long vereinId;      // Mitgliedschaft
    private Boolean aktiv;      // aktiv / inaktiv
    private Sex sex;

    private Integer alterMin;
    private Integer alterMax;

    private String plz;
    private String ort;

    private Boolean unvollstaendig;
}