package com.kcserver.unit;

import com.kcserver.core.entity.Person;
import com.kcserver.vermietung.dto.VerbrauchskostenVorschauDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungAbschlussVorschauTest {
    @ParameterizedTest
    @EnumSource(value = Buchungsstatus.class, names = {"ABRECHENBAR", "ABGESCHLOSSEN"})
    void detailLiefertBestaetigungspflichtigeAblesungenAuchFuerRechnungskorrektur(Buchungsstatus status) {
        var buchungen = mock(BuchungRepository.class);
        var verbrauch = mock(VerbrauchsabrechnungService.class);
        var service = new BuchungService(buchungen, null, null, null, null, null, null, null, null, null, null,
                verbrauch, mock(BuchungVerbrauchskostenRepository.class), null, null);
        var buchung = new Buchung(); buchung.setId(20L); buchung.setBuchungsnummer("2026-020");
        buchung.setStatus(status); buchung.setMieter(new Person()); buchung.setMietobjekt(new Mietobjekt());
        buchung.setAnreise(LocalDateTime.of(2026, 10, 1, 14, 0)); buchung.setAbreise(LocalDateTime.of(2026, 10, 2, 14, 0));
        when(buchungen.findLockedById(20L)).thenReturn(Optional.of(buchung));
        var wert = new VerbrauchskostenVorschauDTO(1L, "Strom-Zähler Bootshaus", "Strom", null, null, "kWh",
                null, null, null, null, null, null, null, null, false, "Ablesungen bestätigen", true);
        when(verbrauch.berechneVorschau(buchung)).thenReturn(List.of(wert));
        var dto = service.getById(20L);
        assertEquals(List.of(wert), dto.getVerbrauchskostenVorschau());
        assertTrue(dto.getVerbrauchskostenVorschau().getFirst().bestaetigungErforderlich());
        assertEquals(status, buchung.getStatus());
        verify(buchungen, never()).save(any());
        verify(verbrauch, never()).berechneUndSpeichere(any());
    }
}
