package com.kcserver.support.builder;

import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;

import java.time.LocalDate;
import java.time.LocalTime;

public class VeranstaltungCreateDTOBuilder {

    private String name = "Test Veranstaltung";
    private VeranstaltungTyp typ = VeranstaltungTyp.JEM;

    private Long vereinId;
    private Long leiterId;

    private LocalDate beginn = LocalDate.now().plusDays(1);
    private LocalDate ende = beginn.plusDays(5);

    private LocalTime beginnZeit = LocalTime.of(10, 0);
    private LocalTime endeZeit = LocalTime.of(18, 0);

    public static VeranstaltungCreateDTOBuilder aDTO() {
        return new VeranstaltungCreateDTOBuilder();
    }

    public VeranstaltungCreateDTOBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public VeranstaltungCreateDTOBuilder withVerein(Long vereinId) {
        this.vereinId = vereinId;
        return this;
    }

    public VeranstaltungCreateDTOBuilder withLeiter(Long leiterId) {
        this.leiterId = leiterId;
        return this;
    }

    public VeranstaltungCreateDTOBuilder withBeginn(LocalDate date) {
        this.beginn = date;
        this.ende = date.plusDays(5);
        return this;
    }

    private boolean aktiv = true;

    public VeranstaltungCreateDTOBuilder inactive() {
        this.aktiv = false;
        return this;
    }

    public VeranstaltungCreateDTO build() {

        if (vereinId == null) {
            throw new IllegalStateException("vereinId muss gesetzt sein");
        }

        if (leiterId == null) {
            throw new IllegalStateException("leiterId muss gesetzt sein");
        }

        VeranstaltungCreateDTO dto = new VeranstaltungCreateDTO();

        dto.setName(name);
        dto.setTyp(typ);
        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);

        dto.setBeginnDatum(beginn);
        dto.setEndeDatum(ende);
        dto.setBeginnZeit(beginnZeit);
        dto.setEndeZeit(endeZeit);

        return dto;
    }
}