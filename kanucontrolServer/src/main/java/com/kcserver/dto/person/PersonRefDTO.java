package com.kcserver.dto.person;

import com.kcserver.dto.validation.DataStatus;
import com.kcserver.enumtype.Sex;
import lombok.Data;

@Data
public class PersonRefDTO {

    private Long id;
    private String vorname;
    private String name;

    private String hauptvereinAbk;

    private Sex sex;

    private boolean verwendetInFahrtabschnitten;

    private DataStatus dataStatus;
}