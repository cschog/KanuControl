package com.kcserver.vermietung.dto;

import com.kcserver.core.dto.person.PersonRefDTO;
import jakarta.validation.constraints.*;
import lombok.Data;

/** Only shared organisation data; no KJFP fields or usage flags are writable here. */
@Data
public class VeranstalterDTO {
    private Long id;
    private long buchungen;
    @NotBlank @Size(min=2,max=100) private String name;
    @NotBlank @Size(min=1,max=10) private String abk;
    private String strasse;
    private String plz;
    private String ort;
    @NotBlank private String countryCode;
    private String bankName;
    private String iban;
    @Size(max=20) private String bic;
    private Long kontoinhaberId;
    private PersonRefDTO kontoinhaber;
}
