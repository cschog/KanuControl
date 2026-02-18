package com.kcserver.dto.verein;

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

    private String ort;

    // getters / setters
}