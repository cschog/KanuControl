package com.kcserver.vermietung.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class HomepageAnbindungDTO {
    private Long version;

    private boolean aktiv;

    @Size(max = 200)
    private String technischerClientId;

    @NotBlank @Size(max = 100)
    private String bezeichnung = "Homepage";

    @Size(max = 2048)
    private String homepageUrl;

    @Size(max = 2048)
    private String datenschutzUrl;

    @Size(max = 2048)
    private String hausregelnUrl;

    @Email @Size(max = 254)
    private String empfaengerEmail;

    private boolean gastBestaetigung = true;
    private boolean verwaltungsBenachrichtigung;
    private boolean teilnehmerPflicht = true;

    @NotNull @Size(max = 200)
    private Set<@NotNull @Positive Long> erlaubteMietbereichIds = new HashSet<>();
}
