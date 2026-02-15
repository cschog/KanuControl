package com.kcserver.dto.person;

import lombok.Data;

@Data
public class PersonRefDTO {

    private Long id;
    private String vorname;
    private String name;

    private String hauptvereinAbk;   // ⭐ NEU

    // getter/setter
}