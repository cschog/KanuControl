package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.exception.BuchungLoeschbestaetigungException;
import com.kcserver.vermietung.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungLoeschServiceTest {
    private final BuchungRepository buchungen = mock(BuchungRepository.class);
    private final VermietungAbrechnungRepository abrechnungen = mock(VermietungAbrechnungRepository.class);
    private final VorlagenVersandRepository archiv = mock(VorlagenVersandRepository.class);
    private final HomepageStatusMailRepository mails = mock(HomepageStatusMailRepository.class);
    private final BuchungLoeschService service = new BuchungLoeschService(buchungen, abrechnungen, archiv, mails);
    private final Buchung buchung = new Buchung();

    @BeforeEach void setup() {
        buchung.setId(19L);
        buchung.setStatus(Buchungsstatus.ABRECHENBAR);
        when(buchungen.findLockedById(19L)).thenReturn(Optional.of(buchung));
    }

    private void nichtsGeloescht() {
        verify(abrechnungen, never()).deleteAllByBuchungId(anyLong());
        verify(archiv, never()).deleteAllByBuchungId(anyLong());
        verifyNoInteractions(mails);
        verify(buchungen, never()).delete(any());
        verify(buchungen, never()).flush();
    }

    @Test void wiedereroeffneteBuchungMitAbrechnungBrauchtWeitereBestaetigung() {
        when(abrechnungen.countByBuchungId(19L)).thenReturn(2L);
        var ex = assertThrows(BuchungLoeschbestaetigungException.class, () -> service.delete(19L, false));
        assertTrue(ex.getMessage().contains("2 Abrechnungsdokument"));
        nichtsGeloescht();
    }

    @Test void auchAbrechnungImVersandarchivBrauchtBestaetigung() {
        when(archiv.countByBuchungIdAndVorgang(19L, "ABGERECHNET")).thenReturn(1L);
        assertThrows(BuchungLoeschbestaetigungException.class, () -> service.delete(19L, false));
        nichtsGeloescht();
    }

    @Test void bestaetigtesLoeschenEntferntAlleArchiveVorDerBuchung() {
        buchung.setStatus(Buchungsstatus.ABGESCHLOSSEN);
        when(abrechnungen.countByBuchungId(19L)).thenReturn(2L);
        when(archiv.countByBuchungIdAndVorgang(19L, "ABGERECHNET")).thenReturn(1L);
        service.delete(19L, true);
        var reihenfolge = inOrder(abrechnungen, archiv, mails, buchungen);
        reihenfolge.verify(abrechnungen).deleteAllByBuchungId(19L);
        reihenfolge.verify(archiv).deleteAllByBuchungId(19L);
        reihenfolge.verify(mails).deleteAllByBuchungId(19L);
        reihenfolge.verify(buchungen).delete(buchung);
        reihenfolge.verify(buchungen).flush();
    }

    @Test void ohneAbrechnungReichtDieNormaleBestaetigung() {
        service.delete(19L, false);
        verify(buchungen).delete(buchung);
        verify(archiv).deleteAllByBuchungId(19L);
    }

    @Test void fehlendeBuchungLoeschtKeineDaten() {
        when(buchungen.findLockedById(19L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.delete(19L, true));
        verifyNoInteractions(abrechnungen, archiv, mails);
        verify(buchungen, never()).delete(any());
    }
}
