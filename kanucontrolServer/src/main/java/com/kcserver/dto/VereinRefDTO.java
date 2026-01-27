package com.kcserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VereinRefDTO {

    private Long id;
    private String name;
    private String abk;

    // getters / setters
}