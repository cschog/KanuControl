package com.kcserver.belegnummer;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.entity.FinanzGruppe;
import com.kcserver.kjfp.entity.beitraege.*;
import com.kcserver.kjfp.entity.fahrkosten.Reisekostenabrechnung;
import com.kcserver.kjfp.enumtype.Zahlungsweg;
import com.kcserver.kjfp.repository.finanz.FinanzGruppeRepository;
import com.kcserver.kjfp.service.abrechnung.BelegnummerService;
import com.kcserver.kjfp.service.reisekosten.ReisekostenabrechnungServiceImpl;
import com.kcserver.kjfp.service.zahlungsnachweis.ZahlungsnachweisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BelegnummerSpeichernTest {
    @Mock BelegnummerService nummern;
    @Mock FinanzGruppeRepository konten;
    @InjectMocks ZahlungsnachweisService zahlungen;
    @InjectMocks ReisekostenabrechnungServiceImpl reisen;
    FinanzGruppe konto() {
        var v = new Veranstaltung(); v.setId(1L);
        var g = new FinanzGruppe(); g.setId(1L); g.setKuerzel("RZ"); g.setVeranstaltung(v); return g;
    }
    @Test void oneNumberForReceiptWithMultipleParticipantsAndRepeatedSave() {
        var konto = konto(); var z = new Zahlungsnachweis();
        z.setZahlungsweg(Zahlungsweg.QUITTUNG); z.setFinanzGruppe(konto);
        z.addPosition(new ZahlungsPosition()); z.addPosition(new ZahlungsPosition());
        when(nummern.next(konto)).thenReturn(new BelegnummerService.Nummer(20, "RZ_020"));
        ReflectionTestUtils.invokeMethod(zahlungen, "nummeriereQuittung", z);
        ReflectionTestUtils.invokeMethod(zahlungen, "nummeriereQuittung", z);
        assertThat(z.getBelegnummer()).isEqualTo("RZ_020");
        assertThat(z.getPositionen()).hasSize(2);
        verify(nummern, times(1)).next(konto);
    }
    @Test void transferDoesNotAllocateNumber() {
        var z = new Zahlungsnachweis(); z.setZahlungsweg(Zahlungsweg.UEBERWEISUNG);
        ReflectionTestUtils.invokeMethod(zahlungen, "nummeriereQuittung", z);
        assertThat(z.getBelegnummer()).isNull(); verifyNoInteractions(nummern);
    }
    @Test void travelAccountRequiredAndNumberStaysOnRepeatedSave() {
        var konto = konto(); var rk = new Reisekostenabrechnung(); rk.setVeranstaltung(konto.getVeranstaltung());
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(reisen, "setKonto", rk, (Long) null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Konto");
        when(konten.findById(1L)).thenReturn(Optional.of(konto));
        when(nummern.next(konto)).thenReturn(new BelegnummerService.Nummer(21, "RZ_021"));
        ReflectionTestUtils.invokeMethod(reisen, "setKonto", rk, 1L);
        ReflectionTestUtils.invokeMethod(reisen, "setKonto", rk, 1L);
        assertThat(rk.getBelegnummer()).isEqualTo("RZ_021"); verify(nummern, times(1)).next(konto);
    }
    @Test void changingAccountOnNumberedDocumentIsRejected() {
        var service = new BelegnummerService(null); var a = konto(); var b = konto(); b.setId(2L);
        assertThatCode(() -> service.validateKontowechsel("RZ_020", a, a)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validateKontowechsel("RZ_020", a, b)).hasMessageContaining("Konto");
        assertThatThrownBy(() -> service.next(null)).hasMessageContaining("Konto");
    }
}
