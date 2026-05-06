package com.kcserver.dto.beitrag;

import lombok.Data;

import java.util.List;

@Data
public class BeitragsstrukturDTO {

    private Long id;
    private String name;
    private boolean template;

    private List<BeitragsregelDTO> regeln;
}
