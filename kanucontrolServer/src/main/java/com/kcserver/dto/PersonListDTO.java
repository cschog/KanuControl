package com.kcserver.dto;

import com.kcserver.enumtype.Sex;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PersonListDTO {

    private Long id;
    private String vorname;
    private String name;
    private Sex sex;
    private boolean aktiv;
    private LocalDate geburtsdatum;
    private int mitgliedschaftenCount;
}