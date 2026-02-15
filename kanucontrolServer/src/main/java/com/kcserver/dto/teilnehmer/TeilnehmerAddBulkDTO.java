package com.kcserver.dto.teilnehmer;

import java.util.List;

public class TeilnehmerAddBulkDTO {
    private List<Long> personIds;

    public List<Long> getPersonIds() {
        return personIds;
    }

    public void setPersonIds(List<Long> personIds) {
        this.personIds = personIds;
    }
}