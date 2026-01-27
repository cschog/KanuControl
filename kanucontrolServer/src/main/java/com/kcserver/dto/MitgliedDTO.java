package com.kcserver.dto;

import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.validation.OnCreate;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MitgliedDTO implements HasHauptverein{

    private Long id;

    @NotNull(groups = OnCreate.class)
    private Long personId;

    @NotNull(groups = OnCreate.class)
    private Long vereinId;

    private String vereinName;    // ✅ nur Read
    private String vereinAbk;     // ✅ nur Read

    private MitgliedFunktion funktion;

    private Boolean hauptVerein;
}