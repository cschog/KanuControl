package com.kcserver.finanz;

import com.kcserver.entity.abrechnung.Abrechnung;
import com.kcserver.entity.abrechnung.AbrechnungBeleg;
import com.kcserver.entity.abrechnung.AbrechnungBuchung;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.BuchungsHerkunft;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.repository.finanz.FinanzGruppeRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.abrechnung.AbrechnungRepository;
import com.kcserver.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.service.FoerderService;
import com.kcserver.service.abrechnung.AbrechnungBelegService;
import com.kcserver.service.abrechnung.AbrechnungSynchronisationsService;
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

        Teilnehmer t1 =
                new Teilnehmer();

        Teilnehmer t2 =
                new Teilnehmer();

        when(
                teilnehmerRepository.findAllWithPerson(1L)
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

        assertThat(buchung.getBeschreibung())
                .isEqualTo(
                        "TN-Beiträge"
                );
    }
}