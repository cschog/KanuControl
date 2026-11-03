package com.kcserver.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.vermietung.dto.BuchungDTO;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BuchungTeilnehmerTest {
    private BuchungDTO gruppe(Integer jugendliche, Integer von, Integer bis, Integer erwachsene) {
        BuchungDTO dto = new BuchungDTO();
        dto.setAnzahlJugendliche(jugendliche);
        dto.setJugendAlterVon(von);
        dto.setJugendAlterBis(bis);
        dto.setAnzahlErwachsene(erwachsene);
        return dto;
    }

    @Test void alteBuchungenOhneTeilnehmerdatenBleibenGueltig() {
        assertTrue(gruppe(null, null, null, null).isTeilnehmerdatenGueltig());
    }
    @Test void erwachsenengruppeBrauchtKeineAltersspanne() {
        assertTrue(gruppe(0, null, null, 12).isTeilnehmerdatenGueltig());
    }
    @Test void gemischteGruppeMitGleichemAlterIstGueltig() {
        assertTrue(gruppe(8, 14, 14, 2).isTeilnehmerdatenGueltig());
    }
    @Test void fehlendeUndUmgekehrteAltersspannenWerdenAbgelehnt() {
        assertFalse(gruppe(8, null, 15, 2).isTeilnehmerdatenGueltig());
        assertFalse(gruppe(8, 12, null, 2).isTeilnehmerdatenGueltig());
        assertFalse(gruppe(8, 15, 12, 2).isTeilnehmerdatenGueltig());
    }
    @Test void negativeUndUnplausibleAngabenWerdenAbgelehnt() {
        assertFalse(gruppe(-1, null, null, 2).isTeilnehmerdatenGueltig());
        assertFalse(gruppe(0, null, null, -1).isTeilnehmerdatenGueltig());
        assertFalse(gruppe(2, -1, 15, 2).isTeilnehmerdatenGueltig());
        assertFalse(gruppe(2, 12, 121, 2).isTeilnehmerdatenGueltig());
    }
    @Test void alterOhneJugendlicheWirdAbgelehnt() {
        assertFalse(gruppe(0, 12, 15, 2).isTeilnehmerdatenGueltig());
        assertFalse(gruppe(null, 12, 15, 2).isTeilnehmerdatenGueltig());
    }
    @Test void controllerValidierungPrueftDieKombination() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validateProperty(
                    gruppe(3, 16, 12, 2), "teilnehmerdatenGueltig").isEmpty());
            assertTrue(factory.getValidator().validateProperty(
                    gruppe(3, 12, 16, 2), "teilnehmerdatenGueltig").isEmpty());
        }
    }
    @Test void teilnehmerdatenUeberstehenJsonRoundtripOhneHilfsproperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(gruppe(8, 12, 16, 2));
        assertFalse(json.contains("teilnehmerdatenGueltig"));
        BuchungDTO dto = mapper.readValue(json, BuchungDTO.class);
        assertEquals(8, dto.getAnzahlJugendliche());
        assertEquals(12, dto.getJugendAlterVon());
        assertEquals(16, dto.getJugendAlterBis());
        assertEquals(2, dto.getAnzahlErwachsene());
    }
}
