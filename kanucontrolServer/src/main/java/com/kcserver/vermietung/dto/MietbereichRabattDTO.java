package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Rabattart;
import com.kcserver.vermietung.enumtype.RabattwertTyp;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MietbereichRabattDTO {

    private Long id;

    private Long mietbereichId;

    private Rabattart rabattart;

    private RabattwertTyp rabattwertTyp;

    private BigDecimal rabattwert;

    private boolean aktiv;
}