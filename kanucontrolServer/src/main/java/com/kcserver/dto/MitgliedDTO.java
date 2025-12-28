package com.kcserver.dto;

import com.kcserver.entity.MitgliedFunktion;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MitgliedDTO {

    @NotNull
    private Long personId;

    @NotNull
    private Long vereinId;

    private String vereinName;
    private String vereinAbk;

    private MitgliedFunktion funktion; // ✅ Enum statt String

    @NotNull
    private Boolean hauptVerein;
}