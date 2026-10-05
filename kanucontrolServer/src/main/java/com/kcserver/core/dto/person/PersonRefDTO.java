package com.kcserver.core.dto.person;

import com.kcserver.kjfp.dto.validation.DataStatus;
import com.kcserver.kjfp.enumtype.Sex;
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