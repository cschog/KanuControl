package com.kcserver.dto.teilnehmer;

import jakarta.validation.constraints.NotNull;

public class TeilnehmerCreateDTO {

    @NotNull
    private Long personId;

    @NotNull
    private Long veranstaltungId;

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getVeranstaltungId() {
        return veranstaltungId;
    }

    public void setVeranstaltungId(Long veranstaltungId) {
        this.veranstaltungId = veranstaltungId;
    }
}