package com.kcserver.dto.person;

import com.kcserver.dto.validation.DataStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonListDTO {

    private Long id;
    private String vorname;
    private String name;
    private String ort;

    private Integer alter;

    private String hauptvereinAbk;

    private DataStatus dataStatus;

    private int mitgliedschaftenCount;
}