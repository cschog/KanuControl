package com.kcserver.support.builder;

public class TeilnehmerCreateDTOBuilder {

    private Long personId;

    public static TeilnehmerCreateDTOBuilder aDTO() {
        return new TeilnehmerCreateDTOBuilder();
    }

    public TeilnehmerCreateDTOBuilder withPerson(Long personId) {
        this.personId = personId;
        return this;
    }

    public Long buildPersonId() {
        return personId;
    }
}