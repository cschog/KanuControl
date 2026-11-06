package com.kcserver.finanz;

import com.kcserver.kjfp.entity.abrechnung.Abrechnung;
import com.kcserver.kjfp.entity.abrechnung.AbrechnungBeleg;
import com.kcserver.kjfp.entity.abrechnung.AbrechnungBuchung;
import com.kcserver.kjfp.entity.FinanzGruppe;
import com.kcserver.core.entity.Teilnehmer;
import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.enumtype.BuchungsHerkunft;
import com.kcserver.kjfp.enumtype.FinanzKategorie;
import com.kcserver.kjfp.repository.finanz.FinanzGruppeRepository;
import com.kcserver.core.repository.TeilnehmerRepository;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungRepository;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsPositionRepository;
import com.kcserver.kjfp.service.FoerderService;
import com.kcserver.kjfp.service.abrechnung.AbrechnungBelegService;
import com.kcserver.kjfp.service.abrechnung.AbrechnungSynchronisationsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbrechnungSynchronisationsServiceTest {

    @Mock
    private AbrechnungBelegService abrechnungBelegService;

    @Mock
    private TeilnehmerRepository teilnehmerRepository;

    @Mock
    private AbrechnungRepository abrechnungRepository;

    @Mock
    private FoerderService foerderService;

    @Mock
    private FinanzGruppeRepository finanzGruppeRepository;

    @Mock
    private ZahlungsnachweisRepository zahlungsnachweisRepository;

    @Mock
    private ZahlungsPositionRepository zahlungsPositionRepository;

    @InjectMocks
    private AbrechnungSynchronisationsService service;


    @Test
    void shouldCreateBookingForPaidParticipants() {

        // ---------------------------------------------------------
        // Veranstaltung
        // ---------------------------------------------------------

        Veranstaltung veranstaltung =
                new Veranstaltung();

        veranstaltung.setId(1L);
        veranstaltung.setName("Testveranstaltung");


        // ---------------------------------------------------------
        // Abrechnung
        // ---------------------------------------------------------

        Abrechnung abrechnung =
                new Abrechnung();

        abrechnung.setVeranstaltung(
                veranstaltung
        );


        // ---------------------------------------------------------
        // VK-Finanzgruppe
        // ---------------------------------------------------------

        FinanzGruppe vk =
                new FinanzGruppe();

        vk.setId(10L);
        vk.setKuerzel("VK");


        // ---------------------------------------------------------
        // Systembeleg für Teilnehmerbeiträge
        // ---------------------------------------------------------

        AbrechnungBeleg beleg =
                new AbrechnungBeleg();

        beleg.setAbrechnung(
                abrechnung
        );

        beleg.setFinanzGruppe(
                vk
        );


        // ---------------------------------------------------------
        // Repository
        // ---------------------------------------------------------

        when(
                abrechnungRepository.findByVeranstaltungId(1L)
        )
                .thenReturn(
                        Optional.of(abrechnung)
                );

        when(
                finanzGruppeRepository
                        .findByVeranstaltungIdAndKuerzel(
                                1L,
                                "VK"
                        )
        )
                .thenReturn(
                        Optional.of(vk)
                );

        when(
                abrechnungBelegService.getOrCreateBeleg(
                        abrechnung,
                        vk,
                        BuchungsHerkunft.TEILNEHMERBEITRAG
                )
        )
                .thenReturn(beleg);

        when(
                abrechnungBelegService.getOrCreateBeleg(
                        abrechnung,
                        vk,
                        BuchungsHerkunft.KJFP
                )
        )
                .thenReturn(beleg);


        // ---------------------------------------------------------
        // Zahlungsnachweise
        // ---------------------------------------------------------

        when(
                zahlungsnachweisRepository
                        .sumBetragByVeranstaltung(1L)
        )
                .thenReturn(
                        new BigDecimal("100.00")
                );


        // ---------------------------------------------------------
        // Teilnehmer für KJFP
        // ---------------------------------------------------------

        when(zahlungsPositionRepository.sumKjfpBetragByVeranstaltung(1L))
                .thenReturn(new BigDecimal("60.00"));

        Teilnehmer t1 =
                new Teilnehmer();

        Teilnehmer t2 =
                new Teilnehmer();

        when(
                teilnehmerRepository.findAllKjfpWithPerson(1L)
        )
                .thenReturn(
                        List.of(t1, t2)
                );


        // ---------------------------------------------------------
        // KJFP = 0
        // ---------------------------------------------------------

        when(
                foerderService.berechneKjfpZuschuss(
                        any(Veranstaltung.class),
                        anyList(),
                        eq(true)
                )
        )
                .thenReturn(
                        BigDecimal.ZERO
                );


        // ---------------------------------------------------------
        // Ausführen
        // ---------------------------------------------------------

        service.synchronisieren(1L);


        // ---------------------------------------------------------
        // Prüfen
        // ---------------------------------------------------------

        assertThat(beleg.getPositionen())
                .hasSize(1);

        AbrechnungBuchung buchung =
                beleg.getPositionen().getFirst();

        assertThat(buchung.getKategorie())
                .isEqualTo(
                        FinanzKategorie.TEILNEHMERBEITRAG
                );

        assertThat(buchung.getHerkunft())
                .isEqualTo(
                        BuchungsHerkunft.TEILNEHMERBEITRAG
                );

        assertThat(buchung.getBetrag())
                .isEqualByComparingTo(
                        "100.00"
                );

        assertThat(buchung.getKjfpBetrag()).isEqualByComparingTo("60.00");
        verify(foerderService).berechneKjfpZuschuss(veranstaltung, List.of(t1, t2), true);

        assertThat(buchung.getBeschreibung())
                .isEqualTo(
                        "TN-Beiträge"
                );
    }
}
